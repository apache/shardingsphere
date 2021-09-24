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
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.infra.config.DatabaseAccessConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.sql.federate.translatable.TranslatableSchema;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.DataSourcesMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.builder.loader.dialect.H2TableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.optimize.ShardingSphereOptimizer;
import org.apache.shardingsphere.infra.optimize.context.translatable.TranslatableOptimizerContextFactory;
import org.apache.shardingsphere.infra.optimize.context.filterable.FilterableOptimizerContext;
import org.apache.shardingsphere.infra.optimize.context.filterable.FilterableOptimizerContextFactory;
import org.apache.shardingsphere.infra.optimize.core.metadata.FederationSchemaMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.schema.SchemaRulesBuilder;
import org.apache.shardingsphere.infra.rule.builder.schema.SchemaRulesBuilderMaterials;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class FederateJDBCExecutorTest {
    
    private static final String SELECT_SQL_BY_ID_ACROSS_SINGLE_AND_SHARDING_TABLES =
        "SELECT t_order_federate.order_id, t_order_federate.user_id, t_user_info.information "
            + "FROM t_order_federate , t_user_info "
            + "WHERE t_order_federate.user_id = t_user_info.user_id";
    
    private ShardingSphereOptimizer optimizer;
    
    @Mock
    private RuleConfiguration testRuleConfig;
    
    @Before
    public void init() throws Exception {
        String schemaName = "federate_jdbc";
        Map<String, List<String>> columnMap = initializeColumnMap();
        Map<String, List<String>> tableMap = initializeTableMap();
        Map<String, DataSource> actualDataSourceMap = initializeDataSourceMap(schemaName);
        TranslatableSchema logicSchema = initializeLogicSchema(schemaName, columnMap, tableMap);
        FilterableOptimizerContext optimizerContext = FilterableOptimizerContextFactory.create(createMetaDataMap(schemaName, actualDataSourceMap));
        optimizer = new ShardingSphereOptimizer(TranslatableOptimizerContextFactory.create(schemaName, logicSchema, optimizerContext));
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
    
    private Map<String, DataSource> initializeDataSourceMap(final String schemaName) {
        Map<String, DataSource> result = new HashMap<>();
        result.put(schemaName, buildDataSource(schemaName));
        return result;
    }
    
    private static DataSource buildDataSource(final String dataSourceName) {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName("org.h2.Driver");
        result.setUrl(String.format("jdbc:h2:mem:%s;DATABASE_TO_UPPER=false;MODE=MySQL", dataSourceName));
        result.setUsername("sa");
        result.setPassword("");
        result.setMaxTotal(50);
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
    
    private Map<String, ShardingSphereMetaData> createMetaDataMap(final String schemaName, final Map<String, DataSource> actualDataSourceMap) throws SQLException {
        DataSource dataSource = actualDataSourceMap.get(schemaName);
        H2TableMetaDataLoader loader = new H2TableMetaDataLoader();
        Map<String, TableMetaData> tableMetaDataList = loader.load(dataSource, Collections.emptyList());
        Collection<RuleConfiguration> ruleConfigurations = Collections.singletonList(testRuleConfig);
        Map<String, String> accessConfiguration = initializeAccessConfiguration();
        return createMetaDataMap(tableMetaDataList, ruleConfigurations, schemaName, accessConfiguration, actualDataSourceMap);
    }
    
    private Map<String, ShardingSphereMetaData> createMetaDataMap(final Map<String, TableMetaData> tableMetaDataList, final Collection<RuleConfiguration> ruleConfigs, 
        final String schemaName, final Map<String, String> accessConfiguration, final Map<String, DataSource> actualDataSourceMap) {
        DataSourcesMetaData dataSourcesMetaData = getInstance(schemaName, accessConfiguration);
        ShardingSphereResource resource = new ShardingSphereResource(actualDataSourceMap, dataSourcesMetaData, null, new MySQLDatabaseType());
        ShardingSphereSchema schema = new ShardingSphereSchema(tableMetaDataList);
        Collection<ShardingSphereRule> rules = SchemaRulesBuilder.buildRules(
                new SchemaRulesBuilderMaterials(schemaName, ruleConfigs, new MySQLDatabaseType(), actualDataSourceMap, new ConfigurationProperties(new Properties())));
        ShardingSphereRuleMetaData shardingSphereRuleMetaData = new ShardingSphereRuleMetaData(ruleConfigs, rules);
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(schemaName, resource, shardingSphereRuleMetaData, schema);
        return Collections.singletonMap("testSchema", metaData);
    }
    
    private Map<String, String> initializeAccessConfiguration() {
        Map<String, String> result = new HashMap<>();
        result.put("jdbcUrl", "jdbc:h2:mem:federate_jdbc;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL");
        result.put("username", "sa");
        return result;
    }
    
    private DataSourcesMetaData getInstance(final String schemaName, final Map<String, String> configMap) {
        DatabaseAccessConfiguration config = new DatabaseAccessConfiguration(configMap.get("jdbcUrl"), configMap.get("username"));
        return new DataSourcesMetaData(new H2DatabaseType(), Collections.singletonMap(schemaName, config));
    }
}
