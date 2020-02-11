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

package org.apache.shardingsphere.shadow.rewrite.parameter.impl;

import com.google.common.base.Optional;
import org.apache.shardingsphere.shadow.rewrite.condition.ShadowCondition;
import org.apache.shardingsphere.shadow.rewrite.condition.ShadowConditionEngine;
import org.apache.shardingsphere.shadow.rewrite.parameter.ShadowParameterRewriter;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.underlying.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.underlying.rewrite.parameter.builder.impl.StandardParameterBuilder;

import java.util.List;
import java.util.Map;

/**
 * Predicate parameter rewriter for shadow.
 *
 * @author zhyee
 */
public final class ShadowPredicateParameterRewriter extends ShadowParameterRewriter {
    
    @Override
    protected boolean isNeedRewriteForShadow(final SQLStatementContext sqlStatementContext) {
        return true;
    }
    
    @Override
    public void rewrite(final ParameterBuilder parameterBuilder, final SQLStatementContext sqlStatementContext, final List<Object> parameters) {
        Optional<ShadowCondition> shadowCondition = new ShadowConditionEngine(getShadowRule()).createShadowCondition(sqlStatementContext);
        if (shadowCondition.isPresent()) {
            replaceShadowParameter(parameterBuilder, shadowCondition.get().getPositionIndexMap());
        }
    }
    
    private void replaceShadowParameter(final ParameterBuilder parameterBuilder, final Map<Integer, Integer> positionIndexes) {
        if (!positionIndexes.isEmpty()) {
            for (Map.Entry<Integer, Integer> entry : positionIndexes.entrySet()) {
                ((StandardParameterBuilder) parameterBuilder).addRemovedParameters(entry.getValue());
            }
        }
    }
}
