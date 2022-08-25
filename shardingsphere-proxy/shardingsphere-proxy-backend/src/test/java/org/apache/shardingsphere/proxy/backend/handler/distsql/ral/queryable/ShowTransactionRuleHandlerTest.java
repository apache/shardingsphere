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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable;

import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowTransactionRuleStatement;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShowTransactionRuleHandlerTest extends ProxyContextRestorer {
    
    @Test
    public void assertExecuteWithXA() throws SQLException {
        ShowTransactionRuleHandler handler = new ShowTransactionRuleHandler();
        handler.init(new ShowTransactionRuleStatement(), null);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereRuleMetaData metaData = createGlobalRuleMetaData("XA", "Atomikos", getProperties());
        when(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(metaData);
        ProxyContext.init(contextManager);
        handler.execute();
        handler.next();
        List<Object> data = handler.getRowData().getData();
        assertThat(data.size(), is(3));
        assertThat(data.get(0), is("XA"));
        assertThat(data.get(1), is("Atomikos"));
        String props = String.valueOf(data.get(2));
        assertThat(props, containsString("\"host\":\"127.0.0.1\""));
        assertThat(props, containsString("\"databaseName\":\"jbossts\""));
    }
    
    @Test
    public void assertExecuteWithLocal() throws SQLException {
        ShowTransactionRuleHandler handler = new ShowTransactionRuleHandler();
        handler.init(new ShowTransactionRuleStatement(), null);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereRuleMetaData metaData = createGlobalRuleMetaData("LOCAL", null, null);
        when(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(metaData);
        ProxyContext.init(contextManager);
        handler.execute();
        handler.next();
        List<Object> data = handler.getRowData().getData();
        assertThat(data.size(), is(3));
        assertThat(data.get(0), is("LOCAL"));
        assertThat(data.get(1), is(""));
        assertThat(data.get(2), is(""));
    }
    
    private ShardingSphereRuleMetaData createGlobalRuleMetaData(final String defaultType, final String providerType, final Properties props) {
        TransactionRule rule = new TransactionRule(new TransactionRuleConfiguration(defaultType, providerType, props), Collections.emptyMap(), mock(InstanceContext.class));
        return new ShardingSphereRuleMetaData(Collections.singleton(rule));
    }
    
    private Properties getProperties() {
        Properties result = new Properties();
        result.setProperty("host", "127.0.0.1");
        result.setProperty("databaseName", "jbossts");
        return result;
    }
}
