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

import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowTableMetadataStatement;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShowTableMetadataHandlerTest extends ProxyContextRestorer {
    
    @Test
    public void assertExecute() throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema("db_name")).thenReturn(new ShardingSphereSchema(createTableMap()));
        when(contextManager.getMetaDataContexts().getMetaData().getDatabases()).thenReturn(Collections.singletonMap("db_name", database));
        when(contextManager.getMetaDataContexts().getMetaData().containsDatabase("db_name")).thenReturn(true);
        when(contextManager.getMetaDataContexts().getMetaData().getDatabase("db_name")).thenReturn(database);
        ProxyContext.init(contextManager);
        ConnectionSession connectionSession = mock(ConnectionSession.class, RETURNS_DEEP_STUBS);
        when(connectionSession.getDatabaseName()).thenReturn("db_name");
        ShowTableMetadataHandler handler = new ShowTableMetadataHandler();
        handler.init(createSqlStatement(), connectionSession);
        handler.execute();
        handler.next();
        List<Object> data = handler.getRowData().getData();
        assertThat(data.size(), is(4));
        assertThat(data.get(0), is("db_name"));
        assertThat(data.get(1), is("t_order"));
        assertThat(data.get(2), is("COLUMN"));
        assertThat(data.get(3), is("order_id"));
        handler.next();
        data = handler.getRowData().getData();
        assertThat(data.size(), is(4));
        assertThat(data.get(0), is("db_name"));
        assertThat(data.get(1), is("t_order"));
        assertThat(data.get(2), is("INDEX"));
        assertThat(data.get(3), is("primary"));
    }
    
    private Map<String, ShardingSphereTable> createTableMap() {
        Map<String, ShardingSphereTable> result = new HashMap<>();
        Collection<ShardingSphereColumn> columns = Collections.singletonList(new ShardingSphereColumn("order_id", 0, false, false, false, true));
        Collection<ShardingSphereIndex> indexes = Collections.singletonList(new ShardingSphereIndex("primary"));
        result.put("t_order", new ShardingSphereTable("t_order", columns, indexes, Collections.emptyList()));
        return result;
    }
    
    private ShowTableMetadataStatement createSqlStatement() {
        return new ShowTableMetadataStatement(Collections.singleton("t_order"), new DatabaseSegment(0, 0, new IdentifierValue("db_name")));
    }
}
