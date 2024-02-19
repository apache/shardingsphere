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

package org.apache.shardingsphere.driver.jdbc.core.driver;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

class DriverDataSourceCacheTest {
    
    private static final String DRIVER_URL_PREFIX = "jdbc:shardingsphere:";
    
    @Test
    void assertGetNewDataSource() {
        DriverDataSourceCache dataSourceCache = new DriverDataSourceCache();
        DataSource fooDataSource = dataSourceCache.get("jdbc:shardingsphere:classpath:config/driver/foo-driver-fixture.yaml", DRIVER_URL_PREFIX);
        DataSource barDataSource = dataSourceCache.get("jdbc:shardingsphere:classpath:config/driver/bar-driver-fixture.yaml", DRIVER_URL_PREFIX);
        assertThat(fooDataSource, not(barDataSource));
    }
    
    @Test
    void assertGetExistedDataSource() {
        DriverDataSourceCache dataSourceCache = new DriverDataSourceCache();
        DataSource dataSource1 = dataSourceCache.get("jdbc:shardingsphere:classpath:config/driver/foo-driver-fixture.yaml", DRIVER_URL_PREFIX);
        DataSource dataSource2 = dataSourceCache.get("jdbc:shardingsphere:classpath:config/driver/foo-driver-fixture.yaml", DRIVER_URL_PREFIX);
        assertThat(dataSource1, is(dataSource2));
    }
}
