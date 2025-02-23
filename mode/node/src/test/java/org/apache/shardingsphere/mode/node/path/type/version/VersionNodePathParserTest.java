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

package org.apache.shardingsphere.mode.node.path.type.version;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionNodePathParserTest {
    
    private final VersionNodePathParser parser = new VersionNodePathParser("/metadata/([\\w\\-]+)/schemas/([\\w\\-]+)/tables/([\\w\\-]+)");
    
    @Test
    void assertIsActiveVersionPath() {
        assertTrue(parser.isActiveVersionPath("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/active_version"));
        assertFalse(parser.isVersionPath("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/versions"));
    }
    
    @Test
    void assertFindIdentifierByActiveVersionPath() {
        String path = "/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/active_version";
        assertThat(parser.findIdentifierByActiveVersionPath(path, 1), is(Optional.of("foo_db")));
        assertThat(parser.findIdentifierByActiveVersionPath(path, 2), is(Optional.of("foo_schema")));
        assertThat(parser.findIdentifierByActiveVersionPath(path, 3), is(Optional.of("foo_tbl")));
    }
    
    @Test
    void assertNotFindIdentifierByActiveVersionPath() {
        String path = "/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/versions";
        assertFalse(parser.findIdentifierByActiveVersionPath(path, 1).isPresent());
    }
    
    @Test
    void assertIsVersionPath() {
        assertTrue(parser.isVersionPath("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/versions/0"));
        assertFalse(parser.isVersionPath("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/versions"));
        assertFalse(parser.isVersionPath("/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/versions/xxx"));
    }
    
    @Test
    void assertFindIdentifierByVersionsPath() {
        String path = "/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/versions/0";
        assertThat(parser.findIdentifierByVersionsPath(path, 1), is(Optional.of("foo_db")));
        assertThat(parser.findIdentifierByVersionsPath(path, 2), is(Optional.of("foo_schema")));
        assertThat(parser.findIdentifierByVersionsPath(path, 3), is(Optional.of("foo_tbl")));
    }
    
    @Test
    void assertNotFindIdentifierByVersionsPath() {
        String path = "/metadata/foo_db/schemas/foo_schema/tables/foo_tbl/active_version";
        assertFalse(parser.findIdentifierByVersionsPath(path, 1).isPresent());
    }
}
