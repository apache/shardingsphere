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

import com.atomikos.jdbc.AtomikosDataSourceBean;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.updatable.AlterTransactionRuleStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContext;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.impl.GlobalRulePersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.transaction.ShardingSphereTransactionManagerEngine;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class AlterTransactionRuleHandlerTest {
    private ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);

    @Before
    public void before() {
        GlobalRulePersistService globalRulePersistService = mock(GlobalRulePersistService.class, RETURNS_DEEP_STUBS);
        MetaDataPersistService metaDataPersistService = mock(MetaDataPersistService.class, RETURNS_DEEP_STUBS);
        when(metaDataPersistService.getGlobalRuleService()).thenReturn(globalRulePersistService);
        when(contextManager.getInstanceContext()).thenReturn(mock(InstanceContext.class, RETURNS_DEEP_STUBS));
        ShardingSphereRuleMetaData shardingSphereRuleMetaData = mock(ShardingSphereRuleMetaData.class, RETURNS_DEEP_STUBS);
        List<ShardingSphereRule> mockRules = new ArrayList<>();
        mockRules.add(getLocalTransactionRule());
        when(shardingSphereRuleMetaData.getRules()).thenReturn(mockRules);
        MetaDataContexts metaDataContexts = new MetaDataContexts(
                metaDataPersistService,
                new ShardingSphereMetaData(
                        getDatabases(),
                        shardingSphereRuleMetaData,
                        new ConfigurationProperties(new Properties())),
                        mock(OptimizerContext.class, RETURNS_DEEP_STUBS)
        );
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
    }

    @After
    public void after() {
    }

    private TransactionRule getLocalTransactionRule() {
        TransactionRule result = new TransactionRule(
                new TransactionRuleConfiguration(
                        "LOCAL",
                        null,
                        new Properties()),
                Collections.emptyMap());
        result.setInstanceContext(mock(InstanceContext.class));
        result.getResources().put(DefaultDatabase.LOGIC_NAME,
                new ShardingSphereTransactionManagerEngine());
        return result;
    }

    private Map<String, ShardingSphereDatabase> getDatabases() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getResource().getDatabaseType()).thenReturn(new H2DatabaseType());
        when(result.getRuleMetaData().getRules())
                .thenReturn(Collections.emptyList());
        when(result.getResource().getDataSources())
                .thenReturn(Collections.singletonMap(DefaultDatabase.LOGIC_NAME,
                        mock(AtomikosDataSourceBean.class, RETURNS_DEEP_STUBS)));
        return Collections.singletonMap("db", result);
    }

    @Test
    public void assertUpdate() {
        AlterTransactionRuleHandler alterTransactionRuleHandler = new AlterTransactionRuleHandler();
        AlterTransactionRuleStatement ralStatement = mock(AlterTransactionRuleStatement.class, RETURNS_DEEP_STUBS);
        when(ralStatement.getDefaultType()).thenReturn("LOCAL");
        ConnectionSession connectionSession = mock(ConnectionSession.class, RETURNS_DEEP_STUBS);
        alterTransactionRuleHandler.init(ralStatement, connectionSession);
        alterTransactionRuleHandler.update(contextManager);
    }

    @Test
    public void assertGetStatement() {
        AlterTransactionRuleHandler alterTransactionRuleHandler = new AlterTransactionRuleHandler();
        AlterTransactionRuleStatement ralStatement = mock(AlterTransactionRuleStatement.class, RETURNS_DEEP_STUBS);
        when(ralStatement.getDefaultType()).thenReturn("LOCAL");
        ConnectionSession connectionSession = mock(ConnectionSession.class, RETURNS_DEEP_STUBS);
        alterTransactionRuleHandler.init(ralStatement, connectionSession);
        AlterTransactionRuleStatement testAlterStatement = alterTransactionRuleHandler.getSqlStatement();
        assertNotNull(testAlterStatement);
    }

    @Test
    public void assertExecute() throws Exception {
        AlterTransactionRuleHandler alterTransactionRuleHandler = new AlterTransactionRuleHandler();
        AlterTransactionRuleStatement ralStatement = mock(AlterTransactionRuleStatement.class, RETURNS_DEEP_STUBS);
        when(ralStatement.getDefaultType()).thenReturn("LOCAL");
        ConnectionSession connectionSession = mock(ConnectionSession.class, RETURNS_DEEP_STUBS);
        alterTransactionRuleHandler.init(ralStatement, connectionSession);
        ResponseHeader testRespHeader = alterTransactionRuleHandler.execute();
        assertNotNull(testRespHeader);
    }

    @Test
    public void assertGetConnectionSession() {
        AlterTransactionRuleHandler alterTransactionRuleHandler = new AlterTransactionRuleHandler();
        AlterTransactionRuleStatement ralStatement = mock(AlterTransactionRuleStatement.class, RETURNS_DEEP_STUBS);
        when(ralStatement.getDefaultType()).thenReturn("LOCAL");
        ConnectionSession connectionSession = mock(ConnectionSession.class, RETURNS_DEEP_STUBS);
        alterTransactionRuleHandler.init(ralStatement, connectionSession);
        ConnectionSession session = alterTransactionRuleHandler.getConnectionSession();
        assertNotNull(session);
    }
}
