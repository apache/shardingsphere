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
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

class PostgreSQLPipelineFreemarkerManagerTest {
    
    @Test
    void assertCreateTableTemplateHandlesHumanFormattedSequenceNumbers() {
        Map<String, Object> column = new LinkedHashMap<>(16, 1F);
        column.put("name", "id");
        column.put("cltype", "int4");
        column.put("displaytypname", "integer");
        column.put("attnotnull", true);
        column.put("attidentity", "a");
        column.put("colconstype", "i");
        column.put("seqincrement", 1L);
        column.put("seqstart", 1L);
        column.put("seqmin", 1L);
        column.put("seqmax", 2147483647L);
        column.put("seqcache", 1L);
        column.put("seqcycle", false);
        Map<String, Object> dataModel = new LinkedHashMap<>(8, 1F);
        dataModel.put("schema", "public");
        dataModel.put("name", "t_order");
        dataModel.put("columns", Collections.singletonList(column));
        dataModel.put("primary_key", Collections.emptyList());
        dataModel.put("unique_constraint", Collections.emptyList());
        dataModel.put("foreign_key", Collections.emptyList());
        dataModel.put("check_constraint", Collections.emptyList());
        dataModel.put("exclude_constraint", Collections.emptyList());
        dataModel.put("coll_inherits", Collections.emptyList());
        dataModel.put("autovacuum_enabled", "x");
        dataModel.put("toast_autovacuum_enabled", "x");
        dataModel.put("autovacuum_custom", false);
        dataModel.put("toast_autovacuum", false);
        dataModel.put("add_vacuum_settings_in_sql", false);
        String sql = PostgreSQLPipelineFreemarkerManager.getSQLByVersion(dataModel, "component/table/%s/create.ftl", 12, 0);
        String compactSql = sql.replaceAll("\\s+", "");
        String expectedSql = "CREATETABLEIFNOTEXISTSpublic.t_order(idintegerNOTNULLGENERATEDALWAYSASIDENTITY(INCREMENT1START1MINVALUE1MAXVALUE2147483647CACHE1))";
        assertThat(compactSql, is(expectedSql));
    }
}
