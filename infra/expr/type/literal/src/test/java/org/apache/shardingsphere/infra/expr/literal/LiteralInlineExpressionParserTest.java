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

package org.apache.shardingsphere.infra.expr.literal;

import org.apache.shardingsphere.infra.expr.spi.InlineExpressionParser;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LiteralInlineExpressionParserTest {
    
    @Test
    void assertEvaluateWithEmptyExpression() {
        assertTrue(getInlineExpressionParser("").splitAndEvaluate().isEmpty());
    }
    
    @Test
    void assertEvaluateWithCommaExpression() {
        List<String> actual = getInlineExpressionParser(",").splitAndEvaluate();
        List<String> expected = Collections.singletonList("");
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertEvaluateWithSimpleExpression() {
        List<String> actual = getInlineExpressionParser(" t_order_0, t_order_1 ").splitAndEvaluate();
        List<String> expected = Arrays.asList("t_order_0", "t_order_1");
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
    void assertEvaluateWithPlaceholderExpression() {
        assertThrows(UnsupportedOperationException.class, () -> getInlineExpressionParser("t_$->{[\"new$->{1+2}\"]}").handlePlaceHolder());
        assertThrows(UnsupportedOperationException.class, () -> getInlineExpressionParser("t_${[\"new$->{1+2}\"]}").handlePlaceHolder());
    }
    
    @Test
    void assertEvaluateWithArgumentsExpression() {
        assertThrows(UnsupportedOperationException.class, () -> getInlineExpressionParser("${1+2}").evaluateWithArgs(Collections.emptyMap()));
    }
    
    private InlineExpressionParser getInlineExpressionParser(final String expression) {
        Properties props = new Properties();
        props.setProperty(InlineExpressionParser.INLINE_EXPRESSION_KEY, expression);
        return TypedSPILoader.getService(InlineExpressionParser.class, "LITERAL", props);
    }
}
