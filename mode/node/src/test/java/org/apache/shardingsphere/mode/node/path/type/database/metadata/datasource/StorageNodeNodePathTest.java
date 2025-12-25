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

package org.apache.shardingsphere.mode.node.path.type.database.metadata.datasource;

import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class StorageNodeNodePathTest {
    
    @Test
    void assertToPath() {
        assertThat(NodePathGenerator.toPath(new StorageNodeNodePath("foo_db", null)), is("/metadata/foo_db/data_sources/nodes"));
        assertThat(NodePathGenerator.toPath(new StorageNodeNodePath("foo_db", "foo_storage_node")), is("/metadata/foo_db/data_sources/nodes/foo_storage_node"));
    }
    
    @Test
    void assertCreateStorageNodeSearchCriteria() {
        assertThat(NodePathSearcher.get("/metadata/foo_db/data_sources/nodes/foo_ds", StorageNodeNodePath.createStorageNodeSearchCriteria("foo_db")), is("foo_ds"));
        assertFalse(NodePathSearcher.find("/xxx/foo_db/data_sources/nodes/foo_ds", StorageNodeNodePath.createStorageNodeSearchCriteria("foo_db")).isPresent());
        assertFalse(NodePathSearcher.find("/metadata/bar_db/data_sources/nodes/foo_ds", StorageNodeNodePath.createStorageNodeSearchCriteria("foo_db")).isPresent());
    }
}
