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
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.parser.rule.builder.DefaultSQLParserRuleConfigurationBuilder;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class ConvertYamlConfigurationHandlerTest {
    
    private final String shardingConfigFilePath = "/conf/convert/config-sharding.yaml";
    
    private final String readWriteSplittingConfigFilePath = "/conf/convert/config-readwrite-splitting.yaml";
    
    private final String databaseDiscoveryConfigFilePath = "/conf/convert/config-database-discovery.yaml";
    
    private final String encryptConfigFilePath = "/conf/convert/config-encrypt.yaml";
    
    private final String shadowConfigFilePath = "/conf/convert/config-shadow.yaml";
    
    private final String mixConfigFilePath = "/conf/convert/config-mix.yaml";
    
    private final String shardingExpectedFilePath = "/expected/convert-sharding.yaml";
    
    private final String readWriteSplittingExpectedFilePath = "/expected/convert-readwrite-splitting.yaml";
    
    private final String databaseDiscoveryExpectedFilePath = "/expected/convert-database-discovery.yaml";
    
    private final String encryptExpectedFilePath = "/expected/convert-encrypt.yaml";
    
    private final String shadowExpectedFilePath = "/expected/convert-shadow.yaml";
    
    private final String mixExpectedFilePath = "/expected/convert-mix.yaml";
    
    private final SQLParserRule sqlParserRule = new SQLParserRule(new DefaultSQLParserRuleConfigurationBuilder().build());
    
    @Test
    public void assertExecuteWithSharding() throws SQLException {
        assertExecute(shardingConfigFilePath, shardingExpectedFilePath);
    }
    
    @Test
    public void assertExecuteWithReadWriteSplitting() throws SQLException {
        assertExecute(readWriteSplittingConfigFilePath, readWriteSplittingExpectedFilePath);
    }
    
    @Test
    public void assertExecuteWithDatabaseDiscovery() throws SQLException {
        assertExecute(databaseDiscoveryConfigFilePath, databaseDiscoveryExpectedFilePath);
    }
    
    @Test
    public void assertExecuteWithEncrypt() throws SQLException {
        assertExecute(encryptConfigFilePath, encryptExpectedFilePath);
    }
    
    @Test
    public void assertExecuteWithShadow() throws SQLException {
        assertExecute(shadowConfigFilePath, shadowExpectedFilePath);
    }
    
    @Test
    public void assertExecuteWithMix() throws SQLException {
        assertExecute(mixConfigFilePath, mixExpectedFilePath);
    }
    
    public void assertExecute(final String configFilePath, final String expectedFilePath) throws SQLException {
        ConvertYamlConfigurationHandler handler = new ConvertYamlConfigurationHandler();
        handler.init(new ConvertYamlConfigurationStatement(Objects.requireNonNull(ConvertYamlConfigurationHandlerTest.class.getResource(configFilePath)).getPath()),
                mock(ConnectionSession.class));
        assertQueryResponseHeader((QueryResponseHeader) handler.execute());
        assertTrue(handler.next());
        assertRowData(handler.getRowData().getData(), expectedFilePath);
        assertFalse(handler.next());
    }
    
    private void assertQueryResponseHeader(final QueryResponseHeader actual) {
        assertThat(actual.getQueryHeaders().size(), is(1));
        assertQueryHeader(actual.getQueryHeaders().get(0));
    }
    
    private void assertQueryHeader(final QueryHeader actual) {
        assertThat(actual.getSchema(), is(""));
        assertThat(actual.getTable(), is(""));
        assertThat(actual.getColumnLabel(), is("distsql"));
        assertThat(actual.getColumnName(), is("distsql"));
        assertThat(actual.getColumnType(), is(1));
        assertThat(actual.getColumnTypeName(), is("CHAR"));
        assertThat(actual.getColumnLength(), is(255));
        assertThat(actual.getDecimals(), is(0));
        assertFalse(actual.isSigned());
        assertFalse(actual.isPrimaryKey());
        assertFalse(actual.isNotNull());
        assertFalse(actual.isAutoIncrement());
    }
    
    private void assertRowData(final Collection<Object> data, final String expectedFilePath) {
        assertThat(data.size(), is(1));
        Object actual = data.iterator().next();
        assertThat(actual, is(loadExpectedRow(expectedFilePath)));
        assertParseSQL((String) actual);
    }
    
    private void assertParseSQL(final String actual) {
        for (String each : Splitter.on(";").trimResults().splitToList(actual)) {
            if (!Strings.isNullOrEmpty(each)) {
                assertNotNull(sqlParserRule.getSQLParserEngine("MYSQL").parse(each, false));
            }
        }
    }
    
    @SneakyThrows(IOException.class)
    private String loadExpectedRow(final String expectedFilePath) {
        StringBuilder result = new StringBuilder();
        String fileName = Objects.requireNonNull(ConvertYamlConfigurationHandlerTest.class.getResource(expectedFilePath)).getFile();
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
