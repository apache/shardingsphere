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

package org.apache.shardingsphere.infra.util.expr;

import groovy.lang.Closure;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.util.groovy.expr.HotspotInlineExpressionParser;

import java.util.List;

/**
 * Inline expression parser.
 */
@RequiredArgsConstructor
public final class InlineExpressionParser {
    
    private static final boolean IS_SUBSTRATE_VM;
    
    private final EspressoInlineExpressionParser espressoInlineExpressionParser;
    
    private final HotspotInlineExpressionParser hotspotInlineExpressionParser;
    
    static {
        // workaround for https://github.com/helidon-io/helidon-build-tools/issues/858
        IS_SUBSTRATE_VM = System.getProperty("java.vm.name").equals("Substrate VM");
    }
    
    public InlineExpressionParser(final String inlineExpression) {
        if (IS_SUBSTRATE_VM) {
            this.hotspotInlineExpressionParser = null;
            this.espressoInlineExpressionParser = new EspressoInlineExpressionParser(inlineExpression);
        } else {
            this.hotspotInlineExpressionParser = new HotspotInlineExpressionParser(inlineExpression);
            this.espressoInlineExpressionParser = null;
        }
    }
    
    /**
     * Replace all inline expression placeholders.
     *
     * @param inlineExpression inline expression with {@code $->}
     * @return result inline expression with {@code $}
     */
    public static String handlePlaceHolder(final String inlineExpression) {
        if (IS_SUBSTRATE_VM) {
            return EspressoInlineExpressionParser.handlePlaceHolder(inlineExpression);
        } else {
            return HotspotInlineExpressionParser.handlePlaceHolder(inlineExpression);
        }
    }
    
    /**
     * Split and evaluate inline expression.
     *
     * @return result list
     */
    public List<String> splitAndEvaluate() {
        if (IS_SUBSTRATE_VM) {
            assert null != espressoInlineExpressionParser;
            return espressoInlineExpressionParser.splitAndEvaluate();
        } else {
            assert null != hotspotInlineExpressionParser;
            return hotspotInlineExpressionParser.splitAndEvaluate();
        }
    }
    
    /**
     * Evaluate closure.
     *
     * @return closure
     */
    public Closure<?> evaluateClosure() {
        if (IS_SUBSTRATE_VM) {
            assert null != espressoInlineExpressionParser;
            return espressoInlineExpressionParser.evaluateClosure();
        } else {
            assert null != hotspotInlineExpressionParser;
            return hotspotInlineExpressionParser.evaluateClosure();
        }
    }
}
