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

package org.apache.shardingsphere.data.pipeline.postgresql.sqlbuilder;

import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.Range;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.sql.PipelineInventoryCalculateSQLBuilder;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class PipelineInventoryCalculateSQLBuilderTest {
    
    private final PipelineInventoryCalculateSQLBuilder sqlBuilder =
            new PipelineInventoryCalculateSQLBuilder(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"));
    
    @Test
    void assertBuildRangeQueryOrderingSQLWithActualIdentifiers() {
        assertThat(sqlBuilder.buildRangeQueryOrderingSQL(new QualifiedTable("TEST", "T_ORDER"), Arrays.asList("ID", "STATUS"), Collections.singletonList("ID"),
                Range.closed(1, 5), true, Collections.singletonList("TENANT_ID")),
                is("SELECT \"ID\",\"STATUS\" FROM \"TEST\".\"T_ORDER\" WHERE \"ID\">=? AND \"ID\"<=? ORDER BY \"ID\" ASC, \"TENANT_ID\" ASC LIMIT ?"));
    }
    
    @Test
    void assertBuildPointQuerySQLWithActualIdentifiers() {
        assertThat(sqlBuilder.buildPointQuerySQL(new QualifiedTable("TEST", "T_ORDER"), Arrays.asList("ID", "STATUS"), Collections.singletonList("ID"), Collections.emptyList()),
                is("SELECT \"ID\",\"STATUS\" FROM \"TEST\".\"T_ORDER\" WHERE \"ID\"=?"));
    }
}
