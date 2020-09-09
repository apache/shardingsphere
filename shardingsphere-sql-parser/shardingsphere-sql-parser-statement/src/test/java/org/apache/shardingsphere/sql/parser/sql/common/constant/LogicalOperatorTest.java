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

package org.apache.shardingsphere.sql.parser.sql.constant;

import org.apache.shardingsphere.sql.parser.sql.common.constant.LogicalOperator;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class LogicalOperatorTest {
    
    @Test
    public void assertValueFromAndText() {
        assertTrue(LogicalOperator.valueFrom("AND").isPresent());
        assertThat(LogicalOperator.valueFrom("AND").get(), is(LogicalOperator.AND));
        assertTrue(LogicalOperator.valueFrom("and").isPresent());
        assertThat(LogicalOperator.valueFrom("and").get(), is(LogicalOperator.AND));
    }
    
    @Test
    public void assertValueFromAndSymbol() {
        assertTrue(LogicalOperator.valueFrom("&&").isPresent());
        assertThat(LogicalOperator.valueFrom("&&").get(), is(LogicalOperator.AND));
    }
    
    @Test
    public void assertValueFromOrText() {
        assertTrue(LogicalOperator.valueFrom("OR").isPresent());
        assertThat(LogicalOperator.valueFrom("OR").get(), is(LogicalOperator.OR));
        assertTrue(LogicalOperator.valueFrom("or").isPresent());
        assertThat(LogicalOperator.valueFrom("or").get(), is(LogicalOperator.OR));
    }
    
    @Test
    public void assertValueFromOrSymbol() {
        assertTrue(LogicalOperator.valueFrom("||").isPresent());
        assertThat(LogicalOperator.valueFrom("||").get(), is(LogicalOperator.OR));
    }
    
    @Test
    public void assertValueFromInvalidValue() {
        assertFalse(LogicalOperator.valueFrom("XX").isPresent());
    }
}
