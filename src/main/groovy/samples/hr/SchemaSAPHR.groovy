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

import com.sap.conn.jco.JCoDestination
import com.sap.conn.jco.JCoRepository

import org.identityconnectors.common.logging.Log
import org.identityconnectors.framework.spi.operations.CreateOp
import org.identityconnectors.framework.spi.operations.DeleteOp
import org.identityconnectors.framework.spi.operations.SyncOp

import org.forgerock.openicf.misc.scriptedcommon.OperationType
import org.forgerock.openicf.misc.scriptedcommon.ICFObjectBuilder
import org.forgerock.openicf.connectors.sap.SapConfiguration

import static org.identityconnectors.framework.common.objects.AttributeInfo.Flags.MULTIVALUED
import static org.identityconnectors.framework.common.objects.AttributeInfo.Flags.NOT_UPDATEABLE

// The connector injects the following variables in the script:
// operation: the operation type ("SCHEMA" here)
// log: a handler to the Log facility
// builder: ICF object builder
// destination: the SAP Jco destination
// repository: the SAP functions repository
// configuration: the connector's configuration

def operation = operation as OperationType
def configuration = configuration as SapConfiguration
def builder = builder as ICFObjectBuilder
def destination = destination as JCoDestination
def repository = repository as JCoRepository
def log = log as Log

log.info("Entering "+operation+" Script");

builder.schema({
    objectClass {
        type 'employee'
        attributes {
            //ORG ASSIGNEMENT (INFOTYPE P0001)
            //Employee number
            //PERNO
            //TO_DATE
            //FROM_DATE
            //Last changed on
            //CH_ON
            //Company Code
            //COMP_CODE
            //Personnel Area
            //PERS_AREA
            //Employee Group
            //EGROUP
            //Employee Subgroup
            //ESUBGROUP
            //Organizational Key
            //ORG_KEY
            //Business Area
            //BUS_AREA
            //CONTRACT
            //COSTCENTER
            //ORG_UNIT
            //POSITION
            //JOB
            //full name sorted
            //SORT_NAME
            //full name
            //NAME
            "ORG_ASSIGNMENT" Map.class, NOT_UPDATEABLE
            //PERSONAL DATA (INFOTYPE P0002)
            //Employee number
            //PERNO" String.class
            //FIRSTNAME" String.class
            //LAST_NAME" String.class
            //INITIALS" String.class
            //TITLE" String.class
            //KNOWN_AS" String.class
            //GENDER" String.class
            //Employee's data valid to
            //TO_DATE" String.class
            //Employee's data valid from
            //FROM_DATE" String.class
            //BIRTHDATE" String.class
            //Employee's Birth country
            //BIRTHCTRY" String.class
            //BIRTHPLACE" String.class
            //Employee’s primary nationality
            //NATIONAL" String.class
            //Employee’s second nationality
            //NATIONAL_2" String.class
            "PERSONAL_DATA" Map.class, NOT_UPDATEABLE
            //COMMUNICATION DATA (INFOTYPE P0105, SUBTYPE 0010) - (EMAIL)
            //Employee number
            //EMPLOYEENO" String.class
            //Email address valid till
            //VALIDEND" String.class
            //Email address valid from
            //VALIDBEGIN" String.class
            //Email address
            //ID" String.class
            "EMAIL" Map.class, MULTIVALUED
            //COMMUNICATION DATA (INFOTYPE P0105, SUBTYPE 0001) - SYSTEM USER NAME
            //Employee number
            //EMPLOYEENO" String.class
            //VALIDEND" String.class
            //VALIDBEGIN" String.class
            //Account name
            //ID" String.class
            "SYS-UNAME" Map.class, MULTIVALUED
        }
        disable CreateOp.class, DeleteOp.class, SyncOp.class
    }
})