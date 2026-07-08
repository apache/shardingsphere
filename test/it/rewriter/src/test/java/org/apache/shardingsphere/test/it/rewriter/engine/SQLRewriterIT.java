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

package org.apache.shardingsphere.test.it.rewriter.engine;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.DefaultDatabase;
import org.apache.shardingsphere.database.connector.core.jdbcurl.parser.ConnectionProperties;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.binder.context.aware.ParameterAware;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CursorHeldSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CursorStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.MetadataIdentifierCaseSensitivity;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.hint.SQLHintUtils;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.parser.SQLParserEngine;
import org.apache.shardingsphere.infra.rewrite.SQLRewriteEntry;
import org.apache.shardingsphere.infra.rewrite.engine.result.GenericSQLRewriteResult;
import org.apache.shardingsphere.infra.rewrite.engine.result.RouteSQLRewriteResult;
import org.apache.shardingsphere.infra.rewrite.engine.result.SQLRewriteResult;
import org.apache.shardingsphere.infra.rewrite.engine.result.SQLRewriteUnit;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.engine.SQLRouteEngine;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.database.DatabaseRulesBuilder;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.test.it.rewriter.engine.mocker.DialectStorageUnitMetaDataMocker;
import org.apache.shardingsphere.test.it.rewriter.engine.parameter.SQLRewriteEngineTestParameters;
import org.apache.shardingsphere.test.it.rewriter.engine.parameter.SQLRewriteEngineTestParametersBuilder;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class SQLRewriterIT {
    
    private static final Map<String, RewriteScenarioContext> REWRITE_SCENARIO_CONTEXT_CACHE = new ConcurrentHashMap<>();
    
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
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, testParams.getDatabaseType());
        String sql = SQLHintUtils.removeHint(testParams.getInputSQL());
        SQLParserEngine sqlParserEngine = new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build()).getSQLParserEngine(databaseType);
        SQLStatement sqlStatement = sqlParserEngine.parse(sql, false);
        RewriteScenarioContext scenarioContext = getCachedRewriteScenarioContext(testParams, databaseType, sqlStatement);
        HintValueContext hintValueContext = SQLHintUtils.extractHint(testParams.getInputSQL());
        SQLStatementContext sqlStatementContext = bind(testParams, scenarioContext.metaData, scenarioContext.database.getName(), hintValueContext, sqlStatement, sqlParserEngine);
        ConnectionContext connectionContext = createConnectionContext(scenarioContext.database.getName());
        QueryContext queryContext = new QueryContext(sqlStatementContext, sql, testParams.getInputParameters(), hintValueContext, connectionContext, scenarioContext.metaData);
        Collection<ShardingSphereRule> rules = scenarioContext.database.getRuleMetaData().getRules();
        RouteContext routeContext = new SQLRouteEngine(rules, scenarioContext.props).route(queryContext, scenarioContext.globalRuleMetaData, scenarioContext.database);
        SQLRewriteResult sqlRewriteResult = new SQLRewriteEntry(scenarioContext.database, scenarioContext.globalRuleMetaData, scenarioContext.props).rewrite(queryContext, routeContext);
        return createSQLRewriteUnits(sqlRewriteResult);
    }
    
    private Collection<SQLRewriteUnit> createSQLRewriteUnits(final SQLRewriteResult sqlRewriteResult) {
        return sqlRewriteResult instanceof GenericSQLRewriteResult
                ? Collections.singleton(((GenericSQLRewriteResult) sqlRewriteResult).getSqlRewriteUnit())
                : (((RouteSQLRewriteResult) sqlRewriteResult).getSqlRewriteUnits()).values();
    }
    
    private RewriteScenarioContext getCachedRewriteScenarioContext(final SQLRewriteEngineTestParameters testParams, final DatabaseType databaseType,
                                                                   final SQLStatement sqlStatement) throws IOException, SQLException {
        String cacheKey = getRewriteScenarioCacheKey(testParams, sqlStatement);
        RewriteScenarioContext result = REWRITE_SCENARIO_CONTEXT_CACHE.get(cacheKey);
        if (null != result) {
            return result;
        }
        RewriteScenarioContext created = createRewriteScenarioContext(testParams, databaseType, sqlStatement);
        REWRITE_SCENARIO_CONTEXT_CACHE.put(cacheKey, created);
        return created;
    }
    
    private RewriteScenarioContext createRewriteScenarioContext(final SQLRewriteEngineTestParameters testParams, final DatabaseType databaseType,
                                                                final SQLStatement sqlStatement) throws IOException, SQLException {
        YamlRootConfiguration rootConfig = loadRootConfiguration(testParams);
        ShardingSphereDatabase database = createDatabase(rootConfig, sqlStatement, databaseType);
        RuleMetaData globalRuleMetaData = new RuleMetaData(GlobalRulesBuilder.buildRules(Collections.emptyList(), Collections.emptyList(), new ConfigurationProperties(new Properties())));
        ConfigurationProperties props = createConfigurationProperties(rootConfig);
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singleton(database), mock(), globalRuleMetaData, props);
        return new RewriteScenarioContext(database, globalRuleMetaData, props, metaData);
    }
    
    private YamlRootConfiguration loadRootConfiguration(final SQLRewriteEngineTestParameters testParams) throws IOException {
        return loadRootConfiguration(testParams.getRuleFile());
    }
    
    private YamlRootConfiguration loadRootConfiguration(final String ruleFile) throws IOException {
        URL url = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(ruleFile), String.format("Can not find configuration file `%s`", ruleFile));
        return YamlEngine.unmarshal(new File(url.getFile()), YamlRootConfiguration.class);
    }
    
    private String getRewriteScenarioCacheKey(final SQLRewriteEngineTestParameters testParams, final SQLStatement sqlStatement) {
        return getClass().getName() + ':' + testParams.getDatabaseType() + ':' + testParams.getRuleFile() + ':' + testParams.getFileName() + ':' + getRewriteSQLType(sqlStatement);
    }
    
    private String getRewriteSQLType(final SQLStatement sqlStatement) {
        return sqlStatement instanceof CreateTableStatement ? "create_table" : "other";
    }
    
    private ShardingSphereDatabase createDatabase(final YamlRootConfiguration rootConfig, final SQLStatement sqlStatement, final DatabaseType databaseType) throws SQLException {
        DatabaseConfiguration databaseConfig = new DataSourceProvidedDatabaseConfiguration(
                new YamlDataSourceConfigurationSwapper().swapToDataSources(rootConfig.getDataSources()), new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(rootConfig.getRules()));
        ResourceMetaData resourceMetaData = new ResourceMetaData(Collections.emptyMap(), createStorageUnits(databaseConfig, databaseType));
        String databaseName = null == rootConfig.getDatabaseName() ? DefaultDatabase.LOGIC_NAME : rootConfig.getDatabaseName();
        String schemaName = new DatabaseTypeRegistry(databaseType).getDefaultSchemaName(databaseName);
        Collection<ShardingSphereRule> rules = createDatabaseRules(databaseConfig, schemaName, sqlStatement, databaseType);
        ConfigurationProperties props = createConfigurationProperties(rootConfig);
        RuleMetaData ruleMetaData = new RuleMetaData(rules);
        Collection<ShardingSphereSchema> schemas = mockSchemas(schemaName);
        return new ShardingSphereDatabase(databaseName, databaseType, resourceMetaData, ruleMetaData, schemas, props);
    }
    
    private ConfigurationProperties createConfigurationProperties(final YamlRootConfiguration rootConfig) {
        Properties result = new Properties();
        result.putAll(rootConfig.getProps());
        result.putIfAbsent(TemporaryConfigurationPropertyKey.METADATA_IDENTIFIER_CASE_SENSITIVITY.getKey(), MetadataIdentifierCaseSensitivity.INSENSITIVE.name());
        return new ConfigurationProperties(result);
    }
    
    private Map<String, StorageUnit> createStorageUnits(final DatabaseConfiguration databaseConfig, final DatabaseType databaseType) throws SQLException {
        Map<String, StorageUnit> result = new LinkedHashMap<>(databaseConfig.getStorageUnits().size(), 1F);
        for (Entry<String, StorageUnit> entry : databaseConfig.getStorageUnits().entrySet()) {
            StorageUnit storageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
            DataSource dataSource = mock(DataSource.class);
            when(storageUnit.getStorageType()).thenReturn(databaseType);
            when(storageUnit.getDataSource()).thenReturn(dataSource);
            when(storageUnit.getConnectionProperties()).thenReturn(new ConnectionProperties("127.0.0.1", 3306, entry.getKey(), null, new Properties()));
            Connection connection = mock(Connection.class);
            DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.getMetaData()).thenReturn(databaseMetaData);
            DatabaseTypedSPILoader.findService(DialectStorageUnitMetaDataMocker.class, databaseType).ifPresent(optional -> optional.mockStorageUnitMetaData(connection, databaseMetaData));
            result.put(entry.getKey(), storageUnit);
        }
        return result;
    }
    
    private SQLStatementContext bind(final SQLRewriteEngineTestParameters testParams, final ShardingSphereMetaData metaData, final String databaseName,
                                     final HintValueContext hintValueContext, final SQLStatement sqlStatement, final SQLParserEngine sqlParserEngine) {
        SQLStatementContext result = new SQLBindEngine(metaData, databaseName, hintValueContext).bind(sqlStatement);
        if (result instanceof ParameterAware) {
            ((ParameterAware) result).bindParameters(testParams.getInputParameters());
        }
        if (result instanceof CursorHeldSQLStatementContext) {
            ((CursorHeldSQLStatementContext) result).setCursorStatementContext(createCursorDefinition(databaseName, metaData, sqlParserEngine));
        }
        return result;
    }
    
    private CursorStatementContext createCursorDefinition(final String schemaName, final ShardingSphereMetaData metaData, final SQLParserEngine sqlParserEngine) {
        SQLStatement sqlStatement = sqlParserEngine.parse("CURSOR t_account_cursor FOR SELECT * FROM t_account WHERE account_id = 100", false);
        return (CursorStatementContext) new SQLBindEngine(metaData, schemaName, new HintValueContext()).bind(sqlStatement);
    }
    
    private ConnectionContext createConnectionContext(final String databaseName) {
        ConnectionContext result = new ConnectionContext(() -> Collections.singleton("foo_ds"));
        result.setCurrentDatabaseName(databaseName);
        return result;
    }
    
    private Collection<ShardingSphereRule> createDatabaseRules(final DatabaseConfiguration databaseConfig, final String schemaName, final SQLStatement sqlStatement, final DatabaseType databaseType) {
        Collection<ShardingSphereRule> result = DatabaseRulesBuilder.build(DefaultDatabase.LOGIC_NAME, databaseType,
                databaseConfig, mock(ComputeNodeInstanceContext.class), new ResourceMetaData(databaseConfig.getDataSources(), databaseConfig.getStorageUnits()));
        mockDatabaseRules(result, schemaName, sqlStatement);
        return result;
    }
    
    protected abstract Collection<ShardingSphereSchema> mockSchemas(String schemaName);
    
    protected abstract void mockDatabaseRules(Collection<ShardingSphereRule> rules, String schemaName, SQLStatement sqlStatement);
    
    @RequiredArgsConstructor
    private static final class RewriteScenarioContext {
        
        private final ShardingSphereDatabase database;
        
        private final RuleMetaData globalRuleMetaData;
        
        private final ConfigurationProperties props;
        
        private final ShardingSphereMetaData metaData;
    }
    
    private static final class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters, final ExtensionContext context) {
            SQLRewriterITSettings settings = context.getRequiredTestClass().getAnnotation(SQLRewriterITSettings.class);
            Preconditions.checkNotNull(settings, "Annotation SQLRewriterITSettings is required.");
            return SQLRewriteEngineTestParametersBuilder.loadTestParameters(settings.value().toUpperCase(), settings.value(), SQLRewriterIT.class).stream().map(Arguments::of);
        }
    }
}
