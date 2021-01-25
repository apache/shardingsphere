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

import org.apache.shardingsphere.scaling.core.config.datasource.StandardJDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.ShardingSphereJDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.fixture.FixtureShardingSphereJDBCConfiguration;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public final class DataSourceFactoryTest {
    
    @Test
    public void assertNewJDBCInstance() {
        StandardJDBCDataSourceConfiguration config = new StandardJDBCDataSourceConfiguration(
                "jdbc:h2:mem:test_db_2;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL", "root", "password");
        DataSourceWrapper actual = new DataSourceFactory().newInstance(config);
        assertNotNull(actual);
    }
    
    @Test
    public void assertNewShardingSphereJDBCInstance() {
        ShardingSphereJDBCDataSourceConfiguration config = new ShardingSphereJDBCDataSourceConfiguration(
                FixtureShardingSphereJDBCConfiguration.DATA_SOURCE, FixtureShardingSphereJDBCConfiguration.RULE);
        DataSourceWrapper actual = new DataSourceFactory().newInstance(config);
        assertNotNull(actual);
    }
}
