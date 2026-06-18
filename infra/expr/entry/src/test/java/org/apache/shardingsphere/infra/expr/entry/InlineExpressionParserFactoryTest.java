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

package org.apache.shardingsphere.infra.expr.entry;

import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InlineExpressionParserFactoryTest {
    
    @Test
    void assertNewInstance() {
        assertThat(InlineExpressionParserFactory.newInstance("t_order_0, t_order_1").getType(), is("GROOVY"));
        assertThat(InlineExpressionParserFactory.newInstance("t_order_0, t_order_1").handlePlaceHolder(), is("t_order_0, t_order_1"));
        assertThat(InlineExpressionParserFactory.newInstance("<GROOVY>t_order_0, t_order_1").getType(), is("GROOVY"));
        assertThat(InlineExpressionParserFactory.newInstance("<GROOVY>t_order_0, t_order_1").handlePlaceHolder(), is("t_order_0, t_order_1"));
        assertThat(InlineExpressionParserFactory.newInstance("<LITERAL>t_order_0, t_order_1").getType(), is("LITERAL"));
        assertThrows(UnsupportedOperationException.class, () -> InlineExpressionParserFactory.newInstance("<LITERAL>t_order_0, t_order_1").handlePlaceHolder());
    }
    
    @Test
    void assertUndefinedInstance() {
        assertThrows(ServiceProviderNotFoundException.class,
                () -> InlineExpressionParserFactory.newInstance("<UNDEFINED>t_order_0, t_order_1").getType());
    }
    
    @Test
    void assertFixtureInstance() {
        assertThat(InlineExpressionParserFactory.newInstance("<CUSTOM.FIXTURE>spring").handlePlaceHolder(), is("spring"));
        assertThat(InlineExpressionParserFactory.newInstance("<CUSTOM.FIXTURE>spring").splitAndEvaluate(),
                is(Arrays.asList("t_order_2023_03", "t_order_2023_04", "t_order_2023_05")));
        assertThat(InlineExpressionParserFactory.newInstance("<CUSTOM.FIXTURE>summer").splitAndEvaluate(),
                is(Arrays.asList("t_order_2023_06", "t_order_2023_07", "t_order_2023_08")));
        assertThat(InlineExpressionParserFactory.newInstance("<CUSTOM.FIXTURE>autumn").splitAndEvaluate(),
                is(Arrays.asList("t_order_2023_09", "t_order_2023_10", "t_order_2023_11")));
        assertThat(InlineExpressionParserFactory.newInstance("<CUSTOM.FIXTURE>winter").splitAndEvaluate(),
                is(Arrays.asList("t_order_2023_12", "t_order_2024_01", "t_order_2024_02")));
        assertThat(InlineExpressionParserFactory.newInstance("<CUSTOM.FIXTURE>").splitAndEvaluate(),
                is(Arrays.asList("t_order_2023_03", "t_order_2023_04", "t_order_2023_05",
                        "t_order_2023_06", "t_order_2023_07", "t_order_2023_08",
                        "t_order_2023_09", "t_order_2023_10", "t_order_2023_11",
                        "t_order_2023_12", "t_order_2024_01", "t_order_2024_02")));
    }
}
