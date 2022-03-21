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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.node;

import org.apache.shardingsphere.infra.metadata.schema.QualifiedSchema;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class StorageStatusNodeTest {
    
    @Test
    public void assertGetRootPath() {
        assertThat(StorageStatusNode.getRootPath(), is("/nodes/storage_nodes/attributes"));
    }
    
    @Test
    public void assertExtractQualifiedSchema() {
        Optional<QualifiedSchema> actual = StorageStatusNode.extractQualifiedSchema("/nodes/storage_nodes/attributes/replica_query_db.readwrite_ds.replica_ds_0");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getSchemaName(), is("replica_query_db"));
        assertThat(actual.get().getGroupName(), is("readwrite_ds"));
        assertThat(actual.get().getDataSourceName(), is("replica_ds_0"));
    }
}
