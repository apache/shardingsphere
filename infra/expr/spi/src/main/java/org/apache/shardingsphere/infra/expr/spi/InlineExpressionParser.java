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
     * The expression used to build the InlineExpressionParser instance will be saved to the Properties instance via this key.
     */
    String INLINE_EXPRESSION_KEY = "inlineExpression";
    
    /**
     * This method is used to return the inlineExpression String itself. In some cases, you may want to do
     * additional processing on inlineExpression to return a specific value, in which case you need to override this method.
     *
     * @return result processed inline expression defined by the SPI implementation
     */
    String handlePlaceHolder();
    
    /**
     * Split and evaluate inline expression.
     *
     * @return result list
     */
    List<String> splitAndEvaluate();
    
    /**
     * Evaluate closure.
     *
     * @return closure
     * @throws UnsupportedOperationException In most cases, users should not implement this method, and the return value of this method can only be a Groovy closure
     */
    default Closure<?> evaluateClosure() {
        throw new UnsupportedOperationException("This SPI implementation does not support the use of this method.");
    }
}
