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

import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.FromDatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.ShowFilterSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.ShowLikeSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.table.MySQLShowTablesStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MySQLShowTablesExecutorTest {
    
    private static final String DATABASE_PATTERN = "db_%s";
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    @Test
    void assertShowTablesExecutorWithoutFilter() throws SQLException {
        MySQLShowTablesExecutor executor = new MySQLShowTablesExecutor(new MySQLShowTablesStatement(databaseType, null, null, false));
        executor.execute(mockConnectionSession(), mockMetaData(mockDatabases()));
        assertThat(executor.getQueryResultMetaData().getColumnCount(), is(1));
        executor.getMergedResult().next();
        assertThat(executor.getMergedResult().getValue(1, Object.class), is("T_TEST"));
        executor.getMergedResult().next();
        assertThat(executor.getMergedResult().getValue(1, Object.class), is("t_account"));
        executor.getMergedResult().next();
        assertThat(executor.getMergedResult().getValue(1, Object.class), is("t_account_bak"));
        executor.getMergedResult().next();
        assertThat(executor.getMergedResult().getValue(1, Object.class), is("t_account_detail"));
        assertFalse(executor.getMergedResult().next());
    }
    
    @Test
    void assertShowTablesExecutorWithFull() throws SQLException {
        MySQLShowTablesStatement sqlStatement = new MySQLShowTablesStatement(databaseType, null, mock(), true);
        MySQLShowTablesExecutor executor = new MySQLShowTablesExecutor(sqlStatement);
        executor.execute(mockConnectionSession(), mockMetaData(mockDatabases()));
        assertThat(executor.getQueryResultMetaData().getColumnCount(), is(2));
    }
    
    @Test
    void assertShowTablesExecutorWithLikeFilter() throws SQLException {
        ShowFilterSegment showFilterSegment = mock(ShowFilterSegment.class);
        when(showFilterSegment.getLike()).thenReturn(Optional.of(new ShowLikeSegment(0, 10, "t_account%")));
        MySQLShowTablesStatement sqlStatement = new MySQLShowTablesStatement(databaseType, null, showFilterSegment, false);
        MySQLShowTablesExecutor executor = new MySQLShowTablesExecutor(sqlStatement);
        executor.execute(mockConnectionSession(), mockMetaData(mockDatabases()));
        assertThat(executor.getQueryResultMetaData().getColumnCount(), is(1));
        executor.getMergedResult().next();
        assertThat(executor.getMergedResult().getValue(1, Object.class), is("t_account"));
        executor.getMergedResult().next();
        assertThat(executor.getMergedResult().getValue(1, Object.class), is("t_account_bak"));
        executor.getMergedResult().next();
        assertThat(executor.getMergedResult().getValue(1, Object.class), is("t_account_detail"));
        assertFalse(executor.getMergedResult().next());
    }
    
    @Test
    void assertShowTablesExecutorWithSpecificTable() throws SQLException {
        ShowFilterSegment showFilterSegment = mock(ShowFilterSegment.class);
        when(showFilterSegment.getLike()).thenReturn(Optional.of(new ShowLikeSegment(0, 10, "t_account")));
        MySQLShowTablesStatement sqlStatement = new MySQLShowTablesStatement(databaseType, null, showFilterSegment, false);
        MySQLShowTablesExecutor executor = new MySQLShowTablesExecutor(sqlStatement);
        executor.execute(mockConnectionSession(), mockMetaData(mockDatabases()));
        assertThat(executor.getQueryResultMetaData().getColumnCount(), is(1));
        executor.getMergedResult().next();
        assertThat(executor.getMergedResult().getValue(1, Object.class), is("t_account"));
        assertFalse(executor.getMergedResult().next());
    }
    
    @Test
    void assertShowTablesExecutorWithUpperCase() throws SQLException {
        ShowFilterSegment showFilterSegment = mock(ShowFilterSegment.class);
        when(showFilterSegment.getLike()).thenReturn(Optional.of(new ShowLikeSegment(0, 10, "T_TEST")));
        MySQLShowTablesStatement sqlStatement = new MySQLShowTablesStatement(databaseType, null, showFilterSegment, false);
        MySQLShowTablesExecutor executor = new MySQLShowTablesExecutor(sqlStatement);
        executor.execute(mockConnectionSession(), mockMetaData(mockDatabases()));
        assertThat(executor.getQueryResultMetaData().getColumnCount(), is(1));
        executor.getMergedResult().next();
        assertThat(executor.getMergedResult().getValue(1, Object.class), is("T_TEST"));
        assertFalse(executor.getMergedResult().next());
    }
    
    @Test
    void assertShowTablesExecutorWithLowerCase() throws SQLException {
        ShowFilterSegment showFilterSegment = mock(ShowFilterSegment.class);
        when(showFilterSegment.getLike()).thenReturn(Optional.of(new ShowLikeSegment(0, 10, "t_test")));
        MySQLShowTablesStatement sqlStatement = new MySQLShowTablesStatement(databaseType, null, showFilterSegment, false);
        MySQLShowTablesExecutor executor = new MySQLShowTablesExecutor(sqlStatement);
        executor.execute(mockConnectionSession(), mockMetaData(mockDatabases()));
        assertThat(executor.getQueryResultMetaData().getColumnCount(), is(1));
        executor.getMergedResult().next();
        assertThat(executor.getMergedResult().getValue(1, Object.class), is("T_TEST"));
        assertFalse(executor.getMergedResult().next());
    }
    
    @Test
    void assertShowTableFromUncompletedDatabase() throws SQLException {
        MySQLShowTablesStatement sqlStatement = new MySQLShowTablesStatement(databaseType, new FromDatabaseSegment(0, new DatabaseSegment(0, 0, new IdentifierValue("uncompleted"))), null, false);
        MySQLShowTablesExecutor executor = new MySQLShowTablesExecutor(sqlStatement);
        executor.execute(mockConnectionSession(), mockMetaData(mockDatabases()));
        QueryResultMetaData actualMetaData = executor.getQueryResultMetaData();
        assertThat(actualMetaData.getColumnCount(), is(1));
        assertThat(actualMetaData.getColumnName(1), is("Tables_in_uncompleted"));
        MergedResult actualResult = executor.getMergedResult();
        assertFalse(actualResult.next());
    }
    
    private static ShardingSphereMetaData mockMetaData(final Collection<ShardingSphereDatabase> databases) {
        return new ShardingSphereMetaData(databases, mock(ResourceMetaData.class), new RuleMetaData(Collections.singleton(mock(AuthorityRule.class))), new ConfigurationProperties(new Properties()));
    }
    
    private Collection<ShardingSphereDatabase> mockDatabases() {
        Collection<ShardingSphereTable> tables = new LinkedList<>();
        tables.add(new ShardingSphereTable("t_account", Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
        tables.add(new ShardingSphereTable("t_account_bak", Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
        tables.add(new ShardingSphereTable("t_account_detail", Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
        tables.add(new ShardingSphereTable("T_TEST", Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_db", tables, Collections.emptyList());
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn(String.format(DATABASE_PATTERN, 0));
        when(database.getProtocolType()).thenReturn(databaseType);
        when(database.isComplete()).thenReturn(true);
        when(database.getSchema(String.format(DATABASE_PATTERN, 0))).thenReturn(schema);
        return Arrays.asList(database, new ShardingSphereDatabase("uncompleted", mock(), mock(), mock(), Collections.emptyList()));
    }
    
    private ConnectionSession mockConnectionSession() {
        ConnectionSession result = mock(ConnectionSession.class, RETURNS_DEEP_STUBS);
        when(result.getUsedDatabaseName()).thenReturn(String.format(DATABASE_PATTERN, 0));
        return result;
    }
}
