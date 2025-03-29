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

class StatisticsDatabaseNodePathTest {
    
    @Test
    void assertToPath() {
        assertThat(NodePathGenerator.toPath(new StatisticsDatabaseNodePath(null)), is("/statistics/databases"));
        assertThat(NodePathGenerator.toPath(new StatisticsDatabaseNodePath("foo_db")), is("/statistics/databases/foo_db"));
    }
    
    @Test
    void assertCreateDatabaseSearchCriteria() {
        assertThat(NodePathSearcher.get("/statistics/databases/foo_db", StatisticsDatabaseNodePath.createDatabaseSearchCriteria(false)), is("foo_db"));
        assertThat(NodePathSearcher.get("/statistics/databases/foo_db", StatisticsDatabaseNodePath.createDatabaseSearchCriteria(true)), is("foo_db"));
        assertThat(NodePathSearcher.get("/statistics/databases/foo_db/schemas/db_schema", StatisticsDatabaseNodePath.createDatabaseSearchCriteria(true)), is("foo_db"));
        assertFalse(NodePathSearcher.find("/statistics/databases", StatisticsDatabaseNodePath.createDatabaseSearchCriteria(false)).isPresent());
        assertFalse(NodePathSearcher.find("/statistics/databases", StatisticsDatabaseNodePath.createDatabaseSearchCriteria(true)).isPresent());
    }
}
