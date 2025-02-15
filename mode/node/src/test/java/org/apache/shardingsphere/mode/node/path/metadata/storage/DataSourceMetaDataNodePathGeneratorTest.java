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

package org.apache.shardingsphere.mode.node.path.metadata.storage;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class DataSourceMetaDataNodePathGeneratorTest {
    
    @Test
    void assertGetDataSourceRootPath() {
        assertThat(DataSourceMetaDataNodePathGenerator.getDataSourceRootPath("foo_db"), is("/metadata/foo_db/data_sources"));
    }
    
    @Test
    void assertGetStorageUnitsPath() {
        assertThat(DataSourceMetaDataNodePathGenerator.getStorageUnitsPath("foo_db"), is("/metadata/foo_db/data_sources/units"));
    }
    
    @Test
    void assertGetStorageNodesPath() {
        assertThat(DataSourceMetaDataNodePathGenerator.getStorageNodesPath("foo_db"), is("/metadata/foo_db/data_sources/nodes"));
    }
    
    @Test
    void assertGetStorageUnitPath() {
        assertThat(DataSourceMetaDataNodePathGenerator.getStorageUnitPath("foo_db", "foo_ds"), is("/metadata/foo_db/data_sources/units/foo_ds"));
    }
    
    @Test
    void assertGetStorageNodePath() {
        assertThat(DataSourceMetaDataNodePathGenerator.getStorageNodePath("foo_db", "foo_ds"), is("/metadata/foo_db/data_sources/nodes/foo_ds"));
    }
    
    @Test
    void assertGetStorageUnitVersion() {
        assertThat(DataSourceMetaDataNodePathGenerator.getStorageUnitVersion("foo_db", "foo_ds").getActiveVersionPath(),
                is("/metadata/foo_db/data_sources/units/foo_ds/active_version"));
        assertThat(DataSourceMetaDataNodePathGenerator.getStorageUnitVersion("foo_db", "foo_ds").getVersionsPath(), is("/metadata/foo_db/data_sources/units/foo_ds/versions"));
        assertThat(DataSourceMetaDataNodePathGenerator.getStorageUnitVersion("foo_db", "foo_ds").getVersionPath(0), is("/metadata/foo_db/data_sources/units/foo_ds/versions/0"));
    }
    
    @Test
    void assertGetStorageNodeVersion() {
        assertThat(DataSourceMetaDataNodePathGenerator.getStorageNodeVersion("foo_db", "foo_ds").getActiveVersionPath(),
                is("/metadata/foo_db/data_sources/nodes/foo_ds/active_version"));
        assertThat(DataSourceMetaDataNodePathGenerator.getStorageNodeVersion("foo_db", "foo_ds").getVersionsPath(), is("/metadata/foo_db/data_sources/nodes/foo_ds/versions"));
        assertThat(DataSourceMetaDataNodePathGenerator.getStorageNodeVersion("foo_db", "foo_ds").getVersionPath(0), is("/metadata/foo_db/data_sources/nodes/foo_ds/versions/0"));
    }
}
