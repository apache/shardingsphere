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

package org.apache.shardingsphere.mode.node.path.metadata.database;

import org.apache.shardingsphere.mode.node.path.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePath;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class TableMetadataNodePathTest {
    
    @Test
    void assertToPath() {
        assertThat(NodePathGenerator.toPath(new TableMetadataNodePath("foo_db", null, null), false), is("/metadata/foo_db/schemas"));
        assertThat(NodePathGenerator.toPath(new TableMetadataNodePath("foo_db", "foo_schema", null), false), is("/metadata/foo_db/schemas/foo_schema/tables"));
        assertThat(NodePathGenerator.toPath(new TableMetadataNodePath("foo_db", "foo_schema", null), true), is("/metadata/foo_db/schemas/foo_schema"));
        assertThat(NodePathGenerator.toPath(new TableMetadataNodePath("foo_db", "foo_schema", "foo_tbl"), false), is("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl"));
    }
    
    @Test
    void assertToVersionPath() {
        VersionNodePath versionNodePath = NodePathGenerator.toVersionPath(new TableMetadataNodePath("foo_db", "foo_schema", "foo_tbl"));
        assertThat(versionNodePath.getActiveVersionPath(), is("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/active_version"));
        assertThat(versionNodePath.getVersionsPath(), is("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/versions"));
        assertThat(versionNodePath.getVersionPath(0), is("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/versions/0"));
    }
}
