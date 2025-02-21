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

package org.apache.shardingsphere.mode.node.path.statistics;

import org.apache.shardingsphere.mode.node.path.NodePathGenerator;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class StatisticsDataNodePathTest {
    
    @Test
    void assertGeneratePath() {
        assertThat(NodePathGenerator.generatePath(new StatisticsDataNodePath(null, null, null, null), false), is("/statistics/databases"));
        assertThat(NodePathGenerator.generatePath(new StatisticsDataNodePath("foo_db", null, null, null), true), is("/statistics/databases/foo_db"));
        assertThat(NodePathGenerator.generatePath(new StatisticsDataNodePath("foo_db", null, null, null), false), is("/statistics/databases/foo_db/schemas"));
        assertThat(NodePathGenerator.generatePath(new StatisticsDataNodePath("foo_db", "foo_schema", null, null), true), is("/statistics/databases/foo_db/schemas/foo_schema"));
        assertThat(NodePathGenerator.generatePath(new StatisticsDataNodePath("foo_db", "foo_schema", null, null), false), is("/statistics/databases/foo_db/schemas/foo_schema/tables"));
        assertThat(NodePathGenerator.generatePath(new StatisticsDataNodePath("foo_db", "foo_schema", "foo_tbl", null), false),
                is("/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl"));
        assertThat(NodePathGenerator.generatePath(new StatisticsDataNodePath("foo_db", "foo_schema", "foo_tbl", "foo_key"), false),
                is("/statistics/databases/foo_db/schemas/foo_schema/tables/foo_tbl/foo_key"));
    }
}
