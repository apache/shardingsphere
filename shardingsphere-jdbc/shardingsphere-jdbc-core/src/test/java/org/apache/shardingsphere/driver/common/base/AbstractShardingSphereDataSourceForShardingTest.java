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

package org.apache.shardingsphere.driver.common.base;

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
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.File;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractShardingSphereDataSourceForShardingTest extends AbstractSQLTest {
    
    private static ShardingSphereDataSource dataSource;
    
    private static final List<String> SHARDING_DB_NAMES = Arrays.asList("jdbc_0", "jdbc_1");
    
    private static final String CONFIG_SHARDING = "config-sharding.yaml";
    
    @BeforeClass
    public static void initShardingSphereDataSource() throws SQLException, IOException {
        if (null != dataSource) {
            return;
        }
        dataSource = (ShardingSphereDataSource) YamlShardingSphereDataSourceFactory.createDataSource(getDataSourceMap(), getFile(CONFIG_SHARDING));
    }
    
    private static Map<String, DataSource> getDataSourceMap() {
        return Maps.filterKeys(getDatabaseTypeMap().values().iterator().next(), SHARDING_DB_NAMES::contains);
    }
    
    private static File getFile(final String fileName) {
        return new File(Preconditions.checkNotNull(
                AbstractShardingSphereDataSourceForShardingTest.class.getClassLoader().getResource(fileName), "file resource `%s` must not be null.", fileName).getFile());
    }
    
    @Before
    public void initTable() {
        try {
            ShardingSphereConnection conn = dataSource.getConnection();
            RunScript.execute(conn, new InputStreamReader(Objects.requireNonNull(AbstractSQLTest.class.getClassLoader().getResourceAsStream("jdbc_data.sql"))));
            conn.close();
        } catch (final SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    protected final ShardingSphereDataSource getShardingSphereDataSource() {
        return dataSource;
    }
    
    @AfterClass
    public static void clear() throws Exception {
        if (null == dataSource) {
            return;
        }
        dataSource.close();
        dataSource = null;
    }
}
