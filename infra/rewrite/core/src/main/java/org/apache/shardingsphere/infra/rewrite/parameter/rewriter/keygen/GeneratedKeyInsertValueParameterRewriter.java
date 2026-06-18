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

package org.apache.shardingsphere.infra.rewrite.parameter.rewriter.keygen;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.GroupedParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.rewriter.ParameterRewriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Generated key insert value parameter rewriter.
 */
public final class GeneratedKeyInsertValueParameterRewriter implements ParameterRewriter {
    
    @Override
    public boolean isNeedRewrite(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof InsertStatementContext
                && ((InsertStatementContext) sqlStatementContext).getGeneratedKeyContext().isPresent()
                && ((InsertStatementContext) sqlStatementContext).getGeneratedKeyContext().get().isGenerated()
                && !((InsertStatementContext) sqlStatementContext).getGeneratedKeyContext().get().getGeneratedValues().isEmpty();
    }
    
    @Override
    public void rewrite(final ParameterBuilder paramBuilder, final SQLStatementContext sqlStatementContext, final List<Object> params) {
        InsertStatementContext insertStatementContext = (InsertStatementContext) sqlStatementContext;
        if (!insertStatementContext.getGeneratedKeyContext().isPresent()) {
            return;
        }
        ((GroupedParameterBuilder) paramBuilder).setDerivedColumnName(insertStatementContext.getGeneratedKeyContext().get().getColumnName());
        Iterator<Comparable<?>> generatedValues = insertStatementContext.getGeneratedKeyContext().get().getGeneratedValues().iterator();
        int count = 0;
        int parameterCount = 0;
        for (List<Object> each : insertStatementContext.getGroupedParameters()) {
            parameterCount += insertStatementContext.getInsertValueContexts().get(count).getParameterCount();
            Comparable<?> generatedValue = generatedValues.next();
            if (!each.isEmpty()) {
                ((GroupedParameterBuilder) paramBuilder).getParameterBuilders().get(count).addAddedParameters(parameterCount, new ArrayList<>(Collections.singleton(generatedValue)));
            }
            count++;
        }
    }
}
