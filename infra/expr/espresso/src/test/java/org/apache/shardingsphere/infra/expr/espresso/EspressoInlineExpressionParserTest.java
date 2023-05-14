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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledInNativeImage;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@EnabledInNativeImage
@DisabledIfSystemProperty(named = "org.graalvm.nativeimage.imagecode", matches = "agent", disabledReason = "Skip this unit test when using GraalVM Native Build Tools")
class EspressoInlineExpressionParserTest {
    
    @Test
    void assertEvaluateForExpressionIsNull() {
        List<String> expected = new EspressoInlineExpressionParser().splitAndEvaluate(null);
        assertThat(expected, is(Collections.<String>emptyList()));
    }
    
    @Test
    void assertEvaluateForSimpleString() {
        List<String> expected = new EspressoInlineExpressionParser().splitAndEvaluate(" t_order_0, t_order_1 ");
        assertThat(expected.size(), is(2));
        assertThat(expected, hasItems("t_order_0", "t_order_1"));
    }
    
    @Test
    void assertEvaluateForNull() {
        List<String> expected = new EspressoInlineExpressionParser().splitAndEvaluate("t_order_${null}");
        assertThat(expected.size(), is(1));
        assertThat(expected, hasItems("t_order_"));
    }
    
    @Test
    void assertEvaluateForLiteral() {
        List<String> expected = new EspressoInlineExpressionParser().splitAndEvaluate("t_order_${'xx'}");
        assertThat(expected.size(), is(1));
        assertThat(expected, hasItems("t_order_xx"));
    }
    
    @Test
    void assertEvaluateForArray() {
        List<String> expected = new EspressoInlineExpressionParser().splitAndEvaluate("t_order_${[0, 1, 2]},t_order_item_${[0, 2]}");
        assertThat(expected.size(), is(5));
        assertThat(expected, hasItems("t_order_0", "t_order_1", "t_order_2", "t_order_item_0", "t_order_item_2"));
    }
    
    @Test
    void assertEvaluateForRange() {
        List<String> expected = new EspressoInlineExpressionParser().splitAndEvaluate("t_order_${0..2},t_order_item_${0..1}");
        assertThat(expected.size(), is(5));
        assertThat(expected, hasItems("t_order_0", "t_order_1", "t_order_2", "t_order_item_0", "t_order_item_1"));
    }
    
    @Test
    void assertEvaluateForComplex() {
        List<String> expected = new EspressoInlineExpressionParser().splitAndEvaluate("t_${['new','old']}_order_${1..2}, t_config");
        assertThat(expected.size(), is(5));
        assertThat(expected, hasItems("t_new_order_1", "t_new_order_2", "t_old_order_1", "t_old_order_2", "t_config"));
    }
    
    @Test
    void assertEvaluateForCalculate() {
        List<String> expected = new EspressoInlineExpressionParser().splitAndEvaluate("t_${[\"new${1+2}\",'old']}_order_${1..2}");
        assertThat(expected.size(), is(4));
        assertThat(expected, hasItems("t_new3_order_1", "t_new3_order_2", "t_old_order_1", "t_old_order_2"));
    }
    
    @Test
    void assertEvaluateForExpressionPlaceHolder() {
        List<String> expected = new EspressoInlineExpressionParser().splitAndEvaluate("t_$->{[\"new$->{1+2}\",'old']}_order_$->{1..2}");
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
        List<String> expected = new EspressoInlineExpressionParser().splitAndEvaluate(expression.toString());
        assertThat(expected.size(), is(1024));
        assertThat(expected, hasItems("ds_0.t_user_0", "ds_15.t_user_1023"));
    }
    
    @Test
    void assertHandlePlaceHolder() {
        assertThat(new EspressoInlineExpressionParser().handlePlaceHolder("t_$->{[\"new$->{1+2}\"]}"), is("t_${[\"new${1+2}\"]}"));
        assertThat(new EspressoInlineExpressionParser().handlePlaceHolder("t_${[\"new$->{1+2}\"]}"), is("t_${[\"new${1+2}\"]}"));
    }
    
    /*
     * TODO This method needs to avoid returning a groovy.lang.Closure class instance, and instead return the result of `Closure#call`. Because `org.graalvm.polyglot.Value#as` does not allow this type
     * to be returned from the guest JVM.
     */
    @Test
    @Disabled("See java doc")
    void assertEvaluateClosure() {
        assertThat(new EspressoInlineExpressionParser().evaluateClosure("${1+2}").call().toString(), is("3"));
    }
}
