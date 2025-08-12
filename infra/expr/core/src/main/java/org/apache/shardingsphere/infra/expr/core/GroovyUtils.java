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

package org.apache.shardingsphere.infra.expr.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * GroovyShell expression common utility class. It mainly serves the use of the following classes.
 * - `org.apache.shardingsphere.infra.expr.espresso.EspressoInlineExpressionParser`
 * - `org.apache.shardingsphere.infra.expr.groovy.GroovyInlineExpressionParser`
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GroovyUtils {
    
    /**
     * Split GroovyShell expression to a ArrayList.
     *
     * @param inlineExpression GroovySHell expression.
     * @return result ArrayList of GroovyShell expression with {@code $}.
     */
    public static List<String> split(final String inlineExpression) {
        List<String> result = new ArrayList<>();
        StringBuilder segment = new StringBuilder();
        int bracketsDepth = 0;
        for (int i = 0; i < inlineExpression.length(); i++) {
            char each = inlineExpression.charAt(i);
            switch (each) {
                case ',':
                    handleSplitter(bracketsDepth, segment, each, result);
                    break;
                case '$':
                    bracketsDepth = handleDollarSign(inlineExpression, i, bracketsDepth, segment, each);
                    break;
                case '}':
                    bracketsDepth = handleClosingBracket(bracketsDepth, segment, each);
                    break;
                default:
                    segment.append(each);
                    break;
            }
        }
        if (segment.length() > 0) {
            result.add(segment.toString().trim());
        }
        return result;
    }
    
    private static void handleSplitter(final int bracketsDepth, final StringBuilder segment, final char each, final List<String> result) {
        if (bracketsDepth > 0) {
            segment.append(each);
        } else {
            result.add(segment.toString().trim());
            segment.setLength(0);
        }
    }
    
    private static int handleDollarSign(final String inlineExpression, final int i, final int bracketsDepth, final StringBuilder segment, final char each) {
        int bracketsDepthResult = bracketsDepth;
        if ('{' == inlineExpression.charAt(i + 1)) {
            bracketsDepthResult = bracketsDepthResult + 1;
        }
        if ("->{".equals(inlineExpression.substring(i + 1, i + 4))) {
            bracketsDepthResult = bracketsDepthResult + 1;
        }
        segment.append(each);
        return bracketsDepthResult;
    }
    
    private static int handleClosingBracket(final int bracketsDepth, final StringBuilder segment, final char each) {
        segment.append(each);
        return bracketsDepth > 0 ? bracketsDepth - 1 : bracketsDepth;
    }
}
