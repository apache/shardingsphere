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

package org.apache.shardingsphere.infra.expr.espresso;

import org.apache.shardingsphere.infra.expr.spi.InlineExpressionParser;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.condition.OS;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledOnJre(value = {JRE.JAVA_21, JRE.JAVA_23}, disabledReason = "This is used to match the requirement of `org.graalvm.polyglot:polyglot:24.1.0`")
@EnabledIfSystemProperty(named = "java.vm.vendor", matches = "GraalVM Community", disabledReason = "Github Actions device performance is too low")
@EnabledOnOs(value = OS.LINUX, architectures = "amd64", disabledReason = "See https://www.graalvm.org/jdk21/reference-manual/java-on-truffle/faq/#does-java-running-on-truffle-run-on-hotspot-too")
class EspressoInlineExpressionParserTest {
    
    @Test
    void assertEvaluateWithEmptyExpression() {
        assertTrue(getInlineExpressionParser("").splitAndEvaluate().isEmpty());
    }
    
    @Test
    void assertEvaluateWithSimpleExpression() {
        List<String> actual = getInlineExpressionParser(" t_order_0, t_order_1 ").splitAndEvaluate();
        List<String> expected = Arrays.asList("t_order_0", "t_order_1");
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertEvaluateWithNullExpression() {
        List<String> actual = getInlineExpressionParser("t_order_${null}").splitAndEvaluate();
        List<String> expected = Collections.singletonList("t_order_");
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertEvaluateWithLiteralExpression() {
        List<String> actual = getInlineExpressionParser("t_order_${'xx'}").splitAndEvaluate();
        List<String> expected = Collections.singletonList("t_order_xx");
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertEvaluateWithArrayExpression() {
        List<String> actual = getInlineExpressionParser("t_order_${[0, 1, 2]},t_order_item_${[0, 2]}").splitAndEvaluate();
        List<String> expected = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_item_0", "t_order_item_2");
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertEvaluateWithRangeExpression() {
        List<String> actual = getInlineExpressionParser("t_order_${0..2},t_order_item_${0..1}").splitAndEvaluate();
        List<String> expected = Arrays.asList("t_order_0", "t_order_1", "t_order_2", "t_order_item_0", "t_order_item_1");
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertEvaluateWithComplexExpression() {
        List<String> actual = getInlineExpressionParser("t_${['new','old']}_order_${1..2}, t_config").splitAndEvaluate();
        List<String> expected = Arrays.asList("t_new_order_1", "t_new_order_2", "t_old_order_1", "t_old_order_2", "t_config");
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertEvaluateWithCalculateExpression() {
        List<String> actual = getInlineExpressionParser("t_${[\"new${1+2}\",'old']}_order_${1..2}").splitAndEvaluate();
        List<String> expected = Arrays.asList("t_new3_order_1", "t_new3_order_2", "t_old_order_1", "t_old_order_2");
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertEvaluateWithPlaceHolderExpression() {
        List<String> actual = getInlineExpressionParser("t_$->{[\"new$->{1+2}\",'old']}_order_$->{1..2}").splitAndEvaluate();
        List<String> expected = Arrays.asList("t_new3_order_1", "t_new3_order_2", "t_old_order_1", "t_old_order_2");
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertEvaluateWithLongExpression() {
        StringBuilder expression = new StringBuilder();
        for (int i = 0; i < 1024; i++) {
            expression.append("ds_");
            expression.append(i / 64);
            expression.append(".t_user_");
            expression.append(i);
            if (i != 1023) {
                expression.append(",");
            }
        }
        List<String> actual = getInlineExpressionParser(expression.toString()).splitAndEvaluate();
        assertThat(actual.size(), is(1024));
        assertThat(actual, hasItems("ds_0.t_user_0", "ds_15.t_user_1023"));
    }
    
    @Test
    void assertEvaluateWithArgsExpression() {
        assertThrows(UnsupportedOperationException.class, () -> getInlineExpressionParser("${1+2}").evaluateWithArgs(Collections.emptyMap()));
    }
    
    private InlineExpressionParser getInlineExpressionParser(final String expression) {
        return TypedSPILoader.getService(InlineExpressionParser.class, "ESPRESSO", PropertiesBuilder.build(new PropertiesBuilder.Property(InlineExpressionParser.INLINE_EXPRESSION_KEY, expression)));
    }
}
