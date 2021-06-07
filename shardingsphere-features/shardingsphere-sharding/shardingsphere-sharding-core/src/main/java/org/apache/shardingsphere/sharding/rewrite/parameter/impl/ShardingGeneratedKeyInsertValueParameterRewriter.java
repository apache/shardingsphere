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
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.GroupedParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewriter;

import java.util.Iterator;
import java.util.List;

/**
 * Sharding generated key insert value parameter rewriter.
 */
@Setter
public final class ShardingGeneratedKeyInsertValueParameterRewriter implements ParameterRewriter<InsertStatementContext> {
    
    @Override
    public boolean isNeedRewrite(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof InsertStatementContext 
                && ((InsertStatementContext) sqlStatementContext).getGeneratedKeyContext().isPresent() && ((InsertStatementContext) sqlStatementContext).getGeneratedKeyContext().get().isGenerated();
    }
    
    @Override
    public void rewrite(final ParameterBuilder parameterBuilder, final InsertStatementContext insertStatementContext, final List<Object> parameters) {
        Preconditions.checkState(insertStatementContext.getGeneratedKeyContext().isPresent());
        ((GroupedParameterBuilder) parameterBuilder).setDerivedColumnName(insertStatementContext.getGeneratedKeyContext().get().getColumnName());
        Iterator<Comparable<?>> generatedValues = insertStatementContext.getGeneratedKeyContext().get().getGeneratedValues().iterator();
        int count = 0;
        int parameterCount = 0;
        for (List<Object> each : insertStatementContext.getGroupedParameters()) {
            parameterCount += insertStatementContext.getInsertValueContexts().get(count).getParameterCount();
            Comparable<?> generatedValue = generatedValues.next();
            if (!each.isEmpty()) {
                ((GroupedParameterBuilder) parameterBuilder).getParameterBuilders().get(count).addAddedParameters(parameterCount, Lists.newArrayList(generatedValue));
            }
            count++;
        }
    }
}
