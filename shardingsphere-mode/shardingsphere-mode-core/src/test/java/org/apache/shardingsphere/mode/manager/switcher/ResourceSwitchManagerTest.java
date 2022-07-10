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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResource;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ResourceSwitchManagerTest {

    @Test
    public void assertCreate() {
        MockedDataSource dataSource = new MockedDataSource();
        ShardingSphereResource resource = new ShardingSphereResource(Collections.singletonMap("foo_ds", dataSource));
        DataSourceProperties dataSourceProperties =
                new DataSourceProperties(MockedDataSource.class.getName(), createUserProperties("foo"));
        ResourceSwitchManager manager = new ResourceSwitchManager();
        SwitchingResource switchingResource = manager.create(resource, Collections.singletonMap("db", dataSourceProperties));
        assertTrue(switchingResource.getNewDataSources().containsKey("db"));
    }

    private Map<String, Object> createUserProperties(final String username) {
        Map<String, Object> result = new LinkedHashMap<>(1, 1);
        result.put("username", username);
        return result;
    }
}
