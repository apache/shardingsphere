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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import org.apache.shardingsphere.distsql.parser.segment.TransactionProviderSegment;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.AlterTransactionRuleStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AlterTransactionRuleHandlerTest extends ProxyContextRestorer {
    
    @Mock
    private ContextManager contextManager;
    
    @Before
    public void before() {
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class, RETURNS_DEEP_STUBS), createMetaData());
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
    }
    
    private ShardingSphereMetaData createMetaData() {
        Map<String, ShardingSphereDatabase> databases = new HashMap<>(Collections.singletonMap("foo_db", mockDatabase()));
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(new LinkedList<>(Collections.singleton(createTransactionRule(databases))));
        return new ShardingSphereMetaData(databases, ruleMetaData, new ConfigurationProperties(new Properties()));
    }
    
    private TransactionRule createTransactionRule(final Map<String, ShardingSphereDatabase> databases) {
        return new TransactionRule(new TransactionRuleConfiguration("LOCAL", null, new Properties()), databases, mock(InstanceContext.class));
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getResource().getDataSources()).thenReturn(Collections.singletonMap("foo_ds", mock(AtomikosDataSourceBean.class, RETURNS_DEEP_STUBS)));
        return result;
    }
    
    @Test
    public void assertUpdate() {
        AlterTransactionRuleHandler handler = new AlterTransactionRuleHandler();
        handler.init(new AlterTransactionRuleStatement("BASE", new TransactionProviderSegment(null, new Properties())), mock(ConnectionSession.class, RETURNS_DEEP_STUBS));
        handler.update(contextManager);
        TransactionRule updatedRule = contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(TransactionRule.class);
        assertThat(updatedRule.getDefaultType(), is(TransactionType.BASE));
        assertTrue(updatedRule.getDatabases().containsKey("foo_db"));
    }
}
