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

import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResource;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

public final class SwitchingResourceTest {
    
    @Test
    public void assertCloseStaleDataSources() throws InterruptedException {
        MockedDataSource dataSource = new MockedDataSource();
        ShardingSphereResource shardingSphereResource = new ShardingSphereResource(Collections.singletonMap("foo_ds", dataSource));
        ShardingSphereResource spyResource = spy(shardingSphereResource);
        SwitchingResource switchingResource = new SwitchingResource(spyResource,
                Collections.singletonMap("foo_ds", dataSource), Collections.singletonMap("foo_ds", dataSource));
        switchingResource.closeStaleDataSources();
        while (null == dataSource.getClosed() || !dataSource.getClosed()) {
            Thread.sleep(10L);
        }
        assertTrue(dataSource.getClosed());
        verify(spyResource, times(1)).close(dataSource);
    }
    
}
