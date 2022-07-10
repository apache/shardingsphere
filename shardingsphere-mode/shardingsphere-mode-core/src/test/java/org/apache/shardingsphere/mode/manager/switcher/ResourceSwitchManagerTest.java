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

import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResource;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ResourceSwitchManagerTest {
    
    @Test
    public void assertCreate() throws InterruptedException {
        Map<String, DataSource> dataSourceMap = createDataSourceMap();
        SwitchingResource actual = new ResourceSwitchManager().create(new ShardingSphereResource(dataSourceMap), createToBeChangedDataSourcePropsMap());
        assertNewDataSources(actual);
        actual.closeStaleDataSources();
        assertStaleDataSources(dataSourceMap);
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(3, 1);
        result.put("not_change", new MockedDataSource());
        result.put("replace", new MockedDataSource());
        result.put("delete", new MockedDataSource());
        return result;
    }
    
    private Map<String, DataSourceProperties> createToBeChangedDataSourcePropsMap() {
        Map<String, DataSourceProperties> result = new HashMap<>(3, 1);
        result.put("new", new DataSourceProperties(MockedDataSource.class.getName(), Collections.emptyMap()));
        result.put("not_change", new DataSourceProperties(MockedDataSource.class.getName(), Collections.emptyMap()));
        result.put("replace", new DataSourceProperties(MockedDataSource.class.getName(), Collections.singletonMap("password", "new_pwd")));
        return result;
    }
    
    private void assertNewDataSources(final SwitchingResource actual) {
        assertThat(actual.getNewDataSources().size(), is(3));
        assertTrue(actual.getNewDataSources().containsKey("not_change"));
        assertTrue(actual.getNewDataSources().containsKey("new"));
        assertTrue(actual.getNewDataSources().containsKey("replace"));
    }
    
    private void assertStaleDataSources(final Map<String, DataSource> originalDataSourceMap) throws InterruptedException {
        assertStaleDataSource((MockedDataSource) originalDataSourceMap.get("delete"));
        assertStaleDataSource((MockedDataSource) originalDataSourceMap.get("replace"));
        assertNotStaleDataSource((MockedDataSource) originalDataSourceMap.get("not_change"));
    }
    
    @SuppressWarnings("BusyWait")
    private void assertStaleDataSource(final MockedDataSource dataSource) throws InterruptedException {
        while (!dataSource.isClosed()) {
            Thread.sleep(10L);
        }
        assertTrue(dataSource.isClosed());
    }
    
    private void assertNotStaleDataSource(final MockedDataSource dataSource) {
        assertFalse(dataSource.isClosed());
    }
}
