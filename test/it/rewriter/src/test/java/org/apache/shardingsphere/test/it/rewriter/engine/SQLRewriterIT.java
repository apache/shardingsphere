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
import org.apache.shardingsphere.database.connector.core.DefaultDatabase;
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
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.hint.SQLHintUtils;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
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
import org.apache.shardingsphere.test.it.rewriter.engine.parameter.SQLRewriteEngineTestParameters;
import org.apache.shardingsphere.test.it.rewriter.engine.parameter.SQLRewriteEngineTestParametersBuilder;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public abstract class SQLRewriterIT {
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertRewrite(final SQLRewriteEngineTestParameters testParams) throws IOException {
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
    
    private Collection<SQLRewriteUnit> createSQLRewriteUnits(final SQLRewriteEngineTestParameters testParams) throws IOException {
        YamlRootConfiguration rootConfig = loadRootConfiguration(testParams);
        DatabaseConfiguration databaseConfig = new DataSourceProvidedDatabaseConfiguration(
                new YamlDataSourceConfigurationSwapper().swapToDataSources(rootConfig.getDataSources()), new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(rootConfig.getRules()));
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, testParams.getDatabaseType());
        ResourceMetaData resourceMetaData = new ResourceMetaData(Collections.emptyMap(), databaseConfig.getStorageUnits());
        String databaseName = null == rootConfig.getDatabaseName() ? DefaultDatabase.LOGIC_NAME : rootConfig.getDatabaseName();
        String schemaName = new DatabaseTypeRegistry(databaseType).getDefaultSchemaName(databaseName);
        String sql = SQLHintUtils.removeHint(testParams.getInputSQL());
        SQLParserEngine sqlParserEngine = new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build()).getSQLParserEngine(databaseType);
        SQLStatement sqlStatement = sqlParserEngine.parse(sql, false);
        Collection<ShardingSphereRule> rules = createDatabaseRules(databaseConfig, schemaName, sqlStatement, databaseType);
        ShardingSphereDatabase database = new ShardingSphereDatabase(databaseName, databaseType, resourceMetaData, new RuleMetaData(rules), mockSchemas(schemaName));
        RuleMetaData globalRuleMetaData = new RuleMetaData(GlobalRulesBuilder.buildRules(Collections.emptyList(), Collections.emptyList(), new ConfigurationProperties(new Properties())));
        ConfigurationProperties props = new ConfigurationProperties(rootConfig.getProps());
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singleton(database), mock(), globalRuleMetaData, props);
        HintValueContext hintValueContext = SQLHintUtils.extractHint(testParams.getInputSQL());
        SQLStatementContext sqlStatementContext = bind(testParams, metaData, databaseName, hintValueContext, sqlStatement, sqlParserEngine);
        ConnectionContext connectionContext = createConnectionContext(database.getName());
        QueryContext queryContext = new QueryContext(sqlStatementContext, sql, testParams.getInputParameters(), hintValueContext, connectionContext, metaData);
        RouteContext routeContext = new SQLRouteEngine(rules, props).route(queryContext, globalRuleMetaData, database);
        SQLRewriteEntry sqlRewriteEntry = new SQLRewriteEntry(database, globalRuleMetaData, props);
        SQLRewriteResult sqlRewriteResult = sqlRewriteEntry.rewrite(queryContext, routeContext);
        return sqlRewriteResult instanceof GenericSQLRewriteResult
                ? Collections.singleton(((GenericSQLRewriteResult) sqlRewriteResult).getSqlRewriteUnit())
                : (((RouteSQLRewriteResult) sqlRewriteResult).getSqlRewriteUnits()).values();
    }
    
    private YamlRootConfiguration loadRootConfiguration(final SQLRewriteEngineTestParameters testParams) throws IOException {
        URL url = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource(testParams.getRuleFile()),
                String.format("Can not find configuration file `%s`", testParams.getRuleFile()));
        return YamlEngine.unmarshal(new File(url.getFile()), YamlRootConfiguration.class);
    }
    
    private Collection<ShardingSphereRule> createDatabaseRules(final DatabaseConfiguration databaseConfig, final String schemaName, final SQLStatement sqlStatement, final DatabaseType databaseType) {
        Collection<ShardingSphereRule> result = DatabaseRulesBuilder.build(
                DefaultDatabase.LOGIC_NAME, databaseType, databaseConfig, mock(), new ResourceMetaData(databaseConfig.getDataSources(), databaseConfig.getStorageUnits()));
        mockDatabaseRules(result, schemaName, sqlStatement);
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
    
    protected abstract Collection<ShardingSphereSchema> mockSchemas(String schemaName);
    
    protected abstract void mockDatabaseRules(Collection<ShardingSphereRule> rules, String schemaName, SQLStatement sqlStatement);
    
    private static final class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters, final ExtensionContext context) {
            SQLRewriterITSettings settings = context.getRequiredTestClass().getAnnotation(SQLRewriterITSettings.class);
            Preconditions.checkNotNull(settings, "Annotation SQLRewriterITSettings is required.");
            return SQLRewriteEngineTestParametersBuilder.loadTestParameters(settings.value().toUpperCase(), settings.value(), SQLRewriterIT.class).stream().map(Arguments::of);
        }
    }
}
