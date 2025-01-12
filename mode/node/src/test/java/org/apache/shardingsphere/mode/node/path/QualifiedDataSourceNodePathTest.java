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

package org.apache.shardingsphere.mode.node.path;

import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDataSource;
import org.apache.shardingsphere.mode.node.path.metadata.QualifiedDataSourceNodePath;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QualifiedDataSourceNodePathTest {
    
    @Test
    void assertGetRootPath() {
        MatcherAssert.assertThat(QualifiedDataSourceNodePath.getRootPath(), is("/nodes/qualified_data_sources"));
    }
    
    @Test
    void assertGetQualifiedDataSourcePath() {
        assertThat(QualifiedDataSourceNodePath.getQualifiedDataSourcePath(new QualifiedDataSource("replica_query_db.readwrite_ds.replica_ds_0")),
                is("/nodes/qualified_data_sources/replica_query_db.readwrite_ds.replica_ds_0"));
    }
    
    @Test
    void assertFindQualifiedDataSource() {
        Optional<QualifiedDataSource> actual = QualifiedDataSourceNodePath.findQualifiedDataSource("/nodes/qualified_data_sources/replica_query_db.readwrite_ds.replica_ds_0");
        assertTrue(actual.isPresent());
        assertThat(actual.get().getDatabaseName(), is("replica_query_db"));
        assertThat(actual.get().getGroupName(), is("readwrite_ds"));
        assertThat(actual.get().getDataSourceName(), is("replica_ds_0"));
    }
    
    @Test
    void assertNotFindQualifiedDataSource() {
        Optional<QualifiedDataSource> actual = QualifiedDataSourceNodePath.findQualifiedDataSource("/nodes/xxx/");
        assertFalse(actual.isPresent());
    }
}
