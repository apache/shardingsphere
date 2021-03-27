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

package org.apache.shardingsphere.infra.executor.exec;

import org.apache.calcite.config.CalciteConnectionConfigImpl;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.plan.Contexts;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptCostImpl;
import org.apache.calcite.plan.RelOptPlanner;
import org.apache.calcite.plan.RelOptSchema;
import org.apache.calcite.plan.volcano.VolcanoPlanner;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.core.RelFactories;
import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.rex.RexBuilder;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.type.SqlTypeFactoryImpl;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.RelBuilder;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.executor.exec.ExecContext.OriginSql;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.optimize.schema.ShardingSphereCalciteSchema;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.ShardingSphereRulesBuilder;
import org.apache.shardingsphere.infra.yaml.config.YamlRootRuleConfigurations;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.swapper.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.h2.tools.RunScript;
import org.junit.BeforeClass;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.TreeMap;

public abstract class BaseExecutorTest {
    
    private static final YamlRuleConfigurationSwapperEngine SWAPPER_ENGINE = new YamlRuleConfigurationSwapperEngine();
    
    private static final YamlDataSourceConfigurationSwapper DATASOURCE_SWAPPER = new YamlDataSourceConfigurationSwapper();
    
    private static final Map<String, DataSource> ACTUAL_DATA_SOURCES = new HashMap<>();
    
    private static final String INIT_CALCITE_DATABASE_0 = "h2/jdbc_init_calcite_0.sql";
    
    private static final String INIT_CALCITE_DATABASE_1 = "h2/jdbc_init_calcite_1.sql";
    
    private static final String SCHEMA_NAME = "logic_db";
    
    protected static Collection<ShardingSphereRule> rules;
    
    protected static ShardingSphereSchema schema;
    
    protected RelBuilder relBuilder;
    
    @BeforeClass
    public static synchronized void initializeDataSource() throws SQLException, IOException {
        initRule();
        initTable();
        initSchema();
    }
    
    public static void initTable() throws SQLException {
        Map<String, DataSource> dataSourceMap = ACTUAL_DATA_SOURCES;
        Connection database0 = dataSourceMap.get("ds_0").getConnection();
        RunScript.execute(database0, new InputStreamReader(Objects.requireNonNull(BaseExecutorTest.class.getClassLoader().getResourceAsStream(INIT_CALCITE_DATABASE_0))));
        RunScript.execute(database0, new InputStreamReader(Objects.requireNonNull(BaseExecutorTest.class.getClassLoader().getResourceAsStream("h2/calcite_data_0.sql"))));
    
        Connection database1 = dataSourceMap.get("ds_1").getConnection();
        RunScript.execute(database1, new InputStreamReader(Objects.requireNonNull(BaseExecutorTest.class.getClassLoader().getResourceAsStream(INIT_CALCITE_DATABASE_1))));
        RunScript.execute(database1, new InputStreamReader(Objects.requireNonNull(BaseExecutorTest.class.getClassLoader().getResourceAsStream("h2/calcite_data_1.sql"))));
    }
    
    public static void initRule() throws IOException {
        File yamlFile = new File(BaseExecutorTest.class.getResource("/config/sharding-databases-tables.yaml").getFile());
        YamlRootRuleConfigurations configurations = YamlEngine.unmarshal(yamlFile, YamlRootRuleConfigurations.class);
        ACTUAL_DATA_SOURCES.putAll(DATASOURCE_SWAPPER.swapToDataSources(configurations.getDataSources()));
        Collection<RuleConfiguration> ruleConfigs = SWAPPER_ENGINE.swapToRuleConfigurations(configurations.getRules());
        rules = ShardingSphereRulesBuilder.build(ruleConfigs, new H2DatabaseType(), ACTUAL_DATA_SOURCES, SCHEMA_NAME);
    }
    
    private static void createDataSources(final String dataSourceName, final String initSql) throws SQLException {
        ACTUAL_DATA_SOURCES.put(dataSourceName, build(dataSourceName));
        initializeSchema(dataSourceName, initSql);
    }
    
    private static void initializeSchema(final String dataSourceName, final String initSql) throws SQLException {
        try (Connection conn = ACTUAL_DATA_SOURCES.get(dataSourceName).getConnection()) {
            
        }
    }
    
    protected static void initSchema() {
        Map<String, TableMetaData> tableMetaDataMap = new HashMap<>(3, 1);
        tableMetaDataMap.put("t_order", new TableMetaData(
                Arrays.asList(new ColumnMetaData("order_id", Types.INTEGER, true, false, false),
                        new ColumnMetaData("user_id", Types.INTEGER, false, false, false),
                        new ColumnMetaData("status", Types.VARCHAR, false, false, false)), Collections.emptySet()));
        tableMetaDataMap.put("t_order_item", new TableMetaData(Arrays.asList(new ColumnMetaData("order_item_id", Types.INTEGER, true, false, false),
                new ColumnMetaData("order_id", Types.INTEGER, false, false, false),
                new ColumnMetaData("user_id", Types.INTEGER, false, false, false),
                new ColumnMetaData("status", Types.VARCHAR, false, false, false),
                new ColumnMetaData("remarks", Types.VARCHAR, false, false, false)), Collections.emptySet()));
        tableMetaDataMap.put("t_user", new TableMetaData(Arrays.asList(new ColumnMetaData("user_id", Types.INTEGER, true, false, false),
                new ColumnMetaData("plain_pwd", Types.VARCHAR, false, false, false),
                new ColumnMetaData("user_name", Types.VARCHAR, false, false, false)), Collections.emptySet()));
        schema = new ShardingSphereSchema(tableMetaDataMap);
    }
    
    protected static Map<String, DataSource> getActualDataSources() {
        return ACTUAL_DATA_SOURCES;
    }
    
    protected RelBuilder relBuilder() {
        String schemaName = SCHEMA_NAME;
        RelOptPlanner planner = new VolcanoPlanner(RelOptCostImpl.FACTORY, Contexts.EMPTY_CONTEXT);
        planner.clearRelTraitDefs();
        RexBuilder rexBuilder = new RexBuilder(new SqlTypeFactoryImpl(RelDataTypeSystem.DEFAULT));
        RelOptCluster cluster = RelOptCluster.create(planner, rexBuilder);
    
        SchemaPlus rootSchema = Frameworks.createRootSchema(true);
        rootSchema.add(schemaName, new ShardingSphereCalciteSchema(schema));
        CalciteSchema schema =  CalciteSchema.from(rootSchema);
    
        RelOptSchema catalog = new CalciteCatalogReader(schema,
                schema.path(schemaName),
                new JavaTypeFactoryImpl(),
                new CalciteConnectionConfigImpl(new Properties()));
    
        return RelFactories.LOGICAL_BUILDER.create(cluster, catalog);
    }
    
    protected ExecContext statementExecContext() {
        ShardingRule shardingRule = rules.stream().filter(rule -> rule instanceof ShardingRule).map(ShardingRule.class::cast).findFirst().get();
        return ExecContext.ExecContextBuilder.builder(shardingRule, Collections.emptyList(), new H2DatabaseType(), new ExecutorJDBCManagerImpl(getActualDataSources()))
                .originSql(new OriginSql("", null))
                .storageResourceOption(new StatementOption(true))
                .props(new ConfigurationProperties(new Properties()))
                .holdTransaction(false)
                .build();
    }
    
    protected Executor buildExecutor(RelNode relNode) {
        return ExecutorBuilder.build(statementExecContext(), relNode);
    }
    
    protected Map<String, Integer> createColumnLabelAndIndexMap(final QueryResultMetaData resultSetMetaData) throws SQLException {
        Map<String, Integer> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (int columnIndex = resultSetMetaData.getColumnCount(); columnIndex > 0; columnIndex--) {
            result.put(resultSetMetaData.getColumnLabel(columnIndex), columnIndex);
        }
        return result;
    }
    
    /**
     * Build data source.
     *
     * @param dataSourceName data source name
     * @return data source
     */
    public static DataSource build(final String dataSourceName) {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName("org.h2.Driver");
        result.setUrl(String.format("jdbc:h2:mem:%s;DATABASE_TO_UPPER=false;MODE=MySQL", dataSourceName));
        result.setUsername("sa");
        result.setPassword("");
        result.setMaxTotal(50);
        return result;
    }
}
