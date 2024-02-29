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

package org.apache.shardingsphere.infra.expr.groovy;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.expr.spi.InlineExpressionParser;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class GroovyInlineExpressionParserTest {
    
    @Test
    void assertEvaluateForExpressionIsNull() {
        InlineExpressionParser parser = TypedSPILoader.getService(InlineExpressionParser.class, "GROOVY", new Properties());
        List<String> expected = parser.splitAndEvaluate();
        assertThat(expected, is(Collections.<String>emptyList()));
    }
    
    @Test
    void assertEvaluateForSimpleString() {
        List<String> expected = TypedSPILoader.getService(InlineExpressionParser.class, "GROOVY", PropertiesBuilder.build(
                new PropertiesBuilder.Property(InlineExpressionParser.INLINE_EXPRESSION_KEY, " t_order_0, t_order_1 "))).splitAndEvaluate();
        assertThat(expected.size(), is(2));
        assertThat(expected, hasItems("t_order_0", "t_order_1"));
    }
    
    @Test
    void assertEvaluateForNull() {
        List<String> expected = TypedSPILoader.getService(InlineExpressionParser.class, "GROOVY", PropertiesBuilder.build(
                new PropertiesBuilder.Property(InlineExpressionParser.INLINE_EXPRESSION_KEY, "t_order_${null}"))).splitAndEvaluate();
        assertThat(expected.size(), is(1));
        assertThat(expected, hasItems("t_order_"));
    }
    
    @Test
    void assertEvaluateForLiteral() {
        List<String> expected = TypedSPILoader.getService(InlineExpressionParser.class, "GROOVY", PropertiesBuilder.build(
                new PropertiesBuilder.Property(InlineExpressionParser.INLINE_EXPRESSION_KEY, "t_order_${'xx'}"))).splitAndEvaluate();
        assertThat(expected.size(), is(1));
        assertThat(expected, hasItems("t_order_xx"));
    }
    
    @Test
    void assertEvaluateForArray() {
        List<String> expected = TypedSPILoader.getService(InlineExpressionParser.class, "GROOVY", PropertiesBuilder.build(
                new PropertiesBuilder.Property(InlineExpressionParser.INLINE_EXPRESSION_KEY, "t_order_${[0, 1, 2]},t_order_item_${[0, 2]}"))).splitAndEvaluate();
        assertThat(expected.size(), is(5));
        assertThat(expected, hasItems("t_order_0", "t_order_1", "t_order_2", "t_order_item_0", "t_order_item_2"));
    }
    
    @Test
    void assertEvaluateForRange() {
        List<String> expected = TypedSPILoader.getService(InlineExpressionParser.class, "GROOVY", PropertiesBuilder.build(
                new PropertiesBuilder.Property(InlineExpressionParser.INLINE_EXPRESSION_KEY, "t_order_${0..2},t_order_item_${0..1}"))).splitAndEvaluate();
        assertThat(expected.size(), is(5));
        assertThat(expected, hasItems("t_order_0", "t_order_1", "t_order_2", "t_order_item_0", "t_order_item_1"));
    }
    
    @Test
    void assertEvaluateForComplex() {
        List<String> expected = TypedSPILoader.getService(InlineExpressionParser.class, "GROOVY", PropertiesBuilder.build(
                new PropertiesBuilder.Property(InlineExpressionParser.INLINE_EXPRESSION_KEY, "t_${['new','old']}_order_${1..2}, t_config"))).splitAndEvaluate();
        assertThat(expected.size(), is(5));
        assertThat(expected, hasItems("t_new_order_1", "t_new_order_2", "t_old_order_1", "t_old_order_2", "t_config"));
    }
    
    @Test
    void assertEvaluateForCalculate() {
        List<String> expected = TypedSPILoader.getService(InlineExpressionParser.class, "GROOVY", PropertiesBuilder.build(
                new PropertiesBuilder.Property(InlineExpressionParser.INLINE_EXPRESSION_KEY, "t_${[\"new${1+2}\",'old']}_order_${1..2}"))).splitAndEvaluate();
        assertThat(expected.size(), is(4));
        assertThat(expected, hasItems("t_new3_order_1", "t_new3_order_2", "t_old_order_1", "t_old_order_2"));
    }
    
    @Test
    void assertEvaluateForExpressionPlaceHolder() {
        List<String> expected = TypedSPILoader.getService(InlineExpressionParser.class, "GROOVY", PropertiesBuilder.build(
                new PropertiesBuilder.Property(InlineExpressionParser.INLINE_EXPRESSION_KEY, "t_$->{[\"new$->{1+2}\",'old']}_order_$->{1..2}"))).splitAndEvaluate();
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
        List<String> expected = TypedSPILoader.getService(InlineExpressionParser.class, "GROOVY", PropertiesBuilder.build(
                new PropertiesBuilder.Property(InlineExpressionParser.INLINE_EXPRESSION_KEY, expression.toString()))).splitAndEvaluate();
        assertThat(expected.size(), is(1024));
        assertThat(expected, hasItems("ds_0.t_user_0", "ds_15.t_user_1023"));
    }
    
    @Test
    void assertHandlePlaceHolder() {
        assertThat(TypedSPILoader.getService(InlineExpressionParser.class, "GROOVY", PropertiesBuilder.build(
                new PropertiesBuilder.Property(InlineExpressionParser.INLINE_EXPRESSION_KEY, "t_$->{[\"new$->{1+2}\"]}"))).handlePlaceHolder(), is("t_${[\"new${1+2}\"]}"));
        assertThat(TypedSPILoader.getService(InlineExpressionParser.class, "GROOVY", PropertiesBuilder.build(
                new PropertiesBuilder.Property(InlineExpressionParser.INLINE_EXPRESSION_KEY, "t_${[\"new$->{1+2}\"]}"))).handlePlaceHolder(), is("t_${[\"new${1+2}\"]}"));
    }
    
    @Test
    void assertEvaluateWithArgs() {
        assertThat(TypedSPILoader.getService(InlineExpressionParser.class, "GROOVY", PropertiesBuilder.build(
                new PropertiesBuilder.Property(InlineExpressionParser.INLINE_EXPRESSION_KEY, "${1+2}"))).evaluateWithArgs(new LinkedHashMap<>()), is("3"));
    }
    
    @Test
    @SneakyThrows({ExecutionException.class, InterruptedException.class})
    void assertThreadSafety() {
        int threadCount = 10;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        List<Future<?>> futures = new ArrayList<>(threadCount);
        for (int i = 0; i < threadCount; i++) {
            Future<?> future = pool.submit(this::createInlineExpressionParseTask);
            futures.add(future);
        }
        for (Future<?> future : futures) {
            future.get();
        }
        pool.shutdown();
    }
    
    private void createInlineExpressionParseTask() {
        for (int j = 0; j < 5; j++) {
            String resultSuffix = Thread.currentThread().getName() + "--" + j;
            String actual = TypedSPILoader.getService(InlineExpressionParser.class, "GROOVY", PropertiesBuilder.build(
                    new PropertiesBuilder.Property(InlineExpressionParser.INLINE_EXPRESSION_KEY, "ds_${id}"))).evaluateWithArgs(Collections.singletonMap("id", resultSuffix));
            assertThat(actual, is(String.format("ds_%s", resultSuffix)));
            String actual2 = TypedSPILoader.getService(InlineExpressionParser.class, "GROOVY", PropertiesBuilder.build(
                    new PropertiesBuilder.Property(InlineExpressionParser.INLINE_EXPRESSION_KEY, "account_${id}"))).evaluateWithArgs(Collections.singletonMap("id", resultSuffix));
            assertThat(actual2, is(String.format("account_%s", resultSuffix)));
        }
    }
}
