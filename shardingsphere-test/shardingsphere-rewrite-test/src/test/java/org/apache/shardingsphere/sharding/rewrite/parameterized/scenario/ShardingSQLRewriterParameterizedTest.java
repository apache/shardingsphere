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

package org.apache.shardingsphere.sharding.rewrite.parameterized.scenario;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.metadata.schema.refresher.event.CreateTableEvent;
import org.apache.shardingsphere.infra.parser.sql.SQLStatementParserEngine;
import org.apache.shardingsphere.infra.rewrite.SQLRewriteEntry;
import org.apache.shardingsphere.infra.rewrite.engine.result.GenericSQLRewriteResult;
import org.apache.shardingsphere.infra.rewrite.engine.result.RouteSQLRewriteResult;
import org.apache.shardingsphere.infra.rewrite.engine.result.SQLRewriteResult;
import org.apache.shardingsphere.infra.rewrite.engine.result.SQLRewriteUnit;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.engine.SQLRouteEngine;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.ShardingSphereRulesBuilder;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootRuleConfigurations;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.sharding.rewrite.parameterized.engine.AbstractSQLRewriterParameterizedTest;
import org.apache.shardingsphere.sharding.rewrite.parameterized.engine.parameter.SQLRewriteEngineTestParameters;
import org.apache.shardingsphere.sharding.rewrite.parameterized.engine.parameter.SQLRewriteEngineTestParametersBuilder;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingSQLRewriterParameterizedTest extends AbstractSQLRewriterParameterizedTest {
    
    private static final String CASE_PATH = "scenario/sharding/case";
    
    public ShardingSQLRewriterParameterizedTest(final String type, final String name, final String fileName, final SQLRewriteEngineTestParameters testParameters) {
        super(testParameters);
    }
    
    @Parameters(name = "{0}: {1} -> {2}")
    public static Collection<Object[]> loadTestParameters() {
        return SQLRewriteEngineTestParametersBuilder.loadTestParameters(CASE_PATH.toUpperCase(), CASE_PATH, ShardingSQLRewriterParameterizedTest.class);
    }
    
    @Override
    protected Collection<SQLRewriteUnit> createSQLRewriteUnits() throws IOException {
        YamlRootRuleConfigurations yamlRootRuleConfigs = createYamlRootRuleConfigurations();
        String databaseType = null == getTestParameters().getDatabaseType() ? "SQL92" : getTestParameters().getDatabaseType();
        Collection<ShardingSphereRule> rules = ShardingSphereRulesBuilder.buildSchemaRules("schema_name", new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(
                yamlRootRuleConfigs.getRules()), DatabaseTypeRegistry.getTrunkDatabaseType(databaseType),
                new YamlDataSourceConfigurationSwapper().swapToDataSources(yamlRootRuleConfigs.getDataSources()));
        SQLStatementParserEngine sqlStatementParserEngine = new SQLStatementParserEngine(databaseType);
        ShardingSphereSchema schema = mockSchema();
        Map<String, ShardingSphereMetaData> metaDataMap = new HashMap<>();
        ShardingSphereMetaData metaData = new ShardingSphereMetaData("sharding_db", mock(ShardingSphereResource.class), new ShardingSphereRuleMetaData(Collections.emptyList(), rules), schema);
        metaDataMap.put(DefaultSchema.LOGIC_NAME, metaData);
        metaDataMap.put("sharding_db", metaData);
        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(metaDataMap, getTestParameters().getInputParameters(), 
                sqlStatementParserEngine.parse(getTestParameters().getInputSQL(), false), DefaultSchema.LOGIC_NAME);
        LogicSQL logicSQL = new LogicSQL(sqlStatementContext, getTestParameters().getInputSQL(), getTestParameters().getInputParameters());
        mockShardingSphereRuleSingleTable();
        ConfigurationProperties props = new ConfigurationProperties(yamlRootRuleConfigs.getProps());
        RouteContext routeContext = new SQLRouteEngine(rules, props).route(logicSQL, metaData);
        SQLRewriteResult sqlRewriteResult = new SQLRewriteEntry(
                schema, props, rules).rewrite(getTestParameters().getInputSQL(), getTestParameters().getInputParameters(), sqlStatementContext, routeContext);
        return sqlRewriteResult instanceof GenericSQLRewriteResult
                ? Collections.singletonList(((GenericSQLRewriteResult) sqlRewriteResult).getSqlRewriteUnit()) : (((RouteSQLRewriteResult) sqlRewriteResult).getSqlRewriteUnits()).values();
    }
        
    private void mockShardingSphereRuleSingleTable() {
        ShardingSphereEventBus.getInstance().post(new CreateTableEvent("db", "t_single", mock(TableMetaData.class)));
        ShardingSphereEventBus.getInstance().post(new CreateTableEvent("db", "t_single_extend", mock(TableMetaData.class)));
    }
    
    private YamlRootRuleConfigurations createYamlRootRuleConfigurations() throws IOException {
        URL url = ShardingSQLRewriterParameterizedTest.class.getClassLoader().getResource(getTestParameters().getRuleFile());
        Preconditions.checkNotNull(url, "Cannot found rewrite rule yaml configuration.");
        return YamlEngine.unmarshal(new File(url.getFile()), YamlRootRuleConfigurations.class);
    }
    
    private ShardingSphereSchema mockSchema() {
        ShardingSphereSchema result = mock(ShardingSphereSchema.class);
        when(result.getAllTableNames()).thenReturn(Arrays.asList("t_account", "t_account_detail"));
        TableMetaData accountTableMetaData = mock(TableMetaData.class);
        when(accountTableMetaData.getColumns()).thenReturn(createColumnMetaDataMap());
        Map<String, IndexMetaData> indexMetaDataMap = new HashMap<>(1, 1);
        indexMetaDataMap.put("index_name", new IndexMetaData("index_name"));
        when(accountTableMetaData.getIndexes()).thenReturn(indexMetaDataMap);
        when(result.containsTable("t_account")).thenReturn(true);
        when(result.get("t_account")).thenReturn(accountTableMetaData);
        when(result.get("t_account_detail")).thenReturn(mock(TableMetaData.class));
        when(result.getAllColumnNames("t_account")).thenReturn(Arrays.asList("account_id", "amount", "status"));
        return result;
    }
    
    private Map<String, ColumnMetaData> createColumnMetaDataMap() {
        Map<String, ColumnMetaData> result = new LinkedHashMap<>(3, 1);
        result.put("account_id", new ColumnMetaData("account_id", Types.INTEGER, true, true, false));
        result.put("amount", mock(ColumnMetaData.class));
        result.put("status", mock(ColumnMetaData.class));
        return result;
    }
}
