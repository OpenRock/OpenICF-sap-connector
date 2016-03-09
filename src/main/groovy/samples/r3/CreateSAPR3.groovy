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
package samples.r3

import com.sap.conn.jco.JCoDestination
import com.sap.conn.jco.JCoRepository

import org.identityconnectors.common.logging.Log
import org.identityconnectors.framework.common.objects.ObjectClass
import org.identityconnectors.framework.common.objects.OperationOptions
import org.identityconnectors.framework.common.objects.AttributesAccessor
import org.identityconnectors.framework.common.objects.Attribute
import org.identityconnectors.common.security.GuardedString

import org.forgerock.openicf.connectors.sap.SapConfiguration
import org.forgerock.openicf.misc.scriptedcommon.OperationType

// The connector injects the following variables in the script:
// destination: the SAP Jco destination
// repository: the SAP functions repository
// configuration: the connector configuration
// operation: the operation type ("CREATE" here)
// objectClass: the Object class (__ACCOUNT__ / __GROUP__ / other)
// options: the OperationOptions
// attributes: the entry attributes
// id: the id of the entry (__NAME__)
// log: the Log facility
//
// It must return the Uid of the created entry (__UID__)

def destination = destination as JCoDestination
def repository = repository as JCoRepository
def configuration = configuration as SapConfiguration
def operation = operation as OperationType
def objectClass = objectClass as ObjectClass
def options = options as OperationOptions
def createAttributes = new AttributesAccessor(attributes as Set<Attribute>)
def id = id as String
def log = log as Log

log.info("Entering {0} script", operation);
// We assume the operation is CREATE
assert operation == OperationType.CREATE, 'Operation must be a CREATE'
// We only deal with users
assert objectClass.getObjectClassValue() == ObjectClass.ACCOUNT_NAME
// Password must be defined for create
def password = createAttributes.getPassword() as GuardedString;
assert password != null, 'Password must be provided on CREATE'

def user = new R3User(repository, destination, id)
return user.create(createAttributes)