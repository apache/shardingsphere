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

package org.apache.shardingsphere.proxy.backend.text.distsql.rdl.impl;

import org.apache.shardingsphere.distsql.parser.segment.TableRuleSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateShardingTableRuleStatement;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.DuplicateTableException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CreateShardingTableRuleBackendHandlerTest {
    
    @Mock
    private BackendConnection backendConnection;
    
    @Mock
    private CreateShardingTableRuleStatement sqlStatement;
    
    @Mock
    private MetaDataContexts metaDataContexts;
    
    @Mock
    private TransactionContexts transactionContexts;
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    @Mock
    private ShardingSphereSchema shardingSphereSchema;
    
    private CreateShardingTableRuleBackendHandler handler = new CreateShardingTableRuleBackendHandler(sqlStatement, backendConnection);
    
    @Before
    public void setUp() throws Exception {
        ProxyContext.getInstance().init(metaDataContexts, transactionContexts);
        when(metaDataContexts.getAllSchemaNames()).thenReturn(Arrays.asList("test"));
        when(metaDataContexts.getMetaData(eq("test"))).thenReturn(shardingSphereMetaData);
        when(shardingSphereMetaData.getSchema()).thenReturn(shardingSphereSchema);
    }
    
    @Test
    public void assertExecute() {
        ResponseHeader responseHeader = handler.execute("test", sqlStatement);
        assertNotNull(responseHeader);
        assertTrue(responseHeader instanceof UpdateResponseHeader);
    }
    
    @Test(expected = DuplicateTableException.class)
    public void assertExecuteWithDuplicateTablesInRDL() {
        TableRuleSegment tableRuleSegment = new TableRuleSegment();
        tableRuleSegment.setLogicTable("t_order");
        when(sqlStatement.getTables()).thenReturn(Arrays.asList(tableRuleSegment, tableRuleSegment));
        handler.execute("test", sqlStatement);
    }
    
    @Test(expected = DuplicateTableException.class)
    public void assertExecuteWithDuplicateTables() {
        when(shardingSphereSchema.containsTable(anyString())).thenReturn(true);
        TableRuleSegment tableRuleSegment = new TableRuleSegment();
        tableRuleSegment.setLogicTable("t_order");
        when(sqlStatement.getTables()).thenReturn(Arrays.asList(tableRuleSegment));
        handler.execute("test", sqlStatement);
    }
}
