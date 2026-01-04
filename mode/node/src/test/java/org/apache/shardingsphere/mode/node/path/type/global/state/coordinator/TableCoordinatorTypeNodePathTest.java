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

package org.apache.shardingsphere.mode.node.path.type.global.state.coordinator;

import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class TableCoordinatorTypeNodePathTest {
    
    @Test
    void assertCreateTableSearchCriteria() {
        String actual = NodePathSearcher.get("/states/coordinator/schemas/table/foo_db.foo_schema.foo_table/READY", TableCoordinatorTypeNodePath.createTableSearchCriteria());
        assertThat(actual, is("foo_db.foo_schema.foo_table"));
    }
    
    @Test
    void assertToPath() {
        TableCoordinatorTypeNodePath actual = new TableCoordinatorTypeNodePath("foo_db.foo_schema.foo_table", "LOCKED");
        String actualPath = NodePathGenerator.toPath(actual);
        assertThat(actual.getQualifiedTableName(), is("foo_db.foo_schema.foo_table"));
        assertThat(actual.getCoordinatorType(), is("LOCKED"));
        assertThat(actualPath, is("/states/coordinator/schemas/table/foo_db.foo_schema.foo_table/LOCKED"));
    }
}
