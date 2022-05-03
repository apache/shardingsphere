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

package org.apache.shardingsphere.sharding.rewrite.parameterized.engine;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.aware.ParameterAware;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.parser.sql.SQLStatementParserEngine;
import org.apache.shardingsphere.infra.rewrite.SQLRewriteEntry;
import org.apache.shardingsphere.infra.rewrite.engine.result.GenericSQLRewriteResult;
import org.apache.shardingsphere.infra.rewrite.engine.result.RouteSQLRewriteResult;
import org.apache.shardingsphere.infra.rewrite.engine.result.SQLRewriteResult;
import org.apache.shardingsphere.infra.rewrite.engine.result.SQLRewriteUnit;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.engine.SQLRouteEngine;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.schema.SchemaRulesBuilder;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.sharding.rewrite.parameterized.engine.parameter.SQLRewriteEngineTestParameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
@Getter
public abstract class AbstractSQLRewriterParameterizedTest {
    
    private final SQLRewriteEngineTestParameters testParameters;
    
    private final SQLParserRule sqlParserRule = new SQLParserRule(new SQLParserRuleConfiguration(true,
            DefaultSQLParserRuleConfigurationBuilder.PARSE_TREE_CACHE_OPTION,
            DefaultSQLParserRuleConfigurationBuilder.SQL_STATEMENT_CACHE_OPTION));
    
    @Test
    public final void assertRewrite() throws IOException, SQLException {
        Collection<SQLRewriteUnit> actual = createSQLRewriteUnits();
        assertThat(actual.size(), is(testParameters.getOutputSQLs().size()));
        int count = 0;
        for (SQLRewriteUnit each : actual) {
            assertThat(each.getSql(), is(testParameters.getOutputSQLs().get(count)));
            assertThat(each.getParameters().size(), is(testParameters.getOutputGroupedParameters().get(count).size()));
            for (int i = 0; i < each.getParameters().size(); i++) {
                assertThat(each.getParameters().get(i).toString(), is(testParameters.getOutputGroupedParameters().get(count).get(i)));
            }
            count++;
        }
    }
    
    private Collection<SQLRewriteUnit> createSQLRewriteUnits() throws IOException, SQLException {
        YamlRootConfiguration rootConfig = createRootConfiguration();
        DatabaseConfiguration databaseConfig = new DataSourceProvidedDatabaseConfiguration(
                new YamlDataSourceConfigurationSwapper().swapToDataSources(rootConfig.getDataSources()), new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(rootConfig.getRules()));
        mockDataSource(databaseConfig.getDataSources());
        Collection<ShardingSphereRule> rules = SchemaRulesBuilder.buildRules("schema_name", databaseConfig, new ConfigurationProperties(new Properties()));
        mockRules(rules);
        rules.add(sqlParserRule);
        SQLStatementParserEngine sqlStatementParserEngine = new SQLStatementParserEngine(getTestParameters().getDatabaseType(),
                sqlParserRule.getSqlStatementCache(), sqlParserRule.getParseTreeCache(), sqlParserRule.isSqlCommentParseEnabled());
        ShardingSphereResource resource = mock(ShardingSphereResource.class);
        DatabaseType databaseType = DatabaseTypeRegistry.getActualDatabaseType(getTestParameters().getDatabaseType());
        when(resource.getDatabaseType()).thenReturn(databaseType);
        String databaseName = databaseType.getDefaultSchema(DefaultSchema.LOGIC_NAME);
        Map<String, ShardingSphereSchema> schemas = mockSchemas(databaseName);
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(databaseName, resource, new ShardingSphereRuleMetaData(Collections.emptyList(), rules), schemas);
        Map<String, ShardingSphereMetaData> metaDataMap = new HashMap<>(2, 1);
        metaDataMap.put(databaseName, metaData);
        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(metaDataMap,
                sqlStatementParserEngine.parse(getTestParameters().getInputSQL(), false), databaseName);
        if (sqlStatementContext instanceof ParameterAware) {
            ((ParameterAware) sqlStatementContext).setUpParameters(getTestParameters().getInputParameters());
        }
        LogicSQL logicSQL = new LogicSQL(sqlStatementContext, getTestParameters().getInputSQL(), getTestParameters().getInputParameters());
        ConfigurationProperties props = new ConfigurationProperties(rootConfig.getProps());
        RouteContext routeContext = new SQLRouteEngine(rules, props).route(logicSQL, metaData);
        SQLRewriteEntry sqlRewriteEntry = new SQLRewriteEntry(databaseName, schemas, props, rules);
        SQLRewriteResult sqlRewriteResult = sqlRewriteEntry.rewrite(getTestParameters().getInputSQL(), getTestParameters().getInputParameters(), sqlStatementContext, routeContext);
        return sqlRewriteResult instanceof GenericSQLRewriteResult
                ? Collections.singletonList(((GenericSQLRewriteResult) sqlRewriteResult).getSqlRewriteUnit())
                : (((RouteSQLRewriteResult) sqlRewriteResult).getSqlRewriteUnits()).values();
    }
    
    protected abstract void mockDataSource(Map<String, DataSource> dataSources) throws SQLException;
    
    protected abstract YamlRootConfiguration createRootConfiguration() throws IOException;
    
    protected abstract Map<String, ShardingSphereSchema> mockSchemas(String databaseName);
    
    protected abstract void mockRules(Collection<ShardingSphereRule> rules);
}
