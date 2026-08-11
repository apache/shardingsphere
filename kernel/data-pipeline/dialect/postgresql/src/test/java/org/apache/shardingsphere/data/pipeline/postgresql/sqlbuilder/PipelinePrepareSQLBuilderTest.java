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

import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.sql.PipelinePrepareSQLBuilder;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class PipelinePrepareSQLBuilderTest {
    
    private final PipelinePrepareSQLBuilder sqlBuilder = new PipelinePrepareSQLBuilder(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"));
    
    @Test
    void assertBuildCountSQLWithActualIdentifiers() {
        assertThat(sqlBuilder.buildCountSQL("TEST", "T_ORDER"), is("SELECT COUNT(*) FROM \"TEST\".\"T_ORDER\""));
    }
    
    @Test
    void assertBuildEstimatedCountSQLWithActualIdentifiers() {
        assertThat(sqlBuilder.buildEstimatedCountSQL("foo_catalog", "TEST", "T_ORDER"),
                is(Optional.of("SELECT reltuples::integer FROM pg_class WHERE oid='\"TEST\".\"T_ORDER\"'::regclass::oid;")));
    }
    
    @Test
    void assertBuildUniqueKeyMinMaxValuesSQLWithActualIdentifiers() {
        assertThat(sqlBuilder.buildUniqueKeyMinMaxValuesSQL("TEST", "T_ORDER", "ID"),
                is("SELECT MIN(\"ID\"), MAX(\"ID\") FROM \"TEST\".\"T_ORDER\""));
    }
    
    @Test
    void assertBuildCheckEmptyTableSQLWithActualIdentifiers() {
        assertThat(sqlBuilder.buildCheckEmptyTableSQL("TEST", "T_ORDER"), is("SELECT * FROM \"TEST\".\"T_ORDER\" LIMIT 1"));
    }
    
    @Test
    void assertBuildSplitByUniqueKeyRangedSQLWithActualIdentifiers() {
        assertThat(sqlBuilder.buildSplitByUniqueKeyRangedSQL("TEST", "T_ORDER", "ID", true),
                is("SELECT MAX(\"ID\"), COUNT(1), MIN(\"ID\") FROM (SELECT \"ID\" FROM \"TEST\".\"T_ORDER\" WHERE \"ID\">? ORDER BY \"ID\" LIMIT ?) t"));
    }
}
