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

package org.apache.shardingsphere.metadata.persist.node.metadata;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataSourceMetaDataNodeTest {
    
    @Test
    void assertGetDataSourceUnitsNode() {
        assertThat(DataSourceMetaDataNode.getDataSourceUnitsNode("foo_db"), is("/metadata/foo_db/data_sources/units"));
    }
    
    @Test
    void assertGetDataSourceNodesNode() {
        assertThat(DataSourceMetaDataNode.getDataSourceNodesNode("foo_db"), is("/metadata/foo_db/data_sources/nodes"));
    }
    
    @Test
    void assertGetDataSourceUnitNode() {
        assertThat(DataSourceMetaDataNode.getDataSourceUnitNode("foo_db", "foo_ds"), is("/metadata/foo_db/data_sources/units/foo_ds"));
    }
    
    @Test
    void assertGetDataSourceNodeNode() {
        assertThat(DataSourceMetaDataNode.getDataSourceNodeNode("foo_db", "foo_ds"), is("/metadata/foo_db/data_sources/nodes/foo_ds"));
    }
    
    @Test
    void assertGetDataSourceUnitVersionNode() {
        assertThat(DataSourceMetaDataNode.getDataSourceUnitVersionNode("foo_db", "foo_ds", "0"), is("/metadata/foo_db/data_sources/units/foo_ds/versions/0"));
    }
    
    @Test
    void assertGetDataSourceUnitVersionsNode() {
        assertThat(DataSourceMetaDataNode.getDataSourceUnitVersionsNode("foo_db", "foo_ds"), is("/metadata/foo_db/data_sources/units/foo_ds/versions"));
    }
    
    @Test
    void assertGetDataSourceUnitActiveVersionNode() {
        assertThat(DataSourceMetaDataNode.getDataSourceUnitActiveVersionNode("foo_db", "foo_ds"), is("/metadata/foo_db/data_sources/units/foo_ds/active_version"));
    }
    
    @Test
    void assertGetDataSourceNodeVersionsNode() {
        assertThat(DataSourceMetaDataNode.getDataSourceNodeVersionsNode("foo_db", "foo_ds"), is("/metadata/foo_db/data_sources/nodes/foo_ds/versions"));
    }
    
    @Test
    void assertGetDataSourceNodeVersionNode() {
        assertThat(DataSourceMetaDataNode.getDataSourceNodeVersionNode("foo_db", "foo_ds", "1"), is("/metadata/foo_db/data_sources/nodes/foo_ds/versions/1"));
    }
    
    @Test
    void assertGetDataSourceNodeActiveVersionNode() {
        assertThat(DataSourceMetaDataNode.getDataSourceNodeActiveVersionNode("foo_db", "foo_ds"), is("/metadata/foo_db/data_sources/nodes/foo_ds/active_version"));
    }
    
    @Test
    void assertIsDataSourcesNode() {
        assertTrue(DataSourceMetaDataNode.isDataSourcesNode("/metadata/logic_db/data_sources/foo_ds"));
    }
    
    @Test
    void assertGetDataSourceNameByDataSourceUnitActiveVersionNode() {
        Optional<String> actual = DataSourceMetaDataNode.getDataSourceNameByDataSourceUnitActiveVersionNode("/metadata/logic_db/data_sources/units/foo_ds/active_version");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_ds"));
    }
    
    @Test
    void assertGetDataSourceNameByDataSourceUnitActiveVersionNodeIfNotFound() {
        assertFalse(DataSourceMetaDataNode.getDataSourceNameByDataSourceUnitActiveVersionNode("/xxx/logic_db/data_sources/units/foo_ds/active_version").isPresent());
    }
    
    @Test
    void assertGetDataSourceNameByDataSourceUnitNode() {
        Optional<String> actual = DataSourceMetaDataNode.getDataSourceNameByDataSourceUnitNode("/metadata/logic_db/data_sources/units/foo_ds");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_ds"));
    }
    
    @Test
    void assertGetDataSourceNameByDataSourceUnitNodeIfNotFound() {
        assertFalse(DataSourceMetaDataNode.getDataSourceNameByDataSourceUnitNode("/xxx/logic_db/data_sources/units/foo_ds").isPresent());
    }
    
    @Test
    void assertGetDataSourceNameByDataSourceNodeActiveVersionNode() {
        Optional<String> actual = DataSourceMetaDataNode.getDataSourceNameByDataSourceNodeActiveVersionNode("/metadata/logic_db/data_sources/nodes/foo_ds/active_version");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_ds"));
    }
    
    @Test
    void assertGetDataSourceNameByDataSourceNodeActiveVersionNodeIfNotFound() {
        assertFalse(DataSourceMetaDataNode.getDataSourceNameByDataSourceNodeActiveVersionNode("/xxx/logic_db/data_sources/nodes/foo_ds/active_version").isPresent());
    }
    
    @Test
    void assertGetDataSourceNameByDataSourceNodeNode() {
        Optional<String> actual = DataSourceMetaDataNode.getDataSourceNameByDataSourceNodeNode("/metadata/logic_db/data_sources/nodes/foo_ds");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_ds"));
    }
    
    @Test
    void assertGetDataSourceNameByDataSourceNodeNodeIfNotFound() {
        assertFalse(DataSourceMetaDataNode.getDataSourceNameByDataSourceNodeNode("/xxx/logic_db/data_sources/nodes/foo_ds").isPresent());
    }
    
    @Test
    void assertIsDataSourceUnitActiveVersionNode() {
        assertTrue(DataSourceMetaDataNode.isDataSourceUnitActiveVersionNode("/metadata/logic_db/data_sources/units/foo_ds/active_version"));
    }
    
    @Test
    void assertIsDataSourceUnitNode() {
        assertTrue(DataSourceMetaDataNode.isDataSourceUnitNode("/metadata/logic_db/data_sources/units/foo_ds"));
    }
    
    @Test
    void assertIsDataSourceNodeActiveVersionNode() {
        assertTrue(DataSourceMetaDataNode.isDataSourceNodeActiveVersionNode("/metadata/logic_db/data_sources/nodes/foo_ds/active_version"));
    }
    
    @Test
    void assertIsDataSourceNodeNode() {
        assertTrue(DataSourceMetaDataNode.isDataSourceNodeNode("/metadata/logic_db/data_sources/nodes/foo_ds"));
    }
}
