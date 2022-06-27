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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;
import java.util.stream.Collectors;
import org.apache.shardingsphere.distsql.parser.segment.TransactionProviderSegment;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.updatable.AlterTransactionRuleStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContext;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.transaction.ShardingSphereTransactionManagerEngine;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AlterTransactionRuleHandlerTest extends ProxyContextRestorer {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;

    @Before
    public void before() {
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class, RETURNS_DEEP_STUBS), createMetaData(), mock(OptimizerContext.class, RETURNS_DEEP_STUBS));
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
    }

    private ShardingSphereMetaData createMetaData() {
        ShardingSphereRuleMetaData ruleMetaData = mock(ShardingSphereRuleMetaData.class);
        when(ruleMetaData.getRules()).thenReturn(new LinkedList<>(Collections.singleton(createTransactionRule())));
        return new ShardingSphereMetaData(Collections.singletonMap("foo_db", mockDatabase()), ruleMetaData, new ConfigurationProperties(new Properties()));
    }

    private TransactionRule createTransactionRule() {
        TransactionRule result = new TransactionRule(new TransactionRuleConfiguration("LOCAL", null, new Properties()), Collections.emptyMap());
        result.setInstanceContext(mock(InstanceContext.class));
        result.getResources().put(DefaultDatabase.LOGIC_NAME, new ShardingSphereTransactionManagerEngine());
        return result;
    }

    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getResource().getDataSources()).thenReturn(Collections.singletonMap("foo_ds", mock(AtomikosDataSourceBean.class, RETURNS_DEEP_STUBS)));
        return result;
    }

    @Test
    public void assertUpdate() {
        AlterTransactionRuleHandler handler = new AlterTransactionRuleHandler();
        handler.init(new AlterTransactionRuleStatement("LOCAL", new TransactionProviderSegment(null, new Properties())), mock(ConnectionSession.class, RETURNS_DEEP_STUBS));
        handler.update(contextManager);
        Collection<ShardingSphereRule> collection = contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getRules();
        Collection<TransactionRule> transactionRules = collection.stream().map(item -> (TransactionRule) item).collect(Collectors.toList());
        assertNotNull(transactionRules);
        assertTrue(!transactionRules.isEmpty());
        assertTrue(transactionRules.stream().anyMatch(item -> item.getConfiguration().getDefaultType().equals("LOCAL")));
        assertTrue(transactionRules.stream().anyMatch(item -> item.getDatabases().containsKey("foo_db")));

    }
}
