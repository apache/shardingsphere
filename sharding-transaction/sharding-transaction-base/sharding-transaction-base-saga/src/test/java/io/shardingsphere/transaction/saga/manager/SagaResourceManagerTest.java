/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.transaction.saga.manager;

import org.junit.Test;

import javax.sql.DataSource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import io.shardingsphere.core.exception.ShardingException;

public class SagaResourceManagerTest {
    
    private final SagaResourceManager resourceManager = new SagaResourceManager();
    
    @Test
    public void assertRegisterDataSourceMap() {
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("ds1", mock(DataSource.class));
        resourceManager.registerDataSourceMap(dataSourceMap);
        assertEquals(resourceManager.getDataSourceMap().size(), 1);
        dataSourceMap = new HashMap<>();
        dataSourceMap.put("ds2", mock(DataSource.class));
        dataSourceMap.put("ds3", mock(DataSource.class));
        resourceManager.registerDataSourceMap(dataSourceMap);
        assertEquals(resourceManager.getDataSourceMap().size(), 3);
    }
    
    @Test(expected = ShardingException.class)
    public void assertRegisterDuplicateDataSourceMap() {
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("ds1", mock(DataSource.class));
        resourceManager.registerDataSourceMap(dataSourceMap);
        assertEquals(resourceManager.getDataSourceMap().size(), 1);
        dataSourceMap = new HashMap<>();
        dataSourceMap.put("ds2", mock(DataSource.class));
        dataSourceMap.put("ds1", mock(DataSource.class));
        resourceManager.registerDataSourceMap(dataSourceMap);
    }
    
    @Test
    public void assertReleaseDataSourceMap() {
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("ds1", mock(DataSource.class));
        dataSourceMap.put("ds2", mock(DataSource.class));
        dataSourceMap.put("ds3", mock(DataSource.class));
        resourceManager.registerDataSourceMap(dataSourceMap);
        assertEquals(resourceManager.getDataSourceMap().size(), 3);
        dataSourceMap = new HashMap<>();
        dataSourceMap.put("ds1", mock(DataSource.class));
        dataSourceMap.put("ds2", mock(DataSource.class));
        resourceManager.releaseDataSourceMap(dataSourceMap);
        assertEquals(resourceManager.getDataSourceMap().size(), 1);
        dataSourceMap = new HashMap<>();
        dataSourceMap.put("ds3", mock(DataSource.class));
        resourceManager.releaseDataSourceMap(dataSourceMap);
        assertEquals(resourceManager.getDataSourceMap().size(), 0);
    }
}