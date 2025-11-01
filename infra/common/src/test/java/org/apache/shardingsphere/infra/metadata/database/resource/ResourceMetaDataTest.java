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

package org.apache.shardingsphere.infra.metadata.database.resource;

import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceMetaDataTest {
    
    @Test
    void assertGetAllInstanceDataSourceNames() {
        Map<String, DataSource> dataSources = new LinkedHashMap<>(2, 1F);
        dataSources.put("foo_ds", new MockedDataSource());
        dataSources.put("bar_ds", new MockedDataSource());
        assertThat(new ResourceMetaData(dataSources).getAllInstanceDataSourceNames().size(), is(1));
    }
    
    @Test
    void assertGetNotExistedDataSources() {
        assertTrue(new ResourceMetaData(Collections.singletonMap("foo_ds", new MockedDataSource())).getNotExistedDataSources(Collections.singleton("foo_ds")).isEmpty());
    }
    
    @Test
    void assertGetDataSourceMap() {
        assertThat(new ResourceMetaData(Collections.singletonMap("foo_ds", new MockedDataSource())).getDataSourceMap().size(), is(1));
        assertTrue(new ResourceMetaData(Collections.singletonMap("foo_ds", new MockedDataSource())).getDataSourceMap().containsKey("foo_ds"));
    }
}
