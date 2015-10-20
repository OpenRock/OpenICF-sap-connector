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

import java.util.HashMap;
import java.util.Properties;

import com.sap.conn.jco.JCoContext;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.ext.DestinationDataEventListener;
import com.sap.conn.jco.ext.DestinationDataProvider;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString.Accessor;
import org.identityconnectors.framework.common.exceptions.ConnectorException;

/**
 * Class to represent an SAP Connection
 */
public class SapConnection {

    private SapConfiguration configuration;
    private JCoDestination destination = null;
    private Properties connectionProperties = null;
    private ConnectorDestinationDataProvider cddProvider = null;
    /**
     * Setup logging for the {@link ScriptedSQLConnection}.
     */
    private static final Log logger = Log.getLog(SapConnection.class);

    public SapConnection(SapConfiguration configuration) {
        this.configuration = configuration;
        this.connectionProperties = initializeProperties(configuration);
        this.cddProvider = ConnectorDestinationDataProvider.getInstance();

        if (cddProvider.getDestinationProperties(this.configuration.getDestination()) == null) {
            cddProvider.createDestination(this.configuration.getDestination(), initializeProperties(this.configuration));
            logger.info("Destination {0} has been created", this.configuration.getDestination());
        }
        try {
            destination = JCoDestinationManager.getDestination(this.configuration.getDestination());
        } catch (JCoException jcoe) {
            throw new ConnectorException(jcoe);
        }
    }

    /**
     * Release internal resources
     */
    public void dispose() {
        if ((this.destination != null)) {
            if (JCoContext.isStateful(this.destination)) {
                try {
                    JCoContext.end(this.destination);
                } catch (JCoException jcoe) {
                    //log
                    throw new ConnectorException(jcoe);
                }
            }
            this.cddProvider.dispose(this.configuration.getDestination());
            logger.info("Destination {0} has been disposed", this.configuration.getDestination());
        }
    }

    /**
     * If internal connection is not usable, throw IllegalStateException
     */
    public void test() {
        try {
            this.destination.ping();
        } catch (JCoException jcoe) {
            throw new ConnectorException(jcoe);
        }
    }
    
    public JCoDestination getDestination(){
        return destination;
    }

    /**
     * Initialize the connection
     */
    private Properties initializeProperties(SapConfiguration configuration) {
        final Properties connProperties = new Properties();
        connProperties.setProperty(DestinationDataProvider.JCO_USE_SAPGUI, "0");
        connProperties.setProperty(DestinationDataProvider.JCO_TRACE, configuration.getTrace());
        connProperties.setProperty(DestinationDataProvider.JCO_CPIC_TRACE, configuration.getCpicTrace());
        connProperties.setProperty(DestinationDataProvider.JCO_LANG, configuration.getLanguage());
        connProperties.setProperty(DestinationDataProvider.JCO_CLIENT, configuration.getClient());

        if (configuration.getSncMode().equals("0")) {
            connProperties.setProperty(DestinationDataProvider.JCO_SNC_MODE, configuration.getSncMode());
            connProperties.setProperty(DestinationDataProvider.JCO_USER, configuration.getUser());
            configuration.getPassword().access(new Accessor() {
                @Override
                public void access(char[] clearChars) {
                    connProperties.setProperty(DestinationDataProvider.JCO_PASSWD, new String(clearChars));
                }
            });
        } else { // using secure channel
            connProperties.setProperty(DestinationDataProvider.JCO_SNC_MODE, configuration.getSncMode());
            connProperties.setProperty(DestinationDataProvider.JCO_SNC_LIBRARY, configuration.getSncLibrary());
            connProperties.setProperty(DestinationDataProvider.JCO_SNC_MYNAME, configuration.getSncMyName());
            connProperties.setProperty(DestinationDataProvider.JCO_SNC_PARTNERNAME, configuration.getSncPartnerName());
            connProperties.setProperty(DestinationDataProvider.JCO_SNC_QOP, configuration.getSncQoP());
            connProperties.setProperty(DestinationDataProvider.JCO_X509CERT, configuration.getX509Cert());
//            connectionProperties.setProperty(DestinationDataProvider.JCO_SNC_SSO, configuration.getSncSSO());
        }

        if (configuration.isDirectConnection()) {
            connProperties.setProperty(DestinationDataProvider.JCO_SYSNR, configuration.getSystemNumber());
            connProperties.setProperty(DestinationDataProvider.JCO_ASHOST, configuration.getAsHost());
        }
        // Not used for now - Related to message server
//        else {
//            connProperties.setProperty(DestinationDataProvider.JCO_MSHOST, configuration.getMsHost());
//            connProperties.setProperty(DestinationDataProvider.JCO_MSSERV, configuration.getMsServ());
//        }

        if (configuration.getGroup() != null) {
            connProperties.setProperty(DestinationDataProvider.JCO_GROUP, configuration.getSapRouter());
        } 
        if (configuration.getR3Name() != null) {
            connProperties.setProperty(DestinationDataProvider.JCO_R3NAME, configuration.getSapRouter());
        } 
        if (configuration.getSapRouter() != null) {
            connProperties.setProperty(DestinationDataProvider.JCO_SAPROUTER, configuration.getSapRouter());
        } 
        if (configuration.getGwHost() != null) {
            connProperties.setProperty(DestinationDataProvider.JCO_GWHOST, configuration.getGwHost());
        } 
        if (configuration.getGwServ() != null) {
            connProperties.setProperty(DestinationDataProvider.JCO_GWSERV, configuration.getGwServ());
        } 

        connProperties.setProperty(DestinationDataProvider.JCO_EXPIRATION_PERIOD, configuration.getExpirationPeriod());
        connProperties.setProperty(DestinationDataProvider.JCO_EXPIRATION_TIME, configuration.getExpirationTime());
        connProperties.setProperty(DestinationDataProvider.JCO_MAX_GET_TIME, configuration.getMaxGetTime());
        connProperties.setProperty(DestinationDataProvider.JCO_PEAK_LIMIT,configuration.getPeakLimit());
        connProperties.setProperty(DestinationDataProvider.JCO_POOL_CAPACITY,configuration.getPoolCapacity());

        // Not used for now
//        connectionProperties.setProperty(DestinationDataProvider.JCO_ALIAS_USER,
//        connectionProperties.setProperty(DestinationDataProvider.JCO_AUTH_TYPE,
//        connectionProperties.setProperty(DestinationDataProvider.JCO_AUTH_TYPE_CONFIGURED_USER,configuration);
//        connectionProperties.setProperty(DestinationDataProvider.JCO_AUTH_TYPE_CURRENT_USER,configuration);
//        connectionProperties.setProperty(DestinationDataProvider.JCO_CODEPAGE,configuration);
//        connectionProperties.setProperty(DestinationDataProvider.JCO_DELTA,configuration);
//        connectionProperties.setProperty(DestinationDataProvider.JCO_DENY_INITIAL_PASSWORD,configuration);
//        connectionProperties.setProperty(DestinationDataProvider.JCO_DEST,configuration);
//        connectionProperties.setProperty(DestinationDataProvider.JCO_EXTID_DATA,configuration);
//        connectionProperties.setProperty(DestinationDataProvider.JCO_EXTID_TYPE,configuration);
//        connectionProperties.setProperty(DestinationDataProvider.JCO_GETSSO2,configuration);
//        connectionProperties.setProperty(DestinationDataProvider.JCO_LCHECK,configuration);
//        connectionProperties.setProperty(DestinationDataProvider.JCO_MYSAPSSO2,configuration);
//        connectionProperties.setProperty(DestinationDataProvider.JCO_PCS,configuration);
//        connectionProperties.setProperty(DestinationDataProvider.JCO_REPOSITORY_DEST,configuration);
//        connectionProperties.setProperty(DestinationDataProvider.JCO_REPOSITORY_PASSWD,configuration);
//        connectionProperties.setProperty(DestinationDataProvider.JCO_REPOSITORY_ROUNDTRIP_OPTIMIZATION,configuration);
//        connectionProperties.setProperty(DestinationDataProvider.JCO_REPOSITORY_SNC,configuration);
//        connectionProperties.setProperty(DestinationDataProvider.JCO_REPOSITORY_USER,configuration);
//        connectionProperties.setProperty(DestinationDataProvider.JCO_TPHOST,configuration);
//        connectionProperties.setProperty(DestinationDataProvider.JCO_TPNAME,configuration);
//        connectionProperties.setProperty(DestinationDataProvider.JCO_TYPE,configuration);
//        connectionProperties.setProperty(DestinationDataProvider.JCO_USER_ID,configuration);

        return connProperties;
    }

    static class ConnectorDestinationDataProvider implements DestinationDataProvider {

        private DestinationDataEventListener eventListener;
        private final HashMap<String, Properties> connectorProps = new HashMap<>();
        private static ConnectorDestinationDataProvider instance = null;

        public ConnectorDestinationDataProvider() {
        }

        @Override
        public Properties getDestinationProperties(String destinationName) {
            return connectorProps.get(destinationName);
        }

        @Override
        public void setDestinationDataEventListener(DestinationDataEventListener eventListener) {
            this.eventListener = eventListener;
        }

        @Override
        public boolean supportsEvents() {
            return true;
        }

        public static ConnectorDestinationDataProvider getInstance() {
            if (instance == null) {
                instance = new ConnectorDestinationDataProvider();
            }
            try {
                com.sap.conn.jco.ext.Environment.registerDestinationDataProvider(instance);
            } catch (IllegalStateException e) {
                //already registered
                throw new ConnectorException(e);
            }
            return instance;
        }

        public void createDestination(String destination, Properties connectionProperties) {
            connectorProps.put(destination, connectionProperties);
        }
        
        public void dispose(String destination){
            eventListener.deleted(destination);
        }
        
        public void refresh(String destination){
            eventListener.updated(destination);
        }
    }
}