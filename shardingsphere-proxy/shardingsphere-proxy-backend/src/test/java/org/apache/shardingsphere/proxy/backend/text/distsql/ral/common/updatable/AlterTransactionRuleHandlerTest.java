/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.updatable;

import org.apache.shardingsphere.distsql.parser.statement.ral.RALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.updatable.AlterTransactionRuleStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.impl.GlobalRulePersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockitoSession;
import static org.mockito.Mockito.when;

public class AlterTransactionRuleHandlerTest {


    @Before
    public void before() throws Exception {
        GlobalRulePersistService globalRulePersistService =
                mock(GlobalRulePersistService.class,RETURNS_DEEP_STUBS);
        MetaDataPersistService metaDataPersistService =
                mock(MetaDataPersistService.class,RETURNS_DEEP_STUBS);
        when(metaDataPersistService.getGlobalRuleService()).thenReturn(globalRulePersistService);

        ShardingSphereRuleMetaData shardingSphereRuleMetaData =
                mock(ShardingSphereRuleMetaData.class,RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(
                metaDataPersistService,
                new ShardingSphereMetaData(
                        getDatabases()
                        ,shardingSphereRuleMetaData
                        , new ConfigurationProperties(
                                new Properties()))
                , mock(OptimizerContext.class, RETURNS_DEEP_STUBS)
        );
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);

        ProxyContext.init(contextManager);
    }
    
    @After
    public void after() throws Exception {
    }
    private Map<String, ShardingSphereDatabase> getDatabases() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getResource().getDatabaseType()).thenReturn(new H2DatabaseType());
        when(result.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        return Collections.singletonMap("db", result);
    }
    /**
     * Method: update(final ContextManager contextManager, final AlterTransactionRuleStatement sqlStatement)
     */
    @Test
    public void testUpdate() throws Exception {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        AlterTransactionRuleStatement statement =
                mock(AlterTransactionRuleStatement.class, RETURNS_DEEP_STUBS);

        AlterTransactionRuleHandler alterTransactionRuleHandler =
                new AlterTransactionRuleHandler();
        AlterTransactionRuleStatement ralStatement = mock(AlterTransactionRuleStatement.class,RETURNS_DEEP_STUBS);

        when(ralStatement.getDefaultType()).thenReturn("LOCAL");


        ConnectionSession connectionSession =mock(ConnectionSession.class,
                RETURNS_DEEP_STUBS);

        alterTransactionRuleHandler.init(ralStatement,connectionSession);

        alterTransactionRuleHandler.update(contextManager);

    }
    
    /**
     * Method: getStatement()
     */
    @Test
    public void testGetStatement() throws Exception {
        // TODO: Test goes here...
    }
    
    /**
     * Method: getDatabaseType()
     */
    @Test
    public void testGetDatabaseType() throws Exception {
        // TODO: Test goes here...
    }
    
    /**
     * Method: getConnectionSession()
     */
    @Test
    public void testGetConnectionSession() throws Exception {
        // TODO: Test goes here...
    }
    
    /**
     * Method: buildTransactionRuleConfiguration()
     */
    @Test
    public void testBuildTransactionRuleConfiguration() throws Exception {
        // TODO: Test goes here...
        /*
         * try { Method method = AlterTransactionRuleHandler.getClass().getMethod("buildTransactionRuleConfiguration"); method.setAccessible(true); method.invoke(<Object>, <Parameters>); }
         * catch(NoSuchMethodException e) { } catch(IllegalAccessException e) { } catch(InvocationTargetException e) { }
         */
    }
    
}
