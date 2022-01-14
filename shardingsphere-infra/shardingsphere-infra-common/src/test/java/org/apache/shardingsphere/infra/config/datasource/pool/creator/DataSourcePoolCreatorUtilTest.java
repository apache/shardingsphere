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

package org.apache.shardingsphere.infra.config.datasource.pool.creator;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.infra.config.datasource.props.DataSourceProperties;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class DataSourcePoolCreatorUtilTest {
    
    @Test
    public void assertGetDataSourcePropertiesMap() {
        Map<String, DataSourceProperties> actual = DataSourcePoolCreatorUtil.getDataSourcePropertiesMap(createDataSourceMap());
        assertThat(actual.size(), is(2));
        assertNotNull(actual.get("ds_0"));
        assertNotNull(actual.get("ds_1"));
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new LinkedHashMap<>(2, 1);
        result.put("ds_0", createDataSource("ds_0"));
        result.put("ds_1", createDataSource("ds_1"));
        return result;
    }
    
    private DataSource createDataSource(final String name) {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName("com.mysql.jdbc.Driver");
        result.setUrl("jdbc:mysql://localhost:3306/" + name);
        result.setUsername("root");
        result.setPassword("root");
        return result;
    }
}
