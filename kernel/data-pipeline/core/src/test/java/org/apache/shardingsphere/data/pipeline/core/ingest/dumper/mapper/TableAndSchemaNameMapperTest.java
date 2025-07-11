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

package org.apache.shardingsphere.data.pipeline.core.ingest.dumper.mapper;

import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;

class TableAndSchemaNameMapperTest {
    
    @Test
    void assertConstructFromNull() {
        assertDoesNotThrow(() -> new TableAndSchemaNameMapper((Map<String, String>) null));
    }
    
    @Test
    void assertConstructFromValueNullMap() {
        assertNull(new TableAndSchemaNameMapper(Collections.singletonMap("t_order", null)).getSchemaName("t_order"));
    }
    
    @Test
    void assertConstructFromMap() {
        assertThat(new TableAndSchemaNameMapper(Collections.singletonMap("t_order", "public")).getSchemaName("t_order"), is("public"));
    }
    
    @Test
    void assertConstructFromCollection() {
        assertThat(new TableAndSchemaNameMapper(Arrays.asList("public.t_order", "t_order_item")).getSchemaName("t_order"), is("public"));
    }
    
    @Test
    void assertGetQualifiedTables() {
        TableAndSchemaNameMapper tableAndSchemaNameMapper = new TableAndSchemaNameMapper(Collections.singletonMap("foo_tbl", "foo_schema"));
        assertThat(tableAndSchemaNameMapper.getQualifiedTables(), is(Collections.singletonList(new QualifiedTable("foo_schema", "foo_tbl"))));
    }
}
