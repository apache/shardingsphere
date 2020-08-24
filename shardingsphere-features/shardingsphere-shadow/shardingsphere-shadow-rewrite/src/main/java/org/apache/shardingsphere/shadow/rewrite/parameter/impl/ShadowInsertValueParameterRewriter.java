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

import org.apache.shardingsphere.shadow.rewrite.parameter.ShadowParameterRewriter;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.GroupedParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.StandardParameterBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Insert value parameter rewriter for shadow.
 */
public final class ShadowInsertValueParameterRewriter extends ShadowParameterRewriter<InsertStatementContext> {
    
    @Override
    protected boolean isNeedRewriteForShadow(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof InsertStatementContext && (((InsertStatementContext) sqlStatementContext).getSqlStatement()).getColumnNames().contains(getShadowRule().getColumn());
    }
    
    @Override
    public void rewrite(final ParameterBuilder parameterBuilder, final InsertStatementContext insertStatementContext, final List<Object> parameters) {
        String columnName = getShadowRule().getColumn();
        int columnIndex = getColumnIndex((GroupedParameterBuilder) parameterBuilder, insertStatementContext, columnName);
        int count = 0;
        for (List<Object> each : insertStatementContext.getGroupedParameters()) {
            if (!each.isEmpty()) {
                StandardParameterBuilder standardParameterBuilder = ((GroupedParameterBuilder) parameterBuilder).getParameterBuilders().get(count);
                standardParameterBuilder.addRemovedParameters(columnIndex);
            }
            count++;
        }
    }
    
    private int getColumnIndex(final GroupedParameterBuilder parameterBuilder, final InsertStatementContext insertStatementContext, final String shadowColumnName) {
        List<String> columnNames;
        if (parameterBuilder.getDerivedColumnName().isPresent()) {
            columnNames = new ArrayList<>(insertStatementContext.getColumnNames());
            columnNames.remove(parameterBuilder.getDerivedColumnName().get());
        } else {
            columnNames = insertStatementContext.getColumnNames();
        }
        return columnNames.indexOf(shadowColumnName);
    }
}
