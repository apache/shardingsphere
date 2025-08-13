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
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroovyInlineExpressionParserTest {
    
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
        assertThat(getInlineExpressionParser("${1+2}").evaluateWithArgs(Collections.emptyMap()), is("3"));
    }
    
    @Test
    @SneakyThrows({ExecutionException.class, InterruptedException.class})
    void assertEvaluateForThreadSafety() {
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        for (Future<?> future : IntStream.range(0, threadCount).mapToObj(i -> executorService.submit(this::createInlineExpressionParseTask)).collect(Collectors.toList())) {
            future.get();
        }
        executorService.shutdown();
    }
    
    private void createInlineExpressionParseTask() {
        for (int j = 0; j < 5; j++) {
            String resultSuffix = Thread.currentThread().getName() + "--" + j;
            String actual = getInlineExpressionParser("ds_${id}").evaluateWithArgs(Collections.singletonMap("id", resultSuffix));
            assertThat(actual, is(String.format("ds_%s", resultSuffix)));
            String actual2 = getInlineExpressionParser("account_${id}").evaluateWithArgs(Collections.singletonMap("id", resultSuffix));
            assertThat(actual2, is(String.format("account_%s", resultSuffix)));
        }
    }
    
    private InlineExpressionParser getInlineExpressionParser(final String expression) {
        Properties props = new Properties();
        props.setProperty(InlineExpressionParser.INLINE_EXPRESSION_KEY, expression);
        return TypedSPILoader.getService(InlineExpressionParser.class, "GROOVY", props);
    }
}
