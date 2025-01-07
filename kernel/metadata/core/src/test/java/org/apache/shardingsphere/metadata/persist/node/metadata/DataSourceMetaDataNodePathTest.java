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

class DataSourceMetaDataNodePathTest {
    
    @Test
    void assertGetDataSourceUnitsPath() {
        assertThat(DataSourceMetaDataNodePath.getDataSourceUnitsPath("foo_db"), is("/metadata/foo_db/data_sources/units"));
    }
    
    @Test
    void assertGetDataSourceNodesPath() {
        assertThat(DataSourceMetaDataNodePath.getDataSourceNodesPath("foo_db"), is("/metadata/foo_db/data_sources/nodes"));
    }
    
    @Test
    void assertGetDataSourceUnitPath() {
        assertThat(DataSourceMetaDataNodePath.getDataSourceUnitPath("foo_db", "foo_ds"), is("/metadata/foo_db/data_sources/units/foo_ds"));
    }
    
    @Test
    void assertGetDataSourceNodePath() {
        assertThat(DataSourceMetaDataNodePath.getDataSourceNodePath("foo_db", "foo_ds"), is("/metadata/foo_db/data_sources/nodes/foo_ds"));
    }
    
    @Test
    void assertGetDataSourceUnitVersionPath() {
        assertThat(DataSourceMetaDataNodePath.getDataSourceUnitVersionPath("foo_db", "foo_ds", "0"), is("/metadata/foo_db/data_sources/units/foo_ds/versions/0"));
    }
    
    @Test
    void assertGetDataSourceUnitVersionsPath() {
        assertThat(DataSourceMetaDataNodePath.getDataSourceUnitVersionsPath("foo_db", "foo_ds"), is("/metadata/foo_db/data_sources/units/foo_ds/versions"));
    }
    
    @Test
    void assertGetDataSourceUnitActiveVersionPath() {
        assertThat(DataSourceMetaDataNodePath.getDataSourceUnitActiveVersionPath("foo_db", "foo_ds"), is("/metadata/foo_db/data_sources/units/foo_ds/active_version"));
    }
    
    @Test
    void assertGetDataSourceNodeVersionsPath() {
        assertThat(DataSourceMetaDataNodePath.getDataSourceNodeVersionsPath("foo_db", "foo_ds"), is("/metadata/foo_db/data_sources/nodes/foo_ds/versions"));
    }
    
    @Test
    void assertGetDataSourceNodeVersionPath() {
        assertThat(DataSourceMetaDataNodePath.getDataSourceNodeVersionPath("foo_db", "foo_ds", "1"), is("/metadata/foo_db/data_sources/nodes/foo_ds/versions/1"));
    }
    
    @Test
    void assertGetDataSourceNodeActiveVersionPath() {
        assertThat(DataSourceMetaDataNodePath.getDataSourceNodeActiveVersionPath("foo_db", "foo_ds"), is("/metadata/foo_db/data_sources/nodes/foo_ds/active_version"));
    }
    
    @Test
    void assertIsDataSourcesPath() {
        assertTrue(DataSourceMetaDataNodePath.isDataSourcesPath("/metadata/logic_db/data_sources/foo_ds"));
    }
    
    @Test
    void assertFindDataSourceNameByDataSourceUnitActiveVersionPath() {
        Optional<String> actual = DataSourceMetaDataNodePath.findDataSourceNameByDataSourceUnitActiveVersionPath("/metadata/logic_db/data_sources/units/foo_ds/active_version");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_ds"));
    }
    
    @Test
    void assertFindDataSourceNameByDataSourceUnitActiveVersionPathIfNotFound() {
        assertFalse(DataSourceMetaDataNodePath.findDataSourceNameByDataSourceUnitActiveVersionPath("/xxx/logic_db/data_sources/units/foo_ds/active_version").isPresent());
    }
    
    @Test
    void assertFindDataSourceNameByDataSourceUnitPath() {
        Optional<String> actual = DataSourceMetaDataNodePath.findDataSourceNameByDataSourceUnitPath("/metadata/logic_db/data_sources/units/foo_ds");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_ds"));
    }
    
    @Test
    void assertFindDataSourceNameByDataSourceUnitPathIfNotFound() {
        assertFalse(DataSourceMetaDataNodePath.findDataSourceNameByDataSourceUnitPath("/xxx/logic_db/data_sources/units/foo_ds").isPresent());
    }
    
    @Test
    void assertFindDataSourceNameByDataSourceNodeActiveVersionPath() {
        Optional<String> actual = DataSourceMetaDataNodePath.findDataSourceNameByDataSourceNodeActiveVersionPath("/metadata/logic_db/data_sources/nodes/foo_ds/active_version");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_ds"));
    }
    
    @Test
    void assertFindDataSourceNameByDataSourceNodeActiveVersionPathIfNotFound() {
        assertFalse(DataSourceMetaDataNodePath.findDataSourceNameByDataSourceNodeActiveVersionPath("/xxx/logic_db/data_sources/nodes/foo_ds/active_version").isPresent());
    }
    
    @Test
    void assertFindDataSourceNameByDataSourceNodePath() {
        Optional<String> actual = DataSourceMetaDataNodePath.findDataSourceNameByDataSourceNodePath("/metadata/logic_db/data_sources/nodes/foo_ds");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_ds"));
    }
    
    @Test
    void assertFindDataSourceNameByDataSourceNodePathIfNotFound() {
        assertFalse(DataSourceMetaDataNodePath.findDataSourceNameByDataSourceNodePath("/xxx/logic_db/data_sources/nodes/foo_ds").isPresent());
    }
    
    @Test
    void assertIsDataSourceUnitActiveVersionPath() {
        assertTrue(DataSourceMetaDataNodePath.isDataSourceUnitActiveVersionPath("/metadata/logic_db/data_sources/units/foo_ds/active_version"));
    }
    
    @Test
    void assertIsDataSourceUnitPath() {
        assertTrue(DataSourceMetaDataNodePath.isDataSourceUnitPath("/metadata/logic_db/data_sources/units/foo_ds"));
    }
    
    @Test
    void assertIsDataSourceNodeActiveVersionPath() {
        assertTrue(DataSourceMetaDataNodePath.isDataSourceNodeActiveVersionPath("/metadata/logic_db/data_sources/nodes/foo_ds/active_version"));
    }
    
    @Test
    void assertIsDataSourceNodePath() {
        assertTrue(DataSourceMetaDataNodePath.isDataSourceNodePath("/metadata/logic_db/data_sources/nodes/foo_ds"));
    }
}
