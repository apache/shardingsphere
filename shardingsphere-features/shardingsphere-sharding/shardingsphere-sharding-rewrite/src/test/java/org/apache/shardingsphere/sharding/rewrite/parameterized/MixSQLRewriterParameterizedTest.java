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

package org.apache.shardingsphere.sharding.rewrite.parameterized;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.model.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.model.addressing.TableAddressingMetaData;
import org.apache.shardingsphere.infra.metadata.model.datasource.CachedDatabaseMetaData;
import org.apache.shardingsphere.infra.metadata.model.datasource.DataSourcesMetaData;
import org.apache.shardingsphere.infra.metadata.model.logic.LogicSchemaMetaData;
import org.apache.shardingsphere.infra.metadata.model.physical.model.column.PhysicalColumnMetaData;
import org.apache.shardingsphere.infra.metadata.model.physical.model.index.PhysicalIndexMetaData;
import org.apache.shardingsphere.infra.metadata.model.physical.model.schema.PhysicalSchemaMetaData;
import org.apache.shardingsphere.infra.metadata.model.physical.model.table.PhysicalTableMetaData;
import org.apache.shardingsphere.infra.rewrite.SQLRewriteEntry;
import org.apache.shardingsphere.infra.rewrite.engine.result.GenericSQLRewriteResult;
import org.apache.shardingsphere.infra.rewrite.engine.result.RouteSQLRewriteResult;
import org.apache.shardingsphere.infra.rewrite.engine.result.SQLRewriteResult;
import org.apache.shardingsphere.infra.rewrite.engine.result.SQLRewriteUnit;
import org.apache.shardingsphere.infra.rewrite.parameterized.engine.AbstractSQLRewriterParameterizedTest;
import org.apache.shardingsphere.infra.rewrite.parameterized.engine.parameter.SQLRewriteEngineTestParameters;
import org.apache.shardingsphere.infra.rewrite.parameterized.engine.parameter.SQLRewriteEngineTestParametersBuilder;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.engine.SQLRouteEngine;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.ShardingSphereRulesBuilder;
import org.apache.shardingsphere.infra.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.yaml.config.YamlRootRuleConfigurations;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.sql.parser.engine.SQLParserEngineFactory;
import org.apache.shardingsphere.sql.parser.engine.StandardSQLParserEngine;
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

public final class MixSQLRewriterParameterizedTest extends AbstractSQLRewriterParameterizedTest {
    
    private static final String PATH = "mix";
    
    public MixSQLRewriterParameterizedTest(final String type, final String name, final String fileName, final SQLRewriteEngineTestParameters testParameters) {
        super(testParameters);
    }
    
    @Parameters(name = "{0}: {1} -> {2}")
    public static Collection<Object[]> loadTestParameters() {
        return SQLRewriteEngineTestParametersBuilder.loadTestParameters(PATH.toUpperCase(), PATH, MixSQLRewriterParameterizedTest.class);
    }
    
    @Override
    protected Collection<SQLRewriteUnit> createSQLRewriteUnits() throws IOException {
        YamlRootRuleConfigurations ruleConfigurations = createRuleConfigurations();
        Collection<ShardingSphereRule> rules = ShardingSphereRulesBuilder.build(
                new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(ruleConfigurations.getRules()), ruleConfigurations.getDataSources().keySet());
        StandardSQLParserEngine standardSqlParserEngine = SQLParserEngineFactory.getSQLParserEngine(null == getTestParameters().getDatabaseType() ? "SQL92" : getTestParameters().getDatabaseType());
        ShardingSphereMetaData metaData = createShardingSphereMetaData();
        ConfigurationProperties props = new ConfigurationProperties(ruleConfigurations.getProps());
        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(
                metaData.getSchemaMetaData().getConfiguredSchemaMetaData(), getTestParameters().getInputParameters(), standardSqlParserEngine.parse(getTestParameters().getInputSQL(), false));
        LogicSQL logicSQL = new LogicSQL(sqlStatementContext, getTestParameters().getInputSQL(), getTestParameters().getInputParameters());
        ShardingSphereSchema schema = new ShardingSphereSchema("sharding_db", Collections.emptyList(), rules, Collections.emptyMap(), metaData);
        RouteContext routeContext = new SQLRouteEngine(props, rules).route(logicSQL, schema);
        SQLRewriteResult sqlRewriteResult = new SQLRewriteEntry(metaData.getSchemaMetaData().getConfiguredSchemaMetaData(),
                props, rules).rewrite(getTestParameters().getInputSQL(), getTestParameters().getInputParameters(), sqlStatementContext, routeContext);
        return sqlRewriteResult instanceof GenericSQLRewriteResult
                ? Collections.singletonList(((GenericSQLRewriteResult) sqlRewriteResult).getSqlRewriteUnit()) : (((RouteSQLRewriteResult) sqlRewriteResult).getSqlRewriteUnits()).values();
    }
    
    private YamlRootRuleConfigurations createRuleConfigurations() throws IOException {
        URL url = MixSQLRewriterParameterizedTest.class.getClassLoader().getResource(getTestParameters().getRuleFile());
        Preconditions.checkNotNull(url, "Cannot found rewrite rule yaml configurations.");
        return YamlEngine.unmarshal(new File(url.getFile()), YamlRootRuleConfigurations.class);
    }
    
    private ShardingSphereMetaData createShardingSphereMetaData() {
        PhysicalSchemaMetaData schemaMetaData = mock(PhysicalSchemaMetaData.class);
        when(schemaMetaData.getAllTableNames()).thenReturn(Arrays.asList("t_account", "t_account_bak", "t_account_detail"));
        PhysicalTableMetaData accountTableMetaData = mock(PhysicalTableMetaData.class);
        when(accountTableMetaData.getColumns()).thenReturn(createColumnMetaDataMap());
        Map<String, PhysicalIndexMetaData> indexMetaDataMap = new HashMap<>(1, 1);
        indexMetaDataMap.put("index_name", new PhysicalIndexMetaData("index_name"));
        when(accountTableMetaData.getIndexes()).thenReturn(indexMetaDataMap);
        when(schemaMetaData.containsTable("t_account")).thenReturn(true);
        when(schemaMetaData.get("t_account")).thenReturn(accountTableMetaData);
        PhysicalTableMetaData accountBakTableMetaData = mock(PhysicalTableMetaData.class);
        when(accountBakTableMetaData.getColumns()).thenReturn(createColumnMetaDataMap());
        when(schemaMetaData.containsTable("t_account_bak")).thenReturn(true);
        when(schemaMetaData.get("t_account_bak")).thenReturn(accountBakTableMetaData);
        when(schemaMetaData.get("t_account_detail")).thenReturn(mock(PhysicalTableMetaData.class));
        when(schemaMetaData.getAllColumnNames("t_account")).thenReturn(Arrays.asList("account_id", "password", "amount", "status"));
        when(schemaMetaData.getAllColumnNames("t_account_bak")).thenReturn(Arrays.asList("account_id", "password", "amount", "status"));
        LogicSchemaMetaData logicSchemaMetaData = mock(LogicSchemaMetaData.class);
        when(logicSchemaMetaData.getConfiguredSchemaMetaData()).thenReturn(schemaMetaData);
        when(logicSchemaMetaData.getSchemaMetaData()).thenReturn(schemaMetaData);
        return new ShardingSphereMetaData(mock(DataSourcesMetaData.class), logicSchemaMetaData, mock(TableAddressingMetaData.class), mock(CachedDatabaseMetaData.class));
    }
    
    private Map<String, PhysicalColumnMetaData> createColumnMetaDataMap() {
        Map<String, PhysicalColumnMetaData> result = new LinkedHashMap<>();
        result.put("account_id", new PhysicalColumnMetaData("account_id", Types.INTEGER, "INT", true, true, false));
        result.put("password", mock(PhysicalColumnMetaData.class));
        result.put("amount", mock(PhysicalColumnMetaData.class));
        result.put("status", mock(PhysicalColumnMetaData.class));
        return result;
    }
}
