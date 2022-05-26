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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.queryable;

import org.apache.shardingsphere.distsql.parser.statement.ral.common.queryable.ShowTableMetadataStatement;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.TableMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.RALBackendHandler.HandlerParameter;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
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
    public void assertExecutor() throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchemas().get("db_name")).thenReturn(new ShardingSphereSchema(createTableMap()));
        when(contextManager.getMetaDataContexts().getMetaData().getDatabases()).thenReturn(Collections.singletonMap("db_name", database));
        when(contextManager.getMetaDataContexts().getDatabase("db_name")).thenReturn(database);
        ProxyContext.init(contextManager);
        ConnectionSession connectionSession = mock(ConnectionSession.class, RETURNS_DEEP_STUBS);
        when(connectionSession.getDatabaseName()).thenReturn("db_name");
        ShowTableMetadataHandler handler = new ShowTableMetadataHandler().init(getParameter(createSqlStatement(), connectionSession));
        handler.execute();
        handler.next();
        List<Object> data = new ArrayList<>(handler.getRowData());
        assertThat(data.size(), is(4));
        assertThat(data.get(0), is("db_name"));
        assertThat(data.get(1), is("t_order"));
        assertThat(data.get(2), is("COLUMN"));
        assertThat(data.get(3), is("order_id"));
        handler.next();
        data = new ArrayList<>(handler.getRowData());
        assertThat(data.size(), is(4));
        assertThat(data.get(0), is("db_name"));
        assertThat(data.get(1), is("t_order"));
        assertThat(data.get(2), is("INDEX"));
        assertThat(data.get(3), is("primary"));
    }
    
    private Map<String, TableMetaData> createTableMap() {
        Map<String, TableMetaData> result = new HashMap<>();
        List<ColumnMetaData> columns = Collections.singletonList(new ColumnMetaData("order_id", 0, false, false, false));
        List<IndexMetaData> indexes = Collections.singletonList(new IndexMetaData("primary"));
        result.put("t_order", new TableMetaData("t_order", columns, indexes, Collections.emptyList()));
        return result;
    }
    
    private ShowTableMetadataStatement createSqlStatement() {
        return new ShowTableMetadataStatement(Collections.singleton("t_order"), new DatabaseSegment(0, 0, new IdentifierValue("db_name")));
    }
    
    private HandlerParameter<ShowTableMetadataStatement> getParameter(final ShowTableMetadataStatement statement, final ConnectionSession connectionSession) {
        return new HandlerParameter<>(statement, new MySQLDatabaseType(), connectionSession);
    }
}
