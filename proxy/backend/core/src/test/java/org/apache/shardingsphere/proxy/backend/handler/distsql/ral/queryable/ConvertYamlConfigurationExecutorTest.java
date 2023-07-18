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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.SneakyThrows;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ConvertYamlConfigurationStatement;
import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConvertYamlConfigurationExecutorTest {
    
    private final SQLParserRule sqlParserRule = new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build());
    
    @Test
    void assertExecuteWithSharding() {
        assertExecute("/conf/convert/config-sharding.yaml", "/expected/convert-sharding.yaml");
    }
    
    @Test
    void assertExecuteWithShardingAutoTables() {
        assertExecute("/conf/convert/config-sharding-auto-tables.yaml", "/expected/convert-sharding-auto-tables.yaml");
    }
    
    @Test
    void assertExecuteWithReadWriteSplitting() {
        assertExecute("/conf/convert/config-readwrite-splitting.yaml", "/expected/convert-readwrite-splitting.yaml");
    }
    
    @Test
    void assertExecuteWithEncrypt() {
        assertExecute("/conf/convert/config-encrypt.yaml", "/expected/convert-encrypt.yaml");
    }
    
    @Test
    void assertExecuteWithShadow() {
        assertExecute("/conf/convert/config-shadow.yaml", "/expected/convert-shadow.yaml");
    }
    
    @Test
    void assertExecuteWithMix() {
        assertExecute("/conf/convert/config-mix.yaml", "/expected/convert-mix.yaml");
    }
    
    @Test
    void assertGetColumnNames() {
        ConvertYamlConfigurationExecutor executor = new ConvertYamlConfigurationExecutor();
        Collection<String> columns = executor.getColumnNames();
        assertThat(columns.size(), is(1));
        Iterator<String> iterator = columns.iterator();
        assertThat(iterator.next(), is("dist_sql"));
    }
    
    private void assertExecute(final String configFilePath, final String expectedFilePath) {
        ConvertYamlConfigurationExecutor executor = new ConvertYamlConfigurationExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(
                new ConvertYamlConfigurationStatement(Objects.requireNonNull(ConvertYamlConfigurationExecutorTest.class.getResource(configFilePath)).getPath()));
        assertRowData(actual, expectedFilePath);
    }
    
    private void assertRowData(final Collection<LocalDataQueryResultRow> data, final String expectedFilePath) {
        assertThat(data.size(), is(1));
        LocalDataQueryResultRow actual = data.iterator().next();
        assertThat(actual.getCell(1), is(loadExpectedRow(expectedFilePath)));
        assertParseSQL((String) actual.getCell(1));
    }
    
    private void assertParseSQL(final String actual) {
        Splitter.on(";").trimResults().splitToList(actual).forEach(each -> {
            if (!Strings.isNullOrEmpty(each)) {
                assertNotNull(sqlParserRule.getSQLParserEngine(new MySQLDatabaseType().getType()).parse(each, false));
            }
        });
    }
    
    @SneakyThrows(IOException.class)
    private String loadExpectedRow(final String expectedFilePath) {
        StringBuilder result = new StringBuilder();
        String fileName = Objects.requireNonNull(ConvertYamlConfigurationExecutorTest.class.getResource(expectedFilePath)).getFile();
        try (
                FileReader fileReader = new FileReader(fileName);
                BufferedReader reader = new BufferedReader(fileReader)) {
            String line;
            while (null != (line = reader.readLine())) {
                if (!line.startsWith("#")) {
                    result.append(line).append(System.lineSeparator());
                }
            }
            result.append(System.lineSeparator());
        }
        return result.toString();
    }
}
