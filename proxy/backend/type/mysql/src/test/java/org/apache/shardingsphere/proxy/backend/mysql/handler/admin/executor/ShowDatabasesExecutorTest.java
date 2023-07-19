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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor;

import org.apache.shardingsphere.authority.provider.simple.model.privilege.AllPrivilegesPermittedShardingSpherePrivileges;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.ShowFilterSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.ShowLikeSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowDatabasesStatement;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ShowDatabasesExecutorTest {
    
    private static final String DATABASE_PATTERN = "database_%s";
    
    @Test
    void assertExecute() throws SQLException {
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().getAllDatabaseNames()).thenReturn(IntStream.range(0, 10).mapToObj(each -> String.format("database_%s", each)).collect(Collectors.toList()));
        ShowDatabasesExecutor executor = new ShowDatabasesExecutor(new MySQLShowDatabasesStatement());
        executor.execute(mockConnectionSession());
        assertThat(executor.getQueryResultMetaData().getColumnCount(), is(1));
        assertThat(getActual(executor), is(getExpected()));
    }
    
    @Test
    void assertExecuteWithPrefixLike() throws SQLException {
        MySQLShowDatabasesStatement showDatabasesStatement = new MySQLShowDatabasesStatement();
        ShowFilterSegment showFilterSegment = new ShowFilterSegment(0, 0);
        ShowLikeSegment showLikeSegment = new ShowLikeSegment(0, 0, "database%");
        showFilterSegment.setLike(showLikeSegment);
        showDatabasesStatement.setFilter(showFilterSegment);
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().getAllDatabaseNames()).thenReturn(IntStream.range(0, 10).mapToObj(each -> String.format("database_%s", each)).collect(Collectors.toList()));
        ShowDatabasesExecutor executor = new ShowDatabasesExecutor(showDatabasesStatement);
        executor.execute(mockConnectionSession());
        assertThat(getActual(executor), is(getExpected()));
    }
    
    private Collection<String> getActual(final ShowDatabasesExecutor executor) throws SQLException {
        Map<String, String> result = new ConcurrentHashMap<>(10, 1F);
        while (executor.getMergedResult().next()) {
            String value = executor.getMergedResult().getValue(1, Object.class).toString();
            result.put(value, value);
        }
        return result.keySet();
    }
    
    private Collection<String> getExpected() {
        Map<String, String> result = new ConcurrentHashMap<>(10, 1F);
        for (int i = 0; i < 10; i++) {
            String value = String.format(DATABASE_PATTERN, i);
            result.put(value, value);
        }
        return result.keySet();
    }
    
    @Test
    void assertExecuteWithSuffixLike() throws SQLException {
        MySQLShowDatabasesStatement showDatabasesStatement = new MySQLShowDatabasesStatement();
        ShowFilterSegment showFilterSegment = new ShowFilterSegment(0, 0);
        ShowLikeSegment showLikeSegment = new ShowLikeSegment(0, 0, "%_1");
        showFilterSegment.setLike(showLikeSegment);
        showDatabasesStatement.setFilter(showFilterSegment);
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().getAllDatabaseNames()).thenReturn(IntStream.range(0, 10).mapToObj(each -> String.format("database_%s", each)).collect(Collectors.toList()));
        ShowDatabasesExecutor executor = new ShowDatabasesExecutor(showDatabasesStatement);
        executor.execute(mockConnectionSession());
        assertThat(executor.getQueryResultMetaData().getColumnCount(), is(1));
        int count = 0;
        while (executor.getMergedResult().next()) {
            assertThat(executor.getMergedResult().getValue(1, Object.class), is("database_1"));
            count++;
        }
        assertThat(count, is(1));
    }
    
    @Test
    void assertExecuteWithPreciseLike() throws SQLException {
        MySQLShowDatabasesStatement showDatabasesStatement = new MySQLShowDatabasesStatement();
        ShowFilterSegment showFilterSegment = new ShowFilterSegment(0, 0);
        ShowLikeSegment showLikeSegment = new ShowLikeSegment(0, 0, "database_9");
        showFilterSegment.setLike(showLikeSegment);
        showDatabasesStatement.setFilter(showFilterSegment);
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().getAllDatabaseNames()).thenReturn(IntStream.range(0, 10).mapToObj(each -> String.format("database_%s", each)).collect(Collectors.toList()));
        ShowDatabasesExecutor executor = new ShowDatabasesExecutor(showDatabasesStatement);
        executor.execute(mockConnectionSession());
        assertThat(executor.getQueryResultMetaData().getColumnCount(), is(1));
        int count = 0;
        while (executor.getMergedResult().next()) {
            assertThat(executor.getMergedResult().getValue(1, Object.class), is("database_9"));
            count++;
        }
        assertThat(count, is(1));
    }
    
    @Test
    void assertExecuteWithLikeMatchNone() throws SQLException {
        MySQLShowDatabasesStatement showDatabasesStatement = new MySQLShowDatabasesStatement();
        ShowFilterSegment showFilterSegment = new ShowFilterSegment(0, 0);
        ShowLikeSegment showLikeSegment = new ShowLikeSegment(0, 0, "not_exist_database");
        showFilterSegment.setLike(showLikeSegment);
        showDatabasesStatement.setFilter(showFilterSegment);
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().getAllDatabaseNames()).thenReturn(IntStream.range(0, 10).mapToObj(each -> String.format("database_%s", each)).collect(Collectors.toList()));
        ShowDatabasesExecutor executor = new ShowDatabasesExecutor(showDatabasesStatement);
        executor.execute(mockConnectionSession());
        assertThat(executor.getQueryResultMetaData().getColumnCount(), is(1));
        int count = 0;
        while (executor.getMergedResult().next()) {
            assertThat(executor.getMergedResult().getValue(1, Object.class), is("not_exist_database"));
            count++;
        }
        assertThat(count, is(0));
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereRuleMetaData globalRuleMetaData = new ShardingSphereRuleMetaData(Collections.singleton(mockAuthorityRule()));
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class), new ShardingSphereMetaData(getDatabases(),
                mock(ShardingSphereResourceMetaData.class), globalRuleMetaData, new ConfigurationProperties(new Properties())));
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        return result;
    }
    
    private AuthorityRule mockAuthorityRule() {
        AuthorityRule result = mock(AuthorityRule.class);
        when(result.findPrivileges(new Grantee("root", ""))).thenReturn(Optional.of(new AllPrivilegesPermittedShardingSpherePrivileges()));
        return result;
    }
    
    private Map<String, ShardingSphereDatabase> getDatabases() {
        Map<String, ShardingSphereDatabase> result = new LinkedHashMap<>(10, 1F);
        for (int i = 0; i < 10; i++) {
            ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
            when(database.getProtocolType()).thenReturn(new MySQLDatabaseType());
            when(database.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
            when(database.getName()).thenReturn(String.format(DATABASE_PATTERN, i));
            result.put(String.format(DATABASE_PATTERN, i), database);
        }
        return result;
    }
    
    private ConnectionSession mockConnectionSession() {
        ConnectionSession result = mock(ConnectionSession.class);
        when(result.getGrantee()).thenReturn(new Grantee("root", ""));
        return result;
    }
}
