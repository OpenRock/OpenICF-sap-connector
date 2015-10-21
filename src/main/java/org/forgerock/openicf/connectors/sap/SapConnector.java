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

package org.forgerock.openicf.connectors.sap;

import com.sap.conn.jco.JCoDestination;

import groovy.lang.Binding;
import groovy.lang.Closure;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.ConnectorClass;

import org.forgerock.openicf.misc.scriptedcommon.ScriptedConnectorBase;
/**
 * Main implementation of the Sap Connector.
 *
 */
@ConnectorClass(displayNameKey = "Sap.connector.display",
        configurationClass = SapConfiguration.class, messageCatalogPaths = {
            "org/forgerock/openicf/connectors/groovy/Messages",
            "org/forgerock/openicf/connectors/sap/Messages" })
public class SapConnector extends ScriptedConnectorBase<SapConfiguration> implements Connector {

    /**
     * Setup logging for the {@link SapConnector}.
     */
    private static final Log logger = Log.getLog(SapConnector.class);
    
    @Override
    protected Object evaluateScript(String scriptName, Binding arguments,
            Closure<Object> scriptEvaluator) throws Exception {
        final JCoDestination destination = ((SapConfiguration) getScriptedConfiguration()).getDestinationHandler();
        arguments.setVariable("destination", destination);
        arguments.setVariable("repository", destination.getRepository());
        arguments.setVariable(LOGGER, logger);
        return scriptEvaluator.call(scriptName, arguments);
    }
}
