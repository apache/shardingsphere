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

import org.apache.shardingsphere.metadata.persist.node.DatabaseMetaDataNode;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ViewMetaDataNodeTest {
    
    @Test
    void assertGetViewName() {
        Optional<String> actual = ViewMetaDataNode.getViewName("/metadata/foo_db/schemas/foo_schema/views/foo_view");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_view"));
    }
    
    @Test
    void assertIsViewActiveVersionNode() {
        assertTrue(ViewMetaDataNode.isViewActiveVersionNode("/metadata/foo_db/schemas/foo_schema/views/foo_view/active_version"));
    }
    
    @Test
    void assertIsViewNode() {
        assertTrue(ViewMetaDataNode.isViewNode("/metadata/foo_db/schemas/foo_schema/views/foo_view"));
    }
    
    @Test
    void assertGetVersionNodeByActiveVersionPath() {
        assertThat(DatabaseMetaDataNode.getVersionNodeByActiveVersionPath("/metadata/foo_db/schemas/foo_schema/views/foo_view/active_version", "0"),
                is("/metadata/foo_db/schemas/foo_schema/views/foo_view/versions/0"));
    }
    
    @Test
    void assertGetViewActiveVersionNode() {
        assertThat(ViewMetaDataNode.getViewActiveVersionNode("foo_db", "foo_schema", "foo_view"), is("/metadata/foo_db/schemas/foo_schema/views/foo_view/active_version"));
    }
    
    @Test
    void assertGetViewVersionsNode() {
        assertThat(ViewMetaDataNode.getViewVersionsNode("foo_db", "foo_schema", "foo_view"), is("/metadata/foo_db/schemas/foo_schema/views/foo_view/versions"));
    }
    
    @Test
    void assertGetViewVersionNode() {
        assertThat(ViewMetaDataNode.getViewVersionNode("foo_db", "foo_schema", "foo_view", "0"), is("/metadata/foo_db/schemas/foo_schema/views/foo_view/versions/0"));
    }
    
    @Test
    void assertGetViewNode() {
        assertThat(ViewMetaDataNode.getViewNode("foo_db", "foo_schema", "foo_view"), is("/metadata/foo_db/schemas/foo_schema/views/foo_view"));
    }
    
    @Test
    void assertGetTableNameByActiveVersionNode() {
        Optional<String> actual = ViewMetaDataNode.getViewNameByActiveVersionNode("/metadata/foo_db/schemas/foo_schema/views/foo_view/active_version");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_view"));
    }
}
