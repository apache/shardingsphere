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

package org.apache.shardingsphere.shardingjdbc.common.base;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.apache.shardingsphere.core.yaml.config.YamlRootRuleConfigurations;
import org.apache.shardingsphere.core.yaml.swapper.root.RuleRootConfigurationsYamlSwapper;
import org.apache.shardingsphere.shardingjdbc.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.shardingjdbc.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.underlying.common.yaml.engine.YamlEngine;
import org.h2.tools.RunScript;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public abstract class AbstractEncryptJDBCDatabaseAndTableTest extends AbstractSQLTest {
    
    private static ShardingSphereDataSource encryptDataSource;
    
    private static ShardingSphereDataSource encryptDataSourceWithProps;
    
    private static final List<String> ENCRYPT_DB_NAMES = Collections.singletonList("encrypt");
    
    private static final String ENCRYPT_CONFIG_FILE = "encrypt-config.yaml";
    
    @BeforeClass
    public static void initEncryptDataSource() throws SQLException, IOException {
        if (null != encryptDataSource && null != encryptDataSourceWithProps) {
            return;
        }
        File encryptFile = getFile(ENCRYPT_CONFIG_FILE);
        DataSource dataSource = getDataSources().values().iterator().next();
        encryptDataSource = (ShardingSphereDataSource) createDataSourceWithEmptyProps(dataSource, encryptFile);
        encryptDataSourceWithProps = (ShardingSphereDataSource) YamlShardingSphereDataSourceFactory.createDataSource(dataSource, encryptFile);
    }
    
    private static File getFile(final String fileName) {
        return new File(Preconditions.checkNotNull(AbstractEncryptJDBCDatabaseAndTableTest.class.getClassLoader().getResource(fileName), "file resource `%s` must not be null.", fileName).getFile());
    }
    
    private static Map<String, DataSource> getDataSources() {
        return Maps.filterKeys(getDatabaseTypeMap().values().iterator().next(), ENCRYPT_DB_NAMES::contains);
    }
    
    private static DataSource createDataSourceWithEmptyProps(final DataSource dataSource, final File yamlFile) throws IOException, SQLException {
        YamlRootRuleConfigurations configurations = YamlEngine.unmarshal(yamlFile, YamlRootRuleConfigurations.class);
        return ShardingSphereDataSourceFactory.createDataSource(dataSource, new RuleRootConfigurationsYamlSwapper().swap(configurations), new Properties());
    }
    
    @Before
    public void initTable() {
        try (ShardingSphereConnection connection = encryptDataSource.getConnection()) {
            RunScript.execute(connection, new InputStreamReader(AbstractSQLTest.class.getClassLoader().getResourceAsStream("encrypt_data.sql")));
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    protected final ShardingSphereConnection getEncryptConnection() {
        return encryptDataSource.getConnection();
    }
    
    protected final ShardingSphereConnection getEncryptConnectionWithProps() {
        return encryptDataSourceWithProps.getConnection();
    }
    
    @AfterClass
    public static void close() throws Exception {
        if (null == encryptDataSource) {
            return;
        }
        encryptDataSource.close();
        encryptDataSource = null;
        if (null == encryptDataSourceWithProps) {
            return;
        }
        encryptDataSourceWithProps.close();
        encryptDataSourceWithProps = null;
    }
}
