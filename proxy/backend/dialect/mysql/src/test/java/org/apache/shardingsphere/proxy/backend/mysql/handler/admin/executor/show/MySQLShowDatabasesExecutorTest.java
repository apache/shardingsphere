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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.show;

import org.apache.shardingsphere.authority.model.ShardingSpherePrivileges;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.ShowFilterSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.ShowLikeSegment;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.database.MySQLShowDatabasesStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MySQLShowDatabasesExecutorTest {
    
    private static final String DATABASE_PATTERN = "database_%s";
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    @Test
    void assertExecute() throws SQLException {
        MySQLShowDatabasesExecutor executor = new MySQLShowDatabasesExecutor(new MySQLShowDatabasesStatement(databaseType, null));
        executor.execute(mockConnectionSession(), mockMetaData());
        QueryResultMetaData queryResultMetaData = executor.getQueryResultMetaData();
        assertThat(queryResultMetaData.getColumnCount(), is(1));
        assertThat(queryResultMetaData.getTableName(1), is("SCHEMATA"));
        assertThat(queryResultMetaData.getColumnLabel(1), is("Database"));
        assertThat(queryResultMetaData.getColumnName(1), is("SCHEMA_NAME"));
        assertThat(getActual(executor), is(getExpected()));
    }
    
    @Test
    void assertExecuteWithPrefixLike() throws SQLException {
        ShowFilterSegment showFilterSegment = new ShowFilterSegment(0, 0);
        ShowLikeSegment showLikeSegment = new ShowLikeSegment(0, 0, "database%");
        showFilterSegment.setLike(showLikeSegment);
        MySQLShowDatabasesStatement showDatabasesStatement = new MySQLShowDatabasesStatement(databaseType, showFilterSegment);
        MySQLShowDatabasesExecutor executor = new MySQLShowDatabasesExecutor(showDatabasesStatement);
        executor.execute(mockConnectionSession(), mockMetaData());
        assertThat(getActual(executor), is(getExpected()));
    }
    
    private Collection<String> getActual(final MySQLShowDatabasesExecutor executor) throws SQLException {
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
        ShowFilterSegment showFilterSegment = new ShowFilterSegment(0, 0);
        ShowLikeSegment showLikeSegment = new ShowLikeSegment(0, 0, "%_1");
        showFilterSegment.setLike(showLikeSegment);
        MySQLShowDatabasesStatement showDatabasesStatement = new MySQLShowDatabasesStatement(databaseType, showFilterSegment);
        MySQLShowDatabasesExecutor executor = new MySQLShowDatabasesExecutor(showDatabasesStatement);
        executor.execute(mockConnectionSession(), mockMetaData());
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
        ShowFilterSegment showFilterSegment = new ShowFilterSegment(0, 0);
        ShowLikeSegment showLikeSegment = new ShowLikeSegment(0, 0, "database_9");
        showFilterSegment.setLike(showLikeSegment);
        MySQLShowDatabasesStatement showDatabasesStatement = new MySQLShowDatabasesStatement(databaseType, showFilterSegment);
        MySQLShowDatabasesExecutor executor = new MySQLShowDatabasesExecutor(showDatabasesStatement);
        executor.execute(mockConnectionSession(), mockMetaData());
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
        ShowFilterSegment showFilterSegment = new ShowFilterSegment(0, 0);
        ShowLikeSegment showLikeSegment = new ShowLikeSegment(0, 0, "not_exist_database");
        showFilterSegment.setLike(showLikeSegment);
        MySQLShowDatabasesStatement showDatabasesStatement = new MySQLShowDatabasesStatement(databaseType, showFilterSegment);
        MySQLShowDatabasesExecutor executor = new MySQLShowDatabasesExecutor(showDatabasesStatement);
        executor.execute(mockConnectionSession(), mockMetaData());
        assertThat(executor.getQueryResultMetaData().getColumnCount(), is(1));
        int count = 0;
        while (executor.getMergedResult().next()) {
            assertThat(executor.getMergedResult().getValue(1, Object.class), is("not_exist_database"));
            count++;
        }
        assertThat(count, is(0));
    }
    
    private ShardingSphereMetaData mockMetaData() {
        RuleMetaData globalRuleMetaData = new RuleMetaData(Collections.singleton(mockAuthorityRule()));
        return new ShardingSphereMetaData(createDatabases(), mock(), globalRuleMetaData, mock());
    }
    
    private AuthorityRule mockAuthorityRule() {
        AuthorityRule result = mock(AuthorityRule.class);
        ShardingSpherePrivileges privileges = mockPrivileges();
        when(result.findPrivileges(new Grantee("root"))).thenReturn(Optional.of(privileges));
        return result;
    }
    
    private ShardingSpherePrivileges mockPrivileges() {
        ShardingSpherePrivileges result = mock(ShardingSpherePrivileges.class);
        when(result.hasPrivileges(anyString())).thenReturn(true);
        return result;
    }
    
    private Collection<ShardingSphereDatabase> createDatabases() {
        return IntStream.range(0, 10)
                .mapToObj(each -> new ShardingSphereDatabase(String.format(DATABASE_PATTERN, each), databaseType, mock(), mock(), Collections.emptyList())).collect(Collectors.toList());
    }
    
    private ConnectionSession mockConnectionSession() {
        ConnectionSession result = mock(ConnectionSession.class, RETURNS_DEEP_STUBS);
        when(result.getConnectionContext().getGrantee()).thenReturn(new Grantee("root"));
        return result;
    }
}
