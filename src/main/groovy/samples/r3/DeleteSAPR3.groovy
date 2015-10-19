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

import com.sap.conn.jco.JCoDestination
import com.sap.conn.jco.JCoRepository

import org.identityconnectors.common.logging.Log
import org.identityconnectors.framework.common.objects.ObjectClass
import org.identityconnectors.framework.common.objects.OperationOptions
import org.identityconnectors.framework.common.objects.Uid

import org.forgerock.openicf.connectors.sap.SapConfiguration
import org.forgerock.openicf.misc.scriptedcommon.OperationType

// The connector injects the following variables in the script:
// destination: the SAP Jco destination
// repository: the SAP functions repository
// configuration: the connector configuration
// operation: the operation type ("DELETE" here)
// options: the OperationOptions
// objectClass: the Object class (__ACCOUNT__ / __GROUP__ / other)
// uid: the uid of the entry to delete (__UID__)
// log: the Log facility
//
// It does not need to return anything

def destination = destination as JCoDestination
def repository = repository as JCoRepository
def operation = operation as OperationType
def configuration = configuration as SapConfiguration
def log = log as Log
def objectClass = objectClass as ObjectClass
def uid = uid as Uid
def options = options as OperationOptions

log.info("Entering {0} script", operation);
// We assume the operation is DELETE
assert operation == OperationType.DELETE, 'Operation must be a DELETE'
// We only deal with users
assert objectClass.getObjectClassValue() == ObjectClass.ACCOUNT_NAME

def user = new R3User(repository, destination, uid.getUidValue())
user.delete()