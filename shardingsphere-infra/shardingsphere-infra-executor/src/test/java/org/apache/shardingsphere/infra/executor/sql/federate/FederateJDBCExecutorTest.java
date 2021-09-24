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

package org.apache.shardingsphere.infra.executor.sql.federate;

import org.apache.calcite.rel.RelNode;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.federate.translatable.TranslatableSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.optimize.ShardingSphereOptimizer;
import org.apache.shardingsphere.infra.optimize.context.translatable.TranslatableOptimizerContextFactory;
import org.apache.shardingsphere.infra.optimize.core.metadata.FederationSchemaMetaData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class FederateJDBCExecutorTest {
    
    private static final String SELECT_SQL_BY_ID_ACROSS_SINGLE_AND_SHARDING_TABLES =
        "SELECT t_order_federate.order_id, t_order_federate.user_id, t_user_info.information "
            + "FROM t_order_federate , t_user_info "
            + "WHERE t_order_federate.user_id = t_user_info.user_id";
    
    private ShardingSphereOptimizer optimizer;
    
    @Before
    public void init() throws Exception {
        String schemaName = "federate_jdbc";
        Map<String, List<String>> columnMap = initializeColumnMap();
        Map<String, List<String>> tableMap = initializeTableMap();
        TranslatableSchema logicSchema = initializeLogicSchema(schemaName, columnMap, tableMap);
        optimizer = new ShardingSphereOptimizer(TranslatableOptimizerContextFactory.create(schemaName, logicSchema, new H2DatabaseType()));
    }
    
    @Test
    public void testSimpleSelect() {
        RelNode relNode = optimizer.optimize(SELECT_SQL_BY_ID_ACROSS_SINGLE_AND_SHARDING_TABLES);
        String temp = "EnumerableCalc(expr#0..4=[{inputs}],expr#5=[CAST($t1):VARCHAR],expr#6=[CAST($t3):VARCHAR],expr#7=[=($t5,$t6)],proj#0..1=[{exprs}],information=[$t4],$condition=[$t7])"
            + "  EnumerableNestedLoopJoin(condition=[true],joinType=[inner])"
            + "    EnumerableTableScan(table=[[federate_jdbc,t_order_federate]])"
            + "    EnumerableTableScan(table=[[federate_jdbc,t_user_info]])";
        String expected = temp.replaceAll("\\s*", "");
        String actual = relNode.explain().replaceAll("\\s*", "");
        assertThat(actual, is(expected));
    }
    
    private Map<String, List<String>> initializeTableMap() {
        Map<String, List<String>> result = new HashMap<>();
        List<String> tableList = new ArrayList<>();
        tableList.add("t_order_federate");
        tableList.add("t_user_info");
        result.put("federate_jdbc", tableList);
        return result;
    }
    
    private Map<String, List<String>> initializeColumnMap() {
        final Map<String, List<String>> result = new HashMap<>();
        List<String> columnList = new ArrayList<>();
        columnList.add("order_id");
        columnList.add("user_id");
        columnList.add("status");
        result.put("t_order_federate", columnList);
        List<String> columnList2 = new ArrayList<>();
        columnList2.add("user_id");
        columnList2.add("information");
        result.put("t_user_info", columnList2);
        return result;
    }
    
    private TranslatableSchema initializeLogicSchema(final String schemaName, final Map<String, List<String>> columnMap, final Map<String, List<String>> tableMap) {
        FederationSchemaMetaData federationSchemaMetaData = buildSchemaMetaData(schemaName, tableMap.get(schemaName), columnMap);
        return new TranslatableSchema(federationSchemaMetaData);
    }
    
    private FederationSchemaMetaData buildSchemaMetaData(final String schemaName, final List<String> tableNames, final Map<String, List<String>> tableColumns) {
        Map<String, TableMetaData> tableMetaDataList = new HashMap<>();
        for (String table: tableNames) {
            List<ColumnMetaData> columnMetaDataList = new ArrayList<>();
            List<IndexMetaData> indexMetaDataList = new ArrayList<>();
            for (String column: tableColumns.get(table)) {
                columnMetaDataList.add(new ColumnMetaData(column, 1, false, false, false));
                indexMetaDataList.add(new IndexMetaData("index"));
            }
            TableMetaData tableMetaData = new TableMetaData(table, columnMetaDataList, indexMetaDataList);
            tableMetaDataList.put(table, tableMetaData);
        }
        return new FederationSchemaMetaData(schemaName, tableMetaDataList);
    }
}
