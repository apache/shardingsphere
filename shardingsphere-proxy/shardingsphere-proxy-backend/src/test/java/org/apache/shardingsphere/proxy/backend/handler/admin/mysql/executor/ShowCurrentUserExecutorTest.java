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

package org.apache.shardingsphere.proxy.backend.handler.admin.mysql.executor;

import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShowCurrentUserExecutorTest extends ProxyContextRestorer {
    
    private static final Grantee GRANTEE = new Grantee("root", "");
    
    @Before
    public void setUp() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(new HashMap<>(), mockShardingSphereRuleMetaData(), new ConfigurationProperties(new Properties())));
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
    }
    
    private ShardingSphereRuleMetaData mockShardingSphereRuleMetaData() {
        AuthorityRule authorityRule = mock(AuthorityRule.class);
        ShardingSphereUser shardingSphereUser = mock(ShardingSphereUser.class);
        when(shardingSphereUser.getGrantee()).thenReturn(new Grantee("root", "%"));
        when(authorityRule.findUser(GRANTEE)).thenReturn(Optional.of(shardingSphereUser));
        return new ShardingSphereRuleMetaData(Collections.singletonList(authorityRule));
    }
    
    @Test
    public void assertExecute() throws SQLException {
        ShowCurrentUserExecutor executor = new ShowCurrentUserExecutor();
        executor.execute(mockConnectionSession());
        assertThat(executor.getQueryResultMetaData().getColumnCount(), is(1));
        while (executor.getMergedResult().next()) {
            assertThat(executor.getMergedResult().getValue(1, Object.class), is("root@%"));
        }
    }
    
    private ConnectionSession mockConnectionSession() {
        ConnectionSession result = mock(ConnectionSession.class);
        when(result.getGrantee()).thenReturn(GRANTEE);
        return result;
    }
}
