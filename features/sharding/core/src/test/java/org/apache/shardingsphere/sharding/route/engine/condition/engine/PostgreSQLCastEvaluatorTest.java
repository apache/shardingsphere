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

package org.apache.shardingsphere.sharding.route.engine.condition.engine;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostgreSQLCastEvaluatorTest {
    
    @Test
    void assertEvaluateReturnsEmptyForNullValue() {
        assertFalse(PostgreSQLCastEvaluator.evaluate(null, "int4").isPresent());
    }
    
    @Test
    void assertEvaluateReturnsEmptyForNullTarget() {
        assertFalse(PostgreSQLCastEvaluator.evaluate(1, null).isPresent());
    }
    
    @Test
    void assertEvaluateReturnsEmptyForUnsupportedTarget() {
        assertFalse(PostgreSQLCastEvaluator.evaluate(1, "uuid").isPresent());
    }
    
    @Test
    void assertIntegerToInt4() {
        assertThat(PostgreSQLCastEvaluator.evaluate(7, "int4").orElseThrow(AssertionError::new), is(7));
    }
    
    @Test
    void assertLongToInt4() {
        assertThat(PostgreSQLCastEvaluator.evaluate(7L, "int4").orElseThrow(AssertionError::new), is(7));
    }
    
    @Test
    void assertIntegerToInt8() {
        assertThat(PostgreSQLCastEvaluator.evaluate(7, "int8").orElseThrow(AssertionError::new), is(7L));
    }
    
    @Test
    void assertIntegerToSmallInt() {
        assertThat(PostgreSQLCastEvaluator.evaluate(7, "smallint").orElseThrow(AssertionError::new), is((short) 7));
    }
    
    @Test
    void assertSmallIntOverflowReturnsEmpty() {
        assertFalse(PostgreSQLCastEvaluator.evaluate(32_768, "smallint").isPresent());
    }
    
    @Test
    void assertInt4OverflowReturnsEmpty() {
        assertFalse(PostgreSQLCastEvaluator.evaluate(new BigInteger("2147483648"), "int4").isPresent());
    }
    
    @Test
    void assertStringNumberToInt4() {
        assertThat(PostgreSQLCastEvaluator.evaluate("1", "int4").orElseThrow(AssertionError::new), is(1));
    }
    
    @Test
    void assertStringWithLeadingSpaceToInt4() {
        assertThat(PostgreSQLCastEvaluator.evaluate(" 1 ", "int4").orElseThrow(AssertionError::new), is(1));
    }
    
    @Test
    void assertStringUnparseableToInt4ReturnsEmpty() {
        assertFalse(PostgreSQLCastEvaluator.evaluate("abc", "int4").isPresent());
    }
    
    @Test
    void assertBigDecimalHalfRoundsHalfEvenUpForOne() {
        assertThat(PostgreSQLCastEvaluator.evaluate(new BigDecimal("1.5"), "int4").orElseThrow(AssertionError::new), is(2));
    }
    
    @Test
    void assertBigDecimalHalfRoundsHalfEvenDownForTwo() {
        assertThat(PostgreSQLCastEvaluator.evaluate(new BigDecimal("2.5"), "int4").orElseThrow(AssertionError::new), is(2));
    }
    
    @Test
    void assertBigDecimalNegativeHalfRoundsHalfEven() {
        assertThat(PostgreSQLCastEvaluator.evaluate(new BigDecimal("-1.5"), "int4").orElseThrow(AssertionError::new), is(-2));
    }
    
    @Test
    void assertBigDecimalToNumeric() {
        Optional<Comparable<?>> actual = PostgreSQLCastEvaluator.evaluate(new BigDecimal("1.50"), "numeric");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is(new BigDecimal("1.50")));
    }
    
    @Test
    void assertStringToNumeric() {
        Optional<Comparable<?>> actual = PostgreSQLCastEvaluator.evaluate("1.5", "numeric");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is(new BigDecimal("1.5")));
    }
    
    @Test
    void assertIntegerToFloat4() {
        assertThat(PostgreSQLCastEvaluator.evaluate(7, "real").orElseThrow(AssertionError::new), is(7.0F));
    }
    
    @Test
    void assertIntegerToFloat8() {
        assertThat(PostgreSQLCastEvaluator.evaluate(7, "double precision").orElseThrow(AssertionError::new), is(7.0));
    }
    
    @Test
    void assertIntegerToText() {
        assertThat(PostgreSQLCastEvaluator.evaluate(42, "text").orElseThrow(AssertionError::new), is("42"));
    }
    
    @Test
    void assertBigDecimalToTextUsesPlainString() {
        assertThat(PostgreSQLCastEvaluator.evaluate(new BigDecimal("1.50"), "text").orElseThrow(AssertionError::new), is("1.50"));
    }
    
    @Test
    void assertBooleanToText() {
        assertThat(PostgreSQLCastEvaluator.evaluate(Boolean.TRUE, "text").orElseThrow(AssertionError::new), is("true"));
    }
    
    @Test
    void assertStringToVarcharWithSize() {
        assertThat(PostgreSQLCastEvaluator.evaluate("foo", "varchar(10)").orElseThrow(AssertionError::new), is("foo"));
    }
    
    @Test
    void assertStringTrueToBoolean() {
        assertThat(PostgreSQLCastEvaluator.evaluate("true", "bool").orElseThrow(AssertionError::new), is(Boolean.TRUE));
    }
    
    @Test
    void assertStringYesToBoolean() {
        assertThat(PostgreSQLCastEvaluator.evaluate("YES", "boolean").orElseThrow(AssertionError::new), is(Boolean.TRUE));
    }
    
    @Test
    void assertStringOffToBoolean() {
        assertThat(PostgreSQLCastEvaluator.evaluate("off", "bool").orElseThrow(AssertionError::new), is(Boolean.FALSE));
    }
    
    @Test
    void assertStringUnknownToBooleanReturnsEmpty() {
        assertFalse(PostgreSQLCastEvaluator.evaluate("maybe", "bool").isPresent());
    }
    
    @Test
    void assertNumericNonZeroToBoolean() {
        assertThat(PostgreSQLCastEvaluator.evaluate(7, "bool").orElseThrow(AssertionError::new), is(Boolean.TRUE));
    }
    
    @Test
    void assertNumericZeroToBoolean() {
        assertThat(PostgreSQLCastEvaluator.evaluate(0, "bool").orElseThrow(AssertionError::new), is(Boolean.FALSE));
    }
    
    @Test
    void assertBooleanToBoolean() {
        assertThat(PostgreSQLCastEvaluator.evaluate(Boolean.TRUE, "bool").orElseThrow(AssertionError::new), is(Boolean.TRUE));
    }
    
    @Test
    void assertBooleanToFloatReturnsEmpty() {
        assertFalse(PostgreSQLCastEvaluator.evaluate(Boolean.TRUE, "real").isPresent());
    }
}
