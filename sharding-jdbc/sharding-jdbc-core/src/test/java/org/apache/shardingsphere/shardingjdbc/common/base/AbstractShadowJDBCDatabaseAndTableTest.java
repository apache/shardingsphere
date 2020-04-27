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

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.api.config.shadow.ShadowRuleConfiguration;
import org.apache.shardingsphere.core.rule.ShadowRule;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShadowDataSource;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.underlying.common.database.type.DatabaseTypes;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

public abstract class AbstractShadowJDBCDatabaseAndTableTest extends AbstractSQLTest {
    
    private static ShadowDataSource shadowDataSource;
    
    @BeforeClass
    public static void initEncryptDataSource() throws SQLException {
        if (null != shadowDataSource) {
            return;
        }
        initDataSource();
        Map<String, DataSource> dataSources = getDatabaseTypeMap().get(DatabaseTypes.getActualDatabaseType("H2"));
        shadowDataSource = new ShadowDataSource(dataSources.get("jdbc_0"), dataSources.get("jdbc_1"), new ShadowRule(createShadowRuleConfiguration()), createProperties());
    }
    
    private static Properties createProperties() {
        Properties result = new Properties();
        result.put(ConfigurationPropertyKey.SQL_SHOW.getKey(), true);
        return result;
    }
    
    private static ShadowRuleConfiguration createShadowRuleConfiguration() {
        ShadowRuleConfiguration shardingRuleConfiguration = new ShadowRuleConfiguration();
        shardingRuleConfiguration.setColumn("shadow");
        shardingRuleConfiguration.setShadowMappings(ImmutableMap.of("jdbc_0", "jdbc_1"));
        return shardingRuleConfiguration;
    }
    
    protected Connection getShadowConnection() throws SQLException {
        return shadowDataSource.getShadowDataSource().getConnection();
    }
    
    protected Connection getActualConnection() throws SQLException {
        return shadowDataSource.getActualDataSource().getConnection();
    }
    
    protected Connection getConnection() throws SQLException {
        return shadowDataSource.getConnection();
    }
    
    @AfterClass
    public static void close() throws Exception {
        if (null == shadowDataSource) {
            return;
        }
        shadowDataSource.close();
        shadowDataSource = null;
    }
}
