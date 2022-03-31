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
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.RALBackendHandler.HandlerParameter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.SchemaSegment;
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

public final class ShowTableMetadataHandlerTest {
    
    @Test
    public void assertExecutor() throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getAllSchemaNames()).thenReturn(Collections.singletonList("schema_name"));
        ShardingSphereMetaData shardingSphereMetaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(shardingSphereMetaData.getDefaultSchema()).thenReturn(new ShardingSphereSchema(createTableMap()));
        when(contextManager.getMetaDataContexts().getMetaData("schema_name")).thenReturn(shardingSphereMetaData);
        ProxyContext.getInstance().init(contextManager);
        ShowTableMetadataHandler handler = new ShowTableMetadataHandler().init(getParameter(createSqlStatement(), mockConnectionSession()));
        handler.execute();
        handler.next();
        ArrayList<Object> data = new ArrayList<>(handler.getRowData());
        assertThat(data.size(), is(4));
        assertThat(data.get(0), is("schema_name"));
        assertThat(data.get(1), is("t_order"));
        assertThat(data.get(2), is("COLUMN"));
        assertThat(data.get(3), is("order_id"));
        handler.next();
        data = new ArrayList<>(handler.getRowData());
        assertThat(data.size(), is(4));
        assertThat(data.get(0), is("schema_name"));
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
        return new ShowTableMetadataStatement(Collections.singleton("t_order"), new SchemaSegment(0, 0, new IdentifierValue("schema_name")));
    }
    
    private ConnectionSession mockConnectionSession() {
        return mock(ConnectionSession.class);
    }
    
    private HandlerParameter<ShowTableMetadataStatement> getParameter(final ShowTableMetadataStatement statement, final ConnectionSession connectionSession) {
        return new HandlerParameter<ShowTableMetadataStatement>().setStatement(statement).setConnectionSession(connectionSession);
    }
}
