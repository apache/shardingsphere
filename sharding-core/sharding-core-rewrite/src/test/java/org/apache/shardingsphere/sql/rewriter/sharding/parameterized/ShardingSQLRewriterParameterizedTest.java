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

package org.apache.shardingsphere.sql.rewriter.sharding.parameterized;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.core.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.core.metadata.column.ColumnMetaData;
import org.apache.shardingsphere.core.metadata.datasource.DataSourceMetas;
import org.apache.shardingsphere.core.metadata.table.TableMetaData;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.router.sharding.ShardingRouter;
import org.apache.shardingsphere.core.route.type.RoutingUnit;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.yaml.config.sharding.YamlRootShardingConfiguration;
import org.apache.shardingsphere.core.yaml.engine.YamlEngine;
import org.apache.shardingsphere.core.yaml.swapper.impl.ShardingRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.sql.parser.SQLParseEngine;
import org.apache.shardingsphere.sql.parser.SQLParseEngineFactory;
import org.apache.shardingsphere.sql.parser.relation.metadata.RelationMetas;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.sql.rewriter.context.SQLRewriteContext;
import org.apache.shardingsphere.sql.rewriter.engine.SQLRewriteResult;
import org.apache.shardingsphere.sql.rewriter.parameterized.engine.AbstractSQLRewriterParameterizedTest;
import org.apache.shardingsphere.sql.rewriter.sharding.context.ShardingSQLRewriteContextDecorator;
import org.apache.shardingsphere.sql.rewriter.sharding.engine.ShardingSQLRewriteEngine;
import org.apache.shardingsphere.sql.rewriter.parameterized.engine.parameter.SQLRewriteEngineTestParameters;
import org.apache.shardingsphere.sql.rewriter.parameterized.engine.parameter.SQLRewriteEngineTestParametersBuilder;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingSQLRewriterParameterizedTest extends AbstractSQLRewriterParameterizedTest {
    
    private static final String PATH = "sharding";
    
    public ShardingSQLRewriterParameterizedTest(final String type, final String name, final String fileName, final SQLRewriteEngineTestParameters testParameters) {
        super(testParameters);
    }
    
    @Parameters(name = "{0}: {1} -> {2}")
    public static Collection<Object[]> loadTestParameters() {
        return SQLRewriteEngineTestParametersBuilder.loadTestParameters(PATH.toUpperCase(), PATH, ShardingSQLRewriterParameterizedTest.class);
    }
    
    @Override
    protected Collection<SQLRewriteResult> createSQLRewriteResults() throws IOException {
        YamlRootShardingConfiguration ruleConfiguration = createRuleConfiguration();
        ShardingRule shardingRule = new ShardingRule(new ShardingRuleConfigurationYamlSwapper().swap(ruleConfiguration.getShardingRule()), ruleConfiguration.getDataSources().keySet());
        SQLParseEngine parseEngine = SQLParseEngineFactory.getSQLParseEngine(null == getTestParameters().getDatabaseType() ? "SQL92" : getTestParameters().getDatabaseType());
        ShardingRouter shardingRouter = new ShardingRouter(shardingRule, createShardingSphereMetaData(), parseEngine);
        SQLStatement sqlStatement = shardingRouter.parse(getTestParameters().getInputSQL(), false);
        SQLRouteResult sqlRouteResult = shardingRouter.route(getTestParameters().getInputSQL(), getTestParameters().getInputParameters(), sqlStatement);
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(
                mock(RelationMetas.class), sqlRouteResult.getSqlStatementContext(), getTestParameters().getInputSQL(), getTestParameters().getInputParameters());
        new ShardingSQLRewriteContextDecorator(shardingRule, sqlRouteResult).decorate(sqlRewriteContext);
        sqlRewriteContext.generateSQLTokens();
        Collection<SQLRewriteResult> result = new LinkedList<>();
        for (RoutingUnit each : sqlRouteResult.getRoutingResult().getRoutingUnits()) {
            result.add(new ShardingSQLRewriteEngine(shardingRule, sqlRouteResult.getShardingConditions(), each).rewrite(sqlRewriteContext));
        }
        return result;
    }
    
    private YamlRootShardingConfiguration createRuleConfiguration() throws IOException {
        URL url = ShardingSQLRewriterParameterizedTest.class.getClassLoader().getResource(getTestParameters().getRuleFile());
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
}
