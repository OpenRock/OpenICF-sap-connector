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
 * Copyright 2015-2016 ForgeRock AS.
 */

package samples

import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoStructure;
import com.sap.conn.jco.JCoDestination
import com.sap.conn.jco.JCoRepository

import org.forgerock.openicf.misc.scriptedcommon.OperationType
import org.forgerock.openicf.connectors.sap.SapConfiguration

import org.identityconnectors.common.logging.Log

// The connector injects the following variables in the script:
// destination: SAP destination
// repository: SAP functions repository
// operation: the operation type ("TEST" here)
// configuration: the connector configuration
// log: the Log facility

// The test script returns nothing if successful
// It must throw an exception if test failed

def destination = destination as JCoDestination
def repository = repository as JCoRepository
def operation = operation as OperationType
def configuration = configuration as SapConfiguration
def log = log as Log

log.info("Entering {0} script",operation);
destination.ping();

// This Test script executes RFC_SYSTEM_INFO BAPI.
// RFC_SYSTEM_INFO is a standard SAP function module available 
// within R/3 systems.
// It returns various information in the RFCSI_EXPORT structure

def function = repository.getFunction("RFC_SYSTEM_INFO");
log.info("Executing {0}", function.getName());
function.execute(destination);
JCoStructure exportStructure = function.getExportParameterList().getStructure("RFCSI_EXPORT");

def result = "SAP System information: \n"
for(int i = 0; i < exportStructure.getMetaData().getFieldCount(); i++)
{ 
    name = exportStructure.getMetaData().getName(i)
    value = exportStructure.getString(i)
    result = result.concat("$name: $value\n")
}
log.info(result)
log.info("Test script done");