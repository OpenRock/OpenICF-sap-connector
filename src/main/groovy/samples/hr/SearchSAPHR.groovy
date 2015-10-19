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

import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoTable;
import com.sap.conn.jco.JCoRecordFieldIterator
import com.sap.conn.jco.JCoField
import com.sap.conn.jco.JCoDestination
import com.sap.conn.jco.JCoRepository

import org.identityconnectors.common.logging.Log
import org.identityconnectors.framework.common.objects.AttributeUtil
import org.identityconnectors.framework.common.objects.ObjectClass
import org.identityconnectors.framework.common.objects.OperationOptions
import org.identityconnectors.framework.common.objects.SearchResult
import org.identityconnectors.framework.common.objects.Uid
import org.identityconnectors.framework.common.objects.filter.EqualsFilter
import org.identityconnectors.framework.common.objects.filter.Filter

import org.forgerock.openicf.connectors.sap.SapConfiguration
import org.forgerock.openicf.connectors.sap.SapHrFilterVisitor
import org.forgerock.openicf.misc.scriptedcommon.OperationType

// The connector injects the following variables in the script:
// destination: the SAP Jco destination
// repository: the SAP functions repository
// handler: a callback to pass the search results (Closure here)
// operation: the operation type ("SEARCH" here)
// objectClass: the Object class (__ACCOUNT__ / __GROUP__ / other)
// log: the Log facility
// options: the OperationOptions
// filter: the query filter
// configuration: the connector configuration

def destination = destination as JCoDestination
def repository = repository as JCoRepository
def operation = operation as OperationType
def configuration = configuration as SapConfiguration
def filter = filter as Filter
def log = log as Log
def objectClass = objectClass as ObjectClass
def options = options as OperationOptions
def attributesToGet = options.getAttributesToGet() as String[]

log.info("Entering {0} script",operation);
assert operation == OperationType.SEARCH, 'Operation must be a SEARCH'
assert objectClass.is("employee"), "Object Class must be employee"

// We do not want __UID__ and __NAME__
if (attributesToGet != null){
    attributesToGet = attributesToGet - [Uid.NAME] - [Name.NAME] as String[]
} else {
    attributesToGet = []
}

// Get today's date formated the SAP way: yyyyMMdd
// def now = new Date();
// def today = now.format('yyyyMMdd');

// Employee number - the key
def empNumber = null;

def function = repository.getFunction("BAPI_EMPLOYEE_GETDATA");

if (filter == null) {
    // We want to return all ids
    function.getImportParameterList().setValue("LASTNAME_M",'*');
} else if (filter instanceof EqualsFilter && ((EqualsFilter) filter).getAttribute().is(Uid.NAME)) {
    // This is a GET/READ
    empNumber = ((EqualsFilter) filter).getAttribute().getValue().get(0)
    function.getImportParameterList().setValue("EMPLOYEE_ID",empNumber);
} else {
    // we need to check if the filter matches the capabilities
    filter.accept(SapHrFilterVisitor.INSTANCE, function)
}

log.info("Executing BAPI_EMPLOYEE_GETDATA function...");
function.execute(destination);
    
// Check status
if (!"".equalsIgnoreCase(function.getExportParameterList().getStructure("RETURN").getString("TYPE"))){
    log.info("BAPI_EMPLOYEE_GETDATA - Returned message: {0}",function.getExportParameterList().getStructure("RETURN").getString("MESSAGE"));
    log.info("BAPI_EMPLOYEE_GETDATA - Returned type: {0}",function.getExportParameterList().getStructure("RETURN").getString("TYPE"));
    // Nothing found or problem... return here
    return;
}

JCoTable persData = function.getTableParameterList().getTable("PERSONAL_DATA");
JCoTable orgData = null
if (attributesToGet.contains("ORG_ASSIGNMENT")){
    orgData = function.getTableParameterList().getTable("ORG_ASSIGNMENT");
}

for (int i = 0; i < persData.getNumRows(); i++) {
    persData.setRow(i);
    log.ok("Employee Number: {0}",persData.getString("PERNO"));
    def perno = persData.getString("PERNO")
    
    handler {
        uid perno
        id perno
        if (attributesToGet.size() > 0) {
            // PERSONAL_DATA table (INFOTYPE: 0002)
            log.info("Processing PERSONAL DATA:");
            def personal_data_attributes = [:]
            def org_data_attributes = [:]
            def sys_attributes = [:]
            JCoRecordFieldIterator fit = persData.getRecordFieldIterator();
            while (fit.hasNextField()) {
                JCoField  field = fit.nextField();
                if (field.getValue() != null  && !field.getValue().toString().isEmpty()){
                    //log.info("field name: {0}, value: {1}",field.getName(),field.getValue().toString());
                    personal_data_attributes[field.getName()] = field.getValue().toString()
                    }
            }
            attribute "PERSONAL_DATA", personal_data_attributes

            // ORG_ASSIGNMENT table (INFOTYPE: 0001)
            if (orgData != null) {
                log.info("Processing ORG ASSIGNEMENT:");
                orgData.setRow(i);
                if (!orgData.isEmpty()) {
                    fit = orgData.getRecordFieldIterator();
                    while (fit.hasNextField()) {
                        JCoField  field = fit.nextField();
                        if (field.getValue() != null && !field.getValue().toString().isEmpty()){
                            //log.info("field name: {0}, value: {1}",field.getName(),field.getValue().toString());
                            org_data_attributes[field.getName()] = field.getValue().toString()
                        }
                    }
                    attribute "ORG_ASSIGNMENT", org_data_attributes
                }
            }
            if (attributesToGet.contains("EMAIL") || attributesToGet.contains("SYS-UNAME")){
                // COMMUNICATION table (INFOTYPE: 0105)
                // EMAIL SUBTYPE: 0010
                // SYS-UNAME (System-User) SUBTYPE: 0001
                def empComm = new EmplComm(repository, destination, perno)
                
                if (attributesToGet.contains("EMAIL")) {
                    def email_records = empComm.get("0010")
                    if (email_records != null) {
                        attribute "EMAIL", email_records
                    }
                }
                if (attributesToGet.contains("SYS-UNAME")) {
                    def sys_records = empComm.get("0001")
                    if (sys_records != null) {
                        attribute "SYS-UNAME", sys_records
                    }
                }
            }
        }
    }
}   
log.info("Exiting {0} script",operation);
return new SearchResult();