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

package org.apache.shardingsphere.infra.datasource.pool.creator;

import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

class DataSourcePoolCreatorTest {
    
    @Test
    void assertCreateMap() {
        Map<String, DataSource> actual = DataSourcePoolCreator.create(Collections.singletonMap("foo_ds", new DataSourcePoolProperties(MockedDataSource.class.getName(), createProperties())), true);
        assertThat(actual.size(), is(1));
        assertDataSource((MockedDataSource) actual.get("foo_ds"));
    }
    
    @Test
    void assertCreate() {
        assertDataSource((MockedDataSource) DataSourcePoolCreator.create(new DataSourcePoolProperties(MockedDataSource.class.getName(), createProperties())));
    }
    
    private Map<String, Object> createProperties() {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("url", "jdbc:mock://127.0.0.1/foo_ds");
        result.put("username", "root");
        result.put("password", "root");
        result.put("driverClassName", MockedDataSource.class.getName());
        result.put("connectionTimeout", "120");
        return result;
    }
    
    private void assertDataSource(final MockedDataSource actual) {
        assertThat(actual.getUrl(), is("jdbc:mock://127.0.0.1/foo_ds"));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getPassword(), is("root"));
        assertThat(actual.getMaxPoolSize(), is(100));
        assertNull(actual.getMinPoolSize());
        assertThat(actual.getDriverClassName(), is(MockedDataSource.class.getName()));
        assertThat(actual.getConnectionTimeout(), is(Duration.ofSeconds(120)));
    }
}
