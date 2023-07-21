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

package org.apache.shardingsphere.test.it.rewrite.engine;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.aware.CursorDefinitionAware;
import org.apache.shardingsphere.infra.binder.aware.ParameterAware;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CursorStatementContext;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.parser.sql.SQLStatementParserEngine;
import org.apache.shardingsphere.infra.rewrite.SQLRewriteEntry;
import org.apache.shardingsphere.infra.rewrite.engine.result.GenericSQLRewriteResult;
import org.apache.shardingsphere.infra.rewrite.engine.result.RouteSQLRewriteResult;
import org.apache.shardingsphere.infra.rewrite.engine.result.SQLRewriteResult;
import org.apache.shardingsphere.infra.rewrite.engine.result.SQLRewriteUnit;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.engine.SQLRouteEngine;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.database.DatabaseRulesBuilder;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.connection.cursor.CursorConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sqlfederation.api.config.SQLFederationRuleConfiguration;
import org.apache.shardingsphere.sqlfederation.rule.SQLFederationRule;
import org.apache.shardingsphere.sqltranslator.api.config.SQLTranslatorRuleConfiguration;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.apache.shardingsphere.test.it.rewrite.engine.parameter.SQLRewriteEngineTestParameters;
import org.apache.shardingsphere.test.it.rewrite.engine.parameter.SQLRewriteEngineTestParametersBuilder;
import org.apache.shardingsphere.timeservice.api.config.TimestampServiceRuleConfiguration;
import org.apache.shardingsphere.timeservice.core.rule.TimestampServiceRule;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class SQLRewriterIT {
    
    private final SQLParserRule sqlParserRule = new SQLParserRule(new SQLParserRuleConfiguration(true,
            DefaultSQLParserRuleConfigurationBuilder.PARSE_TREE_CACHE_OPTION, DefaultSQLParserRuleConfigurationBuilder.SQL_STATEMENT_CACHE_OPTION));
    
    private final TimestampServiceRule timestampServiceRule = new TimestampServiceRule(new TimestampServiceRuleConfiguration("System", new Properties()));
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertRewrite(final SQLRewriteEngineTestParameters testParams) throws IOException, SQLException {
        Collection<SQLRewriteUnit> actual = createSQLRewriteUnits(testParams);
        assertThat(actual.size(), is(testParams.getOutputSQLs().size()));
        int count = 0;
        for (SQLRewriteUnit each : actual) {
            assertThat(each.getSql(), is(testParams.getOutputSQLs().get(count)));
            assertThat(each.getParameters().size(), is(testParams.getOutputGroupedParameters().get(count).size()));
            for (int i = 0; i < each.getParameters().size(); i++) {
                assertThat(String.valueOf(each.getParameters().get(i)), is(String.valueOf(testParams.getOutputGroupedParameters().get(count).get(i))));
            }
            count++;
        }
    }
    
    private Collection<SQLRewriteUnit> createSQLRewriteUnits(final SQLRewriteEngineTestParameters testParams) throws IOException, SQLException {
        YamlRootConfiguration rootConfig = createRootConfiguration(testParams);
        DatabaseConfiguration databaseConfig = new DataSourceProvidedDatabaseConfiguration(
                new YamlDataSourceConfigurationSwapper().swapToDataSources(rootConfig.getDataSources()), new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(rootConfig.getRules()));
        mockDataSource(databaseConfig.getDataSources());
        ShardingSphereResourceMetaData resourceMetaData = mock(ShardingSphereResourceMetaData.class);
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, testParams.getDatabaseType());
        Map<String, DatabaseType> storageTypes = createStorageTypes(databaseConfig, databaseType);
        when(resourceMetaData.getStorageTypes()).thenReturn(storageTypes);
        String schemaName = DatabaseTypeEngine.getDefaultSchemaName(databaseType, DefaultDatabase.LOGIC_NAME);
        SQLStatementParserEngine sqlStatementParserEngine = new SQLStatementParserEngine(testParams.getDatabaseType(),
                sqlParserRule.getSqlStatementCache(), sqlParserRule.getParseTreeCache(), sqlParserRule.isSqlCommentParseEnabled());
        SQLStatement sqlStatement = sqlStatementParserEngine.parse(testParams.getInputSQL(), false);
        Collection<ShardingSphereRule> databaseRules = createDatabaseRules(databaseConfig, schemaName, sqlStatement);
        ShardingSphereRuleMetaData databaseRuleMetaData = new ShardingSphereRuleMetaData(databaseRules);
        ShardingSphereDatabase database = new ShardingSphereDatabase(schemaName, databaseType, resourceMetaData, databaseRuleMetaData, mockSchemas(schemaName));
        Map<String, ShardingSphereDatabase> databases = new HashMap<>(2, 1F);
        databases.put(schemaName, database);
        
        ShardingSphereRuleMetaData globalRuleMetaData = new ShardingSphereRuleMetaData(createGlobalRules());
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(databases, mock(ShardingSphereResourceMetaData.class), globalRuleMetaData, mock(ConfigurationProperties.class));
        SQLStatementContext sqlStatementContext = SQLStatementContextFactory.newInstance(metaData, sqlStatement, schemaName);
        if (sqlStatementContext instanceof ParameterAware) {
            ((ParameterAware) sqlStatementContext).setUpParameters(testParams.getInputParameters());
        }
        if (sqlStatementContext instanceof CursorDefinitionAware) {
            ((CursorDefinitionAware) sqlStatementContext).setUpCursorDefinition(createCursorDefinition(schemaName, metaData, sqlStatementParserEngine));
        }
        QueryContext queryContext = new QueryContext(sqlStatementContext, testParams.getInputSQL(), testParams.getInputParameters());
        ConfigurationProperties props = new ConfigurationProperties(rootConfig.getProps());
        RouteContext routeContext = new SQLRouteEngine(databaseRules, props).route(new ConnectionContext(), queryContext, globalRuleMetaData, database);
        SQLRewriteEntry sqlRewriteEntry = new SQLRewriteEntry(database, globalRuleMetaData, props);
        ConnectionContext connectionContext = mock(ConnectionContext.class);
        when(connectionContext.getCursorContext()).thenReturn(new CursorConnectionContext());
        SQLRewriteResult sqlRewriteResult = sqlRewriteEntry.rewrite(testParams.getInputSQL(), testParams.getInputParameters(), sqlStatementContext, routeContext, connectionContext,
                queryContext.getHintValueContext());
        return sqlRewriteResult instanceof GenericSQLRewriteResult
                ? Collections.singleton(((GenericSQLRewriteResult) sqlRewriteResult).getSqlRewriteUnit())
                : (((RouteSQLRewriteResult) sqlRewriteResult).getSqlRewriteUnits()).values();
    }
    
    private Collection<ShardingSphereRule> createDatabaseRules(final DatabaseConfiguration databaseConfig, final String schemaName, final SQLStatement sqlStatement) {
        Collection<ShardingSphereRule> result = DatabaseRulesBuilder.build(DefaultDatabase.LOGIC_NAME, databaseConfig, mock(InstanceContext.class));
        mockRules(result, schemaName, sqlStatement);
        result.add(sqlParserRule);
        result.add(timestampServiceRule);
        return result;
    }
    
    private Collection<ShardingSphereRule> createGlobalRules() {
        Collection<ShardingSphereRule> result = new LinkedList<>();
        result.add(new SQLTranslatorRule(new SQLTranslatorRuleConfiguration()));
        result.add(new SQLFederationRule(new SQLFederationRuleConfiguration(false, mock(CacheOption.class)), Collections.emptyMap(), mock(ConfigurationProperties.class)));
        result.add(new TimestampServiceRule(mock(TimestampServiceRuleConfiguration.class)));
        return result;
    }
    
    private Map<String, DatabaseType> createStorageTypes(final DatabaseConfiguration databaseConfig, final DatabaseType databaseType) {
        Map<String, DatabaseType> result = new LinkedHashMap<>(databaseConfig.getDataSources().size(), 1F);
        for (Entry<String, DataSource> entry : databaseConfig.getDataSources().entrySet()) {
            result.put(entry.getKey(), databaseType);
        }
        return result;
    }
    
    private CursorStatementContext createCursorDefinition(final String schemaName, final ShardingSphereMetaData metaData, final SQLStatementParserEngine sqlStatementParserEngine) {
        return (CursorStatementContext) SQLStatementContextFactory.newInstance(metaData,
                sqlStatementParserEngine.parse("CURSOR t_account_cursor FOR SELECT * FROM t_account WHERE account_id = 100", false), schemaName);
    }
    
    protected abstract void mockDataSource(Map<String, DataSource> dataSources) throws SQLException;
    
    protected abstract YamlRootConfiguration createRootConfiguration(SQLRewriteEngineTestParameters testParams) throws IOException;
    
    protected abstract Map<String, ShardingSphereSchema> mockSchemas(String schemaName);
    
    protected abstract void mockRules(Collection<ShardingSphereRule> rules, String schemaName, SQLStatement sqlStatement);
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            SQLRewriterITSettings settings = extensionContext.getRequiredTestClass().getAnnotation(SQLRewriterITSettings.class);
            Preconditions.checkNotNull(settings, "Annotation ExternalSQLParserITSettings is required.");
            return SQLRewriteEngineTestParametersBuilder.loadTestParameters(settings.value().toUpperCase(), settings.value(), SQLRewriterIT.class).stream().map(Arguments::of);
        }
    }
}
