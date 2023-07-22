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

import org.apache.shardingsphere.infra.datasource.storage.StorageResource;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SwitchingResourceTest {
    
    @Test
    void assertCloseStaleDataSources() {
        MockedDataSource staleDataSource = new MockedDataSource();
        ShardingSphereResourceMetaData resourceMetaData = mock(ShardingSphereResourceMetaData.class);
        StorageResource newStorageResource = new StorageResource(Collections.singletonMap("new_ds", new MockedDataSource()), Collections.emptyMap());
        StorageResource staleStorageResource = new StorageResource(Collections.singletonMap("stale_ds", staleDataSource), Collections.emptyMap());
        new SwitchingResource(resourceMetaData, newStorageResource, staleStorageResource, Collections.emptyMap()).closeStaleDataSources();
        verify(resourceMetaData).close(staleDataSource);
    }
}
