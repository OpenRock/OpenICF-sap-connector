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

package samples.hr

import com.sap.conn.jco.JCoRepository;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoContext;
import com.sap.conn.jco.JCoFunction
import com.sap.conn.jco.JCoTable
import com.sap.conn.jco.JCoField
import com.sap.conn.jco.JCoRecordFieldIterator

import org.identityconnectors.framework.common.exceptions.ConnectorException
import org.identityconnectors.common.logging.Log

class EmplComm {
    private static final String END_DATE = "99991231"
    private static final String REC_ZERO = "000"
    private static final Log log = Log.getLog(EmplComm.class);
    
    def repository
    def destination
    def employeeId
    
    public EmplComm(JCoRepository repository, JCoDestination destination, String employeeId) {
        this.employeeId = employeeId
        this.destination = destination
        this.repository = repository
    }
    
    def get(String subType) {
        def record = null
        def results = null
        
        JCoFunction function = repository.getFunction("BAPI_EMPLCOMM_GETDETAILEDLIST");
        function.getImportParameterList().setValue("EMPLOYEENUMBER",employeeId);
        function.getImportParameterList().setValue("SUBTYPE",subType);
        function.getImportParameterList().setValue("TIMEINTERVALLOW",new Date().format('yyyyMMdd'));
        //function.getImportParameterList().setValue("TIMEINTERVALHIGH","99991231");
        log.info("Executing BAPI_EMPLCOMM_GETDETAILEDLIST, SUBTYPE: {0} function...",subType);
        function.execute(destination);
        
        // Check status
        if ("".equalsIgnoreCase(function.getExportParameterList().getStructure("RETURN").getString("TYPE"))){
            JCoTable comData = function.getTableParameterList().getTable("COMMUNICATION");
            // COMMUNICATION can have multiple records
            if (!comData.isEmpty()) {
                results = []
                for (int i = 0; i < comData.getNumRows(); i++) {
                    record = [:]
                    comData.setRow(i)
                    JCoRecordFieldIterator fit = comData.getRecordFieldIterator();
                    while (fit.hasNextField()) {
                        JCoField  field = fit.nextField();
                        if (field.getValue() != null && !field.getValue().toString().isEmpty()){
                            //log.info("field name: {0}, value: {1}",field.getName(),field.getValue().toString());
                            record[field.getName()] = field.getValue().toString()
                        }
                    }
                    results.add(record)    
                }
            }
        }
        else{
            log.info("BAPI_EMPLCOMM_GETDETAILEDLIST SUBTYPE {0}: {1}", 
                subType, function.getExportParameterList().getStructure("RETURN").getString("MESSAGE"));
            log.info("BAPI_EMPLCOMM_GETDETAILEDLIST SUBTYPE {0}: {1}", 
                subType, function.getExportParameterList().getStructure("RETURN").getString("TYPE"));
        }
        return results
    }

    def create(String subType, Map attributes){
        // The operation is done within a JCoContext begin/end.
        // It allows the execution of stateful function calls with JCo.
        // The same connection will be used for all function calls within the script
        try{
            JCoContext.begin(destination)
            enqueue()
            
            def begin = attributes.VALIDBEGIN != null ? attributes.VALIDBEGIN : new Date().format('yyyyMMdd')
            def end = attributes.VALIDEND != null ? attributes.VALIDEND : END_DATE
            def cid = attributes.ID != null ? attributes.ID : ""

            // call BAPI_EMPLCOMM_CREATE to create new communication record
            JCoFunction function = repository.getFunction("BAPI_EMPLCOMM_CREATE");
            function.getImportParameterList().setValue("SUBTYPE", subType);
            function.getImportParameterList().setValue("EMPLOYEENUMBER", employeeId);
            function.getImportParameterList().setValue("COMMUNICATIONID", cid);
            function.getImportParameterList().setValue("VALIDITYBEGIN", begin);
            function.getImportParameterList().setValue("VALIDITYEND", end);
            log.info("Executing BAPI_EMPLCOMM_CREATE (SUBTYPE {0}) function for {1}", subType, employeeId);
            function.execute(destination);

            if (!"".equalsIgnoreCase(function.getExportParameterList().getStructure("RETURN").getString("TYPE"))){
                def errMessage = "BAPI_EMPLCOMM_CREATE (SUBTYPE $subType): " + function.getExportParameterList().getStructure("RETURN").getString("MESSAGE")
                // You can chose to either log/throw or both
                log.warn(errMessage)
                //dequeue()
                //throw new ConnectorException(errMessage);
            }
            dequeue()
        } finally {
            JCoContext.end(destination)
        }
    }
    
    def update(String subType, Map attributes){
        // Based onthe attributes present in the update, this function
        // will either call change/create_subsequent/delete or delimit
        // a communication record
        // A communication record looks like:
        // "EMAIL": {
        //        "EMPLOYEENO": "00001330",
        //        "SUBTYPE": "0010",
        //        "VALIDEND": "Fri Dec 31 00:00:00 CET 9999",
        //        "VALIDBEGIN": "Sat Jan 01 00:00:00 CET 2000",
        //        "RECORDNR": "000",
        //        "COMMTYPE": "0010",
        //        "NAMEOFCOMMTYPE": "E-mail",
        //        "ID": "FOO@EXAMPLE.COM"
        //        }
        //
        // The algorithm is the following:
        // If the update does not contain a "RECORDNR" attribute {
        //      a subsequent record is created with the provided VALIDBEGIN/VALIDEND date and ID
        // } else {
        //      If the update contains a "RECORDNR" attribute and VALIDBEGIN/VALIDEND {
        //          if the update has no "ID" and no "DELIMIT_DATE"{
        //              the record is deleted
        //          } else {
        //              if the update contains an "ID" {
        //                  the record is changed
        //              }
        //              if the update contains a "DELIMIT_DATE"{
        //                  the record validity is delimited
        //              }
        //          }
        // }
        
        // The Update operation is done within a JCoContext begin/end.
        // It allows the execution of stateful function calls with JCo.
        // The same connection will be used for all function calls within the script
        try{
            JCoContext.begin(destination)
            enqueue()
            if (attributes.RECORDNR == null) {
                create_subsequent(subType, attributes)
            }
            else {
                if (attributes.VALIDBEGIN != null && attributes.VALIDEND != null){
                    if (attributes.ID == null && attributes.DELIMIT_DATE == null){
                        delete(subType, attributes)
                    }
                    else {
                        if (attributes.ID != null) {
                            change(subType, attributes)
                        }
                        if (attributes.DELIMIT_DATE != null) {
                            delimit(subType, attributes)
                        }
                    }
                }
                else {
                    log.info("No VALIDBEGIN and VALIDEND provided");
                }
            }
            dequeue()
        } finally {
            JCoContext.end(destination)
        }
    }
    
    def change(String subType, Map attributes){
        // call BAPI_EMPLCOMM_CHANGE to update a specific communication record
        // http://www.sapdatasheet.org/abap/func/bapi_emplcomm_change.html
        JCoFunction function = repository.getFunction("BAPI_EMPLCOMM_CHANGE");
        function.getImportParameterList().setValue("SUBTYPE", subType);
        function.getImportParameterList().setValue("EMPLOYEENUMBER", employeeId);
        function.getImportParameterList().setValue("COMMUNICATIONID", attributes.ID);
        function.getImportParameterList().setValue("RECORDNUMBER", attributes.RECORDNR);
        function.getImportParameterList().setValue("VALIDITYBEGIN", attributes.VALIDBEGIN);
        function.getImportParameterList().setValue("VALIDITYEND", attributes.VALIDEND);
        function.getImportParameterList().setValue("OBJECTID", "");
        function.getImportParameterList().setValue("LOCKINDICATOR", "");
        log.info("Executing BAPI_EMPLCOMM_CHANGE (SUBTYPE {0}) function for {1}", subType, employeeId);
        function.execute(destination);

        if (!"".equalsIgnoreCase(function.getExportParameterList().getStructure("RETURN").getString("TYPE"))){
            def errMessage = "BAPI_EMPLCOMM_CHANGE (SUBTYPE $subType): " + function.getExportParameterList().getStructure("RETURN").getString("MESSAGE")
            // You can chose to either log/throw or both
            log.warn(errMessage)
            //dequeue()
            //throw new ConnectorException(errMessage);
        }
    }
    
    def delimit(String subType, Map attributes){
        // call BAPI_EMPLCOMM_DELIMIT to delimit a specific communication record
        // http://www.sapdatasheet.org/abap/func/bapi_emplcomm_delimit.html
        JCoFunction function = repository.getFunction("BAPI_EMPLCOMM_DELIMIT");
        function.getImportParameterList().setValue("SUBTYPE", subType);
        function.getImportParameterList().setValue("EMPLOYEENUMBER", employeeId);
        function.getImportParameterList().setValue("DELIMIT_DATE", attributes.DELIMIT_DATE);
        function.getImportParameterList().setValue("RECORDNUMBER", attributes.RECORDNR);
        function.getImportParameterList().setValue("VALIDITYBEGIN", attributes.VALIDBEGIN);
        function.getImportParameterList().setValue("VALIDITYEND", attributes.VALIDEND);
        function.getImportParameterList().setValue("OBJECTID", "");
        function.getImportParameterList().setValue("LOCKINDICATOR", "");
        log.info("Executing BAPI_EMPLCOMM_DELIMIT (SUBTYPE {0}) function for {1}", subType, employeeId);
        function.execute(destination);

        if (!"".equalsIgnoreCase(function.getExportParameterList().getStructure("RETURN").getString("TYPE"))){
            def errMessage = "BAPI_EMPLCOMM_DELIMIT (SUBTYPE $subType): " + function.getExportParameterList().getStructure("RETURN").getString("MESSAGE")
            // You can chose to either log/throw or both
            log.warn(errMessage)
            //dequeue()
            //throw new ConnectorException(errMessage);
        }
    }
    
    def create_subsequent(String subType, Map attributes){
        def begin = attributes.VALIDBEGIN != null ? attributes.VALIDBEGIN : new Date().format('yyyyMMdd')
        def end = attributes.VALIDEND != null ? attributes.VALIDEND : END_DATE
        def cid = attributes.ID != null ? attributes.ID : ""

        // call BAPI_EMPLCOMM_CREATESUCCESSOR to create subsequent communication record
        // http://www.sapdatasheet.org/abap/func/bapi_emplcomm_createsuccessor.html
        JCoFunction function = repository.getFunction("BAPI_EMPLCOMM_CREATESUCCESSOR");
        function.getImportParameterList().setValue("SUBTYPE", subType);
        function.getImportParameterList().setValue("EMPLOYEENUMBER", employeeId);
        function.getImportParameterList().setValue("COMMUNICATIONID", cid);
        function.getImportParameterList().setValue("VALIDITYBEGIN", begin);
        function.getImportParameterList().setValue("VALIDITYEND", end);
        log.info("Executing BAPI_EMPLCOMM_CREATESUCCESSOR (SUBTYPE {0}) function for {1}", subType, employeeId);
        function.execute(destination);

        if (!"".equalsIgnoreCase(function.getExportParameterList().getStructure("RETURN").getString("TYPE"))){
            def errMessage = "BAPI_EMPLCOMM_CREATESUCCESSOR (SUBTYPE $subType): " + function.getExportParameterList().getStructure("RETURN").getString("MESSAGE")
            // You can chose to either log/throw or both
            log.warn(errMessage)
            //dequeue()
            //throw new ConnectorException(errMessage);
        }
    }
    
    def delete(String subType, Map attributes){
        // call BAPI_EMPLCOMM_DELETE to delete a specific communication record
        // http://www.sapdatasheet.org/abap/func/bapi_emplcomm_delete.html
        JCoFunction function = repository.getFunction("BAPI_EMPLCOMM_DELETE");
        function.getImportParameterList().setValue("SUBTYPE", subType);
        function.getImportParameterList().setValue("EMPLOYEENUMBER", employeeId);
        function.getImportParameterList().setValue("RECORDNUMBER", attributes.RECORDNR);
        function.getImportParameterList().setValue("VALIDITYBEGIN", attributes.VALIDBEGIN);
        function.getImportParameterList().setValue("VALIDITYEND", attributes.VALIDEND);
        function.getImportParameterList().setValue("OBJECTID", "");
        function.getImportParameterList().setValue("LOCKINDICATOR", "");
        log.info("Executing BAPI_EMPLCOMM_DELETE (SUBTYPE {0}) function for {1}", subType, employeeId);
        function.execute(destination);

        if (!"".equalsIgnoreCase(function.getExportParameterList().getStructure("RETURN").getString("TYPE"))){
            def errMessage = "BAPI_EMPLCOMM_DELETE (SUBTYPE $subType): " + function.getExportParameterList().getStructure("RETURN").getString("MESSAGE")
            // You can chose to either log/throw or both
            log.warn(errMessage)
            //dequeue()
            //throw new ConnectorException(errMessage);
        }
    }
    
    def enqueue(){
        // call BAPI_EMPLOYEET_ENQUEUE to lock the employee entry
        JCoFunction enqueue = repository.getFunction("BAPI_EMPLOYEET_ENQUEUE");
        enqueue.getImportParameterList().setValue("NUMBER", employeeId);
        enqueue.getImportParameterList().setValue("VALIDITYBEGIN", new Date().format('yyyyMMdd'));
        log.info("Executing BAPI_EMPLOYEET_ENQUEUE({0}) function...", employeeId);
        enqueue.execute(destination);
        
        def message = enqueue.getExportParameterList().getStructure("RETURN").getString("MESSAGE")
        def status = enqueue.getExportParameterList().getStructure("RETURN").getString("TYPE")

        if (!"".equalsIgnoreCase(status)){
            log.warn("BAPI_EMPLOYEET_ENQUEUE: {0}",message);
            throw new ConnectorException(message);
        }
        
    }
    
    def dequeue(){
        // call BAPI_EMPLOYEET_DEQUEUE to unlock employee
        JCoFunction dequeue = repository.getFunction("BAPI_EMPLOYEET_DEQUEUE");
        dequeue.getImportParameterList().setValue("NUMBER",employeeId);
        log.info("Executing BAPI_EMPLOYEET_DEQUEUE({0}) function...", employeeId);
        dequeue.execute(destination);
            
        def message = dequeue.getExportParameterList().getStructure("RETURN").getString("MESSAGE")
        def status = dequeue.getExportParameterList().getStructure("RETURN").getString("TYPE")
        
        if (!"".equalsIgnoreCase(status)){
            log.warn("BAPI_EMPLOYEET_DEQUEUE: {0}", message);
            throw new ConnectorException(message);
        }
    }
}