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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable.yaml;

import com.google.common.base.Splitter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.convert.ConvertYamlConfigurationStatement;
import org.apache.shardingsphere.infra.exception.generic.FileIOException;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDataSourceConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDatabaseConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class ConvertYamlConfigurationExecutorTest {
    
    private static final String MISSING_FILE_PATH = "src/test/resources/conf/convert/not-exist.yaml";
    
    private final SQLParserRule sqlParserRule = new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build());
    
    private final ConvertYamlConfigurationExecutor executor = (ConvertYamlConfigurationExecutor) TypedSPILoader.getService(DistSQLQueryExecutor.class, ConvertYamlConfigurationStatement.class);
    
    @Test
    void assertGetColumnNames() {
        assertThat(executor.getColumnNames(new ConvertYamlConfigurationStatement("foo")), is(Collections.singleton("dist_sql")));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("resourceExpectedCases")
    void assertExecuteWithExpectedResource(final String caseName, final String configFilePath, final String expectedFilePath) {
        assertRowData(
                executor.getRows(new ConvertYamlConfigurationStatement(Objects.requireNonNull(ConvertYamlConfigurationExecutorTest.class.getResource(configFilePath)).getPath()), mock()),
                loadExpectedRow(expectedFilePath));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("inlineExpectedCases")
    void assertExecuteWithInlineExpected(final String caseName, final String configFilePath, final String expectedRow) {
        assertRowData(
                executor.getRows(new ConvertYamlConfigurationStatement(Objects.requireNonNull(ConvertYamlConfigurationExecutorTest.class.getResource(configFilePath)).getPath()), mock()),
                expectedRow);
    }
    
    @Test
    void assertExecuteWithNullDataSources() throws IOException {
        YamlProxyDatabaseConfiguration yamlConfig = new YamlProxyDatabaseConfiguration();
        yamlConfig.setDatabaseName("null_data_sources_db");
        yamlConfig.setDataSources(null);
        yamlConfig.setRules(Collections.emptyList());
        assertExecuteWithMockedYamlConfig(yamlConfig, toExpectedRow("CREATE DATABASE null_data_sources_db;", "USE null_data_sources_db;"));
    }
    
    @Test
    void assertExecuteWithNullRules() throws IOException {
        YamlProxyDatabaseConfiguration yamlConfig = new YamlProxyDatabaseConfiguration();
        yamlConfig.setDatabaseName("null_rules_db");
        YamlProxyDataSourceConfiguration dataSourceConfig = new YamlProxyDataSourceConfiguration();
        dataSourceConfig.setUrl("jdbc:mysql://127.0.0.1:3306/demo_convert_ds_0?useSSL=false");
        dataSourceConfig.setUsername("root");
        dataSourceConfig.setPassword("12345678");
        dataSourceConfig.setConnectionTimeoutMilliseconds(30000L);
        dataSourceConfig.setIdleTimeoutMilliseconds(60000L);
        dataSourceConfig.setMaxLifetimeMilliseconds(1800000L);
        dataSourceConfig.setMaxPoolSize(50);
        dataSourceConfig.setMinPoolSize(1);
        yamlConfig.setDataSources(Collections.singletonMap("ds_0", dataSourceConfig));
        yamlConfig.setRules(null);
        assertExecuteWithMockedYamlConfig(yamlConfig, toExpectedRow(
                "CREATE DATABASE null_rules_db;",
                "USE null_rules_db;",
                "",
                "REGISTER STORAGE UNIT ds_0 (",
                "URL='jdbc:mysql://127.0.0.1:3306/demo_convert_ds_0?useSSL=false',",
                "USER='root',",
                "PASSWORD='12345678',",
                "PROPERTIES('minPoolSize'='1','connectionTimeoutMilliseconds'='30000','maxLifetimeMilliseconds'='1800000',"
                        + "'idleTimeoutMilliseconds'='60000','maxPoolSize'='50')",
                ");"));
    }
    
    @Test
    void assertExecuteWithMissingDatabaseName() {
        String filePath = Objects.requireNonNull(ConvertYamlConfigurationExecutorTest.class.getResource("/conf/convert/database-without-database-name.yaml")).getPath();
        NullPointerException actual = assertThrows(NullPointerException.class,
                () -> executor.getRows(new ConvertYamlConfigurationStatement(filePath), mock()));
        assertThat(actual.getMessage(), is("`databaseName` in file `database-without-database-name.yaml` is required."));
    }
    
    @Test
    void assertExecuteWithMissingFile() {
        FileIOException actual = assertThrows(FileIOException.class, () -> executor.getRows(new ConvertYamlConfigurationStatement(MISSING_FILE_PATH), mock()));
        assertThat(actual.getMessage(), is(String.format("File access failed, file is: %s", new File(MISSING_FILE_PATH).getAbsolutePath())));
    }
    
    private void assertExecuteWithMockedYamlConfig(final YamlProxyDatabaseConfiguration yamlConfig, final String expectedRow) throws IOException {
        try (MockedStatic<YamlEngine> mockedYamlEngine = mockStatic(YamlEngine.class)) {
            mockedYamlEngine.when(() -> YamlEngine.unmarshal(any(File.class), eq(YamlProxyDatabaseConfiguration.class))).thenReturn(yamlConfig);
            Collection<LocalDataQueryResultRow> actual = executor.getRows(new ConvertYamlConfigurationStatement("mocked-path.yaml"), mock());
            assertRowData(actual, expectedRow);
        }
    }
    
    private void assertRowData(final Collection<LocalDataQueryResultRow> data, final String expectedRow) {
        assertThat(data.size(), is(1));
        LocalDataQueryResultRow actual = data.iterator().next();
        assertThat(actual.getCell(1), is(expectedRow));
        assertParseSQL((String) actual.getCell(1));
    }
    
    private void assertParseSQL(final String distSQLs) {
        Splitter.on(";").trimResults().omitEmptyStrings().splitToList(distSQLs)
                .forEach(each -> assertNotNull(sqlParserRule.getSQLParserEngine(TypedSPILoader.getService(DatabaseType.class, "MySQL")).parse(each, false)));
    }
    
    @SneakyThrows({IOException.class, URISyntaxException.class})
    private String loadExpectedRow(final String expectedFilePath) {
        URL url = Objects.requireNonNull(ConvertYamlConfigurationExecutorTest.class.getResource(expectedFilePath));
        return Files.readAllLines(Paths.get(url.toURI())).stream().filter(each -> !each.startsWith("#")).collect(Collectors.joining(System.lineSeparator()))
                + System.lineSeparator() + System.lineSeparator();
    }
    
    private static Stream<Arguments> resourceExpectedCases() {
        return Stream.of(
                Arguments.of("empty-database", "/conf/convert/database-empty.yaml", "/expected/convert-empty-database.yaml"),
                Arguments.of("sharding", "/conf/convert/database-sharding.yaml", "/expected/convert-sharding.yaml"),
                Arguments.of("sharding-auto-tables", "/conf/convert/database-sharding-auto-tables.yaml", "/expected/convert-sharding-auto-tables.yaml"),
                Arguments.of("readwrite-splitting", "/conf/convert/database-readwrite-splitting.yaml", "/expected/convert-readwrite-splitting.yaml"),
                Arguments.of("encrypt", "/conf/convert/database-encrypt.yaml", "/expected/convert-encrypt.yaml"),
                Arguments.of("shadow", "/conf/convert/database-shadow.yaml", "/expected/convert-shadow.yaml"),
                Arguments.of("mix", "/conf/convert/database-mix.yaml", "/expected/convert-mix.yaml"));
    }
    
    private static Stream<Arguments> inlineExpectedCases() {
        return Stream.of(
                Arguments.of("without-password", "/conf/convert/database-without-password.yaml", toExpectedRow(
                        "CREATE DATABASE no_password_db;",
                        "USE no_password_db;",
                        "",
                        "REGISTER STORAGE UNIT ds_0 (",
                        "URL='jdbc:mysql://127.0.0.1:3306/demo_convert_ds_0?useSSL=false',",
                        "USER='root',",
                        "PROPERTIES('minPoolSize'='1','connectionTimeoutMilliseconds'='30000','maxLifetimeMilliseconds'='1800000',"
                                + "'idleTimeoutMilliseconds'='60000','maxPoolSize'='50'));")),
                Arguments.of("empty-rules", "/conf/convert/database-empty-rules.yaml", toExpectedRow(
                        "CREATE DATABASE empty_rules_db;",
                        "USE empty_rules_db;",
                        "",
                        "REGISTER STORAGE UNIT ds_0 (",
                        "URL='jdbc:mysql://127.0.0.1:3306/demo_convert_ds_0?useSSL=false',",
                        "USER='root',",
                        "PASSWORD='12345678',",
                        "PROPERTIES('minPoolSize'='1','connectionTimeoutMilliseconds'='30000','maxLifetimeMilliseconds'='1800000',"
                                + "'idleTimeoutMilliseconds'='60000','maxPoolSize'='50')",
                        ");")),
                Arguments.of("custom-pool-props", "/conf/convert/database-with-custom-pool-props.yaml", toExpectedRow(
                        "CREATE DATABASE custom_pool_props_db;",
                        "USE custom_pool_props_db;",
                        "",
                        "REGISTER STORAGE UNIT ds_0 (",
                        "URL='jdbc:mysql://127.0.0.1:3306/demo_convert_ds_0?useSSL=false',",
                        "USER='root',",
                        "PASSWORD='12345678',",
                        "PROPERTIES('minPoolSize'='1','connectionTimeoutMilliseconds'='30000','maxLifetimeMilliseconds'='1800000',"
                                + "'idleTimeoutMilliseconds'='60000','maxPoolSize'='50','foo1'='bar_1')",
                        ");")));
    }
    
    private static String toExpectedRow(final String... lines) {
        return String.join(System.lineSeparator(), lines) + System.lineSeparator() + System.lineSeparator();
    }
}
