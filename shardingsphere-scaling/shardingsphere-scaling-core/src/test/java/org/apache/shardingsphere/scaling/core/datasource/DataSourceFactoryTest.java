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

package org.apache.shardingsphere.scaling.core.datasource;

import org.apache.shardingsphere.scaling.core.config.JDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.ShardingSphereJDBCConfiguration;
import org.apache.shardingsphere.scaling.core.fixture.FixtureShardingSphereJDBCConfiguration;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class DataSourceFactoryTest {
    
    @Test
    public void assertNewJDBCInstance() {
        JDBCDataSourceConfiguration jdbcDataSourceConfig = new JDBCDataSourceConfiguration(
                "jdbc:h2:mem:test_db_2;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL",
                "root",
                "password");
        DataSourceWrapper actual = new DataSourceFactory().newInstance(jdbcDataSourceConfig);
        assertThat(actual, is(notNullValue()));
    }
    
    @Test
    public void assertNewShardingSphereJDBCInstance() {
        ShardingSphereJDBCConfiguration shardingSphereJDBCConfig = new ShardingSphereJDBCConfiguration(
                FixtureShardingSphereJDBCConfiguration.DATA_SOURCE, FixtureShardingSphereJDBCConfiguration.RULE);
        DataSourceWrapper actual = new DataSourceFactory().newInstance(shardingSphereJDBCConfig);
        assertThat(actual, is(notNullValue()));
    }
}
