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

package org.apache.shardingsphere.mode.node.path.version;

import org.apache.shardingsphere.mode.node.path.type.database.metadata.schema.TableMetadataNodePath;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionNodePathTest {
    
    @Test
    void assertGetActiveVersionPath() {
        assertThat(new VersionNodePath(new TableMetadataNodePath("foo_db", "foo_schema", "foo_tbl")).getActiveVersionPath(), is("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/active_version"));
    }
    
    @Test
    void assertGetVersionsPath() {
        assertThat(new VersionNodePath(new TableMetadataNodePath("foo_db", "foo_schema", "foo_tbl")).getVersionsPath(), is("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/versions"));
    }
    
    @Test
    void assertGetVersionPath() {
        assertThat(new VersionNodePath(new TableMetadataNodePath("foo_db", "foo_schema", "foo_tbl")).getVersionPath(0), is("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/versions/0"));
    }
    
    @Test
    void assertIsActiveVersionPath() {
        assertTrue(VersionNodePath.isActiveVersionPath("foo/active_version"));
        assertFalse(VersionNodePath.isActiveVersionPath("foo/versions"));
    }
}
