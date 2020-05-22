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

import lombok.SneakyThrows;
import org.junit.Test;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.mock.env.MockEnvironment;

import javax.sql.DataSource;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class DataSourceMapSetterTest {
    
    @SneakyThrows
    @Test
    public void assetMergedAll() {
        MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.setProperty("spring.shardingsphere.datasource.names", "ds0,ds1");
        mockEnvironment.setProperty("spring.shardingsphere.datasource.common.type", "org.apache.commons.dbcp2.BasicDataSource");
        mockEnvironment.setProperty("spring.shardingsphere.datasource.common.driver-class-name", "org.h2.Driver");
        mockEnvironment.setProperty("spring.shardingsphere.datasource.common.max-total", "100");
        mockEnvironment.setProperty("spring.shardingsphere.datasource.common.username", "sa");
        mockEnvironment.setProperty("spring.shardingsphere.datasource.common.password", "");
        mockEnvironment.setProperty("spring.shardingsphere.datasource.ds0.url", "jdbc:h2:mem:ds;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL");
        mockEnvironment.setProperty("spring.shardingsphere.datasource.ds1.url", "jdbc:h2:mem:ds;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL");
        StandardEnvironment standardEnvironment = new StandardEnvironment();
        standardEnvironment.merge(mockEnvironment);
        Map<String, DataSource> dataSourceMap = DataSourceMapSetter.getDataSourceMap(standardEnvironment);
        assertThat(dataSourceMap.size(), is(2));
        assertThat(dataSourceMap.get("ds0").getConnection().getMetaData().getURL(), is("jdbc:h2:mem:ds"));
        assertThat(dataSourceMap.get("ds1").getConnection().getMetaData().getURL(), is("jdbc:h2:mem:ds"));
    }
    
    @SneakyThrows
    @Test
    public void assetMergedReplaceAndAdd() {
        MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.setProperty("spring.shardingsphere.datasource.names", "ds0,ds1");
        mockEnvironment.setProperty("spring.shardingsphere.datasource.common.type", "org.apache.commons.dbcp2.BasicDataSource");
        mockEnvironment.setProperty("spring.shardingsphere.datasource.common.driver-class-name", "org.h2.Driver");
        mockEnvironment.setProperty("spring.shardingsphere.datasource.common.max-total", "100");
        mockEnvironment.setProperty("spring.shardingsphere.datasource.common.username", "Asa");
        mockEnvironment.setProperty("spring.shardingsphere.datasource.common.password", "123");
        mockEnvironment.setProperty("spring.shardingsphere.datasource.ds0.url", "jdbc:h2:mem:ds;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL");
        mockEnvironment.setProperty("spring.shardingsphere.datasource.ds0.username", "sa");
        mockEnvironment.setProperty("spring.shardingsphere.datasource.ds0.max-total", "50");
        mockEnvironment.setProperty("spring.shardingsphere.datasource.ds0.password", "");
        mockEnvironment.setProperty("spring.shardingsphere.datasource.ds1.url", "jdbc:h2:mem:ds;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL");
        mockEnvironment.setProperty("spring.shardingsphere.datasource.ds1.username", "sa");
        mockEnvironment.setProperty("spring.shardingsphere.datasource.ds1.max-total", "150");
        mockEnvironment.setProperty("spring.shardingsphere.datasource.ds1.password", "");
        StandardEnvironment standardEnvironment = new StandardEnvironment();
        standardEnvironment.merge(mockEnvironment);
        Map<String, DataSource> dataSourceMap = DataSourceMapSetter.getDataSourceMap(standardEnvironment);
        assertThat(dataSourceMap.size(), is(2));
        assertThat(dataSourceMap.get("ds0").getConnection().getMetaData().getUserName(), is("SA"));
        assertThat(dataSourceMap.get("ds1").getConnection().getMetaData().getUserName(), is("SA"));
    }
}

