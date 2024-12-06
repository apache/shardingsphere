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

package org.apache.shardingsphere.data.pipeline.postgresql.sqlbuilder.template;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PostgreSQLPipelineFreemarkerManagerTest {
    
    @Test
    void assertGetSQLByDefaultVersion() {
        String actual = PostgreSQLPipelineFreemarkerManager.getSQLByVersion(Collections.singletonMap("databaseName", "foo_db"), "component/table/%s/get_database_id.ftl", 10, 0);
        String expected = "\n" + "SELECT oid AS did, datlastsysoid FROM pg_catalog.pg_database WHERE datname = 'foo_db';" + "\n";
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertGetSQLByVersion() {
        Map<String, Object> dataModel = new HashMap<>(2, 1F);
        dataModel.put("tid", 1);
        dataModel.put("tname", "foo_tb l");
        String actual = PostgreSQLPipelineFreemarkerManager.getSQLByVersion(dataModel, "component/table/%s/get_columns_for_table.ftl", 10, 0);
        assertNotNull(actual);
    }
}
