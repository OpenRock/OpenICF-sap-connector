/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2015 ForgeRock AS.
 */

package samples.r3

import static org.identityconnectors.common.security.SecurityUtil.decrypt

import com.sap.conn.jco.JCoRepository;
import com.sap.conn.jco.JCoContext;
import com.sap.conn.jco.JCoRuntimeException
import com.sap.conn.jco.JCoTable
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoFunction
import com.sap.conn.jco.JCoRecordField
import com.sap.conn.jco.JCoRecordFieldIterator

import org.identityconnectors.framework.common.exceptions.ConnectorException
import org.identityconnectors.framework.common.exceptions.UnknownUidException
import org.identityconnectors.framework.common.objects.AttributesAccessor
import org.identityconnectors.framework.common.objects.OperationalAttributes
import org.identityconnectors.common.logging.Log

class R3User {
    
    private static final String[] STRUCTURES = ["ADDRESS","LOGONDATA","DEFAULTS","COMPANY","ISLOCKED","ALIAS","LASTMODIFIED","REF_USER","UCLASS"]
    private static final String[] TABLES = ["PROFILES","ACTIVITYGROUPS","ADDTEL","GROUPS"]
    private static final String NODATE = "0000-00-00"
    
    private static final Log log = Log.getLog(R3User.class);
    
    def repository
    def destination
    def userName
    
    def attributes = [:]
    def structures = []
    def tables = []
    
    public R3User(JCoRepository repository, JCoDestination destination, String userName) {
        this.userName = userName
        this.destination = destination
        this.repository = repository
    }

    def get(String[] attrsToGet){
        attrsToGet.each() {
            if (STRUCTURES.contains(it)) {
                structures.add(it)
            } else if (TABLES.contains(it)) {
                tables.add(it)
            }
        }
        // User existence could be checked first with BAPI_USER_EXISTENCE_CHECK
        def function = execute(init("BAPI_USER_GET_DETAIL"))
        
        if (structures.size() == 0) {
            structures.addAll(STRUCTURES)
        }
        
        if (tables.size() == 0) {
            tables.addAll(TABLES)
        }
        
        for (struct in structures){
            def values = [:];
            try{
                JCoRecordFieldIterator iter = function.getExportParameterList().getStructure(struct).getRecordFieldIterator();
                while (iter.hasNextField()){
                    JCoRecordField jrf = iter.nextRecordField();
                    //log.info(struct+" field name: "+jrf.getName()+",TYPE: "+jrf.getTypeAsString()+", VALUE: "+jrf.getString());
                    if (!jrf.getString().isEmpty()){
                        values.put(jrf.getName(),jrf.getString())
                    }
                }
                if (!values.isEmpty()){
                    attributes.put(struct,values)
                }
            }
            catch(JCoRuntimeException e){ //thrown with group JCO_ERROR_FIELD_NOT_FOUND if a field with the specified name does not exist
                log.warn(e.getMessage())
            }
        }
        
        for (table in tables){
            JCoTable profiles = function.getTableParameterList().getTable(table);
            def list = []
            def iter
            for (int i = 0; i < profiles.getNumRows(); i++)
            {
                def values = [:];
                profiles.setRow(i);
                iter = profiles.getRecordFieldIterator();
                while (iter.hasNextField()){
                    JCoRecordField jrf = iter.nextRecordField();
                    //log.info("PROFILES field name: "+jrf.getName()+",TYPE: "+jrf.getTypeAsString()+", VALUE: "+jrf.getString());
                    if (!jrf.getString().isEmpty()){
                        values.put(jrf.getName(),jrf.getString())
                    }
                }
                if (!values.isEmpty()){
                    list.add(values);
                }
            }
            if (list.size() > 0){
                attributes.put(table,list)
            }
        }
        
        getEnableStatus()
        getLockOutStatus()
        
    }
    
    def create(AttributesAccessor accessor){
        try {
            JCoContext.begin(destination)
            JCoFunction function = init("BAPI_USER_CREATE1")
            function.getImportParameterList().getStructure("ADDRESS").setValue("LASTNAME",userName);
            function.getImportParameterList().getStructure("PASSWORD").setValue("BAPIPWD",decrypt(accessor.getPassword()));
            // Password need to be changed at first logon - default: true
            def exp_pwd = accessor.findBoolean(OperationalAttributes.PASSWORD_EXPIRED_NAME)
            if (exp_pwd != null && !exp_pwd) {
                function.getImportParameterList().setValue("SELF_REGISTER","X")
            }

            // Address Data
            R3UserAddress.setValues(function, accessor.findMap("ADDRESS"), false)
            // Structure with Logon Data
            R3UserLogonData.setValues(function, accessor.findMap("LOGONDATA"), false)
            // User's company
            setCompany(function, accessor.findMap("COMPANY"), false)
            // User Name Alias
            setAlias(function, accessor.findMap("ALIAS"), false)
            // Reference user for rights
            setRefUser(function, accessor.findMap("REF_USER"), false)
            
            execute(function)
            
            // Assign Roles (Activity Groups)
            def agrList = accessor.findList("ACTIVITYGROUPS")
            if ((agrList != null) && (!agrList.isEmpty())) {
                function = init("BAPI_USER_ACTGROUPS_ASSIGN")
                R3UserActivityGroup.setValues(function, agrList)
                execute(function)
            }
            
            // Assign Profiles
            def profList = accessor.findList("PROFILES")
            if ((profList != null) && (!profList.isEmpty())) {
                function = init("BAPI_USER_PROFILES_ASSIGN")
                R3UserProfile.setValues(function, profList)
                execute(function)
            }

            // Account lock/unlock (default = unlock)
            def lockout = accessor.findBoolean(OperationalAttributes.LOCK_OUT_NAME)
            if (lockout != null && lockout) {
                lock()
            }
        } catch(JCoRuntimeException e) {
                log.warn(e.getMessage())
        } finally {
            JCoContext.end(destination)
        }
        return userName;
    }
    
    def update(AttributesAccessor accessor){
        try {
            JCoContext.begin(destination)
            JCoFunction function = init("BAPI_USER_CHANGE")

            // Password change
            if (accessor.getPassword() != null) {
                function.getImportParameterList().getStructure("PASSWORD").setValue("BAPIPWD",decrypt(accessor.getPassword()));
                function.getImportParameterList().getStructure("PASSWORDX").setValue("BAPIPWD","X")
                def exp_pwd = accessor.findBoolean(OperationalAttributes.PASSWORD_EXPIRED_NAME)
                // Set this field if the new password is not expired
                if (exp_pwd != null && !exp_pwd) {
                    function.getImportParameterList().setValue("PRODUCTIVE_PWD","X")
                }
            }

            // Address Data
            R3UserAddress.setValues(function, accessor.findMap("ADDRESS"), true)
            // Structure with Logon Data
            R3UserLogonData.setValues(function, accessor.findMap("LOGONDATA"), true)
            // User's company
            setCompany(function, accessor.findMap("COMPANY"), true)
            // User Name Alias
            setAlias(function, accessor.findMap("ALIAS"), true)
            // Reference user for rights
            setRefUser(function, accessor.findMap("REF_USER"), true)
            
            execute(function)

            // Roles (Activity Groups)
            def agrList = accessor.findList("ACTIVITYGROUPS")
            if ( agrList != null) {
                if (agrList.isEmpty()) {
                    // An empty list on update means delete all the roles
                    function = init("BAPI_USER_ACTGROUPS_DELETE")
                } else {
                    // Replace existing list of roles
                    function = init("BAPI_USER_ACTGROUPS_ASSIGN")
                    R3UserActivityGroup.setValues(function, agrList)
                }
                execute(function)
            }
            
            // Profiles
            def profList = accessor.findList("PROFILES")
            if (profList != null) {
                if (profList.isEmpty()) {
                    function = init("BAPI_USER_PROFILES_DELETE")
                } else {
                    function = init("BAPI_USER_PROFILES_ASSIGN")
                    R3UserProfile.setValues(function, profList)
                }
                execute(function)
            }
            
            // Account lock/unlock (default = unlock)
            def lockout = accessor.findBoolean(OperationalAttributes.LOCK_OUT_NAME)
            if (lockout != null) {
                if (lockout) {
                    lock()
                } else {
                    unlock()
                }
            }
        } catch(JCoRuntimeException e){ //thrown with group JCO_ERROR_FIELD_NOT_FOUND if a field with the specified name does not exist
                log.warn(e.getMessage())
        } finally {
            JCoContext.end(destination)
        }
        return userName;
    }
    
    def delete(){
        execute(init("BAPI_USER_DELETE"))
    }
    
    def lock(){
        execute(init("BAPI_USER_LOCK"))
    }
    
    def unlock(){
        execute(init("BAPI_USER_UNLOCK"))
    }
    
    def getEnableStatus(){
        
        def logond = attributes.LOGONDATA
        def from = null
        def to = null
        def now = new Date()
        
        if (logond != null){
            attributes[OperationalAttributes.ENABLE_NAME] = true
            if (logond.GLTGV != NODATE) {
                attributes[OperationalAttributes.ENABLE_DATE_NAME] = logond.GLTGV
                from = new Date().parse('yyyy-MM-dd', logond.GLTGV)
            }
            if (logond.GLTGB != NODATE) {
                attributes[OperationalAttributes.DISABLE_DATE_NAME] = logond.GLTGB
                to = new Date().parse('yyyy-MM-dd', logond.GLTGB)
            }
            if (from != null && from > now) {
                attributes[OperationalAttributes.ENABLE_NAME] = false
            } 
            if (to != null && to < now) {
                attributes[OperationalAttributes.ENABLE_NAME] = false
            } 
        }
    }
    
    def getLockOutStatus(){
        def islock = attributes.ISLOCKED
        
        if (islock != null){
            attributes[OperationalAttributes.LOCK_OUT_NAME] = "L".equals(islock.LOCAL_LOCK) ? true : false
        }
    }
    
    def init(String bapi_name) {
        JCoFunction function = repository.getFunction(bapi_name);
        function.getImportParameterList().setValue("USERNAME",userName);
        return function
    }
    
    def execute(JCoFunction function){
        function.execute(destination);
        // Check the STATUS
        // Message type: S Success, E Error, W Warning, I Info, A Abort
        def message = function.getTableParameterList().getTable("RETURN").getString("MESSAGE")
        def status = function.getTableParameterList().getTable("RETURN").getString("TYPE")
        if ("E".equalsIgnoreCase(status)){
            throw new ConnectorException(message)
        }
        if (message != null) {
            log.info(message)
        }
        return function
    }
    
    // Set User's company
    def setCompany(JCoFunction function, Map company, boolean update){
        if ((company != null) && (company.COMPANY != null)){
                function.getImportParameterList().getStructure("COMPANY").setValue("COMPANY",company.COMPANY);
                if (update) function.getImportParameterList().getStructure("COMPANYX").setValue("COMPANY","X");
            }
    }
    
    // Set User Name Alias
    def setAlias(JCoFunction function, Map alias, boolean update) {
       if ((alias != null) && (alias.USERALIAS != null)){
                function.getImportParameterList().getStructure("ALIAS").setValue("USERALIAS",alias.USERALIAS);
                if (update) function.getImportParameterList().getStructure("ALIASX").setValue("BAPIALIAS","X");
            } 
    }
    
    // Set Reference User
    def setRefUser(JCoFunction function, Map ref_user, boolean update) {
       if ((ref_user != null) && (ref_user.REF_USER != null)){
                function.getImportParameterList().getStructure("REF_USER").setValue("REF_USER",refusr.REF_USER);
                if (update) function.getImportParameterList().getStructure("REF_USERX").setValue("REF_USER","X");
            } 
    }
}