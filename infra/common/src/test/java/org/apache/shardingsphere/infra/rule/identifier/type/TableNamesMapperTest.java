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

package org.apache.shardingsphere.infra.rule.identifier.type;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class TableNamesMapperTest {

    private TableNamesMapper tableNamesMapper;

    @BeforeEach
    void setUp() {
        tableNamesMapper = new TableNamesMapper();
    }

    @Test
    void assertContainsTable() {
        tableNamesMapper.put("foo_table");
        assertTrue(tableNamesMapper.contains("foo_table"));
    }

    @Test
    void assertGetTableNames() {
        tableNamesMapper.put("foo_table_1");
        tableNamesMapper.put("foo_table_2");
        assertThat(tableNamesMapper.getTableNames(), hasItems("foo_table_1", "foo_table_2"));
        assertThat(tableNamesMapper.getTableNames(), hasSize(2));
    }

    @Test
    void assertRemove() {
        tableNamesMapper.put("foo_table_1");
        tableNamesMapper.remove("foo_table_1");
        assertFalse(tableNamesMapper.contains("foo_table_1"));
        assertThat(tableNamesMapper.getTableNames(), hasSize(0));
    }

    @Test
    void assertPut() {
        tableNamesMapper.put("foo_table");
        assertThat(tableNamesMapper.getTableNames(), hasSize(1));
    }
}