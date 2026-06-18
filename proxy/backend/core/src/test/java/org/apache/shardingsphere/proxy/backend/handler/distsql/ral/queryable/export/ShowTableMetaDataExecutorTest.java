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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable.export;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.show.ShowTableMetaDataStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.FromDatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShowTableMetaDataExecutorTest {
    
    @Test
    void assertExecute() {
        ShardingSphereDatabase database = mockDatabase();
        ShowTableMetaDataExecutor executor = new ShowTableMetaDataExecutor();
        executor.setDatabase(database);
        Collection<LocalDataQueryResultRow> actual = executor.getRows(createSqlStatement(), mock(ContextManager.class));
        assertThat(actual.size(), is(2));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("foo_db"));
        assertThat(row.getCell(2), is("t_order"));
        assertThat(row.getCell(3), is("COLUMN"));
        assertThat(row.getCell(4), is("order_id"));
        assertThat(row.getCell(5),
                is("{\"name\":\"order_id\",\"dataType\":0,\"primaryKey\":false,\"generated\":false,\"caseSensitive\":false,\"visible\":true,\"unsigned\":false,\"nullable\":false}"));
        row = iterator.next();
        assertThat(row.getCell(1), is("foo_db"));
        assertThat(row.getCell(2), is("t_order"));
        assertThat(row.getCell(3), is("INDEX"));
        assertThat(row.getCell(4), is("primary"));
        assertThat(row.getCell(5), is("{\"name\":\"primary\",\"columns\":[],\"unique\":false}"));
    }
    
    @Test
    void assertExecuteWithPostgreSQLQuotedUpperCaseTable() {
        ShowTableMetaDataExecutor executor = new ShowTableMetaDataExecutor();
        executor.setDatabase(createDatabase("foo_db", "PostgreSQL", "public", "T_USER_UPPER"));
        Collection<LocalDataQueryResultRow> actual = executor.getRows(new ShowTableMetaDataStatement(Collections.singleton(new IdentifierValue("\"T_USER_UPPER\"")), null), mock(ContextManager.class));
        assertThat(actual.size(), is(2));
        assertThat(actual.iterator().next().getCell(2), is("T_USER_UPPER"));
    }
    
    @Test
    void assertExecuteWithPostgreSQLUnquotedUpperCaseTable() {
        ShowTableMetaDataExecutor executor = new ShowTableMetaDataExecutor();
        executor.setDatabase(createDatabase("foo_db", "PostgreSQL", "public", "T_USER_UPPER"));
        assertTrue(executor.getRows(new ShowTableMetaDataStatement(Collections.singleton(new IdentifierValue("T_USER_UPPER")), null), mock(ContextManager.class)).isEmpty());
    }
    
    @Test
    void assertExecuteWithMySQLUpperCaseTable() {
        ShowTableMetaDataExecutor executor = new ShowTableMetaDataExecutor();
        executor.setDatabase(createDatabase("foo_db", "MySQL", "foo_db", "T_USER_UPPER"));
        Collection<LocalDataQueryResultRow> actual = executor.getRows(new ShowTableMetaDataStatement(Collections.singleton(new IdentifierValue("t_user_upper")), null), mock(ContextManager.class));
        assertThat(actual.size(), is(2));
        assertThat(actual.iterator().next().getCell(2), is("T_USER_UPPER"));
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        when(result.getName()).thenReturn("foo_db");
        when(result.getSchema("foo_db")).thenReturn(new ShardingSphereSchema("foo_db", mock(DatabaseType.class), createTables(), Collections.emptyList()));
        return result;
    }
    
    private Collection<ShardingSphereTable> createTables() {
        Collection<ShardingSphereColumn> columns = Collections.singletonList(new ShardingSphereColumn("order_id", 0, false, false, false, true, false, false));
        Collection<ShardingSphereIndex> indexes = Collections.singletonList(new ShardingSphereIndex("primary", Collections.emptyList(), false));
        return Collections.singleton(new ShardingSphereTable("t_order", columns, indexes, Collections.emptyList()));
    }
    
    private Collection<ShardingSphereTable> createTables(final String tableName) {
        Collection<ShardingSphereColumn> columns = Collections.singletonList(new ShardingSphereColumn("order_id", 0, false, false, false, true, false, false));
        Collection<ShardingSphereIndex> indexes = Collections.singletonList(new ShardingSphereIndex("primary", Collections.emptyList(), false));
        return Collections.singleton(new ShardingSphereTable(tableName, columns, indexes, Collections.emptyList()));
    }
    
    private ShardingSphereDatabase createDatabase(final String databaseName, final String databaseType, final String schemaName, final String tableName) {
        DatabaseType type = TypedSPILoader.getService(DatabaseType.class, databaseType);
        StorageUnit storageUnit = mock(StorageUnit.class);
        when(storageUnit.getStorageType()).thenReturn(type);
        return new ShardingSphereDatabase(databaseName, type, new ResourceMetaData(Collections.emptyMap(), Collections.singletonMap("foo_ds", storageUnit)), mock(RuleMetaData.class),
                Collections.singleton(new ShardingSphereSchema(schemaName, type, createTables(tableName), Collections.emptyList())), new ConfigurationProperties(new Properties()));
    }
    
    private ShowTableMetaDataStatement createSqlStatement() {
        return new ShowTableMetaDataStatement(Collections.singleton(new IdentifierValue("t_order")), new FromDatabaseSegment(0, new DatabaseSegment(0, 0, new IdentifierValue("foo_db"))));
    }
}
