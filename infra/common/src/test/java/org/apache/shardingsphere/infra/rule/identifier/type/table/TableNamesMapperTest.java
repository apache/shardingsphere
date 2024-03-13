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

package org.apache.shardingsphere.infra.rule.identifier.type.table;

import org.apache.shardingsphere.infra.rule.attribute.table.TableNamesMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TableNamesMapperTest {
    
    private TableNamesMapper tableNamesMapper;
    
    @BeforeEach
    void setUp() {
        tableNamesMapper = new TableNamesMapper();
    }
    
    @Test
    void assertContainsTable() {
        tableNamesMapper.put("foo_table");
        tableNamesMapper.put("FoO_TaBlE_2");
        assertTrue(tableNamesMapper.contains("foo_table"));
        assertTrue(tableNamesMapper.contains("foo_table_2"));
    }
    
    @Test
    void assertGetTableNames() {
        tableNamesMapper.put("foo_table_1");
        tableNamesMapper.put("foo_table_2");
        Collection<String> actualTables = tableNamesMapper.getTableNames();
        assertThat(actualTables.size(), is(2));
        Iterator<String> iterator = actualTables.iterator();
        assertThat(iterator.next(), is("foo_table_1"));
        assertThat(iterator.next(), is("foo_table_2"));
    }
    
    @Test
    void assertRemove() {
        tableNamesMapper.put("foo_table_1");
        Collection<String> actualTables = tableNamesMapper.getTableNames();
        assertThat(actualTables.size(), is(1));
        tableNamesMapper.remove("foo_table_1");
        assertThat(actualTables.size(), is(0));
    }
    
    @Test
    void assertPut() {
        Collection<String> actualTables = tableNamesMapper.getTableNames();
        assertThat(actualTables.size(), is(0));
        tableNamesMapper.put("foo_table");
        assertThat(actualTables.size(), is(1));
    }
}
