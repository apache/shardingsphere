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

package org.apache.shardingsphere.data.pipeline.common.sqlbuilder;

import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class PipelineSQLSegmentBuilderTest {
    
    @Test
    void assertGetEscapedIdentifier() {
        PipelineSQLSegmentBuilder sqlSegmentBuilder = new PipelineSQLSegmentBuilder(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        assertThat(sqlSegmentBuilder.getEscapedIdentifier("SELECT"), is("`SELECT`"));
    }
    
    @Test
    void assertGetUnescapedIdentifier() {
        PipelineSQLSegmentBuilder sqlSegmentBuilder = new PipelineSQLSegmentBuilder(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        assertThat(sqlSegmentBuilder.getEscapedIdentifier("SELECT1"), is("SELECT1"));
    }
    
    @Test
    void assertGetQualifiedTableNameWithUnsupportedSchema() {
        PipelineSQLSegmentBuilder sqlSegmentBuilder = new PipelineSQLSegmentBuilder(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        assertThat(sqlSegmentBuilder.getQualifiedTableName("foo_schema", "foo_tbl"), is("foo_tbl"));
    }
    
    @Test
    void assertGetQualifiedTableNameWithSupportedSchema() {
        PipelineSQLSegmentBuilder sqlSegmentBuilder = new PipelineSQLSegmentBuilder(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"));
        assertThat(sqlSegmentBuilder.getQualifiedTableName("foo_schema", "foo_tbl"), is("foo_schema.foo_tbl"));
    }
}
