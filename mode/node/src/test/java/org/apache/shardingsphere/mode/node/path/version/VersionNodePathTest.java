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

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionNodePathTest {
    
    private final VersionNodePath versionPatternNodePath = new VersionNodePath("/metadata/([\\w\\-]+)/schemas/([\\w\\-]+)/tables/([\\w\\-]+)");
    
    @Test
    void assertGetActiveVersionPath() {
        assertThat(new VersionNodePath("foo").getActiveVersionPath(), is("foo/active_version"));
    }
    
    @Test
    void assertGetVersionsPath() {
        assertThat(new VersionNodePath("foo").getVersionsPath(), is("foo/versions"));
    }
    
    @Test
    void assertGetVersionPath() {
        assertThat(new VersionNodePath("foo").getVersionPath(0), is("foo/versions/0"));
    }
    
    @Test
    void assertIsActiveVersionPath() {
        assertTrue(versionPatternNodePath.isActiveVersionPath("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/active_version"));
        assertFalse(versionPatternNodePath.isVersionPath("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/versions"));
    }
    
    @Test
    void assertFindIdentifierByActiveVersionPath() {
        String path = "/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/active_version";
        assertThat(versionPatternNodePath.findIdentifierByActiveVersionPath(path, 1), is(Optional.of("foo_db")));
        assertThat(versionPatternNodePath.findIdentifierByActiveVersionPath(path, 2), is(Optional.of("foo_schema")));
        assertThat(versionPatternNodePath.findIdentifierByActiveVersionPath(path, 3), is(Optional.of("foo_tbl")));
    }
    
    @Test
    void assertNotFindIdentifierByActiveVersionPath() {
        String path = "/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/versions";
        assertFalse(versionPatternNodePath.findIdentifierByActiveVersionPath(path, 1).isPresent());
    }
    
    @Test
    void assertIsVersionPath() {
        assertTrue(versionPatternNodePath.isVersionPath("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/versions/0"));
        assertFalse(versionPatternNodePath.isVersionPath("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/versions"));
        assertFalse(versionPatternNodePath.isVersionPath("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/versions/xxx"));
    }
    
    @Test
    void assertFindIdentifierByVersionsPath() {
        String path = "/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/versions";
        assertThat(versionPatternNodePath.findIdentifierByVersionsPath(path, 1), is(Optional.of("foo_db")));
        assertThat(versionPatternNodePath.findIdentifierByVersionsPath(path, 2), is(Optional.of("foo_schema")));
        assertThat(versionPatternNodePath.findIdentifierByVersionsPath(path, 3), is(Optional.of("foo_tbl")));
    }
    
    @Test
    void assertNotFindIdentifierByVersionsPath() {
        String path = "/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/active_version";
        assertFalse(versionPatternNodePath.findIdentifierByVersionsPath(path, 1).isPresent());
    }
}
