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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.OperationOptionsBuilder;
import org.identityconnectors.framework.common.objects.OperationalAttributes;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterBuilder;
import org.identityconnectors.framework.impl.api.local.LocalConnectorFacadeImpl;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.test.common.PropertyBag;
import org.identityconnectors.test.common.TestHelpers;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.Assert;
import org.testng.SkipException;


/**
 * Test the {@link SapConnector} with the framework.
 *
 */
public class SapConnectorTests {

    /**
    * Setup logging for the {@link SapConnectorTests}.
    */
    private static final Log LOGGER = Log.getLog(SapConnectorTests.class);
    
    private SapConfiguration config;
    /**
     * Single ThreadSafe Facade.
     */
    private ConnectorFacade connectorFacade = null;

    private static final PropertyBag PROPERTIES = TestHelpers.getProperties(SapConnector.class);

    @BeforeClass
    public void setUp() {
//        config = new SapConfiguration();
//        config.setClient(PROPERTIES.getStringProperty("configuration.client"));
//        config.setAsHost(PROPERTIES.getStringProperty("configuration.asHost"));
//        config.setDestination(PROPERTIES.getStringProperty("configuration.destination"));
//        config.setDirectConnection(true);
//        config.setSapRouter(PROPERTIES.getStringProperty("configuration.sapRouter"));
//        config.setSystemNumber(PROPERTIES.getStringProperty("configuration.systemNumber"));
//        config.setLanguage(PROPERTIES.getStringProperty("configuration.language"));
//        config.setUser(PROPERTIES.getStringProperty("configuration.user"));
//        config.setPassword(PROPERTIES.getProperty("configuration.password", GuardedString.class));
//
//        config.setScriptRoots(PROPERTIES.getProperty("configuration.scriptRoots", String[].class));
//        config.setTestScriptFileName(PROPERTIES.getStringProperty("configuration.testScriptFileName"));
//        config.setSearchScriptFileName(PROPERTIES.getStringProperty("configuration.searchScriptFileName"));
//        config.setUpdateScriptFileName(PROPERTIES.getStringProperty("configuration.updateScriptFileName"));
//        config.setCreateScriptFileName(PROPERTIES.getStringProperty("configuration.createScriptFileName"));
//        config.setDeleteScriptFileName(PROPERTIES.getStringProperty("configuration.deleteScriptFileName"));

        //connector = new SAPConnector();
        //connector.init(config);
        //connectorFacade = getFacade(config);
        connectorFacade = getFacade(SapConnector.class, "SAP_HR");
    }

    @AfterClass
    public void tearDown() {
        //
        // clean up resources
        //
        if (connectorFacade instanceof LocalConnectorFacadeImpl) {
            ((LocalConnectorFacadeImpl) connectorFacade).dispose();
        }
    }
    
    @Test(enabled = true)
    public void testTest() {
        LOGGER.info("Running testTest...");
        connectorFacade.test();
    }
    
    /////////////////////   HR TESTS ////////////////////////////////////

    @Test(enabled = true)
    public void testQueryListAllSAPHREMployee() {
        // just need to send an empty query
        // to fetch all entries
        LOGGER.info("Running testQueryListAllSAPHREMployee...");
        OperationOptionsBuilder oob = new OperationOptionsBuilder();
        List<ConnectorObject> results = TestHelpers.searchToList(connectorFacade, new ObjectClass("employee"), null, oob.build());
        LOGGER.info("testQueryListAllSAPHREMployee reports " + results.size() + " employees");
        //LOGGER.info(results.toString());
    }

    @Test(enabled = true)
    public void testQuerySearchSAPHREmployee() {
        LOGGER.info("Running testQueryGetSAPHREmployee...");
        OperationOptionsBuilder oob = new OperationOptionsBuilder();
        oob.setAttributesToGet("ORG_ASSIGNMENT", "COMMUNICATION_EMAIL", "COMMUNICATION_ACCOUNT");
        Filter filter = FilterBuilder.equalTo(AttributeBuilder.build("EMPLOYEE_ID", "00001327"));
        List<ConnectorObject> results = TestHelpers.searchToList(connectorFacade, new ObjectClass("employee"), filter, oob.build());
        LOGGER.info("testQueryGetSAPHREmployee reports " + results.size() + " employee");
        Assert.assertEquals(1, results.size());
    }

    @Test(enabled = true)
    public void testGetSAPHREmployee() {
        LOGGER.info("Running testQueryGetSAPHREmployee...");
        OperationOptionsBuilder oob = new OperationOptionsBuilder();
        Assert.assertNotNull(connectorFacade.getObject(new ObjectClass("employee"), new Uid("00001327"), oob.build()));
    }

    @Test(enabled = true)
    public void testUpdateSAPHREmail() {
        String empno = "200013";
        Uid uid = new Uid(empno);
        AttributeBuilder ab = new AttributeBuilder();
        ab.setName("COMMUNICATION_EMAIL_ID");
        ab.addValue("Bob.Flemming@fast.com");
        java.util.Set<Attribute> replaceAttributes = new java.util.HashSet<>();
        replaceAttributes.add(ab.build());
        OperationOptionsBuilder oob = new OperationOptionsBuilder();
        connectorFacade.update(new ObjectClass("employee"), uid, replaceAttributes, oob.build());
        System.out.println("UPDATE done, retrieving user to check... ");

        Filter filter = FilterBuilder.equalTo(AttributeBuilder.build("EMPLOYEE_ID", empno));
        List<ConnectorObject> results = TestHelpers.searchToList(connectorFacade, new ObjectClass("employee"), filter, oob.build());
        System.out.println("Returned email is: " + results.get(0).getAttributeByName("COMMUNICATION_EMAIL_ID").getValue().get(0));
    }

    @Test(enabled = true)
    public void testUpdateSAPHRSysName() {
        String empno = "200013";
        Uid uid = new Uid(empno);
        AttributeBuilder ab = new AttributeBuilder();
        ab.setName("COMMUNICATION_ACCOUNT_ID");
        ab.addValue("BFlemm");
        java.util.Set<Attribute> replaceAttributes = new java.util.HashSet<>();
        replaceAttributes.add(ab.build());
        OperationOptionsBuilder oob = new OperationOptionsBuilder();
        connectorFacade.update(new ObjectClass("employee"), uid, replaceAttributes, oob.build());
        System.out.println("UPDATE done, retrieving user to check... ");

        Filter filter = FilterBuilder.equalTo(AttributeBuilder.build("EMPLOYEE_ID", empno));
        List<ConnectorObject> results = TestHelpers.searchToList(connectorFacade, new ObjectClass("employee"), filter, oob.build());
        System.out.println("Returned account is: " + results.get(0).getAttributeByName("COMMUNICATION_ACCOUNT_ID").getValue().get(0));
    }

    
    ///////////////////////////////////        R/3 TESTS         /////////////////////////////////////
    
    
    //////////////  QUERY/SEARCH
    @Test(enabled = true, groups = {"search"})
    public void testListAllSAPR3Users() {
        // empty query to fetch all entries
        OperationOptionsBuilder oob = new OperationOptionsBuilder();
        List<ConnectorObject> results = TestHelpers.searchToList(connectorFacade, ObjectClass.ACCOUNT, null, oob.build());
        Assert.assertTrue(results.size() > 3);
    }
    
    @Test(enabled = true, groups = {"search"})
    public void testListAllSAPR3Profiles() {
        // empty query to fetch all entries
        OperationOptionsBuilder oob = new OperationOptionsBuilder();
        List<ConnectorObject> results = TestHelpers.searchToList(connectorFacade, new ObjectClass("profile"), null, oob.build());
        Assert.assertTrue(results.size() > 3);
    }
    
    @Test(enabled = true, groups = {"search"})
    public void testListAllSAPR3Roles() {
        // empty query to fetch all entries
        OperationOptionsBuilder oob = new OperationOptionsBuilder();
        List<ConnectorObject> results = TestHelpers.searchToList(connectorFacade, new ObjectClass("activity_group"), null, oob.build());
        Assert.assertTrue(results.size() > 3);
    }
    
    @Test(enabled = true, groups = {"search"})
    public void testListAllSAPR3Companies() {
        // empty query to fetch all entries
        OperationOptionsBuilder oob = new OperationOptionsBuilder();
        List<ConnectorObject> results = TestHelpers.searchToList(connectorFacade, new ObjectClass("company"), null, oob.build());
        Assert.assertTrue(results.size() > 0);
    }

    @Test(enabled = true, groups = {"search"}, expectedExceptions = {ConnectorException.class})
    public void testFailGetSAPR3User() {
        OperationOptionsBuilder oob = new OperationOptionsBuilder();
        connectorFacade.getObject(ObjectClass.ACCOUNT, new Uid("NOTEXIST"), oob.build());
        //Filter filter = FilterBuilder.equalTo(AttributeBuilder.build("USERNAME", "NOTEXIST"));
        //List<ConnectorObject> results = TestHelpers.searchToList(connectorFacade, ObjectClass.ACCOUNT, filter, oob.build());
    }

    @Test(enabled = true, groups = {"search"})
    public void testExactQuerySAPR3User() {
        OperationOptionsBuilder oob = new OperationOptionsBuilder();
        Filter filter = FilterBuilder.equalTo(AttributeBuilder.build("USERNAME", "NOTEXIST"));
        Assert.assertTrue(TestHelpers.searchToList(connectorFacade, ObjectClass.ACCOUNT, filter, oob.build()).isEmpty());
    }
    
    //////////////  CREATE

    @Test(enabled = true, groups = {"create"}, dependsOnGroups = {"delete"})
    public void testCreateSimpleSAPR3UserPasswordExpired() {
        java.util.Set<Attribute> createAttributes = new java.util.HashSet<>();
        createAttributes.add(AttributeBuilder.buildPassword(new GuardedString("Passw0rd".toCharArray())));
        createAttributes.add(new Name("PWDEXP"));
        OperationOptionsBuilder oob = new OperationOptionsBuilder();
        Uid uid = connectorFacade.create(ObjectClass.ACCOUNT, createAttributes, oob.build());
        Assert.assertEquals("PWDEXP", uid.getUidValue());
        ConnectorObject co = connectorFacade.getObject(ObjectClass.ACCOUNT, uid, oob.build());
        //Assert.assertTrue((Boolean)co.getAttributeByName(OperationalAttributes.PASSWORD_EXPIRED_NAME).getValue().get(0));
        
    }

    @Test(enabled = true, groups = {"create"}, dependsOnGroups = {"delete"})
    public void testCreateSimpleSAPR3UserPasswordActive() {
        java.util.Set<Attribute> createAttributes = new java.util.HashSet<>();
        createAttributes.add(AttributeBuilder.buildPassword(new GuardedString("Passw0rd".toCharArray())));
        createAttributes.add(AttributeBuilder.buildPasswordExpired(false));
        createAttributes.add(new Name("ACTIV"));
        OperationOptionsBuilder oob = new OperationOptionsBuilder();
        Uid uid = connectorFacade.create(ObjectClass.ACCOUNT, createAttributes, oob.build());
        Assert.assertEquals("ACTIV", uid.getUidValue());
        ConnectorObject co = connectorFacade.getObject(ObjectClass.ACCOUNT, uid, oob.build());
        //Assert.assertFalse((Boolean)co.getAttributeByName(OperationalAttributes.PASSWORD_EXPIRED_NAME).getValue().get(0));
        Assert.assertTrue((Boolean)co.getAttributeByName(OperationalAttributes.ENABLE_NAME).getValue().get(0));
    }

    @Test(enabled = true, groups = {"create"}, dependsOnGroups = {"delete"})
    public void testCreateSimpleSAPR3UserLocked() {
        java.util.Set<Attribute> createAttributes = new java.util.HashSet<>();
        createAttributes.add(AttributeBuilder.buildPassword(new GuardedString("Passw0rd".toCharArray())));
        createAttributes.add(AttributeBuilder.buildLockOut(true));
        createAttributes.add(new Name("LOCKED"));
        OperationOptionsBuilder oob = new OperationOptionsBuilder();
        Uid uid = connectorFacade.create(ObjectClass.ACCOUNT, createAttributes, oob.build());
        Assert.assertEquals("LOCKED", uid.getUidValue());
        ConnectorObject co = connectorFacade.getObject(ObjectClass.ACCOUNT, uid, oob.build());
        //Assert.assertFalse((Boolean)co.getAttributeByName(OperationalAttributes.PASSWORD_EXPIRED_NAME).getValue().get(0));
        Assert.assertTrue((Boolean)co.getAttributeByName(OperationalAttributes.LOCK_OUT_NAME).getValue().get(0));
    }

    @Test(enabled = true, groups = {"create"}, dependsOnGroups = {"delete"})
    public void testCreateSAPR3User() {
        Map<String,String> logonData = new HashMap<>();
        Map<String,String> address = new HashMap<>();
        Map<String,String> company = new HashMap<>();
        Map<String,String> alias = new HashMap<>();
        logonData.put("GLTGV","2014-10-01");
        logonData.put("GLTGB","2017-11-20");
        logonData.put("USTYP","A");
        address.put("FIRSTNAME","John");
        address.put("LASTNAME","Doe");
        address.put("MIDDLENAME","Kevin");
        address.put("TEL1_NUMBR","33297603177");
        address.put("E_MAIL","john.doe@example.com");
        address.put("FUNCTION","Test User");
        company.put("COMPANY", "FORGEROCK");
        alias.put("USERALIAS", "JDOE");
        java.util.Set<Attribute> createAttributes = new java.util.HashSet<>();
        createAttributes.add(new Name("REGULAR"));
        createAttributes.add(AttributeBuilder.buildPassword(new GuardedString("Passw0rd".toCharArray())));
        createAttributes.add(AttributeBuilder.buildPasswordExpired(false));
        createAttributes.add(AttributeBuilder.build("LOGONDATA", logonData));
        createAttributes.add(AttributeBuilder.build("ADDRESS", address));
        createAttributes.add(AttributeBuilder.build("COMPANY", company));
        createAttributes.add(AttributeBuilder.build("ALIAS", alias));
        OperationOptionsBuilder oob = new OperationOptionsBuilder();
        Uid uid = connectorFacade.create(ObjectClass.ACCOUNT, createAttributes, oob.build());
        Assert.assertEquals("REGULAR", uid.getUidValue());
        ConnectorObject co = connectorFacade.getObject(ObjectClass.ACCOUNT, uid, oob.build());
        //Assert.assertTrue((Boolean)co.getAttributeByName(OperationalAttributes.PASSWORD_EXPIRED_NAME).getValue().get(0));
        Assert.assertTrue((Boolean)co.getAttributeByName(OperationalAttributes.ENABLE_NAME).getValue().get(0));
    }
    
    @Test(enabled = true, groups = {"create"}, dependsOnGroups = {"delete"})
    public void testCreateSimpleSAPR3UserWithRoles() {
        List<Map> roles = new ArrayList<>();
        Map<String,String> role1 = new HashMap<>();
        Map<String,String> role2 = new HashMap<>();
        role1.put("AGR_NAME", "SAP_AUDITOR_SA_CCM_USR");
        role1.put("FROM_DAT", "2015-06-02");
        role1.put("TO_DAT", "2016-06-02");
        role2.put("AGR_NAME", "SAP_ALM_ADMINISTRATOR");
        roles.add(role1);
        roles.add(role2);
        java.util.Set<Attribute> createAttributes = new java.util.HashSet<>();
        createAttributes.add(AttributeBuilder.buildPassword(new GuardedString("Passw0rd".toCharArray())));
        createAttributes.add(new Name("TESTAGR"));
        createAttributes.add(AttributeBuilder.build("ACTIVITYGROUPS", roles));
        
        OperationOptionsBuilder oob = new OperationOptionsBuilder();
        Uid uid = connectorFacade.create(ObjectClass.ACCOUNT, createAttributes, oob.build());
        Assert.assertEquals("TESTAGR", uid.getUidValue());
        ConnectorObject co = connectorFacade.getObject(ObjectClass.ACCOUNT, uid, oob.build());
        Assert.assertEquals(2, co.getAttributeByName("ACTIVITYGROUPS").getValue().size());
    }

    @Test(enabled = true, groups = {"create"}, dependsOnGroups = {"delete"})
    public void testCreateSimpleSAPR3UserWithProfiles() {
        List<Map> profiles = new ArrayList<>();
        Map<String,String> prof1 = new HashMap<>();
        Map<String,String> prof2 = new HashMap<>();
        prof1.put("BAPIPROF", "S_A.SYSTEM");
        prof2.put("BAPIPROF", "SAP_ALL");
        profiles.add(prof1);
        profiles.add(prof2);
        java.util.Set<Attribute> createAttributes = new java.util.HashSet<>();
        createAttributes.add(AttributeBuilder.buildPassword(new GuardedString("Passw0rd".toCharArray())));
        createAttributes.add(new Name("TESTPROF"));
        createAttributes.add(AttributeBuilder.build("PROFILES", profiles));
        
        OperationOptionsBuilder oob = new OperationOptionsBuilder();
        Uid uid = connectorFacade.create(ObjectClass.ACCOUNT, createAttributes, oob.build());
        Assert.assertEquals("TESTPROF", uid.getUidValue());
        ConnectorObject co = connectorFacade.getObject(ObjectClass.ACCOUNT, uid, oob.build());
        Assert.assertEquals(2, co.getAttributeByName("PROFILES").getValue().size());
    }
    
    //////////////  UPDATE

    @Test(enabled = true, dependsOnGroups = {"create"}, groups = {"update"})
    public void testUpdateSAPR3UserDelRoles() {
        java.util.Set<Attribute> updateAttributes = new java.util.HashSet<>();
        updateAttributes.add(AttributeBuilder.build("ACTIVITYGROUPS", new ArrayList<Map>()));
        
        OperationOptionsBuilder oob = new OperationOptionsBuilder();
        Uid uid = connectorFacade.update(ObjectClass.ACCOUNT, new Uid("TESTAGR"), updateAttributes, oob.build());
        Assert.assertEquals("TESTAGR", uid.getUidValue());
        ConnectorObject co = connectorFacade.getObject(ObjectClass.ACCOUNT, uid, oob.build());
        Assert.assertNull(co.getAttributeByName("ACTIVITYGROUPS"));
    }


    @Test(enabled = true, dependsOnGroups = {"create"}, groups = {"update"})
    public void testUpdateSAPR3UserDelProfiles() {
        java.util.Set<Attribute> updateAttributes = new java.util.HashSet<>();
        updateAttributes.add(AttributeBuilder.build("PROFILES", new ArrayList<Map>()));
        
        OperationOptionsBuilder oob = new OperationOptionsBuilder();
        Uid uid = connectorFacade.update(ObjectClass.ACCOUNT, new Uid("TESTPROF"), updateAttributes, oob.build());
        Assert.assertEquals("TESTPROF", uid.getUidValue());
        ConnectorObject co = connectorFacade.getObject(ObjectClass.ACCOUNT, uid, oob.build());
        Assert.assertNull(co.getAttributeByName("PROFILES"));
    }

    
    @Test(enabled = true, dependsOnGroups = {"create"}, groups = {"update"})
    public void testUpdateSAPR3User() {
        Map<String,String> logonData = new HashMap<>();
        Map<String,String> address = new HashMap<>();
        Map<String,String> company = new HashMap<>();
        Map<String,String> alias = new HashMap<>();
        logonData.put("GLTGB","2016-11-20");
        address.put("TEL1_NUMBR","33297603100");
        address.put("E_MAIL","john.doe@example1.com");
        company.put("COMPANY", "SAP AG");
        alias.put("USERALIAS", "JDOE2");
        java.util.Set<Attribute> updateAttributes = new java.util.HashSet<>();
        updateAttributes.add(AttributeBuilder.build("LOGONDATA", logonData));
        updateAttributes.add(AttributeBuilder.build("ADDRESS", address));
        updateAttributes.add(AttributeBuilder.build("COMPANY", company));
        updateAttributes.add(AttributeBuilder.build("ALIAS", alias));
        updateAttributes.add(AttributeBuilder.buildPassword(new GuardedString("Passw0rd1".toCharArray())));
        OperationOptionsBuilder oob = new OperationOptionsBuilder();
        Uid uid = connectorFacade.update(ObjectClass.ACCOUNT, new Uid("REGULAR"),updateAttributes, oob.build());
        Assert.assertEquals("REGULAR", uid.getUidValue());
        ConnectorObject co = connectorFacade.getObject(ObjectClass.ACCOUNT, uid, oob.build());
        Assert.assertEquals("2016-11-20",((Map)co.getAttributeByName("LOGONDATA").getValue().get(0)).get("GLTGB"));
        Assert.assertEquals("33297603100",((Map)co.getAttributeByName("ADDRESS").getValue().get(0)).get("TEL1_NUMBR"));
        Assert.assertEquals("john.doe@example1.com",((Map)co.getAttributeByName("ADDRESS").getValue().get(0)).get("E_MAIL"));
        Assert.assertEquals("SAP AG",((Map)co.getAttributeByName("COMPANY").getValue().get(0)).get("COMPANY"));
        Assert.assertEquals("JDOE2",((Map)co.getAttributeByName("ALIAS").getValue().get(0)).get("USERALIAS"));
    }
        
    @Test(enabled = true, groups = {"update"}, dependsOnGroups = {"create"})
    public void testUpdatePasswordSAPR3UserPasswordActive() {
        java.util.Set<Attribute> updateAttributes = new java.util.HashSet<>();
        updateAttributes.add(AttributeBuilder.buildPassword(new GuardedString("Passw0rd1".toCharArray())));
        updateAttributes.add(AttributeBuilder.buildPasswordExpired(false));
        OperationOptionsBuilder oob = new OperationOptionsBuilder();
        Uid uid = connectorFacade.update(ObjectClass.ACCOUNT, new Uid("ACTIV"), updateAttributes, oob.build());
        Assert.assertEquals("ACTIV", uid.getUidValue());
    }
    
    //////////////   LOCK/UNLOCK
    
    @Test(enabled = true, dependsOnGroups = {"update"}, groups = {"lock"})
    public void testLockSAPR3User() {
        OperationOptionsBuilder oob = new OperationOptionsBuilder();
        Uid uid = new Uid("REGULAR");
        
        ConnectorObject co = connectorFacade.getObject(ObjectClass.ACCOUNT, uid, oob.build());
        Assert.assertFalse((Boolean)co.getAttributeByName(OperationalAttributes.LOCK_OUT_NAME).getValue().get(0));
        
        java.util.Set<Attribute> updateAttributes = new java.util.HashSet<>();
        updateAttributes.add(AttributeBuilder.buildLockOut(true));
        connectorFacade.update(ObjectClass.ACCOUNT, uid, updateAttributes, oob.build());
        
        co = connectorFacade.getObject(ObjectClass.ACCOUNT, uid, oob.build());
        Assert.assertTrue((Boolean)co.getAttributeByName(OperationalAttributes.LOCK_OUT_NAME).getValue().get(0));
    }
    
    @Test(enabled = true, dependsOnGroups = {"create"}, groups = {"lock"})
    public void testUnlockSAPR3User() {
        OperationOptionsBuilder oob = new OperationOptionsBuilder();
        Uid uid = new Uid("LOCKED");
        
        ConnectorObject co = connectorFacade.getObject(ObjectClass.ACCOUNT, uid, oob.build());
        Assert.assertTrue((Boolean)co.getAttributeByName(OperationalAttributes.LOCK_OUT_NAME).getValue().get(0));
        
        java.util.Set<Attribute> updateAttributes = new java.util.HashSet<>();
        updateAttributes.add(AttributeBuilder.buildLockOut(false));
        connectorFacade.update(ObjectClass.ACCOUNT, uid, updateAttributes, oob.build());
        
        co = connectorFacade.getObject(ObjectClass.ACCOUNT, uid, oob.build());
        Assert.assertFalse((Boolean)co.getAttributeByName(OperationalAttributes.LOCK_OUT_NAME).getValue().get(0));
    }
    
    //////////////  DELETE
    
    @Test(enabled = true, groups = {"delete"})
    public void testDeleteSAPR3User() {
        String[] ids = new String[]{"PWDEXP","ACTIV","LOCKED","REGULAR","TESTAGR","TESTPROF"};
        OperationOptions oo = new OperationOptionsBuilder().build();
        for(String id: ids) {
            Uid uid = new Uid(id);
            try {
                if (connectorFacade.getObject(ObjectClass.ACCOUNT, uid, oo) != null) {
                    connectorFacade.delete(ObjectClass.ACCOUNT, uid, oo);
                }
            } catch (Exception e) {
                LOGGER.info(e.getMessage());
            }
        }
    }

    protected ConnectorFacade getFacade(SapConfiguration config) {
        ConnectorFacadeFactory factory = ConnectorFacadeFactory.getInstance();
        // **test only**
        APIConfiguration impl = TestHelpers.createTestConfiguration(SapConnector.class, config);
        return factory.newInstance(impl);
    }

    protected ConnectorFacade getFacade(Class<? extends Connector> clazz, String environment) {
        if (null == connectorFacade) {
            synchronized (this) {
                if (null == connectorFacade) {
                    connectorFacade = createConnectorFacade(clazz, environment);
                }
            }
        }
        return connectorFacade;
    }

    
    public ConnectorFacade createConnectorFacade(Class<? extends Connector> clazz,
        String environment) {
        PropertyBag propertyBag = TestHelpers.getProperties(clazz, environment);
        
        if (propertyBag.getStringProperty("configuration.user").equalsIgnoreCase("__configureme__")){
            throw new SkipException("SAP Sample tests are skipped. Create private configuration!");
        }

        APIConfiguration impl =
            TestHelpers.createTestConfiguration(clazz, propertyBag, "configuration");
        impl.setProducerBufferSize(0);
        impl.getResultsHandlerConfiguration().setEnableAttributesToGetSearchResultsHandler(false);
        impl.getResultsHandlerConfiguration().setEnableCaseInsensitiveFilter(false);
        impl.getResultsHandlerConfiguration().setEnableFilteredResultsHandler(false);
        impl.getResultsHandlerConfiguration().setEnableNormalizingResultsHandler(false);

        //impl.setTimeout(CreateApiOp.class, 25000);
        //impl.setTimeout(UpdateApiOp.class, 25000);
        //impl.setTimeout(DeleteApiOp.class, 25000);

        return ConnectorFacadeFactory.getInstance().newInstance(impl);
    }
}