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

package org.apache.shardingsphere.core.rewrite.parameterized.engine;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.core.metadata.column.ColumnMetaData;
import org.apache.shardingsphere.core.metadata.datasource.DataSourceMetas;
import org.apache.shardingsphere.core.metadata.table.TableMetaData;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.core.rewrite.engine.SQLRewriteResult;
import org.apache.shardingsphere.core.rewrite.feature.sharding.context.ShardingSQLRewriteContextDecorator;
import org.apache.shardingsphere.core.rewrite.feature.sharding.engine.ShardingSQLRewriteEngine;
import org.apache.shardingsphere.core.rewrite.parameterized.jaxb.entity.RewriteAssertionEntity;
import org.apache.shardingsphere.core.rewrite.parameterized.jaxb.entity.RewriteAssertionsRootEntity;
import org.apache.shardingsphere.core.rewrite.parameterized.jaxb.entity.RewriteOutputEntity;
import org.apache.shardingsphere.core.rewrite.parameterized.jaxb.loader.EncryptRewriteAssertionsRootEntityLoader;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.router.sharding.ShardingRouter;
import org.apache.shardingsphere.core.route.type.RoutingUnit;
import org.apache.shardingsphere.core.route.type.TableUnit;
import org.apache.shardingsphere.core.rule.BindingTableRule;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.yaml.config.sharding.YamlRootShardingConfiguration;
import org.apache.shardingsphere.core.yaml.engine.YamlEngine;
import org.apache.shardingsphere.core.yaml.swapper.impl.ShardingRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.sql.parser.SQLParseEngine;
import org.apache.shardingsphere.sql.parser.SQLParseEngineFactory;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public final class ShardingSQLRewriteEngineParameterizedTest {
    
    private static final String PATH = "sharding";
    
    private final String fileName;
    
    private final String ruleFile;
    
    private final String name;
    
    private final String inputSQL;
    
    private final List<Object> inputParameters;
    
    private final List<String> outputSQLs;
    
    private final List<List<Object>> outputGroupedParameters;
    
    private final String databaseType;
    
    @Parameters(name = "SHARDING: {2} -> {0}")
    public static Collection<Object[]> getTestParameters() {
        Collection<Object[]> result = new LinkedList<>();
        for (Entry<String, RewriteAssertionsRootEntity> entry : getAllRewriteAssertionsRootEntities().entrySet()) {
            result.addAll(getTestParameters(entry.getKey(), entry.getValue()));
        }
        return result;
    }
    
    private static Collection<Object[]> getTestParameters(final String fileName, final RewriteAssertionsRootEntity rootAssertions) {
        Collection<Object[]> result = new LinkedList<>();
        for (RewriteAssertionEntity each : rootAssertions.getAssertions()) {
            result.add(getTestParameter(fileName, rootAssertions, each));
        }
        return result;
    }
    
    private static Object[] getTestParameter(final String fileName, final RewriteAssertionsRootEntity rootAssertions, final RewriteAssertionEntity assertion) {
        Object[] result = new Object[8];
        result[0] = fileName;
        result[1] = rootAssertions.getYamlRule();
        result[2] = assertion.getId();
        result[3] = assertion.getInput().getSql();
        if (null == assertion.getInput().getParameters()) {
            result[4] = Collections.emptyList();
        } else {
            result[4] = Lists.transform(Splitter.on(",").trimResults().splitToList(assertion.getInput().getParameters()), new Function<String, Object>() {
                
                @Override
                public Object apply(final String input) {
                    Object result = Ints.tryParse(input);
                    return result == null ? input : result;
                }
            });
        }
        List<RewriteOutputEntity> outputs = assertion.getOutputs();
        List<String> outputSQLs = new ArrayList<>(outputs.size());
        List<Object> outputGroupedParameters = new ArrayList<>(outputs.size());
        for (RewriteOutputEntity each : outputs) {
            outputSQLs.add(each.getSql());
            outputGroupedParameters.add(null == each.getParameters() ? Collections.emptyList() : Splitter.on(",").trimResults().splitToList(each.getParameters()));
        }
        result[5] = outputSQLs;
        result[6] = outputGroupedParameters;
        result[7] = assertion.getDatabaseType();
        return result;
    }
    
    private static Map<String, RewriteAssertionsRootEntity> getAllRewriteAssertionsRootEntities() {
        Map<String, RewriteAssertionsRootEntity> result = new LinkedHashMap<>();
        File file = new File(ShardingSQLRewriteEngineParameterizedTest.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "/" + PATH);
        for (File each : Objects.requireNonNull(file.listFiles())) {
            if (each.getName().endsWith(".xml")) {
                result.put(each.getName(), new EncryptRewriteAssertionsRootEntityLoader().load(PATH + "/" + each.getName()));
            }
        }
        return result;
    }
    
    @Test
    public void assertRewrite() throws IOException {
        Collection<SQLRewriteResult> actual = getSQLRewriteResults();
        assertThat(actual.size(), is(outputSQLs.size()));
        int count = 0;
        for (SQLRewriteResult each : actual) {
            assertThat(each.getSql(), is(outputSQLs.get(count)));
            assertThat(each.getParameters().size(), is(outputGroupedParameters.get(count).size()));
            for (int i = 0; i < each.getParameters().size(); i++) {
                assertThat(each.getParameters().get(i).toString(), is(outputGroupedParameters.get(count).get(i).toString()));
            }
            count++;
        }
    }
    
    private Collection<SQLRewriteResult> getSQLRewriteResults() throws IOException {
        YamlRootShardingConfiguration ruleConfiguration = createRuleConfiguration();
        ShardingRule shardingRule = new ShardingRule(new ShardingRuleConfigurationYamlSwapper().swap(ruleConfiguration.getShardingRule()), ruleConfiguration.getDataSources().keySet());
        SQLParseEngine parseEngine = SQLParseEngineFactory.getSQLParseEngine(null == databaseType ? "SQL92" : databaseType);
        ShardingRouter shardingRouter = new ShardingRouter(shardingRule, createShardingSphereMetaData(), parseEngine);
        SQLStatement sqlStatement = shardingRouter.parse(inputSQL, false);
        SQLRouteResult sqlRouteResult = shardingRouter.route(inputSQL, inputParameters, sqlStatement);
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(mock(TableMetas.class), sqlRouteResult.getSqlStatementContext(), inputSQL, inputParameters);
        new ShardingSQLRewriteContextDecorator(shardingRule, sqlRouteResult).decorate(sqlRewriteContext);
        sqlRewriteContext.generateSQLTokens();
        Collection<SQLRewriteResult> result = new LinkedList<>();
        for (RoutingUnit each : sqlRouteResult.getRoutingResult().getRoutingUnits()) {
            result.add(new ShardingSQLRewriteEngine(sqlRouteResult.getShardingConditions(), 
                    each, getLogicAndActualTables(shardingRule, each, sqlRouteResult.getSqlStatementContext().getTablesContext().getTableNames())).rewrite(sqlRewriteContext));
        }
        return result;
    }
    
    private YamlRootShardingConfiguration createRuleConfiguration() throws IOException {
        URL url = ShardingSQLRewriteEngineParameterizedTest.class.getClassLoader().getResource(ruleFile);
        Preconditions.checkNotNull(url, "Cannot found rewrite rule yaml configuration.");
        return YamlEngine.unmarshal(new File(url.getFile()), YamlRootShardingConfiguration.class);
    }
    
    private ShardingSphereMetaData createShardingSphereMetaData() {
        TableMetas tableMetas = mock(TableMetas.class);
        when(tableMetas.getAllTableNames()).thenReturn(Arrays.asList("t_account", "t_account_detail"));
        TableMetaData accountTableMetaData = mock(TableMetaData.class);
        when(accountTableMetaData.getColumns()).thenReturn(createColumnMetaDataMap());
        when(accountTableMetaData.containsIndex(anyString())).thenReturn(true);
        when(tableMetas.get("t_account")).thenReturn(accountTableMetaData);
        when(tableMetas.get("t_account_detail")).thenReturn(mock(TableMetaData.class));
        when(tableMetas.getAllColumnNames("t_account")).thenReturn(Arrays.asList("account_id", "amount", "status"));
        return new ShardingSphereMetaData(mock(DataSourceMetas.class), tableMetas);
    }
    
    private Map<String, ColumnMetaData> createColumnMetaDataMap() {
        Map<String, ColumnMetaData> result = new LinkedHashMap<>();
        result.put("account_id", mock(ColumnMetaData.class));
        result.put("amount", mock(ColumnMetaData.class));
        result.put("status", mock(ColumnMetaData.class));
        return result;
    }
    
    private Map<String, String> getLogicAndActualTables(final ShardingRule shardingRule, final RoutingUnit routingUnit, final Collection<String> parsedTableNames) {
        Map<String, String> result = new HashMap<>();
        for (TableUnit each : routingUnit.getTableUnits()) {
            String logicTableName = each.getLogicTableName().toLowerCase();
            result.put(logicTableName, each.getActualTableName());
            result.putAll(getLogicAndActualTablesFromBindingTable(shardingRule, routingUnit.getMasterSlaveLogicDataSourceName(), each, parsedTableNames));
        }
        return result;
    }
    
    private Map<String, String> getLogicAndActualTablesFromBindingTable(final ShardingRule shardingRule, 
                                                                        final String dataSourceName, final TableUnit tableUnit, final Collection<String> parsedTableNames) {
        Map<String, String> result = new LinkedHashMap<>();
        Optional<BindingTableRule> bindingTableRule = shardingRule.findBindingTableRule(tableUnit.getLogicTableName());
        if (bindingTableRule.isPresent()) {
            result.putAll(getLogicAndActualTablesFromBindingTable(dataSourceName, tableUnit, parsedTableNames, bindingTableRule.get()));
        }
        return result;
    }
    
    private Map<String, String> getLogicAndActualTablesFromBindingTable(
            final String dataSourceName, final TableUnit tableUnit, final Collection<String> parsedTableNames, final BindingTableRule bindingTableRule) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String each : parsedTableNames) {
            String tableName = each.toLowerCase();
            if (!tableName.equals(tableUnit.getLogicTableName().toLowerCase()) && bindingTableRule.hasLogicTable(tableName)) {
                result.put(tableName, bindingTableRule.getBindingActualTable(dataSourceName, tableName, tableUnit.getActualTableName()));
            }
        }
        return result;
    }
}
