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

package org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.global.node.statistics;

import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.yaml.data.pojo.YamlRowStatistics;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.global.GlobalDataChangedEventHandler;
import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StatisticsChangedHandlerTest {
    
    private GlobalDataChangedEventHandler handler;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @BeforeEach
    void setUp() {
        handler = ShardingSphereServiceLoader.getServiceInstances(GlobalDataChangedEventHandler.class).stream()
                .filter(each -> "/statistics/databases".equals(NodePathGenerator.toPath(each.getSubscribedNodePath()))).findFirst().orElse(null);
    }
    
    @Test
    void assertHandleWithDatabaseAdded() {
        handler.handle(contextManager, new DataChangedEvent("/statistics/databases/foo_db", "", Type.ADDED));
        verify(contextManager.getMetaDataContextManager().getStatisticsManager()).addDatabaseStatistics("foo_db");
    }
    
    @Test
    void assertHandleWithDatabaseDeleted() {
        handler.handle(contextManager, new DataChangedEvent("/statistics/databases/foo_db", "", Type.DELETED));
        verify(contextManager.getMetaDataContextManager().getStatisticsManager()).dropDatabaseStatistics("foo_db");
    }
    
    @Test
    void assertHandleWithSchemaAdded() {
        handler.handle(contextManager, new DataChangedEvent("/statistics/databases/foo_db/schemas/foo_schema", "", Type.ADDED));
        verify(contextManager.getMetaDataContextManager().getStatisticsManager()).addSchemaStatistics("foo_db", "foo_schema");
    }
    
    @Test
    void assertHandleWithSchemaDeleted() {
        handler.handle(contextManager, new DataChangedEvent("/statistics/databases/foo_db/schemas/foo_schema", "", Type.DELETED));
        verify(contextManager.getMetaDataContextManager().getStatisticsManager()).dropSchemaStatistics("foo_db", "foo_schema");
    }
    
    @Test
    void assertHandleWithTableAdded() {
        handler.handle(contextManager, new DataChangedEvent("/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl", "", Type.ADDED));
        verify(contextManager.getMetaDataContextManager().getStatisticsManager()).addTableStatistics("foo_db", "foo_schema", "foo_tbl");
    }
    
    @Test
    void assertHandleWithTableDeleted() {
        handler.handle(contextManager, new DataChangedEvent("/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl", "", Type.DELETED));
        verify(contextManager.getMetaDataContextManager().getStatisticsManager()).dropTableStatistics("foo_db", "foo_schema", "foo_tbl");
    }
    
    @Test
    void assertHandleWithShardingSphereRowChanged() {
        YamlRowStatistics rowData = new YamlRowStatistics();
        rowData.setUniqueKey("1");
        handler.handle(contextManager, new DataChangedEvent("/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl/1", "{uniqueKey: 1}", Type.ADDED));
        verify(contextManager.getMetaDataContextManager().getStatisticsManager()).alterRowStatistics(eq("foo_db"), eq("foo_schema"), eq("foo_tbl"), refEq(rowData));
    }
    
    @Test
    void assertHandleWithShardingSphereRowDeleted() {
        handler.handle(contextManager, new DataChangedEvent("/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl/1", "{uniqueKey: 1}", Type.DELETED));
        verify(contextManager.getMetaDataContextManager().getStatisticsManager()).deleteRowStatistics("foo_db", "foo_schema", "foo_tbl", "1");
    }
    
    @Test
    void assertHandleWithMissedDatabaseNameEventKey() {
        handler.handle(contextManager, new DataChangedEvent("/statistics/databases", "=", Type.ADDED));
        verify(contextManager.getMetaDataContextManager().getStatisticsManager(), never()).addDatabaseStatistics(any());
    }
    
    @Test
    void assertHandleWithMissedSchemaNameEventKey() {
        handler.handle(contextManager, new DataChangedEvent("/statistics/databases/foo_db/schemas", "=", Type.ADDED));
        verify(contextManager.getMetaDataContextManager().getStatisticsManager(), never()).addSchemaStatistics(any(), any());
    }
    
    @Test
    void assertHandleWithMissedTableNameEventKey() {
        handler.handle(contextManager, new DataChangedEvent("/statistics/databases/foo_db/schemas/foo_schema/tables", "", Type.ADDED));
        verify(contextManager.getMetaDataContextManager().getStatisticsManager(), never()).addTableStatistics(any(), any(), any());
    }
    
    @Test
    void assertHandleWithMissedRowEventKey() {
        handler.handle(contextManager, new DataChangedEvent("/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl/", "", Type.ADDED));
        verify(contextManager.getMetaDataContextManager().getStatisticsManager(), never()).alterRowStatistics(any(), any(), any(), any());
    }
}
