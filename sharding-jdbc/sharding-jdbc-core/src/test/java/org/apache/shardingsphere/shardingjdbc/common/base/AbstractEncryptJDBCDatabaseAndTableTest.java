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

import com.google.common.collect.Maps;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.shardingsphere.encrypt.api.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.EncryptorRuleConfiguration;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.connection.EncryptConnection;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.EncryptDataSource;
import org.apache.shardingsphere.underlying.common.constant.properties.PropertiesConstant;
import org.h2.tools.RunScript;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.yaml.snakeyaml.Yaml;

public abstract class AbstractEncryptJDBCDatabaseAndTableTest extends AbstractSQLTest {

    private static EncryptDataSource encryptDataSource;

    private static EncryptDataSource encryptDataSourceWithProps;

    private static final List<String> ENCRYPT_DB_NAMES = Collections.singletonList("encrypt");

    private static Map<String, Object> encryptJDBCTestConfig;

    private static final String CONFIG_FILE_NAME = "encrypt_data.yaml";

    private static final Yaml YAML = new Yaml();

    @BeforeClass
    public static void initEncryptDataSource() throws SQLException {
        if (null != encryptDataSource && null != encryptDataSourceWithProps) {
            return;
        }
        encryptJDBCTestConfig = (Map<String, Object>) (YAML.load(AbstractSQLTest.class.getClassLoader().getResourceAsStream(CONFIG_FILE_NAME)));
        Map<String, DataSource> dataSources = getDataSources();
        encryptDataSource = new EncryptDataSource(dataSources.values().iterator().next(), new EncryptRule(createEncryptRuleConfiguration()), new Properties());
        encryptDataSourceWithProps = new EncryptDataSource(dataSources.values().iterator().next(), new EncryptRule(createEncryptRuleConfiguration()), createProperties());
    }

    private static Properties createProperties() {
        Properties result = new Properties();
        result.put(PropertiesConstant.SQL_SHOW.getKey(), true);
        result.put(PropertiesConstant.QUERY_WITH_CIPHER_COLUMN.getKey(), false);
        return result;
    }

    private static Map<String, DataSource> getDataSources() {
        return Maps.filterKeys(getDatabaseTypeMap().values().iterator().next(), ENCRYPT_DB_NAMES::contains);
    }

    private static EncryptRuleConfiguration createEncryptRuleConfiguration() {
        EncryptRuleConfiguration result = new EncryptRuleConfiguration();
        result.getEncryptors().put("test", new EncryptorRuleConfiguration("test", new Properties()));
        result.getEncryptors().put("assistedTest", new EncryptorRuleConfiguration("assistedTest", new Properties()));
        result.getTables().put("t_encrypt", createEncryptTableRule("t_encrypt"));
        result.getTables().put("t_query_encrypt", createQueryEncryptTableRule("t_query_encrypt"));
        result.getTables().put("t_encrypt_contains_column", createEncryptContainsColumnTableRule("t_encrypt1", "t_encrypt2"));
        return result;
    }

    private static EncryptTableRuleConfiguration createEncryptTableRule(final String tEncrypt) {
        EncryptColumnRuleConfiguration columnRuleConfig = buildEncryptColumnRuleConfiguration(tEncrypt);
        return new EncryptTableRuleConfiguration(Collections.singletonMap("pwd", columnRuleConfig));
    }

    private static EncryptTableRuleConfiguration createQueryEncryptTableRule(final String tQueryEncrypt) {
        return createEncryptTableRule(tQueryEncrypt);
    }

    private static EncryptTableRuleConfiguration createEncryptContainsColumnTableRule(final String tEncrypt1, final String tEncrypt2) {
        EncryptColumnRuleConfiguration columnConfig1 = buildEncryptColumnRuleConfiguration(tEncrypt1);
        EncryptColumnRuleConfiguration columnConfig2 = buildEncryptColumnRuleConfiguration(tEncrypt2);
        Map<String, EncryptColumnRuleConfiguration> columns = new LinkedHashMap<>(2, 1);
        columns.put("plain_pwd", columnConfig1);
        columns.put("plain_pwd2", columnConfig2);
        return new EncryptTableRuleConfiguration(columns);
    }

    private static EncryptColumnRuleConfiguration buildEncryptColumnRuleConfiguration(final String key) {
        Map<String, String> ruleConfig = (Map<String, String>) encryptJDBCTestConfig.get(key);
        return new EncryptColumnRuleConfiguration(
            Objects.requireNonNull(ruleConfig.get("plainColumn")),
            Objects.requireNonNull(ruleConfig.get("cipherColumn")),
            Objects.requireNonNull(ruleConfig.get("assistedQueryColumn")),
            Objects.requireNonNull(ruleConfig.get("encryptor")));
    }

    @Before
    public void initTable() {
        try {
            EncryptConnection conn = encryptDataSource.getConnection();

            String runScript = (String) Objects.requireNonNull(encryptJDBCTestConfig.get("RunScript"),
                "RunScript must not be null.");
            RunScript.execute(conn, new InputStreamReader(new ByteArrayInputStream(runScript.getBytes(StandardCharsets.UTF_8))));
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

    @AfterClass
    public static void close() throws Exception {
        if (encryptDataSource == null) {
            return;
        }
        encryptDataSource.close();
        encryptDataSource = null;
    }
}
