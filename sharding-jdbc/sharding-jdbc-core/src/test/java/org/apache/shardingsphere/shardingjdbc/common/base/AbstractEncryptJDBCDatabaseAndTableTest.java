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
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptorRuleConfiguration;
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

    private static YamlEncryptRuleConfiguration encryptRuleConfig;

    @BeforeClass
    public static void initEncryptDataSource() throws SQLException, IOException {
        encryptRuleConfig = getEncryptRuleConfig(getFile("encrypt_config.yaml"));
        System.out.println(encryptRuleConfig);
        if (null != encryptDataSource && null != encryptDataSourceWithProps) {
            return;
        }
        Map<String, DataSource> dataSources = getDataSources();
        encryptDataSource = new EncryptDataSource(dataSources.values().iterator().next(), new EncryptRule(createEncryptRuleConfiguration()), new Properties());
        encryptDataSourceWithProps = new EncryptDataSource(dataSources.values().iterator().next(), new EncryptRule(createEncryptRuleConfiguration()), createProperties());
    }

    private static Properties createProperties() {
        Properties result = new Properties();
        result.put(ConfigurationPropertyKey.SQL_SHOW.getKey(), true);
        result.put(ConfigurationPropertyKey.QUERY_WITH_CIPHER_COLUMN.getKey(), false);
        return result;
    }

    private static Map<String, DataSource> getDataSources() {
        return Maps.filterKeys(getDatabaseTypeMap().values().iterator().next(), ENCRYPT_DB_NAMES::contains);
    }

    private static EncryptRuleConfiguration createEncryptRuleConfiguration() {
        EncryptRuleConfiguration result = new EncryptRuleConfiguration();
        for (Map.Entry<String, YamlEncryptorRuleConfiguration> entry : encryptRuleConfig.getEncryptors().entrySet()) {
            result.getEncryptors().put(entry.getKey(), new EncryptorRuleConfiguration(entry.getValue().getType(), entry.getValue().getProps()));
        }

        for (Map.Entry<String, YamlEncryptTableRuleConfiguration> tableMap : encryptRuleConfig.getTables().entrySet()) {
            Map<String, EncryptColumnRuleConfiguration> columns = new LinkedHashMap<>(2, 1);
            for (Map.Entry<String, YamlEncryptColumnRuleConfiguration> columnMap : tableMap.getValue().getColumns().entrySet()) {
                YamlEncryptColumnRuleConfiguration yamlConfig = columnMap.getValue();
                EncryptColumnRuleConfiguration columnRuleConfiguration = new EncryptColumnRuleConfiguration(yamlConfig.getPlainColumn(), yamlConfig.getCipherColumn(), yamlConfig.getAssistedQueryColumn(),
                    yamlConfig.getEncryptor());
                columns.put(columnMap.getKey(), columnRuleConfiguration);
            }
            result.getTables().put(tableMap.getKey(), new EncryptTableRuleConfiguration(columns));
        }

        return result;
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

    private static YamlEncryptRuleConfiguration getEncryptRuleConfig(final File file) throws IOException {
        return YamlEngine.unmarshal(file, YamlEncryptRuleConfiguration.class);
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
