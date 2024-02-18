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
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LiteralInlineExpressionParserTest {
    
    @Test
    void assertEvaluateForExpressionIsNull() {
        InlineExpressionParser parser = TypedSPILoader.getService(InlineExpressionParser.class, "LITERAL", new Properties());
        List<String> expected = parser.splitAndEvaluate();
        assertThat(expected, is(Collections.<String>emptyList()));
    }
    
    @Test
    void assertEvaluateForSimpleString() {
        List<String> expected = TypedSPILoader.getService(InlineExpressionParser.class, "LITERAL", PropertiesBuilder.build(
                new PropertiesBuilder.Property(InlineExpressionParser.INLINE_EXPRESSION_KEY, " t_order_0, t_order_1 "))).splitAndEvaluate();
        assertThat(expected.size(), is(2));
        assertThat(expected, hasItems("t_order_0", "t_order_1"));
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
        List<String> expected = TypedSPILoader.getService(InlineExpressionParser.class, "LITERAL", PropertiesBuilder.build(
                new PropertiesBuilder.Property(InlineExpressionParser.INLINE_EXPRESSION_KEY, expression.toString()))).splitAndEvaluate();
        assertThat(expected.size(), is(1024));
        assertThat(expected, hasItems("ds_0.t_user_0", "ds_15.t_user_1023"));
    }
    
    @Test
    void assertHandlePlaceHolder() {
        assertThat(TypedSPILoader.getService(InlineExpressionParser.class, "LITERAL", PropertiesBuilder.build(
                new PropertiesBuilder.Property(InlineExpressionParser.INLINE_EXPRESSION_KEY, "t_$->{[\"new$->{1+2}\"]}"))).handlePlaceHolder(), is("t_$->{[\"new$->{1+2}\"]}"));
        assertThat(TypedSPILoader.getService(InlineExpressionParser.class, "LITERAL", PropertiesBuilder.build(
                new PropertiesBuilder.Property(InlineExpressionParser.INLINE_EXPRESSION_KEY, "t_${[\"new$->{1+2}\"]}"))).handlePlaceHolder(), is("t_${[\"new$->{1+2}\"]}"));
    }
    
    @Test
    void assertEvaluateWithArgs() {
        assertThrows(UnsupportedOperationException.class, () -> TypedSPILoader.getService(InlineExpressionParser.class, "LITERAL", PropertiesBuilder.build(
                new PropertiesBuilder.Property(InlineExpressionParser.INLINE_EXPRESSION_KEY, "${1+2}"))).evaluateWithArgs(new LinkedHashMap<>()));
    }
}
