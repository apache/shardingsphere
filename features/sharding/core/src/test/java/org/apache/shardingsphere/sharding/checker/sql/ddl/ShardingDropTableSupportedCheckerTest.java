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

package org.apache.shardingsphere.sharding.checker.sql.ddl;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.DropTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ShardingDropTableSupportedCheckerTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock
    private ShardingRule rule;
    
    @BeforeEach
    void init() {
        Map<String, ShardingTable> shardingTables = new LinkedHashMap<>(2, 1F);
        shardingTables.put("t_order_item", createShardingTable("t_order_item"));
        shardingTables.put("t_order", createShardingTable("t_order"));
        when(rule.getShardingTables()).thenReturn(shardingTables);
    }
    
    @Test
    void assertCheck() {
        DropTableStatement sqlStatement = new DropTableStatement(databaseType);
        SimpleTableSegment table = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_order_item")));
        sqlStatement.getTables().add(table);
        sqlStatement.buildAttributes();
        SQLStatementContext sqlStatementContext = new CommonSQLStatementContext(sqlStatement);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.containsTable("t_order_item")).thenReturn(true);
        ShardingDropTableSupportedChecker checker = new ShardingDropTableSupportedChecker();
        assertDoesNotThrow(() -> checker.check(rule, database, schema, sqlStatementContext));
    }
    
    private ShardingTable createShardingTable(final String tableName) {
        ShardingTable result = mock(ShardingTable.class);
        when(result.getLogicTable()).thenReturn(tableName);
        List<DataNode> dataNodes = new LinkedList<>();
        DataNode dataNode1 = mock(DataNode.class);
        when(dataNode1.getTableName()).thenReturn("t_order_item_1");
        dataNodes.add(dataNode1);
        DataNode dataNode2 = mock(DataNode.class);
        when(dataNode2.getTableName()).thenReturn("t_order_item_2");
        dataNodes.add(dataNode2);
        when(result.getActualDataNodes()).thenReturn(dataNodes);
        return result;
    }
}
