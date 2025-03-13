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

package org.apache.shardingsphere.mode.node.path.type.database.statistics;

import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class StatisticsSchemaNodePathTest {
    
    @Test
    void assertToPath() {
        assertThat(NodePathGenerator.toPath(new StatisticsSchemaNodePath("foo_db", null)), is("/statistics/databases/foo_db/schemas"));
        assertThat(NodePathGenerator.toPath(new StatisticsSchemaNodePath("foo_db", "foo_schema")), is("/statistics/databases/foo_db/schemas/foo_schema"));
    }
    
    @Test
    void assertCreateSchemaSearchCriteria() {
        assertThat(NodePathSearcher.get("/statistics/databases/foo_db/schemas/foo_schema", StatisticsSchemaNodePath.createSchemaSearchCriteria(false)), is("foo_schema"));
        assertThat(NodePathSearcher.get("/statistics/databases/foo_db/schemas/foo_schema", StatisticsSchemaNodePath.createSchemaSearchCriteria(true)), is("foo_schema"));
        assertThat(NodePathSearcher.get("/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl", StatisticsSchemaNodePath.createSchemaSearchCriteria(true)), is("foo_schema"));
        assertFalse(NodePathSearcher.find("/statistics/databases/foo_db", StatisticsSchemaNodePath.createSchemaSearchCriteria(false)).isPresent());
        assertFalse(NodePathSearcher.find("/statistics/databases/foo_db", StatisticsSchemaNodePath.createSchemaSearchCriteria(true)).isPresent());
    }
}
