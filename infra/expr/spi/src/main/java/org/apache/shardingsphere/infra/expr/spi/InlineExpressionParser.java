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

package org.apache.shardingsphere.infra.expr.spi;

import groovy.lang.Closure;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;

import java.util.List;

/**
 * Inline expression parser.
 */
@SingletonSPI
public interface InlineExpressionParser extends TypedSPI {
    
    /**
     * Replace all inline expression placeholders.
     *
     * @param inlineExpression inline expression with {@code $->}
     * @return result inline expression with {@code $}
     */
    String handlePlaceHolder(String inlineExpression);
    
    /**
     * Split and evaluate inline expression.
     *
     * @param inlineExpression inline expression
     * @return result list
     */
    List<String> splitAndEvaluate(String inlineExpression);
    
    /**
     * Evaluate closure.
     *
     * @param inlineExpression inline expression
     * @return closure
     */
    Closure<?> evaluateClosure(String inlineExpression);
}
