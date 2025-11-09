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

package org.apache.shardingsphere.infra.hint;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HintValueContextTest {
    
    @Test
    void assertNotFoundHintDataSourceName() {
        assertFalse(new HintValueContext().findHintDataSourceName().isPresent());
    }
    
    @Test
    void assertFindHintDataSourceName() {
        HintValueContext hintValueContext = new HintValueContext();
        hintValueContext.setDataSourceName("foo_ds");
        Optional<String> actual = hintValueContext.findHintDataSourceName();
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is("foo_ds"));
    }
    
    @Test
    void assertContainsHintShardingDatabaseValue() {
        HintValueContext hintValueContext = new HintValueContext();
        hintValueContext.getShardingDatabaseValues().put("TABLE.SHARDING_DATABASE_VALUE", "1");
        assertTrue(hintValueContext.containsHintShardingDatabaseValue("table"));
        assertTrue(hintValueContext.containsHintShardingDatabaseValue("TABLE"));
        assertFalse(hintValueContext.containsHintShardingDatabaseValue("other"));
    }
    
    @Test
    void assertContainsHintShardingTableValue() {
        HintValueContext hintValueContext = new HintValueContext();
        hintValueContext.getShardingTableValues().put("TABLE.SHARDING_TABLE_VALUE", "1");
        assertTrue(hintValueContext.containsHintShardingTableValue("table"));
        assertTrue(hintValueContext.containsHintShardingTableValue("TABLE"));
        assertFalse(hintValueContext.containsHintShardingTableValue("other"));
    }
    
    @Test
    void assertContainsHintShardingValue() {
        HintValueContext hintValueContext = new HintValueContext();
        hintValueContext.getShardingDatabaseValues().put("TABLE.SHARDING_DATABASE_VALUE", "1");
        assertTrue(hintValueContext.containsHintShardingValue("table"));
        hintValueContext.getShardingDatabaseValues().clear();
        hintValueContext.getShardingTableValues().put("OTHER_TABLE.SHARDING_TABLE_VALUE", "1");
        assertFalse(hintValueContext.containsHintShardingValue("table"));
    }
    
    @Test
    void assertGetHintShardingTableValueWithTableName() {
        HintValueContext hintValueContext = new HintValueContext();
        hintValueContext.getShardingTableValues().put("TABLE.SHARDING_TABLE_VALUE", "1");
        Collection<Comparable<?>> actual = hintValueContext.getHintShardingTableValue("table");
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is("1"));
    }
    
    @Test
    void assertSetHintShardingTableValueWithoutTableName() {
        HintValueContext hintValueContext = new HintValueContext();
        hintValueContext.getShardingTableValues().put("SHARDING_TABLE_VALUE", "2");
        Collection<Comparable<?>> actual = hintValueContext.getHintShardingTableValue("other_table");
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is("2"));
    }
    
    @Test
    void assertGetHintShardingDatabaseValueWithTableName() {
        HintValueContext hintValueContext = new HintValueContext();
        hintValueContext.getShardingDatabaseValues().put("TABLE.SHARDING_DATABASE_VALUE", "1");
        Collection<Comparable<?>> actual = hintValueContext.getHintShardingDatabaseValue("table");
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is("1"));
    }
    
    @Test
    void assertGetHintShardingDatabaseValueWithoutTableName() {
        HintValueContext hintValueContext = new HintValueContext();
        hintValueContext.getShardingDatabaseValues().put("SHARDING_DATABASE_VALUE", "2");
        Collection<Comparable<?>> actual = hintValueContext.getHintShardingDatabaseValue("other_table");
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is("2"));
    }
}
