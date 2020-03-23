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
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.shardingsphere.encrypt.api.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.EncryptorRuleConfiguration;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptorRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.YamlRootEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.swapper.EncryptRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.shardingjdbc.api.EncryptDataSourceFactory;
import org.apache.shardingsphere.shardingjdbc.api.yaml.YamlEncryptDataSourceFactory;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.EncryptConnection;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.EncryptDataSource;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.underlying.common.yaml.engine.YamlEngine;
import org.h2.tools.RunScript;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class AbstractEncryptJDBCDatabaseAndTableTest extends AbstractSQLTest {
    
    private static EncryptDataSource encryptDataSource;
    
    private static EncryptDataSource encryptDataSourceWithProps;
    
    private static final List<String> ENCRYPT_DB_NAMES = Collections.singletonList("encrypt");
    
    private static final String ENCRYPT_CONFIG_FILE = "encrypt_config.yaml";
    
    @BeforeClass
    public static void initEncryptDataSource() throws SQLException, IOException {
        if (null != encryptDataSource && null != encryptDataSourceWithProps) {
            return;
        }
        File encryptFile =  getFile(ENCRYPT_CONFIG_FILE);
        DataSource dataSource = getDataSources().values().iterator().next();
        encryptDataSource = (EncryptDataSource)createDataSourceWithEmptyProps(dataSource, encryptFile);
        encryptDataSourceWithProps = (EncryptDataSource)YamlEncryptDataSourceFactory.createDataSource(dataSource, getFile(ENCRYPT_CONFIG_FILE));
    }
    
    private static Map<String, DataSource> getDataSources() {
        return Maps.filterKeys(getDatabaseTypeMap().values().iterator().next(), ENCRYPT_DB_NAMES::contains);
    }
    
    @Before
    public void initTable() {
        try {
            EncryptConnection conn = encryptDataSource.getConnection();
            RunScript.execute(conn, new InputStreamReader(AbstractSQLTest.class.getClassLoader().getResourceAsStream("encrypt_data.sql")));
            conn.close();
        } catch (final SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    protected final EncryptConnection getEncryptConnection() throws SQLException {
        return encryptDataSource.getConnection();
    }
    
    protected final EncryptConnection getEncryptConnectionWithProps() throws SQLException {
        return encryptDataSourceWithProps.getConnection();
    }
    
    private static File getFile(final String fileName) {
        return new File(Preconditions.checkNotNull(AbstractEncryptJDBCDatabaseAndTableTest.class.getClassLoader().getResource(fileName),
            "file resource must not be null : " + fileName).getFile());
    }
    
    private static YamlRootEncryptRuleConfiguration getEncryptRuleConfig(final File file) throws IOException {
        return YamlEngine.unmarshal(file, YamlRootEncryptRuleConfiguration.class);
    }
    
    private static DataSource createDataSourceWithEmptyProps(final DataSource dataSource, final File yamlFile) throws IOException, SQLException {
        YamlRootEncryptRuleConfiguration config = YamlEngine.unmarshal(yamlFile, YamlRootEncryptRuleConfiguration.class);
        return EncryptDataSourceFactory.createDataSource(dataSource, new EncryptRuleConfigurationYamlSwapper().swap(config.getEncryptRule()), new Properties());
    }
    
    @AfterClass
    public static void close() throws Exception {
        if (encryptDataSource == null) {
            return;
        }
        encryptDataSource.close();
        encryptDataSource = null;
    }
}
