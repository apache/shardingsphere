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
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.yaml.config.sharding.YamlRootShardingConfiguration;
import org.apache.shardingsphere.core.yaml.constructor.YamlRootShardingConfigurationConstructor;
import org.apache.shardingsphere.core.yaml.swapper.ShardingRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.encrypt.rewrite.context.EncryptSQLRewriteContextDecorator;
import org.apache.shardingsphere.sharding.rewrite.context.ShardingSQLRewriteContextDecorator;
import org.apache.shardingsphere.underlying.rewrite.engine.SQLRouteRewriteEngine;
import org.apache.shardingsphere.sharding.route.engine.ShardingRouteDecorator;
import org.apache.shardingsphere.sql.parser.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.SQLParserEngineFactory;
import org.apache.shardingsphere.sql.parser.binder.metadata.column.ColumnMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.index.IndexMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.underlying.common.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.underlying.common.metadata.datasource.DataSourceMetas;
import org.apache.shardingsphere.underlying.common.yaml.engine.YamlEngine;
import org.apache.shardingsphere.underlying.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.underlying.rewrite.engine.SQLRewriteResult;
import org.apache.shardingsphere.underlying.rewrite.parameterized.engine.AbstractSQLRewriterParameterizedTest;
import org.apache.shardingsphere.underlying.rewrite.parameterized.engine.parameter.SQLRewriteEngineTestParameters;
import org.apache.shardingsphere.underlying.rewrite.parameterized.engine.parameter.SQLRewriteEngineTestParametersBuilder;
import org.apache.shardingsphere.underlying.route.DataNodeRouter;
import org.apache.shardingsphere.underlying.route.context.RouteContext;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
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
    protected Collection<SQLRewriteResult> createSQLRewriteResults() throws IOException {
        YamlRootShardingConfiguration ruleConfiguration = createRuleConfiguration();
        ShardingRule shardingRule = new ShardingRule(new ShardingRuleConfigurationYamlSwapper().swap(ruleConfiguration.getShardingRule()), ruleConfiguration.getDataSources().keySet());
        SQLParserEngine sqlParserEngine = SQLParserEngineFactory.getSQLParserEngine(null == getTestParameters().getDatabaseType() ? "SQL92" : getTestParameters().getDatabaseType());
        ShardingSphereMetaData metaData = createShardingSphereMetaData();
        ConfigurationProperties properties = new ConfigurationProperties(ruleConfiguration.getProps());
        RouteContext routeContext = new DataNodeRouter(metaData, properties, sqlParserEngine).route(getTestParameters().getInputSQL(), getTestParameters().getInputParameters(), false);
        ShardingRouteDecorator shardingRouteDecorator = new ShardingRouteDecorator();
        routeContext = shardingRouteDecorator.decorate(routeContext, metaData, shardingRule, properties);
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(
                mock(SchemaMetaData.class), routeContext.getSqlStatementContext(), getTestParameters().getInputSQL(), getTestParameters().getInputParameters());
        ShardingSQLRewriteContextDecorator shardingSQLRewriteContextDecorator = new ShardingSQLRewriteContextDecorator();
        shardingSQLRewriteContextDecorator.setRouteContext(routeContext);
        shardingSQLRewriteContextDecorator.decorate(shardingRule, properties, sqlRewriteContext);
        new EncryptSQLRewriteContextDecorator().decorate(shardingRule.getEncryptRule(), properties, sqlRewriteContext);
        sqlRewriteContext.generateSQLTokens();
        return new SQLRouteRewriteEngine().rewrite(sqlRewriteContext, routeContext.getRouteResult()).values();
    }
    
    private YamlRootShardingConfiguration createRuleConfiguration() throws IOException {
        URL url = MixSQLRewriterParameterizedTest.class.getClassLoader().getResource(getTestParameters().getRuleFile());
        Preconditions.checkNotNull(url, "Cannot found rewrite rule yaml configuration.");
        return YamlEngine.unmarshal(new File(url.getFile()), YamlRootShardingConfiguration.class, new YamlRootShardingConfigurationConstructor());
    }
    
    private ShardingSphereMetaData createShardingSphereMetaData() {
        SchemaMetaData schemaMetaData = mock(SchemaMetaData.class);
        when(schemaMetaData.getAllTableNames()).thenReturn(Arrays.asList("t_account", "t_account_bak", "t_account_detail"));
        TableMetaData accountTableMetaData = mock(TableMetaData.class);
        when(accountTableMetaData.getColumns()).thenReturn(createColumnMetaDataMap());
        Map<String, IndexMetaData> indexMetaDataMap = new HashMap<>(1, 1);
        indexMetaDataMap.put("index_name", new IndexMetaData("index_name"));
        when(accountTableMetaData.getIndexes()).thenReturn(indexMetaDataMap);
        when(schemaMetaData.containsTable("t_account")).thenReturn(true);
        when(schemaMetaData.get("t_account")).thenReturn(accountTableMetaData);
        TableMetaData accountBakTableMetaData = mock(TableMetaData.class);
        when(accountBakTableMetaData.getColumns()).thenReturn(createColumnMetaDataMap());
        when(schemaMetaData.containsTable("t_account_bak")).thenReturn(true);
        when(schemaMetaData.get("t_account_bak")).thenReturn(accountBakTableMetaData);
        when(schemaMetaData.get("t_account_detail")).thenReturn(mock(TableMetaData.class));
        when(schemaMetaData.getAllColumnNames("t_account")).thenReturn(Arrays.asList("account_id", "password", "amount", "status"));
        when(schemaMetaData.getAllColumnNames("t_account_bak")).thenReturn(Arrays.asList("account_id", "password", "amount", "status"));
        return new ShardingSphereMetaData(mock(DataSourceMetas.class), schemaMetaData);
    }
    
    private Map<String, ColumnMetaData> createColumnMetaDataMap() {
        Map<String, ColumnMetaData> result = new LinkedHashMap<>();
        result.put("account_id", new ColumnMetaData("account_id", Types.INTEGER, "INT", true, true, false));
        result.put("password", mock(ColumnMetaData.class));
        result.put("amount", mock(ColumnMetaData.class));
        result.put("status", mock(ColumnMetaData.class));
        return result;
    }
}
