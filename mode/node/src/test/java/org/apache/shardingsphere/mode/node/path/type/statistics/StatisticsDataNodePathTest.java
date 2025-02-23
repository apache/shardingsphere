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

package org.apache.shardingsphere.mode.node.path.type.statistics;

import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class StatisticsDataNodePathTest {
    
    @Test
    void assertToPath() {
        assertThat(NodePathGenerator.toPath(new StatisticsDataNodePath(null, null, null, null), false), is("/statistics/databases"));
        assertThat(NodePathGenerator.toPath(new StatisticsDataNodePath("foo_db", null, null, null), true), is("/statistics/databases/foo_db"));
        assertThat(NodePathGenerator.toPath(new StatisticsDataNodePath("foo_db", null, null, null), false), is("/statistics/databases/foo_db/schemas"));
        assertThat(NodePathGenerator.toPath(new StatisticsDataNodePath("foo_db", "foo_schema", null, null), true), is("/statistics/databases/foo_db/schemas/foo_schema"));
        assertThat(NodePathGenerator.toPath(new StatisticsDataNodePath("foo_db", "foo_schema", null, null), false), is("/statistics/databases/foo_db/schemas/foo_schema/tables"));
        assertThat(NodePathGenerator.toPath(new StatisticsDataNodePath("foo_db", "foo_schema", "foo_tbl", null), false),
                is("/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl"));
        assertThat(NodePathGenerator.toPath(new StatisticsDataNodePath("foo_db", "foo_schema", "foo_tbl", "foo_key"), false),
                is("/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl/foo_key"));
    }
    
    @Test
    void assertCreateDatabaseSearchCriteria() {
        assertThat(NodePathSearcher.find("/statistics/databases/foo_db", StatisticsDataNodePath.createDatabaseSearchCriteria(false)), is(Optional.of("foo_db")));
        assertThat(NodePathSearcher.find("/statistics/databases", StatisticsDataNodePath.createDatabaseSearchCriteria(false)), is(Optional.empty()));
        assertThat(NodePathSearcher.find("/statistics/databases/foo_db", StatisticsDataNodePath.createDatabaseSearchCriteria(true)), is(Optional.of("foo_db")));
        assertThat(NodePathSearcher.find("/statistics/databases/foo_db/schemas/db_schema", StatisticsDataNodePath.createDatabaseSearchCriteria(true)), is(Optional.of("foo_db")));
        assertThat(NodePathSearcher.find("/statistics/databases", StatisticsDataNodePath.createDatabaseSearchCriteria(true)), is(Optional.empty()));
    }
    
    @Test
    void assertCreateSchemaSearchCriteria() {
        assertThat(NodePathSearcher.find("/statistics/databases/foo_db/schemas/foo_schema", StatisticsDataNodePath.createSchemaSearchCriteria(false)), is(Optional.of("foo_schema")));
        assertThat(NodePathSearcher.find("/statistics/databases/foo_db", StatisticsDataNodePath.createSchemaSearchCriteria(false)), is(Optional.empty()));
        assertThat(NodePathSearcher.find("/statistics/databases/foo_db/schemas/foo_schema", StatisticsDataNodePath.createSchemaSearchCriteria(true)), is(Optional.of("foo_schema")));
        assertThat(NodePathSearcher.find("/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl", StatisticsDataNodePath.createSchemaSearchCriteria(true)), is(Optional.of("foo_schema")));
        assertThat(NodePathSearcher.find("/statistics/databases/foo_db", StatisticsDataNodePath.createSchemaSearchCriteria(true)), is(Optional.empty()));
    }
    
    @Test
    void assertCreateTableSearchCriteria() {
        assertThat(NodePathSearcher.find("/statistics/databases/foo_db/schemas/foo_schema/tables/tbl_name", StatisticsDataNodePath.createTableSearchCriteria(false)), is(Optional.of("tbl_name")));
        assertThat(NodePathSearcher.find("/statistics/databases/foo_db/schemas/foo_schema", StatisticsDataNodePath.createTableSearchCriteria(false)), is(Optional.empty()));
        assertThat(NodePathSearcher.find("/statistics/databases/foo_db/schemas/foo_schema/tables/tbl_name", StatisticsDataNodePath.createTableSearchCriteria(true)), is(Optional.of("tbl_name")));
        assertThat(NodePathSearcher.find("/statistics/databases/foo_db/schemas/foo_schema/tables/tbl_name/key", StatisticsDataNodePath.createTableSearchCriteria(true)), is(Optional.of("tbl_name")));
        assertThat(NodePathSearcher.find("/statistics/databases/foo_db/schemas/foo_schema/tables", StatisticsDataNodePath.createTableSearchCriteria(true)), is(Optional.empty()));
    }
    
    @Test
    void assertCreateRowUniqueKeySearchCriteria() {
        assertThat(NodePathSearcher.find("/statistics/databases/foo_db/schemas/foo_schema/tables/tbl_name/key", StatisticsDataNodePath.createRowUniqueKeySearchCriteria()), is(Optional.of("key")));
        assertThat(NodePathSearcher.find("/statistics/databases/foo_db/schemas/foo_schema/tables/tbl_name", StatisticsDataNodePath.createRowUniqueKeySearchCriteria()), is(Optional.empty()));
    }
}
