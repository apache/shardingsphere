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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.condition.OS;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@EnabledForJreRange(min = JRE.JAVA_21)
@EnabledOnOs(value = OS.LINUX, disabledReason = "Refer to https://www.graalvm.org/jdk21/reference-manual/java-on-truffle/faq/#does-java-running-on-truffle-run-on-hotspot-too .")
@EnabledIfSystemProperty(named = "java.vm.vendor", matches = "GraalVM",
        disabledReason = "Executing unit tests of this type in batches will result in a timeout, while executing unit tests individually works fine.")
class EspressoInlineExpressionParserTest {
    
    @Test
    void assertEvaluateForExpressionIsNull() {
        InlineExpressionParser parser = TypedSPILoader.getService(InlineExpressionParser.class, "ESPRESSO", new Properties());
        List<String> expected = parser.splitAndEvaluate();
        assertThat(expected, is(Collections.<String>emptyList()));
    }
    
    @Test
    void assertEvaluateForSimpleString() {
        List<String> expected = createInlineExpressionParser(" t_order_0, t_order_1 ").splitAndEvaluate();
        assertThat(expected.size(), is(2));
        assertThat(expected, hasItems("t_order_0", "t_order_1"));
    }
    
    @Test
    void assertEvaluateForNull() {
        List<String> expected = createInlineExpressionParser("t_order_${null}").splitAndEvaluate();
        assertThat(expected.size(), is(1));
        assertThat(expected, hasItems("t_order_"));
    }
    
    @Test
    void assertEvaluateForLiteral() {
        List<String> expected = createInlineExpressionParser("t_order_${'xx'}").splitAndEvaluate();
        assertThat(expected.size(), is(1));
        assertThat(expected, hasItems("t_order_xx"));
    }
    
    @Test
    void assertEvaluateForArray() {
        List<String> expected = createInlineExpressionParser("t_order_${[0, 1, 2]},t_order_item_${[0, 2]}").splitAndEvaluate();
        assertThat(expected.size(), is(5));
        assertThat(expected, hasItems("t_order_0", "t_order_1", "t_order_2", "t_order_item_0", "t_order_item_2"));
    }
    
    @Test
    void assertEvaluateForRange() {
        List<String> expected = createInlineExpressionParser("t_order_${0..2},t_order_item_${0..1}").splitAndEvaluate();
        assertThat(expected.size(), is(5));
        assertThat(expected, hasItems("t_order_0", "t_order_1", "t_order_2", "t_order_item_0", "t_order_item_1"));
    }
    
    @Test
    void assertEvaluateForComplex() {
        List<String> expected = createInlineExpressionParser("t_${['new','old']}_order_${1..2}, t_config").splitAndEvaluate();
        assertThat(expected.size(), is(5));
        assertThat(expected, hasItems("t_new_order_1", "t_new_order_2", "t_old_order_1", "t_old_order_2", "t_config"));
    }
    
    @Test
    void assertEvaluateForCalculate() {
        List<String> expected = createInlineExpressionParser("t_${[\"new${1+2}\",'old']}_order_${1..2}").splitAndEvaluate();
        assertThat(expected.size(), is(4));
        assertThat(expected, hasItems("t_new3_order_1", "t_new3_order_2", "t_old_order_1", "t_old_order_2"));
    }
    
    @Test
    void assertEvaluateForExpressionPlaceHolder() {
        List<String> expected = createInlineExpressionParser("t_$->{[\"new$->{1+2}\",'old']}_order_$->{1..2}").splitAndEvaluate();
        assertThat(expected.size(), is(4));
        assertThat(expected, hasItems("t_new3_order_1", "t_new3_order_2", "t_old_order_1", "t_old_order_2"));
    }
    
    @Test
    void assertEvaluateForLong() {
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
        List<String> expected = createInlineExpressionParser(expression.toString()).splitAndEvaluate();
        assertThat(expected.size(), is(1024));
        assertThat(expected, hasItems("ds_0.t_user_0", "ds_15.t_user_1023"));
    }
    
    @Test
    void assertHandlePlaceHolder() {
        String expectdString = "t_${[\"new${1+2}\"]}";
        assertThat(createInlineExpressionParser("t_$->{[\"new$->{1+2}\"]}").handlePlaceHolder(), is(expectdString));
        assertThat(createInlineExpressionParser("t_${[\"new$->{1+2}\"]}").handlePlaceHolder(), is(expectdString));
    }
    
    @Test
    void assertEvaluateWithArgs() {
        assertThrows(UnsupportedOperationException.class, () -> createInlineExpressionParser("${1+2}").evaluateWithArgs(Collections.emptyMap()));
    }
    
    private InlineExpressionParser createInlineExpressionParser(final String expression) {
        Properties props = new Properties();
        props.put(InlineExpressionParser.INLINE_EXPRESSION_KEY, expression);
        return TypedSPILoader.getService(InlineExpressionParser.class, "ESPRESSO", props);
    }
}
