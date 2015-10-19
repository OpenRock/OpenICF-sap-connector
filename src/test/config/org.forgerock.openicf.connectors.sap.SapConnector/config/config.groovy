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

import org.identityconnectors.common.security.GuardedString

// Connector WRONG configuration for ValidateApiOpTests
connector.i1.wrong.host=""
connector.i2.wrong.login=""
connector.i3.wrong.password=new GuardedString("".toCharArray())

environments {
    SAP_R3 {
        configuration{
            minimumRecompilationInterval = 100
            searchScriptFileName = "samples/r3/SearchSAPR3.groovy"
            createScriptFileName = "samples/r3/CreateSAPR3.groovy"
            updateScriptFileName = "samples/r3/UpdateSAPR3.groovy"
            deleteScriptFileName = "samples/r3/DeleteSAPR3.groovy"
            testScriptFileName = "samples/TestSAP.groovy"
            syncScriptFileName = null
            schemaScriptFileName = "samples/r3/SchemaSAPR3.groovy"
            authenticateScriptFileName = null
            scriptOnResourceScriptFileName = null
            resolveUsernameScriptFileName = null
            customizerScriptFileName = null
            scriptBaseClass = null
            verbose = true
            tolerance = 10
            customConfiguration = null
            classpath = ["/openicf/connectors/sap/src/main/groovy"]
            recompileGroovySource = true
            scriptRoots = ["/openicf/connectors/sap/src/main/groovy"]
            debug = true
            scriptExtensions = ["groovy"]
            sourceEncoding = "UTF-8"
            warningLevel = 1
            //targetDirectory = "/tmp"
            customSensitiveConfiguration = null
            gwHost = null
            gwServ = null
            asHost = "localhost"
            trace = "0"
            cpicTrace = "0"
            user = "SAP*"
            client = "001"
            systemNumber = "00"
            language = "EN"
            destination = "SAPR3"
            directConnection = true
            sapRouter = null
            msHost = null
            msServ = null
            r3Name = null
            group = null
            expirationPeriod = "60000"
            sncQoP = null
            x509Cert = null
            peakLimit = "0"
            maxGetTime = "30000"
            poolCapacity = "1"
            sncMyName = null
            sncSSO = "1"
            expirationTime = "60000"
            sncMode = "0"
            sncLibrary = null
            sncPartnerName = null
            password=new GuardedString("password".toCharArray())
        }
    }
    SAP_HR {
        configuration{
            minimumRecompilationInterval = 100
            searchScriptFileName = "samples/hr/SearchSAPHR.groovy"
            createScriptFileName = null
            updateScriptFileName = "samples/r3/UpdateSAPHR.groovy"
            deleteScriptFileName = null
            testScriptFileName = "samples/TestSAP.groovy"
            syncScriptFileName = null
            schemaScriptFileName = "samples/r3/SchemaSAPHR.groovy"
            authenticateScriptFileName = null
            scriptOnResourceScriptFileName = null
            resolveUsernameScriptFileName = null
            customizerScriptFileName = null
            scriptBaseClass = null
            verbose = true
            tolerance = 10
            customConfiguration = null
            classpath = ["/openicf/connectors/sap/src/main/groovy"]
            recompileGroovySource = true
            scriptRoots = ["/dvlpt/connectors/sap/src/main/groovy"]
            debug = true
            scriptExtensions = ["groovy"]
            sourceEncoding = "UTF-8"
            warningLevel = 1
            //targetDirectory = "/tmp"
            customSensitiveConfiguration = null
            gwHost = null
            gwServ = null
            asHost = "localhost"
            trace = "0"
            cpicTrace = "0"
            user = "FORGEROCK"
            client = "800"
            systemNumber = "03"
            language = "EN"
            destination = "SAPHR"
            directConnection = true
            sapRouter = "/H/localhost/S/3299"
            msHost = null
            msServ = null
            r3Name = null
            group = null
            expirationPeriod = "60000"
            sncQoP = null
            x509Cert = null
            peakLimit = "0"
            maxGetTime = "30000"
            poolCapacity = "1"
            sncMyName = null
            sncSSO = "1"
            expirationTime = "60000"
            sncMode = "0"
            sncLibrary = null
            sncPartnerName = null
            password=new GuardedString("password".toCharArray())
        }
    }
}