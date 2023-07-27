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

package org.apache.shardingsphere.infra.binder.context.segment.select.projection;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DerivedColumnTest {
    
    @Test
    void assertGetDerivedColumnAlias() {
        assertThat(DerivedColumn.AVG_COUNT_ALIAS.getDerivedColumnAlias(0), is("AVG_DERIVED_COUNT_0"));
        assertThat(DerivedColumn.AVG_SUM_ALIAS.getDerivedColumnAlias(1), is("AVG_DERIVED_SUM_1"));
        assertThat(DerivedColumn.ORDER_BY_ALIAS.getDerivedColumnAlias(0), is("ORDER_BY_DERIVED_0"));
        assertThat(DerivedColumn.GROUP_BY_ALIAS.getDerivedColumnAlias(1), is("GROUP_BY_DERIVED_1"));
    }
    
    @Test
    void assertIsDerivedColumn() {
        assertTrue(DerivedColumn.isDerivedColumn("AVG_DERIVED_COUNT_0"));
        assertTrue(DerivedColumn.isDerivedColumn("AVG_DERIVED_SUM_1"));
        assertTrue(DerivedColumn.isDerivedColumn("ORDER_BY_DERIVED_0"));
        assertTrue(DerivedColumn.isDerivedColumn("GROUP_BY_DERIVED_1"));
    }
    
    @Test
    void assertIsNotDerivedColumn() {
        assertFalse(DerivedColumn.isDerivedColumn("OTHER_DERIVED_COLUMN_0"));
    }
    
    @Test
    void assertIsDerivedColumnName() {
        assertTrue(DerivedColumn.isDerivedColumnName("AVG_DERIVED_COUNT_"));
        assertTrue(DerivedColumn.isDerivedColumnName("AVG_DERIVED_SUM_"));
        assertTrue(DerivedColumn.isDerivedColumnName("ORDER_BY_DERIVED_"));
        assertTrue(DerivedColumn.isDerivedColumnName("GROUP_BY_DERIVED_"));
        assertTrue(DerivedColumn.isDerivedColumnName("AGGREGATION_DISTINCT_DERIVED_"));
    }
    
    @Test
    void assertIsNotDerivedColumnName() {
        assertFalse(DerivedColumn.isDerivedColumnName("OTHER_DERIVED_COLUMN_0"));
    }
}
