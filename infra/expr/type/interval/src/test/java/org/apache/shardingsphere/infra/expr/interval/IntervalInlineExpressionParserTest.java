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

package org.apache.shardingsphere.infra.expr.interval;

import org.apache.shardingsphere.infra.expr.spi.InlineExpressionParser;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IntervalInlineExpressionParserTest {
    
    @Test
    void assertEvaluateWithSimpleExpression() {
        List<String> actual = getInlineExpressionParser("P=ds-0.t_order;SP=yyyyMMdd;DIA=1;DIU=Days;DL=20231202;DU=20231202").splitAndEvaluate();
        List<String> expected = Collections.singletonList("ds-0.t_order20231202");
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertEvaluateWithLocalDateExpression() {
        List<String> actual = getInlineExpressionParser("P=ds-0.t_order_;SP=yyyy_MMdd;DIA=1;DIU=Days;DL=2023_1202;DU=2023_1204").splitAndEvaluate();
        List<String> expected = Arrays.asList("ds-0.t_order_2023_1202", "ds-0.t_order_2023_1203", "ds-0.t_order_2023_1204");
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertEvaluateWithYearMonthExpression() {
        List<String> actual = getInlineExpressionParser("P=ds-0.t_order_;SP=yyyy_MM;DIA=1;DIU=Months;DL=2023_10;DU=2023_12").splitAndEvaluate();
        List<String> expected = Arrays.asList("ds-0.t_order_2023_10", "ds-0.t_order_2023_11", "ds-0.t_order_2023_12");
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertEvaluateWithYearExpression() {
        List<String> actual = getInlineExpressionParser("P=ds-0.t_order_;SP=yyyy;DIA=1;DIU=Years;DL=2021;DU=2023").splitAndEvaluate();
        List<String> expected = Arrays.asList("ds-0.t_order_2021", "ds-0.t_order_2022", "ds-0.t_order_2023");
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertEvaluateWithLocalTimeExpression() {
        List<String> actual = getInlineExpressionParser("P=ds-0.t_order_;SP=HH_mm_ss_SSS;DIA=1;DIU=Millis;DL=22_48_52_131;DU=22_48_52_133").splitAndEvaluate();
        List<String> expected = Arrays.asList("ds-0.t_order_22_48_52_131", "ds-0.t_order_22_48_52_132", "ds-0.t_order_22_48_52_133");
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertEvaluateWithLocalDateTimeExpression() {
        List<String> actual = getInlineExpressionParser("P=ds-0.t_order_;SP=yyyy_MM_dd_HH_mm_ss_SSS;DIA=1;DIU=Days;DL=2023_12_04_22_48_52_131;DU=2023_12_06_22_48_52_131").splitAndEvaluate();
        List<String> expected = Arrays.asList("ds-0.t_order_2023_12_04_22_48_52_131", "ds-0.t_order_2023_12_05_22_48_52_131", "ds-0.t_order_2023_12_06_22_48_52_131");
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertEvaluateWithMonthExpression() {
        List<String> actual = getInlineExpressionParser("P=ds-0.t_order_;SP=MM;DIA=1;DIU=Months;DL=10;DU=12").splitAndEvaluate();
        List<String> expected = Arrays.asList("ds-0.t_order_10", "ds-0.t_order_11", "ds-0.t_order_12");
        assertThat(actual, is(expected));
    }
    
    /**
     * Background reference <a href="https://bugs.openjdk.org/browse/JDK-8068571">JDK-8068571</a>.
     * Unable to parse `GGGGyy_MM_dd` due to {@link java.time.chrono.JapaneseChronology} limitation.
     *
     * @see java.time.chrono.JapaneseChronology
     */
    @Test
    void assertEvaluateWithJapaneseDateExpression() {
        Locale originLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.JAPAN);
            List<String> actualBYGGGGyyyy = getInlineExpressionParser("P=ds-0.t_order_;SP=GGGGyyyy_MM_dd;DIA=1;DIU=Days;DL=平成0001_12_05;DU=平成0001_12_06;C=Japanese").splitAndEvaluate();
            List<String> expectedBYGGGGyyyy = Arrays.asList("ds-0.t_order_平成0001_12_05", "ds-0.t_order_平成0001_12_06");
            assertThat(actualBYGGGGyyyy, is(expectedBYGGGGyyyy));
            List<String> actualByGGGGyyy = getInlineExpressionParser("P=ds-0.t_order_;SP=GGGGyyy_MM_dd;DIA=1;DIU=Days;DL=平成001_12_05;DU=平成001_12_06;C=Japanese").splitAndEvaluate();
            List<String> expectedBYGGGGyyy = Arrays.asList("ds-0.t_order_平成001_12_05", "ds-0.t_order_平成001_12_06");
            assertThat(actualByGGGGyyy, is(expectedBYGGGGyyy));
            List<String> actualByGGGGy = getInlineExpressionParser("P=ds-0.t_order_;SP=GGGGy_MM_dd;DIA=1;DIU=Days;DL=平成1_12_05;DU=平成1_12_06;C=Japanese").splitAndEvaluate();
            List<String> expectedBYGGGGy = Arrays.asList("ds-0.t_order_平成1_12_05", "ds-0.t_order_平成1_12_06");
            assertThat(actualByGGGGy, is(expectedBYGGGGy));
            assertThrows(RuntimeException.class, () -> getInlineExpressionParser("P=ds-0.t_order_;SP=GGGGyy_MM_dd;DIA=1;DIU=Days;DL=平成01_12_05;DU=平成01_12_06;C=Japanese"));
        } finally {
            Locale.setDefault(originLocale);
        }
    }
    
    private InlineExpressionParser getInlineExpressionParser(final String expression) {
        Properties props = new Properties();
        props.setProperty(InlineExpressionParser.INLINE_EXPRESSION_KEY, expression);
        return TypedSPILoader.getService(InlineExpressionParser.class, "INTERVAL", props);
    }
}
