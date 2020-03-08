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

import javax.sql.DataSource;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public abstract class AbstractEncryptJDBCDatabaseAndTableTest extends AbstractSQLTest {
    
    private static EncryptDataSource encryptDataSource;
    
    private static EncryptDataSource encryptDataSourceWithProps;
    
    private static final List<String> ENCRYPT_DB_NAMES = Collections.singletonList("encrypt");
    
    @BeforeClass
    public static void initEncryptDataSource() throws SQLException {
        if (null != encryptDataSource && null != encryptDataSourceWithProps) {
            return;
        }
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
        result.getTables().put("t_encrypt", createEncryptTableRule());
        result.getTables().put("t_query_encrypt", createQueryEncryptTableRule());
        result.getTables().put("t_encrypt_contains_column", createEncryptContainsColumnTableRule());
        return result;
    }
    
    private static EncryptTableRuleConfiguration createEncryptTableRule() {
        EncryptColumnRuleConfiguration columnRuleConfig = new EncryptColumnRuleConfiguration("plain_pwd", "cipher_pwd", "", "test");
        return new EncryptTableRuleConfiguration(Collections.singletonMap("pwd", columnRuleConfig));
    }
    
    private static EncryptTableRuleConfiguration createQueryEncryptTableRule() {
        EncryptColumnRuleConfiguration columnRuleConfig = new EncryptColumnRuleConfiguration("", "cipher_pwd", "assist_pwd", "assistedTest");
        return new EncryptTableRuleConfiguration(Collections.singletonMap("pwd", columnRuleConfig));
    }
    
    private static EncryptTableRuleConfiguration createEncryptContainsColumnTableRule() {
        EncryptColumnRuleConfiguration columnConfig1 = new EncryptColumnRuleConfiguration("plain_pwd", "cipher_pwd", "", "test");
        EncryptColumnRuleConfiguration columnConfig2 = new EncryptColumnRuleConfiguration("plain_pwd2", "cipher_pwd2", "", "test");
        Map<String, EncryptColumnRuleConfiguration> columns = new LinkedHashMap<>(2, 1);
        columns.put("plain_pwd", columnConfig1);
        columns.put("plain_pwd2", columnConfig2);
        return new EncryptTableRuleConfiguration(columns);
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
    
    @AfterClass
    public static void close() throws Exception {
        if (encryptDataSource == null) {
            return;
        }
        encryptDataSource.close();
        encryptDataSource = null;
    }
}
