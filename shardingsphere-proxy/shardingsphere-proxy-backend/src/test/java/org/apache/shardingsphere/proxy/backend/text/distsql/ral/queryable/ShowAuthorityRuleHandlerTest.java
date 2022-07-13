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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.queryable;

import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowAuthorityRuleStatement;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShowAuthorityRuleHandlerTest extends ProxyContextRestorer {
    
    @Test
    public void assertAuthorityRule() throws SQLException {
        ShowAuthorityRuleHandler handler = new ShowAuthorityRuleHandler();
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(createGlobalRuleMetaData());
        ProxyContext.init(contextManager);
        handler.init(new ShowAuthorityRuleStatement(), null);
        handler.execute();
        handler.next();
        List<Object> data = handler.getRowData().getData();
        assertThat(data.size(), is(3));
        assertThat(data.get(0), is("root@localhost"));
        assertThat(data.get(1), is("ALL_PERMITTED"));
        assertThat(data.get(2), is(""));
    }
    
    private ShardingSphereRuleMetaData createGlobalRuleMetaData() {
        ShardingSphereUser root = new ShardingSphereUser("root", "", "localhost");
        AuthorityRuleConfiguration ruleConfig = new AuthorityRuleConfiguration(Collections.singleton(root), new ShardingSphereAlgorithmConfiguration("ALL_PERMITTED", new Properties()));
        return new ShardingSphereRuleMetaData(Collections.singleton(new AuthorityRule(ruleConfig, Collections.emptyMap())));
    }
}
