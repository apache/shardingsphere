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

package org.apache.shardingsphere.infra.expr.entry.fixture;

import org.apache.shardingsphere.infra.expr.spi.InlineExpressionParser;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public final class CustomInlineExpressionParserFixture implements InlineExpressionParser {
    
    private String inlineExpression;
    
    @Override
    public void init(final Properties props) {
        inlineExpression = props.getProperty(INLINE_EXPRESSION_KEY);
    }
    
    @Override
    public String handlePlaceHolder() {
        return inlineExpression;
    }
    
    @Override
    public List<String> splitAndEvaluate() {
        switch (inlineExpression) {
            case "spring":
                return Arrays.asList("t_order_2023_03", "t_order_2023_04", "t_order_2023_05");
            case "summer":
                return Arrays.asList("t_order_2023_06", "t_order_2023_07", "t_order_2023_08");
            case "autumn":
                return Arrays.asList("t_order_2023_09", "t_order_2023_10", "t_order_2023_11");
            case "winter":
                return Arrays.asList("t_order_2023_12", "t_order_2024_01", "t_order_2024_02");
            default:
                return Arrays.asList("t_order_2023_03", "t_order_2023_04", "t_order_2023_05",
                        "t_order_2023_06", "t_order_2023_07", "t_order_2023_08",
                        "t_order_2023_09", "t_order_2023_10", "t_order_2023_11",
                        "t_order_2023_12", "t_order_2024_01", "t_order_2024_02");
        }
    }
    
    @Override
    public Object getType() {
        return "CUSTOM.FIXTURE";
    }
}
