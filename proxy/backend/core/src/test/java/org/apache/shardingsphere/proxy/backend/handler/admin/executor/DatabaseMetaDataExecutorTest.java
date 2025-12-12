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

package org.apache.shardingsphere.proxy.backend.handler.admin.executor;

import org.apache.shardingsphere.authority.provider.database.DatabasePermittedPrivileges;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.util.SystemSchemaUtils;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(SystemSchemaUtils.class)
class DatabaseMetaDataExecutorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private final Grantee grantee = new Grantee("root", "127.0.0.1");
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @BeforeEach
    void setUp() {
        when(connectionSession.getCurrentDatabaseName()).thenReturn("auth_db");
        when(connectionSession.getConnectionContext().getGrantee()).thenReturn(grantee);
    }
    
    @Test
    void assertExecuteWithAlias() throws SQLException {
        Map<String, String> expectedResultSetMap = new HashMap<>(2, 1F);
        expectedResultSetMap.put("sn", "foo_ds");
        expectedResultSetMap.put("DEFAULT_CHARACTER_SET_NAME", "utf8mb4");
        String sql = "SELECT SCHEMA_NAME AS sn, DEFAULT_CHARACTER_SET_NAME FROM information_schema.SCHEMATA";
        ShardingSphereDatabase database = createDatabase(expectedResultSetMap);
        DatabaseMetaDataExecutor executor = new DatabaseMetaDataExecutor(sql, Collections.emptyList());
        executor.execute(connectionSession, mockMetaData(database));
        assertThat(executor.getRows().get(0).get("sn"), is("foo_ds"));
        assertThat(executor.getRows().get(0).get("DEFAULT_CHARACTER_SET_NAME"), is("utf8mb4"));
    }
    
    @Test
    void assertExecuteWithDefaultValue() throws SQLException {
        String sql = "SELECT COUNT(*) AS support_ndb FROM information_schema.ENGINES WHERE Engine = 'ndbcluster'";
        ShardingSphereDatabase database = createDatabase(Collections.singletonMap("support_ndb", "0"));
        DatabaseMetaDataExecutor executor = new DatabaseMetaDataExecutor(sql, Collections.emptyList());
        executor.execute(connectionSession, mockMetaData(database));
        assertThat(executor.getQueryResultMetaData().getColumnCount(), is(1));
        while (executor.getMergedResult().next()) {
            assertThat(executor.getMergedResult().getValue(1, String.class), is("0"));
        }
    }
    
    @Test
    void assertExecuteWithPreparedStatement() throws SQLException {
        String sql = "SELECT COUNT(*) AS support_ndb FROM information_schema.ENGINES WHERE Engine = ?";
        ShardingSphereDatabase database = createDatabase(Collections.singletonMap("support_ndb", "0"));
        DatabaseMetaDataExecutor executor = new DatabaseMetaDataExecutor(sql, Collections.singletonList("ndbcluster"));
        executor.execute(connectionSession, mockMetaData(database));
        assertThat(executor.getQueryResultMetaData().getColumnCount(), is(1));
        while (executor.getMergedResult().next()) {
            assertThat(executor.getMergedResult().getValue(1, String.class), is("0"));
        }
    }
    
    private ShardingSphereDatabase createDatabase(final Map<String, String> expectedResultSetMap) throws SQLException {
        return new ShardingSphereDatabase("auth_db",
                databaseType, new ResourceMetaData(Collections.singletonMap("foo_ds", new MockedDataSource(mockConnection(expectedResultSetMap)))), mock(RuleMetaData.class), Collections.emptyList());
    }
    
    private Connection mockConnection(final Map<String, String> expectedResultSetMap) throws SQLException {
        Connection result = mock(Connection.class, RETURNS_DEEP_STUBS);
        ResultSet resultSet = mockResultSet(expectedResultSetMap);
        when(result.prepareStatement(any(String.class)).executeQuery()).thenReturn(resultSet);
        return result;
    }
    
    private ResultSet mockResultSet(final Map<String, String> expectedResultSetMap) throws SQLException {
        ResultSet result = mock(ResultSet.class, RETURNS_DEEP_STUBS);
        List<String> keys = new ArrayList<>(expectedResultSetMap.keySet());
        for (int i = 0; i < keys.size(); i++) {
            when(result.getMetaData().getColumnName(i + 1)).thenReturn(keys.get(i));
            when(result.getMetaData().getColumnLabel(i + 1)).thenReturn(keys.get(i));
            when(result.getString(i + 1)).thenReturn(expectedResultSetMap.get(keys.get(i)));
        }
        when(result.next()).thenReturn(true, false);
        when(result.getMetaData().getColumnCount()).thenReturn(expectedResultSetMap.size());
        return result;
    }
    
    private ShardingSphereMetaData mockMetaData(final ShardingSphereDatabase database) {
        AuthorityRule authorityRule = mock(AuthorityRule.class);
        when(authorityRule.findPrivileges(grantee)).thenReturn(Optional.of(new DatabasePermittedPrivileges(Collections.singleton("auth_db"))));
        return new ShardingSphereMetaData(
                Collections.singleton(database), mock(ResourceMetaData.class), new RuleMetaData(Collections.singleton(authorityRule)), new ConfigurationProperties(new Properties()));
    }
}
