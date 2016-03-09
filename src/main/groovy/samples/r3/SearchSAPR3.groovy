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
import org.identityconnectors.framework.common.objects.SearchResult
import org.identityconnectors.framework.common.objects.filter.EqualsFilter
import org.identityconnectors.framework.common.objects.filter.Filter
import org.identityconnectors.framework.common.objects.Uid
import org.identityconnectors.framework.common.objects.Name

import org.forgerock.openicf.connectors.sap.SapConfiguration
import org.forgerock.openicf.misc.scriptedcommon.OperationType

// The connector injects the following variables in the script:
// destination: the SAP Jco destination
// repository: the SAP functions repository
// configuration: the connector configuration
// operation: the operation type ("SEARCH" here)
// objectClass: the Object class (__ACCOUNT__ / __GROUP__ / other)
// options: the OperationOptions
// handler: a callback to pass the search results (Closure here)
// log: the Log facility
// filter: the query filter

def destination = destination as JCoDestination
def repository = repository as JCoRepository
def configuration = configuration as SapConfiguration
def operation = operation as OperationType
def objectClass = objectClass as ObjectClass
def options = options as OperationOptions
def filter = filter as Filter
def log = log as Log
def attributesToGet = options.getAttributesToGet() as String[]

log.info("Entering {0} script",operation);
// We assume the operation is SEARCH
assert operation == OperationType.SEARCH, 'Operation must be a SEARCH'

// We do not want __UID__ and __NAME__ being part of the attributes to get
if (attributesToGet != null){
    attributesToGet = attributesToGet - [Uid.NAME] - [Name.NAME] as String[]
}

switch ( objectClass.getObjectClassValue() ) {
    case ObjectClass.ACCOUNT_NAME:
        if (filter == null) {
            ListR3Objects.listAccounts(repository, destination, handler, attributesToGet, log)
        } else if (filter instanceof EqualsFilter && ((EqualsFilter) filter).getAttribute().is(Uid.NAME)) {
            def username = ((EqualsFilter) filter).getAttribute().getValue().get(0)
            def user = new R3User(repository, destination, username)
            user.get(attributesToGet)
            handler {
                uid user.userName
                id user.userName
                user.attributes.each(){ key,value -> attribute key, value }
            }
        }
        break
    case "profile":
        if (filter == null) {
            ListR3Objects.listProfiles(repository, destination, handler, attributesToGet, log)
        }
        break
    case "activity_group":
        if (filter == null) {
            ListR3Objects.listActivityGroups(repository, destination, handler, attributesToGet, log)
        }    
        break
    case "company":
        if (filter == null) {
            ListR3Objects.listCompanies(repository, destination, handler, attributesToGet, log)
        }
        break
    default:
        break
}