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
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.convert.ConvertYamlConfigurationStatement;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class ConvertYamlConfigurationExecutorTest {
    
    private final SQLParserRule sqlParserRule = new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build());
    
    @Test
    void assertExecuteWithEmptyDatabase() {
        assertExecute("/conf/convert/database-empty.yaml", "/expected/convert-empty-database.yaml");
    }
    
    @Test
    void assertExecuteWithSharding() {
        assertExecute("/conf/convert/database-sharding.yaml", "/expected/convert-sharding.yaml");
    }
    
    @Test
    void assertExecuteWithShardingAutoTables() {
        assertExecute("/conf/convert/database-sharding-auto-tables.yaml", "/expected/convert-sharding-auto-tables.yaml");
    }
    
    @Test
    void assertExecuteWithReadWriteSplitting() {
        assertExecute("/conf/convert/database-readwrite-splitting.yaml", "/expected/convert-readwrite-splitting.yaml");
    }
    
    @Test
    void assertExecuteWithEncrypt() {
        assertExecute("/conf/convert/database-encrypt.yaml", "/expected/convert-encrypt.yaml");
    }
    
    @Test
    void assertExecuteWithShadow() {
        assertExecute("/conf/convert/database-shadow.yaml", "/expected/convert-shadow.yaml");
    }
    
    @Test
    void assertExecuteWithMix() {
        assertExecute("/conf/convert/database-mix.yaml", "/expected/convert-mix.yaml");
    }
    
    private void assertExecute(final String configFilePath, final String expectedFilePath) {
        ConvertYamlConfigurationExecutor executor = new ConvertYamlConfigurationExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(
                new ConvertYamlConfigurationStatement(Objects.requireNonNull(ConvertYamlConfigurationExecutorTest.class.getResource(configFilePath)).getPath()), mock(ContextManager.class));
        assertRowData(actual, expectedFilePath);
    }
    
    private void assertRowData(final Collection<LocalDataQueryResultRow> data, final String expectedFilePath) {
        assertThat(data.size(), is(1));
        LocalDataQueryResultRow actual = data.iterator().next();
        assertThat(actual.getCell(1), is(loadExpectedRow(expectedFilePath)));
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
}
