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

import org.apache.shardingsphere.infra.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.GroupedParameterBuilder;
import org.apache.shardingsphere.shadow.rewrite.parameter.ShadowParameterRewriter;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;

import java.util.LinkedList;
import java.util.List;

/**
 * Insert value parameter rewriter for shadow.
 */
public final class ShadowInsertValueParameterRewriter extends ShadowParameterRewriter<InsertStatementContext> {
    
    @Override
    protected boolean isNeedRewriteForShadow(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof InsertStatementContext && isContainShadowColumn((InsertStatementContext) sqlStatementContext);
    }
    
    private boolean isContainShadowColumn(final InsertStatementContext insertStatementContext) {
        return insertStatementContext.getInsertColumnNames().contains(getShadowColumn());
    }
    
    @Override
    public void rewrite(final ParameterBuilder parameterBuilder, final InsertStatementContext insertStatementContext, final List<Object> parameters) {
        doShadowRewrite(parameterBuilder, insertStatementContext);
    }

    private void doShadowRewrite(final ParameterBuilder parameterBuilder, final InsertStatementContext insertStatementContext) {
        if (parameterBuilder instanceof GroupedParameterBuilder) {
            GroupedParameterBuilder groupedParameterBuilder = (GroupedParameterBuilder) parameterBuilder;
            int columnIndex = getShadowColumnIndex(groupedParameterBuilder, insertStatementContext);
            addRemovedParametersForShadow(groupedParameterBuilder, insertStatementContext, columnIndex);
        }
    }

    private void addRemovedParametersForShadow(final GroupedParameterBuilder groupedParameterBuilder, final InsertStatementContext insertStatementContext, final int columnIndex) {
        int count = 0;
        for (List<Object> each : insertStatementContext.getGroupedParameters()) {
            if (!each.isEmpty()) {
                groupedParameterBuilder.getParameterBuilders().get(count).addRemovedParameters(columnIndex);
            }
            count++;
        }
    }

    private int getShadowColumnIndex(final GroupedParameterBuilder groupedParameterBuilder, final InsertStatementContext insertStatementContext) {
        List<String> columnNames = new LinkedList<>(insertStatementContext.getColumnNames());
        groupedParameterBuilder.getDerivedColumnName().ifPresent(columnNames::remove);
        return columnNames.indexOf(getShadowColumn());
    }
}
