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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractShardingSphereDataSourceForCalciteTest extends AbstractSQLCalciteTest {
    
    private static ShardingSphereDataSource dataSource;
    
    private static final List<String> ACTUAL_DATA_SOURCE_NAMES = Arrays.asList("calcite_jdbc_0", "calcite_jdbc_1", "calcite_jdbc_2");
    
    private static final String CONFIG_CALCITE = "config/config-calcite.yaml";
    
    @BeforeClass
    public static void initCalciteDataSource() throws IOException, SQLException {
        if (null != dataSource) {
            return;
        }
        dataSource = (ShardingSphereDataSource) YamlShardingSphereDataSourceFactory.createDataSource(getDataSourceMap(), getFile(CONFIG_CALCITE));
    }
    
    private static Map<String, DataSource> getDataSourceMap() {
        return Maps.filterKeys(getActualDataSources(), ACTUAL_DATA_SOURCE_NAMES::contains);
    }
    
    @Before
    public void initTable() {
        try {
            ShardingSphereConnection conn = dataSource.getConnection();
            Map<String, DataSource> dataSourceMap = conn.getDataSourceMap();
            Connection database0 = dataSourceMap.get("calcite_jdbc_0").getConnection();
            Connection database1 = dataSourceMap.get("calcite_jdbc_1").getConnection();
            Connection database2 = dataSourceMap.get("calcite_jdbc_2").getConnection();
            RunScript.execute(database0, new InputStreamReader(Objects.requireNonNull(AbstractSQLTest.class.getClassLoader().getResourceAsStream("sql/calcite_data_0.sql"))));
            RunScript.execute(database1, new InputStreamReader(Objects.requireNonNull(AbstractSQLTest.class.getClassLoader().getResourceAsStream("sql/calcite_data_1.sql"))));
            RunScript.execute(database2, new InputStreamReader(Objects.requireNonNull(AbstractSQLTest.class.getClassLoader().getResourceAsStream("sql/calcite_data_2.sql"))));
            conn.close();
        } catch (final SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private static File getFile(final String fileName) {
        return new File(Preconditions.checkNotNull(
                AbstractShardingSphereDataSourceForShardingTest.class.getClassLoader().getResource(fileName), "file resource `%s` must not be null.", fileName).getFile());
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
