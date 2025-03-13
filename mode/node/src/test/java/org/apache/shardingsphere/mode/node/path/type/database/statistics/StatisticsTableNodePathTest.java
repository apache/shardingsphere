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

class StatisticsTableNodePathTest {
    
    @Test
    void assertToPath() {
        assertThat(NodePathGenerator.toPath(new StatisticsTableNodePath("foo_db", "foo_schema", null)), is("/statistics/databases/foo_db/schemas/foo_schema/tables"));
        assertThat(NodePathGenerator.toPath(new StatisticsTableNodePath("foo_db", "foo_schema", "foo_tbl")), is("/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl"));
    }
    
    @Test
    void assertCreateTableSearchCriteria() {
        assertThat(NodePathSearcher.get("/statistics/databases/foo_db/schemas/foo_schema/tables/tbl_name", StatisticsTableNodePath.createTableSearchCriteria(false)), is("tbl_name"));
        assertThat(NodePathSearcher.get("/statistics/databases/foo_db/schemas/foo_schema/tables/tbl_name", StatisticsTableNodePath.createTableSearchCriteria(true)), is("tbl_name"));
        assertThat(NodePathSearcher.get("/statistics/databases/foo_db/schemas/foo_schema/tables/tbl_name/key", StatisticsTableNodePath.createTableSearchCriteria(true)), is("tbl_name"));
        assertFalse(NodePathSearcher.find("/statistics/databases/foo_db/schemas/foo_schema", StatisticsTableNodePath.createTableSearchCriteria(false)).isPresent());
        assertFalse(NodePathSearcher.find("/statistics/databases/foo_db/schemas/foo_schema/tables", StatisticsTableNodePath.createTableSearchCriteria(true)).isPresent());
    }
}
