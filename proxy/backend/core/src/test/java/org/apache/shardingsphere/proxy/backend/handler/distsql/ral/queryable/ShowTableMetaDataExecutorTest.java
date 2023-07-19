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

import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowTableMetaDataStatement;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class ShowTableMetaDataExecutorTest {
    
    @Test
    void assertExecute() {
        ConnectionSession connectionSession = mock(ConnectionSession.class, RETURNS_DEEP_STUBS);
        when(connectionSession.getDatabaseName()).thenReturn("foo_db");
        when(connectionSession.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        ShowTableMetaDataExecutor executor = new ShowTableMetaDataExecutor();
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().databaseExists("foo_db")).thenReturn(true);
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db");
        when(ProxyContext.getInstance().getDatabase("foo_db")).thenReturn(database);
        Collection<LocalDataQueryResultRow> actual = executor.getRows(mock(ShardingSphereMetaData.class), connectionSession, createSqlStatement());
        assertThat(actual.size(), is(2));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("foo_db"));
        assertThat(row.getCell(2), is("t_order"));
        assertThat(row.getCell(3), is("COLUMN"));
        assertThat(row.getCell(4), is("order_id"));
        row = iterator.next();
        assertThat(row.getCell(1), is("foo_db"));
        assertThat(row.getCell(2), is("t_order"));
        assertThat(row.getCell(3), is("INDEX"));
        assertThat(row.getCell(4), is("primary"));
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema("foo_db")).thenReturn(new ShardingSphereSchema(createTableMap(), Collections.emptyMap()));
        when(result.getMetaDataContexts().getMetaData().getDatabases()).thenReturn(Collections.singletonMap("foo_db", database));
        when(result.getMetaDataContexts().getMetaData().getDatabase("foo_db")).thenReturn(database);
        return result;
    }
    
    @Test
    void assertGetColumnNames() {
        ShowTableMetaDataExecutor executor = new ShowTableMetaDataExecutor();
        Collection<String> columns = executor.getColumnNames();
        assertThat(columns.size(), is(4));
        Iterator<String> iterator = columns.iterator();
        assertThat(iterator.next(), is("schema_name"));
        assertThat(iterator.next(), is("table_name"));
        assertThat(iterator.next(), is("type"));
        assertThat(iterator.next(), is("name"));
    }
    
    private Map<String, ShardingSphereTable> createTableMap() {
        Map<String, ShardingSphereTable> result = new HashMap<>();
        Collection<ShardingSphereColumn> columns = Collections.singletonList(new ShardingSphereColumn("order_id", 0, false, false, false, true, false));
        Collection<ShardingSphereIndex> indexes = Collections.singletonList(new ShardingSphereIndex("primary"));
        result.put("t_order", new ShardingSphereTable("t_order", columns, indexes, Collections.emptyList()));
        return result;
    }
    
    private ShowTableMetaDataStatement createSqlStatement() {
        return new ShowTableMetaDataStatement(Collections.singleton("t_order"), new DatabaseSegment(0, 0, new IdentifierValue("foo_db")));
    }
}
