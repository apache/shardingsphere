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

package org.apache.shardingsphere.spring.boot.datasource;

import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Test;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.mock.env.MockEnvironment;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class DataSourceMapSetterTest {
    
    @Test
    public void assertGetDataSourceMap() throws SQLException {
        MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.setProperty("spring.shardingsphere.datasource.names", "ds0,ds1");
        mockEnvironment.setProperty("spring.shardingsphere.datasource.ds0.url", "jdbc:mock://127.0.0.1/ds_0");
        mockEnvironment.setProperty("spring.shardingsphere.datasource.ds0.type", MockedDataSource.class.getName());
        mockEnvironment.setProperty("spring.shardingsphere.datasource.ds0.username", "sa");
        mockEnvironment.setProperty("spring.shardingsphere.datasource.ds0.password", "");
        mockEnvironment.setProperty("spring.shardingsphere.datasource.ds0.maxPoolSize", "50");
        mockEnvironment.setProperty("spring.shardingsphere.datasource.ds1.url", "jdbc:mock://127.0.0.1/ds_1");
        mockEnvironment.setProperty("spring.shardingsphere.datasource.ds1.type", MockedDataSource.class.getName());
        mockEnvironment.setProperty("spring.shardingsphere.datasource.ds1.username", "sa");
        mockEnvironment.setProperty("spring.shardingsphere.datasource.ds1.password", "");
        mockEnvironment.setProperty("spring.shardingsphere.datasource.ds1.max-pool-size", "150");
        StandardEnvironment standardEnvironment = new StandardEnvironment();
        standardEnvironment.merge(mockEnvironment);
        Map<String, DataSource> dataSourceMap = DataSourceMapSetter.getDataSourceMap(standardEnvironment);
        assertThat(dataSourceMap.size(), is(2));
        assertThat(dataSourceMap.get("ds0").getConnection().getMetaData().getURL(), is("jdbc:mock://127.0.0.1/ds_0"));
        assertThat(dataSourceMap.get("ds1").getConnection().getMetaData().getURL(), is("jdbc:mock://127.0.0.1/ds_1"));
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertInvalidDataSourceNames() {
        MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.setProperty("spring.shardingsphere.datasource.names", "ds0,ds1");
        mockEnvironment.setProperty("spring.shardingsphere.datasource.ds0.url", "jdbc:mock://127.0.0.1/ds_0");
        mockEnvironment.setProperty("spring.shardingsphere.datasource.ds0.type", MockedDataSource.class.getName());
        mockEnvironment.setProperty("spring.shardingsphere.datasource.ds0.username", "sa");
        mockEnvironment.setProperty("spring.shardingsphere.datasource.ds0.password", "");
        mockEnvironment.setProperty("spring.shardingsphere.datasource.ds0.maxPoolSize", "50");
        StandardEnvironment standardEnvironment = new StandardEnvironment();
        standardEnvironment.merge(mockEnvironment);
        DataSourceMapSetter.getDataSourceMap(standardEnvironment);
    }
}
