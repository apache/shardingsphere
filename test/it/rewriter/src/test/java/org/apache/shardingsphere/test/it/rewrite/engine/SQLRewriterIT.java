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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.aware.CursorDefinitionAware;
import org.apache.shardingsphere.infra.binder.aware.ParameterAware;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CursorStatementContext;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.ConnectionContext;
import org.apache.shardingsphere.infra.context.cursor.CursorConnectionContext;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
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
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sqltranslator.api.config.SQLTranslatorRuleConfiguration;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.apache.shardingsphere.test.it.rewrite.engine.parameter.SQLRewriteEngineTestParameters;
import org.apache.shardingsphere.timeservice.api.config.TimeServiceRuleConfiguration;
import org.apache.shardingsphere.timeservice.core.rule.TimeServiceRule;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
@Getter
public abstract class SQLRewriterIT {
    
    private final SQLRewriteEngineTestParameters testParams;
    
    private final SQLParserRule sqlParserRule = new SQLParserRule(new SQLParserRuleConfiguration(true,
            DefaultSQLParserRuleConfigurationBuilder.PARSE_TREE_CACHE_OPTION, DefaultSQLParserRuleConfigurationBuilder.SQL_STATEMENT_CACHE_OPTION));
    
    private final TimeServiceRule timeServiceRule = new TimeServiceRule(new TimeServiceRuleConfiguration("System", new Properties()));
    
    @Test
    public final void assertRewrite() throws IOException, SQLException {
        Collection<SQLRewriteUnit> actual = createSQLRewriteUnits();
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
    
    private Collection<SQLRewriteUnit> createSQLRewriteUnits() throws IOException, SQLException {
        YamlRootConfiguration rootConfig = createRootConfiguration();
        DatabaseConfiguration databaseConfig = new DataSourceProvidedDatabaseConfiguration(
                new YamlDataSourceConfigurationSwapper().swapToDataSources(rootConfig.getDataSources()), new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(rootConfig.getRules()));
        mockDataSource(databaseConfig.getDataSources());
        ShardingSphereResourceMetaData resourceMetaData = mock(ShardingSphereResourceMetaData.class);
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, getTestParams().getDatabaseType());
        Map<String, DatabaseType> storageTypes = createStorageTypes(databaseConfig, databaseType);
        when(resourceMetaData.getStorageTypes()).thenReturn(storageTypes);
        String schemaName = DatabaseTypeEngine.getDefaultSchemaName(databaseType, DefaultDatabase.LOGIC_NAME);
        Collection<ShardingSphereRule> databaseRules = DatabaseRulesBuilder.build(DefaultDatabase.LOGIC_NAME, databaseConfig, mock(InstanceContext.class));
        SQLStatementParserEngine sqlStatementParserEngine = new SQLStatementParserEngine(getTestParams().getDatabaseType(),
                sqlParserRule.getSqlStatementCache(), sqlParserRule.getParseTreeCache(), sqlParserRule.isSqlCommentParseEnabled());
        SQLStatement sqlStatement = sqlStatementParserEngine.parse(getTestParams().getInputSQL(), false);
        mockRules(databaseRules, schemaName, sqlStatement);
        databaseRules.add(sqlParserRule);
        databaseRules.add(timeServiceRule);
        ShardingSphereDatabase database = new ShardingSphereDatabase(schemaName, databaseType, resourceMetaData, new ShardingSphereRuleMetaData(databaseRules), mockSchemas(schemaName));
        Map<String, ShardingSphereDatabase> databases = new HashMap<>(2, 1);
        databases.put(schemaName, database);
        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(createShardingSphereMetaData(databases), sqlStatement, schemaName);
        if (sqlStatementContext instanceof ParameterAware) {
            ((ParameterAware) sqlStatementContext).setUpParameters(getTestParams().getInputParameters());
        }
        if (sqlStatementContext instanceof CursorDefinitionAware) {
            ((CursorDefinitionAware) sqlStatementContext).setUpCursorDefinition(createCursorDefinition(schemaName, databases, sqlStatementParserEngine));
        }
        QueryContext queryContext = new QueryContext(sqlStatementContext, getTestParams().getInputSQL(), getTestParams().getInputParameters());
        ConfigurationProperties props = new ConfigurationProperties(rootConfig.getProps());
        RouteContext routeContext = new SQLRouteEngine(databaseRules, props).route(new ConnectionContext(), queryContext, mock(ShardingSphereRuleMetaData.class), database);
        SQLRewriteEntry sqlRewriteEntry = new SQLRewriteEntry(database, new ShardingSphereRuleMetaData(Collections.singleton(new SQLTranslatorRule(new SQLTranslatorRuleConfiguration()))), props);
        ConnectionContext connectionContext = mock(ConnectionContext.class);
        when(connectionContext.getCursorConnectionContext()).thenReturn(new CursorConnectionContext());
        SQLRewriteResult sqlRewriteResult = sqlRewriteEntry.rewrite(getTestParams().getInputSQL(), getTestParams().getInputParameters(), sqlStatementContext, routeContext, connectionContext);
        return sqlRewriteResult instanceof GenericSQLRewriteResult
                ? Collections.singletonList(((GenericSQLRewriteResult) sqlRewriteResult).getSqlRewriteUnit())
                : (((RouteSQLRewriteResult) sqlRewriteResult).getSqlRewriteUnits()).values();
    }
    
    private ShardingSphereMetaData createShardingSphereMetaData(final Map<String, ShardingSphereDatabase> databases) {
        return new ShardingSphereMetaData(databases, mock(ShardingSphereRuleMetaData.class), mock(ConfigurationProperties.class));
    }
    
    private static Map<String, DatabaseType> createStorageTypes(final DatabaseConfiguration databaseConfig, final DatabaseType databaseType) {
        Map<String, DatabaseType> result = new LinkedHashMap<>(databaseConfig.getDataSources().size(), 1);
        for (Entry<String, DataSource> entry : databaseConfig.getDataSources().entrySet()) {
            result.put(entry.getKey(), databaseType);
        }
        return result;
    }
    
    private CursorStatementContext createCursorDefinition(final String schemaName, final Map<String, ShardingSphereDatabase> databases, final SQLStatementParserEngine sqlStatementParserEngine) {
        return (CursorStatementContext) SQLStatementContextFactory.newInstance(
                createShardingSphereMetaData(databases), sqlStatementParserEngine.parse("CURSOR t_account_cursor FOR SELECT * FROM t_account WHERE account_id = 100", false), schemaName);
    }
    
    protected abstract void mockDataSource(Map<String, DataSource> dataSources) throws SQLException;
    
    protected abstract YamlRootConfiguration createRootConfiguration() throws IOException;
    
    protected abstract Map<String, ShardingSphereSchema> mockSchemas(String schemaName);
    
    protected abstract void mockRules(Collection<ShardingSphereRule> rules, String schemaName, SQLStatement sqlStatement);
    
    private static class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext) {
            return Stream.of(Arguments.of("select_with_union", "select a+1 as b, name n from table1 join table2 where id=1 and name='lu';", 2, 3),
                    Arguments.of("select_item_nums", "select id, name, age, sex, ss, yy from table1 where id=1", 1, 6),
                    Arguments.of("select_with_subquery", "select id, name, age, count(*) as n, (select id, name, age, sex from table2 where id=2) as sid, yyyy from table1 where id=1", 2, 5),
                    Arguments.of("select_where_num", "select id, name, age, sex, ss, yy from table1 where id=1 and name=1 and a=1 and b=2 and c=4 and d=3", 1, 10),
                    Arguments.of("alter_table", "ALTER TABLE t_order ADD column4 DATE, ADD column5 DATETIME, engine ss max_rows 10,min_rows 2, ADD column6 TIMESTAMP, ADD column7 TIME;", 1, 4),
                    Arguments.of("create_table", "CREATE TABLE IF NOT EXISTS `runoob_tbl`(\n"
                                    + "`runoob_id` INT UNSIGNED AUTO_INCREMENT,\n"
                                    + "`runoob_title` VARCHAR(100) NOT NULL,\n"
                                    + "`runoob_author` VARCHAR(40) NOT NULL,\n"
                                    + "`runoob_test` NATIONAL CHAR(40),\n"
                                    + "`submission_date` DATE,\n"
                                    + "PRIMARY KEY ( `runoob_id` )\n"
                                    + ")ENGINE=InnoDB DEFAULT CHARSET=utf8;",
                            1, 5));
        }
    }
}
