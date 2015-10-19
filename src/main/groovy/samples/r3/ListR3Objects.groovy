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

import com.sap.conn.jco.JCoRepository;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoTable;

import org.identityconnectors.common.logging.Log


public class ListR3Objects {
    
    private static final Log log = Log.getLog(ListR3Objects.class);
    
    private ListR3Objects(){
        // This is a util class
    }
    
    public static void listAccounts(JCoRepository repository, JCoDestination destination, Closure handler, String[] attributesToGet, Log log){
        def function = repository.getFunction("BAPI_USER_GETLIST");
        function.getImportParameterList().setValue("WITH_USERNAME",'*');
        function.execute(destination);
        log.info("Number of users: "+function.getExportParameterList().getString("ROWS"));
        JCoTable  userList = function.getTableParameterList().getTable("USERLIST");
        for (int i = 0; i < userList.getNumRows(); i++)
        {
            userList.setRow(i);
            log.ok("Username: "+userList.getString("USERNAME"));
            handler {
                uid userList.getString("USERNAME")
                id userList.getString("USERNAME")
                attributesToGet.each() { attribute it, userList.getString(it) }
            }
        }
    }
    
    public static void listActivityGroups(JCoRepository repository, JCoDestination destination, Closure handler, String[] attributesToGet, Log log){
        def function = repository.getFunction("RFC_GET_TABLE_ENTRIES");
        function.getImportParameterList().setValue("TABLE_NAME","AGR_DEFINE");
        function.execute(destination);
        log.info("Number of Activity Groups: "+function.getExportParameterList().getString("NUMBER_OF_ENTRIES"));
        JCoTable agr = function.getTableParameterList().getTable("ENTRIES");
        log.info("Num rows: "+agr.getNumRows()+" ::::: Column: "+agr.getNumColumns())
        for (int i = 0; i < agr.getNumRows(); i++)
        {
            agr.setRow(i);
            //The AGR_NAME is offset 3 and length 30
            def agr_name = agr.getString(0).substring(3,30+3).trim()
            handler {
                uid agr_name
                id agr_name
            }
        }
    }
    public static void listCompanies(JCoRepository repository, JCoDestination destination, Closure handler, String[] attributesToGet, Log log){
        def function = repository.getFunction("RFC_GET_TABLE_ENTRIES");
        function.getImportParameterList().setValue("TABLE_NAME","USCOMPANY");
        function.execute(destination);
        log.info("Number of Companies: "+function.getExportParameterList().getString("NUMBER_OF_ENTRIES"));
        JCoTable comp = function.getTableParameterList().getTable("ENTRIES");
        log.info("Num rows: "+comp.getNumRows()+" ::::: Column: "+comp.getNumColumns())
        for (int i = 0; i < comp.getNumRows(); i++)
        {
            comp.setRow(i);
            //The COMPANY is offset 3 and length 42
            def comp_name = comp.getString(0).substring(3,42+3).trim()
            handler {
                uid comp_name
                id comp_name
            }
        }
    }
    
    public static void listRoles(JCoRepository repository, JCoDestination destination, Closure handler, String[] attributesToGet, Log log){
        def function = repository.getFunction("PRGN_ROLE_GETLIST");
        function.execute(destination);
        log.info("Number of Roles: "+function.getExportParameterList().getString("ROWS"));
        JCoTable table = function.getTableParameterList().getTable("ROLES");
        log.info("Num rows: "+table.getNumRows()+" ::::: Column: "+table.getNumColumns())
        for (int i = 0; i < table.getNumRows(); i++)
        {
            table.setRow(i)
            handler{
                uid table.getString("AGR_NAME")
                id table.getString("AGR_NAME")
                attributesToGet.each() { attribute it, table.getString(it) }
            }
        }
    }
    
    public static void listProfiles(JCoRepository repository, JCoDestination destination, Closure handler, String[] attributesToGet, Log log){
        def function = repository.getFunction("BAPI_HELPVALUES_GET");
        function.getImportParameterList().setValue("OBJTYPE","USER");
        function.getImportParameterList().setValue("METHOD","GETDETAIL");
        function.getImportParameterList().setValue("PARAMETER","PROFILES");
        function.getImportParameterList().setValue("FIELD","BAPIPROF");
        function.execute(destination);
        JCoTable table = function.getTableParameterList().getTable("VALUES_FOR_FIELD");
        log.info("Num rows: "+table.getNumRows()+", Num columns: "+table.getNumColumns())
        for (int i = 0; i < table.getNumRows(); i++)
        {
            table.setRow(i)
            handler{
                uid table.getString("VALUES")
                id table.getString("VALUES")
            }
        }
    }
}