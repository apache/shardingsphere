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

import lombok.SneakyThrows;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ConvertYamlConfigurationStatement;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class ConvertYamlConfigurationHandlerTest {
    
    private final String shardingFilePath = "/conf/convert/config-sharding.yaml";
    
    private final String readWriteSplittingFilePath = "/conf/convert/config-readwrite-splitting.yaml";
    
    private final String databaseDiscoveryFilePath = "/conf/convert/config-database-discovery.yaml";
    
    private final String encryptFilePath = "/conf/convert/config-encrypt.yaml";
    
    private final String shadowFilePath = "/conf/convert/config-shadow.yaml";
    
    private final String shardingExpectedFilePath = "/expected/convert-create-sharding.yaml";
    
    private final String readWriteSplittingExpectedFilePath = "/expected/convert-readwrite-splitting.yaml";
    
    private final String databaseDiscoveryExpectedFilePath = "/expected/convert-database-discovery.yaml";
    
    private final String encryptExpectedFilePath = "/expected/convert-create-encrypt.yaml";
    
    private final String shadowExpectedFilePath = "/expected/convert-create-shadow.yaml";
    
    private final String sharding = "sharding";
    
    private final String readWriteSplitting = "readWriteSplitting";
    
    private final String databaseDiscovery = "databaseDiscovery";
    
    private final String encrypt = "encrypt";
    
    private final String shadow = "shadow";
    
    private final Map<String, String> featureMap = new HashMap<>(5, 1);
    
    @Before
    public void setup() {
        featureMap.put(sharding, shardingFilePath);
        featureMap.put(readWriteSplitting, readWriteSplittingFilePath);
        featureMap.put(databaseDiscovery, databaseDiscoveryFilePath);
        featureMap.put(encrypt, encryptFilePath);
        featureMap.put(shadow, shadowFilePath);
    }
    
    @Test
    public void assertExecuteWithSharding() throws SQLException {
        assertExecute(sharding, shardingExpectedFilePath);
    }
    
    @Test
    public void assertExecuteWithReadWriteSplitting() throws SQLException {
        assertExecute(readWriteSplitting, readWriteSplittingExpectedFilePath);
    }
    
    @Test
    public void assertExecuteWithDatabaseDiscovery() throws SQLException {
        assertExecute(databaseDiscovery, databaseDiscoveryExpectedFilePath);
    }
    
    @Test
    public void assertExecuteWithEncrypt() throws SQLException {
        assertExecute(encrypt, encryptExpectedFilePath);
    }
    
    @Test
    public void assertExecuteWithShadow() throws SQLException {
        assertExecute(shadow, shadowExpectedFilePath);
    }
    
    public void assertExecute(final String type, final String expectedFilePath) throws SQLException {
        ConvertYamlConfigurationHandler handler = new ConvertYamlConfigurationHandler();
        handler.init(new ConvertYamlConfigurationStatement(Objects.requireNonNull(ConvertYamlConfigurationHandlerTest.class.getResource(featureMap.get(type))).getPath()),
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
    
    private void assertRowData(final Collection<Object> actual, final String expectedFilePath) {
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(loadExpectedRow(expectedFilePath)));
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
        }
        return result.toString();
    }
}
