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

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.federate.translatable.TranslatableSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.optimize.ShardingSphereOptimizer;
import org.apache.shardingsphere.infra.optimize.context.translatable.TranslatableOptimizerContextFactory;
import org.apache.shardingsphere.infra.optimize.metadata.FederationSchemaMetaData;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class FederateJDBCExecutorTest {
    
    private static final String SELECT_SQL_BY_ID_ACROSS_SINGLE_AND_SHARDING_TABLES =
        "SELECT t_order_federate.order_id, t_order_federate.user_id, t_user_info.information "
            + "FROM t_order_federate , t_user_info "
            + "WHERE t_order_federate.user_id = t_user_info.user_id";
    
    private ShardingSphereOptimizer optimizer;
    
    @Before
    public void init() throws Exception {
        String schemaName = "federate_jdbc";
        TranslatableSchema schema = createSchema(schemaName);
        optimizer = new ShardingSphereOptimizer(TranslatableOptimizerContextFactory.create(schemaName, schema));
    }
    
    private TranslatableSchema createSchema(final String schemaName) {
        Map<String, List<String>> columnMap = createColumnMap();
        Map<String, List<String>> tableMap = createTableMap();
        return new TranslatableSchema(createSchemaMetaData(schemaName, tableMap.get(schemaName), columnMap));
    }
    
    private Map<String, List<String>> createColumnMap() {
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
    
    private Map<String, List<String>> createTableMap() {
        Map<String, List<String>> result = new HashMap<>();
        List<String> tableList = new ArrayList<>();
        tableList.add("t_order_federate");
        tableList.add("t_user_info");
        result.put("federate_jdbc", tableList);
        return result;
    }
    
    private FederationSchemaMetaData createSchemaMetaData(final String schemaName, final List<String> tableNames, final Map<String, List<String>> tableColumns) {
        Map<String, TableMetaData> tableMetaDataList = new HashMap<>(tableNames.size(), 1);
        for (String each: tableNames) {
            tableMetaDataList.put(each, createTableMetaData(each, tableColumns.get(each)));
        }
        return new FederationSchemaMetaData(schemaName, tableMetaDataList);
    }
    
    private TableMetaData createTableMetaData(final String tableName, final Collection<String> columnNames) {
        Collection<ColumnMetaData> columnMetaDataList = new LinkedList<>();
        Collection<IndexMetaData> indexMetaDataList = new LinkedList<>();
        for (String each: columnNames) {
            columnMetaDataList.add(new ColumnMetaData(each, 1, false, false, false));
            indexMetaDataList.add(new IndexMetaData("index"));
        }
        return new TableMetaData(tableName, columnMetaDataList, indexMetaDataList);
    }
    
    @Test
    public void assertSimpleSelect() {
        ShardingSphereSQLParserEngine sqlParserEngine = new ShardingSphereSQLParserEngine(
                DatabaseTypeRegistry.getTrunkDatabaseTypeName(new H2DatabaseType()), new ConfigurationProperties(new Properties()));
        SQLStatement sqlStatement = sqlParserEngine.parse(SELECT_SQL_BY_ID_ACROSS_SINGLE_AND_SHARDING_TABLES, false);
        String actual = optimizer.optimize(sqlStatement).explain();
        String expected = "EnumerableCalc(expr#0..4=[{inputs}],expr#5=[CAST($t1):VARCHAR],expr#6=[CAST($t3):VARCHAR],expr#7=[=($t5,$t6)],proj#0..1=[{exprs}],information=[$t4],$condition=[$t7])"
                + "  EnumerableNestedLoopJoin(condition=[true],joinType=[inner])"
                + "    EnumerableTableScan(table=[[federate_jdbc,t_order_federate]])"
                + "    EnumerableTableScan(table=[[federate_jdbc,t_user_info]])";
        assertThat(actual.replaceAll("\\s*", ""), is(expected.replaceAll("\\s*", "")));
    }
}
