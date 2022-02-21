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

package org.apache.shardingsphere.driver.jdbc.base;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.h2.tools.RunScript;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractShardingSphereDataSourceForEncryptTest extends AbstractSQLTest {
    
    private static ShardingSphereDataSource queryWithPlainDataSource;
    
    private static ShardingSphereDataSource queryWithCipherDataSource;
    
    private static final List<String> ACTUAL_DATA_SOURCE_NAMES = Collections.singletonList("encrypt");
    
    private static final String ENCRYPT_CONFIG_QUERY_WITH_PLAIN_FILE = "config/config-encrypt-query-with-plain.yaml";
    
    private static final String ENCRYPT_CONFIG_QUERY_WITH_CIPHER_FILE = "config/config-encrypt-query-with-cipher.yaml";
    
    @BeforeClass
    public static void initEncryptDataSource() throws SQLException, IOException {
        if (null != queryWithPlainDataSource && null != queryWithCipherDataSource) {
            return;
        }
        DataSource dataSource = getDataSources().values().iterator().next();
        queryWithPlainDataSource = (ShardingSphereDataSource) YamlShardingSphereDataSourceFactory.createDataSource(dataSource, getFile(ENCRYPT_CONFIG_QUERY_WITH_CIPHER_FILE));
        queryWithCipherDataSource = (ShardingSphereDataSource) YamlShardingSphereDataSourceFactory.createDataSource(dataSource, getFile(ENCRYPT_CONFIG_QUERY_WITH_PLAIN_FILE));
    }
    
    private static File getFile(final String fileName) {
        return new File(Preconditions.checkNotNull(
                AbstractShardingSphereDataSourceForEncryptTest.class.getClassLoader().getResource(fileName), "file resource `%s` must not be null.", fileName).getFile());
    }
    
    private static Map<String, DataSource> getDataSources() {
        return Maps.filterKeys(getActualDataSources(), ACTUAL_DATA_SOURCE_NAMES::contains);
    }
    
    @Before
    public void initTable() {
        try (Connection connection = queryWithPlainDataSource.getConnection()) {
            RunScript.execute(connection, new InputStreamReader(Objects.requireNonNull(AbstractSQLTest.class.getClassLoader().getResourceAsStream("sql/encrypt_data.sql"))));
        } catch (final SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    protected final Connection getEncryptConnection() {
        return queryWithPlainDataSource.getConnection();
    }
    
    protected final ShardingSphereConnection getEncryptConnectionWithProps() {
        return (ShardingSphereConnection) queryWithCipherDataSource.getConnection();
    }
    
    @AfterClass
    public static void close() throws Exception {
        if (null == queryWithPlainDataSource) {
            return;
        }
        queryWithPlainDataSource.close();
        queryWithPlainDataSource = null;
        if (null == queryWithCipherDataSource) {
            return;
        }
        queryWithCipherDataSource.close();
        queryWithCipherDataSource = null;
    }
}
