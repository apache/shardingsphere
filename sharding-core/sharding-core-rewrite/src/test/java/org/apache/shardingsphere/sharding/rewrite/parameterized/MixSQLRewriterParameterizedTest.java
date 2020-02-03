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

import org.apache.shardingsphere.core.yaml.constructor.YamlRootShardingConfigurationConstructor;
import org.apache.shardingsphere.sharding.route.engine.context.ShardingRouteContext;
import org.apache.shardingsphere.sharding.route.engine.ShardingRouter;
import org.apache.shardingsphere.underlying.route.context.RouteUnit;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.yaml.config.sharding.YamlRootShardingConfiguration;
import org.apache.shardingsphere.core.yaml.swapper.ShardingRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.encrypt.rewrite.context.EncryptSQLRewriteContextDecorator;
import org.apache.shardingsphere.sharding.rewrite.context.ShardingSQLRewriteContextDecorator;
import org.apache.shardingsphere.sharding.rewrite.engine.ShardingSQLRewriteEngine;
import org.apache.shardingsphere.sql.parser.SQLParseEngine;
import org.apache.shardingsphere.sql.parser.SQLParseEngineFactory;
import org.apache.shardingsphere.sql.parser.relation.metadata.RelationMetas;
import org.apache.shardingsphere.underlying.common.constant.properties.ShardingSphereProperties;
import org.apache.shardingsphere.underlying.common.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.underlying.common.metadata.column.ColumnMetaData;
import org.apache.shardingsphere.underlying.common.metadata.datasource.DataSourceMetas;
import org.apache.shardingsphere.underlying.common.metadata.table.TableMetaData;
import org.apache.shardingsphere.underlying.common.metadata.table.TableMetas;
import org.apache.shardingsphere.underlying.common.yaml.engine.YamlEngine;
import org.apache.shardingsphere.underlying.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.underlying.rewrite.engine.SQLRewriteResult;
import org.apache.shardingsphere.underlying.rewrite.parameterized.engine.AbstractSQLRewriterParameterizedTest;
import org.apache.shardingsphere.underlying.rewrite.parameterized.engine.parameter.SQLRewriteEngineTestParameters;
import org.apache.shardingsphere.underlying.rewrite.parameterized.engine.parameter.SQLRewriteEngineTestParametersBuilder;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Map;
import java.util.LinkedHashMap;

import static org.mockito.ArgumentMatchers.anyString;
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
    protected Collection<SQLRewriteResult> createSQLRewriteResults() throws IOException {
        YamlRootShardingConfiguration ruleConfiguration = createRuleConfiguration();
        ShardingRule shardingRule = new ShardingRule(new ShardingRuleConfigurationYamlSwapper().swap(ruleConfiguration.getShardingRule()), ruleConfiguration.getDataSources().keySet());
        SQLParseEngine parseEngine = SQLParseEngineFactory.getSQLParseEngine(null == getTestParameters().getDatabaseType() ? "SQL92" : getTestParameters().getDatabaseType());
        ShardingRouter shardingRouter = new ShardingRouter(shardingRule, new ShardingSphereProperties(new Properties()), createShardingSphereMetaData(), parseEngine);
        ShardingRouteContext shardingRouteContext = shardingRouter.route(getTestParameters().getInputSQL(), getTestParameters().getInputParameters(), false);
        ShardingSphereProperties properties = new ShardingSphereProperties(ruleConfiguration.getProps());
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(
                mock(RelationMetas.class), shardingRouteContext.getSqlStatementContext(), getTestParameters().getInputSQL(), getTestParameters().getInputParameters());
        new ShardingSQLRewriteContextDecorator(shardingRouteContext).decorate(shardingRule, properties, sqlRewriteContext);
        new EncryptSQLRewriteContextDecorator().decorate(shardingRule.getEncryptRule(), properties, sqlRewriteContext);
        sqlRewriteContext.generateSQLTokens();
        Collection<SQLRewriteResult> result = new LinkedList<>();
        for (RouteUnit each : shardingRouteContext.getRouteResult().getRouteUnits()) {
            result.add(new ShardingSQLRewriteEngine(shardingRule, shardingRouteContext.getShardingConditions(), each).rewrite(sqlRewriteContext));
        }
        return result;
    }
    
    private YamlRootShardingConfiguration createRuleConfiguration() throws IOException {
        URL url = MixSQLRewriterParameterizedTest.class.getClassLoader().getResource(getTestParameters().getRuleFile());
        Preconditions.checkNotNull(url, "Cannot found rewrite rule yaml configuration.");
        return YamlEngine.unmarshal(new File(url.getFile()), YamlRootShardingConfiguration.class, new YamlRootShardingConfigurationConstructor());
    }
    
    private ShardingSphereMetaData createShardingSphereMetaData() {
        TableMetas tableMetas = mock(TableMetas.class);
        when(tableMetas.getAllTableNames()).thenReturn(Arrays.asList("t_account", "t_account_bak", "t_account_detail"));
        TableMetaData accountTableMetaData = mock(TableMetaData.class);
        when(accountTableMetaData.getColumns()).thenReturn(createColumnMetaDataMap());
        when(accountTableMetaData.containsIndex(anyString())).thenReturn(true);
        when(tableMetas.get("t_account")).thenReturn(accountTableMetaData);
        TableMetaData accountBakTableMetaData = mock(TableMetaData.class);
        when(accountBakTableMetaData.getColumns()).thenReturn(createColumnMetaDataMap());
        when(tableMetas.get("t_account_bak")).thenReturn(accountBakTableMetaData);
        when(tableMetas.get("t_account_detail")).thenReturn(mock(TableMetaData.class));
        when(tableMetas.getAllColumnNames("t_account")).thenReturn(Arrays.asList("account_id", "password", "amount", "status"));
        when(tableMetas.getAllColumnNames("t_account_bak")).thenReturn(Arrays.asList("account_id", "password", "amount", "status"));
        return new ShardingSphereMetaData(mock(DataSourceMetas.class), tableMetas);
    }
    
    private Map<String, ColumnMetaData> createColumnMetaDataMap() {
        Map<String, ColumnMetaData> result = new LinkedHashMap<>();
        result.put("account_id", mock(ColumnMetaData.class));
        result.put("password", mock(ColumnMetaData.class));
        result.put("amount", mock(ColumnMetaData.class));
        result.put("status", mock(ColumnMetaData.class));
        return result;
    }
}
