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

package org.apache.shardingsphere.single.distsql.handler.query;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.single.distsql.statement.rql.ShowSingleTablesStatement;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockedConstruction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class ShowSingleTablesExecutorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private final ShowSingleTablesExecutor executor = (ShowSingleTablesExecutor) TypedSPILoader.getService(DistSQLQueryExecutor.class, ShowSingleTablesStatement.class);
    
    @Test
    void assertGetColumnNamesWithoutSchema() {
        executor.setDatabase(new ShardingSphereDatabase(
                "foo_db", databaseType, new ResourceMetaData(Collections.emptyMap(), Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), Collections.emptyList()));
        assertThat(executor.getColumnNames(new ShowSingleTablesStatement(null, null)), is(Arrays.asList("table_name", "storage_unit_name")));
    }
    
    @Test
    void assertGetColumnNamesWithSchema() {
        try (
                MockedConstruction<DatabaseTypeRegistry> ignored = mockConstruction(DatabaseTypeRegistry.class, withSettings().defaultAnswer(Answers.RETURNS_DEEP_STUBS),
                        (mock, context) -> when(mock.getDialectDatabaseMetaData().getSchemaOption().isSchemaAvailable()).thenReturn(true))) {
            executor.setDatabase(new ShardingSphereDatabase(
                    "foo_db", databaseType, new ResourceMetaData(Collections.emptyMap(), Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), Collections.emptyList()));
            assertThat(executor.getColumnNames(new ShowSingleTablesStatement(null, null)), is(Arrays.asList("table_name", "storage_unit_name", "schema_name")));
        }
    }
    
    @Test
    void assertGetRowsWithoutLikePattern() {
        executor.setDatabase(new ShardingSphereDatabase(
                "foo_db", databaseType, new ResourceMetaData(Collections.emptyMap(), Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), Collections.emptyList()));
        Map<String, Collection<DataNode>> singleTableDataNodeMap = new HashMap<>(2, 1F);
        singleTableDataNodeMap.put("t_order_item", Collections.singleton(new DataNode("ds_2", (String) null, "t_order_item")));
        singleTableDataNodeMap.put("t_order", Collections.singleton(new DataNode("ds_1", (String) null, "t_order")));
        executor.setRule(mockRule(singleTableDataNodeMap));
        List<LocalDataQueryResultRow> actualRowList = new ArrayList<>(executor.getRows(new ShowSingleTablesStatement(null, null), null));
        assertThat(actualRowList.size(), is(2));
        assertThat(actualRowList.get(0).getCell(1), is("t_order"));
        assertThat(actualRowList.get(0).getCell(2), is("ds_1"));
        assertThat(actualRowList.get(1).getCell(1), is("t_order_item"));
        assertThat(actualRowList.get(1).getCell(2), is("ds_2"));
    }
    
    @Test
    void assertGetRowsWithLikePatternAndSchema() {
        try (
                MockedConstruction<DatabaseTypeRegistry> ignored = mockConstruction(DatabaseTypeRegistry.class, withSettings().defaultAnswer(Answers.RETURNS_DEEP_STUBS),
                        (mock, context) -> when(mock.getDialectDatabaseMetaData().getSchemaOption().isSchemaAvailable()).thenReturn(true))) {
            executor.setDatabase(new ShardingSphereDatabase(
                    "foo_db", databaseType, new ResourceMetaData(Collections.emptyMap(), Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), Collections.emptyList()));
            Map<String, Collection<DataNode>> singleTableDataNodeMap = new HashMap<>(2, 1F);
            singleTableDataNodeMap.put("t_order", Collections.singleton(new DataNode("ds_1", "public", "t_order")));
            singleTableDataNodeMap.put("t_order_item", Collections.singleton(new DataNode("ds_2", "public", "t_order_item")));
            executor.setRule(mockRule(singleTableDataNodeMap));
            List<LocalDataQueryResultRow> actualRowList = new ArrayList<>(executor.getRows(new ShowSingleTablesStatement(null, "t_order"), null));
            assertThat(actualRowList.size(), is(1));
            assertThat(actualRowList.get(0).getCell(1), is("t_order"));
            assertThat(actualRowList.get(0).getCell(2), is("ds_1"));
            assertThat(actualRowList.get(0).getCell(3), is("public"));
        }
    }
    
    private SingleRule mockRule(final Map<String, Collection<DataNode>> singleTableDataNodeMap) {
        DataNodeRuleAttribute ruleAttribute = mock(DataNodeRuleAttribute.class);
        when(ruleAttribute.getAllDataNodes()).thenReturn(singleTableDataNodeMap);
        SingleRule result = mock(SingleRule.class);
        when(result.getAttributes()).thenReturn(new RuleAttributes(ruleAttribute));
        return result;
    }
    
    @Test
    void assertGetRuleClass() {
        assertThat(executor.getRuleClass(), is(SingleRule.class));
    }
}
