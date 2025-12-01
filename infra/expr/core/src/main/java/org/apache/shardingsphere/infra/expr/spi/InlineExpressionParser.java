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

import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;

import java.util.List;
import java.util.Map;

/**
 * Inline expression parser.
 */
public interface InlineExpressionParser extends TypedSPI {
    
    /**
     * The expression used to build the InlineExpressionParser instance will be saved to the Properties instance via this key.
     */
    String INLINE_EXPRESSION_KEY = "inlineExpression";
    
    /**
     * Split and evaluate inline expression.
     *
     * @return result list
     */
    List<String> splitAndEvaluate();
    
    /**
     * This method is used to return the inlineExpression String itself. In some cases, you may want to do
     * additional processing on inlineExpression to return a specific value, in which case you need to override this method.
     * Normally there is no need to use this method downstream, because this method only encapsulates the use of Groovy syntax
     * by ShardingSphere's existing algorithm classes.
     * An {@link org.apache.shardingsphere.infra.expr.spi.InlineExpressionParser} implementation that implements this method
     * will provide the following algorithm classes with the ability to convert original expressions.
     * 1. `org.apache.shardingsphere.sharding.algorithm.sharding.hint.HintInlineShardingAlgorithm`
     * 2. `org.apache.shardingsphere.sharding.algorithm.sharding.inline.ComplexInlineShardingAlgorithm`
     * 3. `org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineShardingAlgorithm`
     *
     * @return result processed inline expression defined by the SPI implementation
     * @throws UnsupportedOperationException Thrown to indicate that the requested operation is not supported.
     */
    default String handlePlaceHolder() {
        throw new UnsupportedOperationException("This SPI implementation does not support the use of this method.");
    }
    
    /**
     * Evaluate with arguments.
     * Normally there is no need to use this method downstream, because this method only encapsulates the use of Groovy syntax
     * by ShardingSphere's existing algorithm classes.
     * An {@link org.apache.shardingsphere.infra.expr.spi.InlineExpressionParser} implementation that implements this method
     * will provide the following algorithm classes with the ability to use expressions outside the Groovy language.
     * 1. `org.apache.shardingsphere.sharding.algorithm.sharding.hint.HintInlineShardingAlgorithm`
     * 2. `org.apache.shardingsphere.sharding.algorithm.sharding.inline.ComplexInlineShardingAlgorithm`
     * 3. `org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineShardingAlgorithm`
     *
     * @param map map
     * @return closure
     * @throws UnsupportedOperationException By default, users do not need to consider passing in additional parameters.
     */
    default String evaluateWithArgs(final Map<String, Comparable<?>> map) {
        throw new UnsupportedOperationException("This SPI implementation does not support the use of this method.");
    }
}
