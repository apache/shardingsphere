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

package org.apache.shardingsphere.mode.manager.switcher;

import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceSwitchManagerTest {
    
    @Test
    void assertCreate() {
        Map<String, DataSource> dataSourceMap = createDataSourceMap();
        SwitchingResource actual = new ResourceSwitchManager().create(new ResourceMetaData(dataSourceMap), createToBeChangedDataSourcePoolPropertiesMap());
        assertNewDataSources(actual);
        actual.closeStaleDataSources();
        assertStaleDataSources(dataSourceMap);
    }
    
    @Test
    void assertCreateByAlterDataSourcePoolProperties() {
        Map<String, DataSource> dataSourceMap = new HashMap<>(3, 1F);
        dataSourceMap.put("ds_0", new MockedDataSource());
        dataSourceMap.put("ds_1", new MockedDataSource());
        SwitchingResource actual = new ResourceSwitchManager().createByAlterDataSourcePoolProperties(new ResourceMetaData(dataSourceMap), Collections.emptyMap());
        assertTrue(actual.getNewDataSources().isEmpty());
        assertThat(actual.getStaleDataSources().size(), is(2));
        actual.closeStaleDataSources();
        assertStaleDataSource((MockedDataSource) dataSourceMap.get("ds_0"));
        assertStaleDataSource((MockedDataSource) dataSourceMap.get("ds_1"));
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(3, 1F);
        result.put("not_change", new MockedDataSource());
        result.put("replace", new MockedDataSource());
        return result;
    }
    
    private Map<String, DataSourcePoolProperties> createToBeChangedDataSourcePoolPropertiesMap() {
        Map<String, DataSourcePoolProperties> result = new HashMap<>(3, 1F);
        result.put("new", new DataSourcePoolProperties(MockedDataSource.class.getName(), getDataSourcePoolProperties(2)));
        result.put("not_change", new DataSourcePoolProperties(MockedDataSource.class.getName(), getDataSourcePoolProperties(2)));
        Map<String, Object> replaceProps = getDataSourcePoolProperties(3);
        replaceProps.put("password", "new_pwd");
        result.put("replace", new DataSourcePoolProperties(MockedDataSource.class.getName(), replaceProps));
        return result;
    }
    
    private Map<String, Object> getDataSourcePoolProperties(final int initialCapacity) {
        Map<String, Object> result = new LinkedHashMap<>(initialCapacity, 1F);
        result.put("url", new MockedDataSource().getUrl());
        result.put("username", "root");
        return result;
    }
    
    private void assertNewDataSources(final SwitchingResource actual) {
        assertThat(actual.getNewDataSources().size(), is(3));
        assertTrue(actual.getNewDataSources().containsKey(new StorageNode("not_change")));
        assertTrue(actual.getNewDataSources().containsKey(new StorageNode("new")));
        assertTrue(actual.getNewDataSources().containsKey(new StorageNode("replace")));
    }
    
    private void assertStaleDataSources(final Map<String, DataSource> originalDataSourceMap) {
        assertStaleDataSource((MockedDataSource) originalDataSourceMap.get("replace"));
        assertNotStaleDataSource((MockedDataSource) originalDataSourceMap.get("not_change"));
    }
    
    private void assertStaleDataSource(final MockedDataSource dataSource) {
        Awaitility.await().atMost(1L, TimeUnit.MINUTES).pollInterval(10L, TimeUnit.MILLISECONDS).until(dataSource::isClosed);
    }
    
    private void assertNotStaleDataSource(final MockedDataSource dataSource) {
        assertFalse(dataSource.isClosed());
    }
}
