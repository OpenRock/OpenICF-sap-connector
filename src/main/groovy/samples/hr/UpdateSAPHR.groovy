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
package samples.hr

import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoDestination
import com.sap.conn.jco.JCoRepository
import com.sap.conn.jco.JCoContext

import org.forgerock.openicf.misc.scriptedcommon.OperationType
import org.forgerock.openicf.connectors.sap.SapConfiguration

import org.identityconnectors.common.logging.Log
import org.identityconnectors.framework.common.exceptions.ConnectorException
import org.identityconnectors.framework.common.objects.Attribute
import org.identityconnectors.framework.common.objects.AttributesAccessor
import org.identityconnectors.framework.common.objects.ObjectClass
import org.identityconnectors.framework.common.objects.OperationOptions
import org.identityconnectors.framework.common.objects.Uid


// The connector injects the following variables in the script:
// destination: handler to the SAP Jco destination
// repository: handler to the SAP functions repository
//
// configuration: the connector's configuration
// operation: the operation type (UPDATE/ADD_ATTRIBUTE_VALUES/REMOVE_ATTRIBUTE_VALUES)
// objectClass: the Object class (__ACCOUNT__ / __GROUP__ / other) - 'employee' here for SAP HR
// options: a handler to the OperationOptions Map
// attributes: the <Set> of attributes to update
// uid: the entry unique identifier (ICF __UID__)
// log: a handler to the Log facility
//
// Returns: Must return the uid (ICF __UID__)

def destination = destination as JCoDestination
def repository = repository as JCoRepository

def configuration = configuration as SapConfiguration
def operation = operation as OperationType
def objectClass = objectClass as ObjectClass
def options = options as OperationOptions
def updateAttributes = new AttributesAccessor(attributes as Set<Attribute>)
def uid = uid as Uid
def log = log as Log

log.info("Entering {0} script",operation);

// We assume the operation is UPDATE and the object class is 'employee'
assert operation == OperationType.UPDATE, 'Operation must be a SEARCH'
assert objectClass.is("employee"), "Object Class must be employee"

def EMAIL_SUBTYPE = "0010"
def SYS_UNAME_SUBTYPE = "0001"

// This example will show how to update/create an EMAIL address on the INFOTYPE 0105 SUBTYPE 0010
// as well as the SYS-UNAME on the INFOTYPE 0105 SUBTYPE 0001

def empComm = new EmplComm(repository, destination, uid.uidValue)
def email = updateAttributes.findMap("EMAIL");
def sysname = updateAttributes.findMap("SYS-UNAME");

if (email != null){
    if (empComm.get(EMAIL_SUBTYPE) == null){
        empComm.create(EMAIL_SUBTYPE, email)
    }
    else {
        empComm.update(EMAIL_SUBTYPE, email)
    }
}

if (sysname != null){
    if (empComm.get(SYS_UNAME_SUBTYPE) == null){
        empComm.create(SYS_UNAME_SUBTYPE, sysname)
    }
    else {
        empComm.update(SYS_UNAME_SUBTYPE, sysname)
    }
}

log.info("Exiting {0} script",operation);
return uid;