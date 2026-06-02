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
    void assertStringIntegerToInt4() {
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
    void assertStringWithDecimalToInt4ReturnsEmpty() {
        assertFalse(PostgreSQLCastEvaluator.evaluate("1.5", "int4").isPresent());
    }
    
    @Test
    void assertStringWithTrailingZeroDecimalToInt4ReturnsEmpty() {
        assertFalse(PostgreSQLCastEvaluator.evaluate("1.0", "int4").isPresent());
    }
    
    @Test
    void assertStringWithFloatNotationToInt4ReturnsEmpty() {
        assertFalse(PostgreSQLCastEvaluator.evaluate("1e2", "int4").isPresent());
    }
    
    @Test
    void assertBigDecimalPositiveOneHalfRoundsAwayFromZero() {
        assertThat(PostgreSQLCastEvaluator.evaluate(new BigDecimal("1.5"), "int4").orElseThrow(AssertionError::new), is(2));
    }
    
    @Test
    void assertBigDecimalPositiveTwoHalfRoundsAwayFromZero() {
        assertThat(PostgreSQLCastEvaluator.evaluate(new BigDecimal("2.5"), "int4").orElseThrow(AssertionError::new), is(3));
    }
    
    @Test
    void assertBigDecimalNegativeOneHalfRoundsAwayFromZero() {
        assertThat(PostgreSQLCastEvaluator.evaluate(new BigDecimal("-1.5"), "int4").orElseThrow(AssertionError::new), is(-2));
    }
    
    @Test
    void assertBigDecimalNegativeTwoHalfRoundsAwayFromZero() {
        assertThat(PostgreSQLCastEvaluator.evaluate(new BigDecimal("-2.5"), "int4").orElseThrow(AssertionError::new), is(-3));
    }
    
    @Test
    void assertDoublePositiveTwoHalfRoundsHalfEven() {
        assertThat(PostgreSQLCastEvaluator.evaluate(2.5D, "int4").orElseThrow(AssertionError::new), is(2));
    }
    
    @Test
    void assertDoubleNegativeTwoHalfRoundsHalfEven() {
        assertThat(PostgreSQLCastEvaluator.evaluate(-2.5D, "int4").orElseThrow(AssertionError::new), is(-2));
    }
    
    @Test
    void assertDoublePositiveOneHalfRoundsHalfEven() {
        assertThat(PostgreSQLCastEvaluator.evaluate(1.5D, "int4").orElseThrow(AssertionError::new), is(2));
    }
    
    @Test
    void assertDoubleZeroHalfRoundsHalfEven() {
        assertThat(PostgreSQLCastEvaluator.evaluate(0.5D, "int4").orElseThrow(AssertionError::new), is(0));
    }
    
    @Test
    void assertFloatPositiveTwoHalfRoundsHalfEven() {
        assertThat(PostgreSQLCastEvaluator.evaluate(2.5F, "int4").orElseThrow(AssertionError::new), is(2));
    }
    
    @Test
    void assertFloatNegativeTwoHalfRoundsHalfEven() {
        assertThat(PostgreSQLCastEvaluator.evaluate(-2.5F, "int4").orElseThrow(AssertionError::new), is(-2));
    }
    
    @Test
    void assertDoubleNaNToInt4ReturnsEmpty() {
        assertFalse(PostgreSQLCastEvaluator.evaluate(Double.NaN, "int4").isPresent());
    }
    
    @Test
    void assertDoubleInfinityToInt4ReturnsEmpty() {
        assertFalse(PostgreSQLCastEvaluator.evaluate(Double.POSITIVE_INFINITY, "int4").isPresent());
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
    void assertBooleanToNumericReturnsEmpty() {
        assertFalse(PostgreSQLCastEvaluator.evaluate(Boolean.TRUE, "numeric").isPresent());
    }
    
    @Test
    void assertBooleanToDecimalReturnsEmpty() {
        assertFalse(PostgreSQLCastEvaluator.evaluate(Boolean.FALSE, "decimal").isPresent());
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
    void assertBooleanToFloatReturnsEmpty() {
        assertFalse(PostgreSQLCastEvaluator.evaluate(Boolean.TRUE, "real").isPresent());
    }
    
    @Test
    void assertBooleanToDoublePrecisionReturnsEmpty() {
        assertFalse(PostgreSQLCastEvaluator.evaluate(Boolean.TRUE, "double precision").isPresent());
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
    void assertBooleanTrueToText() {
        assertThat(PostgreSQLCastEvaluator.evaluate(Boolean.TRUE, "text").orElseThrow(AssertionError::new), is("true"));
    }
    
    @Test
    void assertBooleanFalseToText() {
        assertThat(PostgreSQLCastEvaluator.evaluate(Boolean.FALSE, "text").orElseThrow(AssertionError::new), is("false"));
    }
    
    @Test
    void assertStringToBpcharReturnsIdentity() {
        assertThat(PostgreSQLCastEvaluator.evaluate("ab", "bpchar").orElseThrow(AssertionError::new), is("ab"));
    }
    
    @Test
    void assertStringToCharNoTypmodTruncatesToFirstChar() {
        assertThat(PostgreSQLCastEvaluator.evaluate("ab", "char").orElseThrow(AssertionError::new), is("a"));
    }
    
    @Test
    void assertStringToCharacterNoTypmodTruncatesToFirstChar() {
        assertThat(PostgreSQLCastEvaluator.evaluate("ab", "character").orElseThrow(AssertionError::new), is("a"));
    }
    
    @Test
    void assertBooleanToCharNoTypmodTruncatesToFirstChar() {
        assertThat(PostgreSQLCastEvaluator.evaluate(Boolean.TRUE, "char").orElseThrow(AssertionError::new), is("t"));
    }
    
    @Test
    void assertEmptyStringToCharNoTypmodReturnsEmpty() {
        assertThat(PostgreSQLCastEvaluator.evaluate("", "char").orElseThrow(AssertionError::new), is(""));
    }
    
    @Test
    void assertShortStringToNameReturnsIdentity() {
        assertThat(PostgreSQLCastEvaluator.evaluate("foo", "name").orElseThrow(AssertionError::new), is("foo"));
    }
    
    @Test
    void assertLongAsciiStringToNameTruncatesTo63Bytes() {
        StringBuilder seventy = new StringBuilder();
        for (int i = 0; i < 70; i++) {
            seventy.append('a');
        }
        StringBuilder sixtyThree = new StringBuilder();
        for (int i = 0; i < 63; i++) {
            sixtyThree.append('a');
        }
        assertThat(PostgreSQLCastEvaluator.evaluate(seventy.toString(), "name").orElseThrow(AssertionError::new), is(sixtyThree.toString()));
    }
    
    @Test
    void assertLongMultibyteStringToNameTruncatesAtUtf8Boundary() {
        StringBuilder seventy = new StringBuilder();
        for (int i = 0; i < 70; i++) {
            seventy.append('中');
        }
        StringBuilder twentyOne = new StringBuilder();
        for (int i = 0; i < 21; i++) {
            twentyOne.append('中');
        }
        assertThat(PostgreSQLCastEvaluator.evaluate(seventy.toString(), "name").orElseThrow(AssertionError::new), is(twentyOne.toString()));
    }
    
    @Test
    void assertMultibyteStringToCharNoTypmodPicksFirstCodepoint() {
        assertThat(PostgreSQLCastEvaluator.evaluate("中文", "char").orElseThrow(AssertionError::new), is("中"));
    }
    
    @Test
    void assertSurrogatePairStringToCharNoTypmodPicksWholeCodepoint() {
        String smiley = new String(Character.toChars(0x1F600));
        assertThat(PostgreSQLCastEvaluator.evaluate(smiley + "x", "char").orElseThrow(AssertionError::new), is(smiley));
    }
    
    @Test
    void assertDoubleRecurringFractionToNumericTruncatesTo15Digits() {
        Optional<Comparable<?>> actual = PostgreSQLCastEvaluator.evaluate(1.0 / 3.0, "numeric");
        assertTrue(actual.isPresent());
        assertThat(((BigDecimal) actual.get()).toPlainString(), is("0.333333333333333"));
    }
    
    @Test
    void assertDoubleIntegralToNumericStripsTrailingZeros() {
        Optional<Comparable<?>> actual = PostgreSQLCastEvaluator.evaluate(150.0D, "numeric");
        assertTrue(actual.isPresent());
        assertThat(((BigDecimal) actual.get()).toPlainString(), is("150"));
    }
    
    @Test
    void assertDoubleOneToNumericStripsTrailingZeros() {
        Optional<Comparable<?>> actual = PostgreSQLCastEvaluator.evaluate(1.0D, "numeric");
        assertTrue(actual.isPresent());
        assertThat(((BigDecimal) actual.get()).toPlainString(), is("1"));
    }
    
    @Test
    void assertDoubleZeroToNumericReturnsZero() {
        Optional<Comparable<?>> actual = PostgreSQLCastEvaluator.evaluate(0.0D, "numeric");
        assertTrue(actual.isPresent());
        assertThat(((BigDecimal) actual.get()).toPlainString(), is("0"));
    }
    
    @Test
    void assertDoubleHalfToNumericKeepsFractional() {
        Optional<Comparable<?>> actual = PostgreSQLCastEvaluator.evaluate(2.5D, "numeric");
        assertTrue(actual.isPresent());
        assertThat(((BigDecimal) actual.get()).toPlainString(), is("2.5"));
    }
    
    @Test
    void assertStringToVarcharWithTypmodReturnsEmpty() {
        assertFalse(PostgreSQLCastEvaluator.evaluate("foo", "varchar(10)").isPresent());
    }
    
    @Test
    void assertOverLengthStringToVarcharWithTypmodReturnsEmpty() {
        assertFalse(PostgreSQLCastEvaluator.evaluate("ab", "varchar(1)").isPresent());
    }
    
    @Test
    void assertStringToCharWithTypmodReturnsEmpty() {
        assertFalse(PostgreSQLCastEvaluator.evaluate("ab", "char(2)").isPresent());
    }
    
    @Test
    void assertBigDecimalToNumericWithPrecisionScaleReturnsEmpty() {
        assertFalse(PostgreSQLCastEvaluator.evaluate(new BigDecimal("1.55"), "numeric(3,1)").isPresent());
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
    void assertIntegerNonZeroToBoolean() {
        assertThat(PostgreSQLCastEvaluator.evaluate(7, "bool").orElseThrow(AssertionError::new), is(Boolean.TRUE));
    }
    
    @Test
    void assertIntegerZeroToBoolean() {
        assertThat(PostgreSQLCastEvaluator.evaluate(0, "bool").orElseThrow(AssertionError::new), is(Boolean.FALSE));
    }
    
    @Test
    void assertLongNonZeroToBoolean() {
        assertThat(PostgreSQLCastEvaluator.evaluate(7L, "bool").orElseThrow(AssertionError::new), is(Boolean.TRUE));
    }
    
    @Test
    void assertBigDecimalToBoolReturnsEmpty() {
        assertFalse(PostgreSQLCastEvaluator.evaluate(new BigDecimal("1"), "bool").isPresent());
    }
    
    @Test
    void assertBigDecimalFractionalToBoolReturnsEmpty() {
        assertFalse(PostgreSQLCastEvaluator.evaluate(new BigDecimal("1.5"), "bool").isPresent());
    }
    
    @Test
    void assertDoubleToBoolReturnsEmpty() {
        assertFalse(PostgreSQLCastEvaluator.evaluate(2.5D, "bool").isPresent());
    }
    
    @Test
    void assertFloatToBoolReturnsEmpty() {
        assertFalse(PostgreSQLCastEvaluator.evaluate(2.5F, "bool").isPresent());
    }
    
    @Test
    void assertBooleanToBoolean() {
        assertThat(PostgreSQLCastEvaluator.evaluate(Boolean.TRUE, "bool").orElseThrow(AssertionError::new), is(Boolean.TRUE));
    }
    
    @Test
    void assertBoolToNumericIsConservativeOnOpenGaussWhichWouldAcceptThisCast() {
        assertFalse(PostgreSQLCastEvaluator.evaluate(Boolean.TRUE, "numeric").isPresent());
    }
    
    @Test
    void assertNumericToBoolIsConservativeOnOpenGaussWhichWouldAcceptThisCast() {
        assertFalse(PostgreSQLCastEvaluator.evaluate(new BigDecimal("1"), "bool").isPresent());
    }
    
    @Test
    void assertBooleanTrueToName() {
        assertThat(PostgreSQLCastEvaluator.evaluate(Boolean.TRUE, "name").orElseThrow(AssertionError::new), is("t"));
    }
    
    @Test
    void assertBooleanFalseToName() {
        assertThat(PostgreSQLCastEvaluator.evaluate(Boolean.FALSE, "name").orElseThrow(AssertionError::new), is("f"));
    }
    
    @Test
    void assertIntegerToNumeric() {
        Optional<Comparable<?>> actual = PostgreSQLCastEvaluator.evaluate(42, "numeric");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is(new BigDecimal(42)));
    }
    
    @Test
    void assertLongToNumeric() {
        Optional<Comparable<?>> actual = PostgreSQLCastEvaluator.evaluate(42L, "numeric");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is(new BigDecimal(42)));
    }
    
    @Test
    void assertBigIntegerToNumeric() {
        Optional<Comparable<?>> actual = PostgreSQLCastEvaluator.evaluate(new BigInteger("99999999999999999999"), "numeric");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is(new BigDecimal("99999999999999999999")));
    }
    
    @Test
    void assertIntegerToCharNoTypmodPicksFirstDigit() {
        assertThat(PostgreSQLCastEvaluator.evaluate(42, "char").orElseThrow(AssertionError::new), is("4"));
    }
    
    @Test
    void assertIntegerToName() {
        assertThat(PostgreSQLCastEvaluator.evaluate(12345, "name").orElseThrow(AssertionError::new), is("12345"));
    }
    
    @Test
    void assertBigDecimalToFloat4() {
        assertThat(PostgreSQLCastEvaluator.evaluate(new BigDecimal("1.5"), "real").orElseThrow(AssertionError::new), is(1.5F));
    }
    
    @Test
    void assertBigDecimalToFloat8() {
        assertThat(PostgreSQLCastEvaluator.evaluate(new BigDecimal("1.5"), "double precision").orElseThrow(AssertionError::new), is(1.5));
    }
    
    @Test
    void assertBigDecimalToCharNoTypmodPicksFirstChar() {
        assertThat(PostgreSQLCastEvaluator.evaluate(new BigDecimal("123.45"), "char").orElseThrow(AssertionError::new), is("1"));
    }
    
    @Test
    void assertDoubleNarrowsToFloat4() {
        assertThat(PostgreSQLCastEvaluator.evaluate(1.5D, "real").orElseThrow(AssertionError::new), is(1.5F));
    }
    
    @Test
    void assertFloatWidensToFloat8() {
        assertThat(PostgreSQLCastEvaluator.evaluate(1.5F, "double precision").orElseThrow(AssertionError::new), is((double) 1.5F));
    }
    
    @Test
    void assertDoubleToText() {
        assertThat(PostgreSQLCastEvaluator.evaluate(2.5D, "text").orElseThrow(AssertionError::new), is("2.5"));
    }
    
    @Test
    void assertNaNDoubleToText() {
        assertThat(PostgreSQLCastEvaluator.evaluate(Double.NaN, "text").orElseThrow(AssertionError::new), is("NaN"));
    }
    
    @Test
    void assertPositiveInfinityDoubleToText() {
        assertThat(PostgreSQLCastEvaluator.evaluate(Double.POSITIVE_INFINITY, "text").orElseThrow(AssertionError::new), is("Infinity"));
    }
    
    @Test
    void assertNegativeInfinityDoubleToText() {
        assertThat(PostgreSQLCastEvaluator.evaluate(Double.NEGATIVE_INFINITY, "text").orElseThrow(AssertionError::new), is("-Infinity"));
    }
    
    @Test
    void assertStringScientificNotationToFloat8() {
        assertThat(PostgreSQLCastEvaluator.evaluate("1e2", "double precision").orElseThrow(AssertionError::new), is(100.0));
    }
    
    @Test
    void assertStringWithScientificNotationToNumeric() {
        Optional<Comparable<?>> actual = PostgreSQLCastEvaluator.evaluate("1.5e2", "numeric");
        assertTrue(actual.isPresent());
        assertThat(((BigDecimal) actual.get()).toPlainString(), is("150"));
    }
    
    @Test
    void assertStringIdentityToText() {
        assertThat(PostgreSQLCastEvaluator.evaluate("hello", "text").orElseThrow(AssertionError::new), is("hello"));
    }
    
    @Test
    void assertEmptyStringToCharNoTypmodReturnsEmptyString() {
        assertThat(PostgreSQLCastEvaluator.evaluate("", "name").orElseThrow(AssertionError::new), is(""));
    }
    
    @Test
    void assertLongMaxValueToBoolean() {
        assertThat(PostgreSQLCastEvaluator.evaluate(Long.MAX_VALUE, "bool").orElseThrow(AssertionError::new), is(Boolean.TRUE));
    }
    
    @Test
    void assertBigIntegerHugeToInt8OverflowReturnsEmpty() {
        assertFalse(PostgreSQLCastEvaluator.evaluate(new BigInteger("99999999999999999999"), "int8").isPresent());
    }
    
    @Test
    void assertEmptyStringToInt4ReturnsEmpty() {
        assertFalse(PostgreSQLCastEvaluator.evaluate("", "int4").isPresent());
    }
    
    @Test
    void assertWhitespaceOnlyStringToInt4ReturnsEmpty() {
        assertFalse(PostgreSQLCastEvaluator.evaluate("   ", "int4").isPresent());
    }
    
    @Test
    void assertStringWithTabAndNewlineToInt4() {
        assertThat(PostgreSQLCastEvaluator.evaluate("\t1\n", "int4").orElseThrow(AssertionError::new), is(1));
    }
    
    @Test
    void assertStringDoubleToInt4ReturnsEmpty() {
        assertFalse(PostgreSQLCastEvaluator.evaluate("1.5e0", "int4").isPresent());
    }
    
    @Test
    void assertNegativeIntegerToBool() {
        assertThat(PostgreSQLCastEvaluator.evaluate(-7, "bool").orElseThrow(AssertionError::new), is(Boolean.TRUE));
    }
}
