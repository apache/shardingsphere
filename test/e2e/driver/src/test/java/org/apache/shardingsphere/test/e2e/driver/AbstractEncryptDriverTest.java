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

package org.apache.shardingsphere.test.e2e.driver;

import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.h2.tools.RunScript;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class AbstractEncryptDriverTest extends AbstractDriverTest {
    
    private static ShardingSphereDataSource queryWithCipherDataSource;
    
    private static final List<String> ACTUAL_DATA_SOURCE_NAMES = Collections.singletonList("encrypt");
    
    private static final String CONFIG_FILE_WITH_QUERY_WITH_CIPHER = "config/database-encrypt-query-with-cipher.yaml";
    
    @BeforeAll
    static void initEncryptDataSource() throws SQLException, IOException {
        if (null != queryWithCipherDataSource) {
            return;
        }
        DataSource dataSource = getDataSourceMap().values().iterator().next();
        queryWithCipherDataSource = (ShardingSphereDataSource) YamlShardingSphereDataSourceFactory.createDataSource(dataSource, getFile(CONFIG_FILE_WITH_QUERY_WITH_CIPHER));
    }
    
    private static File getFile(final String fileName) {
        return new File(Objects.requireNonNull(
                Thread.currentThread().getContextClassLoader().getResource(fileName), String.format("File `%s` is not existed.", fileName)).getFile());
    }
    
    private static Map<String, DataSource> getDataSourceMap() {
        return getActualDataSources().entrySet().stream().filter(entry -> ACTUAL_DATA_SOURCE_NAMES.contains(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
    
    @BeforeEach
    void initTable() {
        try (Connection connection = queryWithCipherDataSource.getConnection()) {
            RunScript.execute(connection, new InputStreamReader(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResourceAsStream("sql/encrypt_data.sql"))));
        } catch (final SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    protected final Connection getEncryptConnection() {
        return queryWithCipherDataSource.getConnection();
    }
    
    protected final ShardingSphereConnection getEncryptConnectionWithProps() {
        return (ShardingSphereConnection) queryWithCipherDataSource.getConnection();
    }
    
    @AfterAll
    static void close() throws Exception {
        if (null == queryWithCipherDataSource) {
            return;
        }
        queryWithCipherDataSource.close();
        queryWithCipherDataSource = null;
    }
}
