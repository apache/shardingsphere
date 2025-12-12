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

package org.apache.shardingsphere.data.pipeline.core.sqlbuilder.sql;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PipelinePrepareSQLBuilderTest {
    
    private final PipelinePrepareSQLBuilder sqlBuilder = new PipelinePrepareSQLBuilder(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
    
    @Test
    void assertBuildCreateSchemaSQL() {
        assertFalse(sqlBuilder.buildCreateSchemaSQL("foo_schema").isPresent());
    }
    
    @Test
    void assertBuildDropSQL() {
        assertThat(sqlBuilder.buildDropSQL("foo_schema", "foo_tbl"), is("DROP TABLE IF EXISTS foo_tbl"));
    }
    
    @Test
    void assertBuildCountSQL() {
        assertThat(sqlBuilder.buildCountSQL("foo_schema", "foo_tbl"), is("SELECT COUNT(*) FROM foo_tbl"));
    }
    
    @Test
    void assertBuildEstimatedCountSQL() {
        assertFalse(sqlBuilder.buildEstimatedCountSQL("foo_catalog", "foo_schema", "foo_tbl").isPresent());
    }
    
    @Test
    void assertBuildUniqueKeyMinMaxValuesSQL() {
        assertThat(sqlBuilder.buildUniqueKeyMinMaxValuesSQL("foo_schema", "foo_tbl", "foo_key"), is("SELECT MIN(foo_key), MAX(foo_key) FROM foo_tbl"));
    }
    
    @Test
    void assertBuildCheckEmptyTableSQL() {
        assertThat(sqlBuilder.buildCheckEmptyTableSQL("foo_schema", "foo_tbl"), is("SELECT * FROM foo_tbl LIMIT 1"));
    }
}
