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

package org.apache.shardingsphere.sharding.rewrite.parameter.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import lombok.Setter;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.GroupedParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewriter;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.ReplaceStatementContext;

import java.util.Iterator;
import java.util.List;

/**
 * Sharding generated key replace value parameter rewriter.
 */
@Setter
public final class ShardingGeneratedKeyReplaceValueParameterRewriter implements ParameterRewriter<ReplaceStatementContext> {
    
    @Override
    public boolean isNeedRewrite(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof ReplaceStatementContext
                && ((ReplaceStatementContext) sqlStatementContext).getGeneratedKeyContext().isPresent() && ((ReplaceStatementContext) sqlStatementContext).getGeneratedKeyContext().get().isGenerated();
    }
    
    @Override
    public void rewrite(final ParameterBuilder parameterBuilder, final ReplaceStatementContext replaceStatementContext, final List<Object> parameters) {
        Preconditions.checkState(replaceStatementContext.getGeneratedKeyContext().isPresent());
        ((GroupedParameterBuilder) parameterBuilder).setDerivedColumnName(replaceStatementContext.getGeneratedKeyContext().get().getColumnName());
        Iterator<Comparable<?>> generatedValues = replaceStatementContext.getGeneratedKeyContext().get().getGeneratedValues().descendingIterator();
        int count = 0;
        int parametersCount = 0;
        for (List<Object> each : replaceStatementContext.getGroupedParameters()) {
            parametersCount += replaceStatementContext.getInsertValueContexts().get(count).getParametersCount();
            Comparable<?> generatedValue = generatedValues.next();
            if (!each.isEmpty()) {
                ((GroupedParameterBuilder) parameterBuilder).getParameterBuilders().get(count).addAddedParameters(parametersCount, Lists.newArrayList(generatedValue));
            }
            count++;
        }
    }
}
