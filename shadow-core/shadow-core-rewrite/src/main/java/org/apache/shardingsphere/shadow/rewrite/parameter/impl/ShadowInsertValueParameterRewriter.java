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
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.underlying.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.underlying.rewrite.parameter.builder.impl.GroupedParameterBuilder;
import org.apache.shardingsphere.underlying.rewrite.parameter.builder.impl.StandardParameterBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Insert value parameter rewriter for shadow.
 *
 * @author zhyee
 */
public final class ShadowInsertValueParameterRewriter extends ShadowParameterRewriter {
    
    @Override
    protected boolean isNeedRewriteForShadow(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof InsertSQLStatementContext && ((InsertStatement) sqlStatementContext.getSqlStatement()).getColumnNames().contains(getShadowRule().getColumn());
    }
    
    @Override
    public void rewrite(final ParameterBuilder parameterBuilder, final SQLStatementContext sqlStatementContext, final List<Object> parameters) {
        String columnName = getShadowRule().getColumn();
        int columnIndex = getColumnIndex((GroupedParameterBuilder) parameterBuilder, (InsertSQLStatementContext) sqlStatementContext, columnName);
        int count = 0;
        for (List<Object> each : ((InsertSQLStatementContext) sqlStatementContext).getGroupedParameters()) {
            if (!each.isEmpty()) {
                StandardParameterBuilder standardParameterBuilder = ((GroupedParameterBuilder) parameterBuilder).getParameterBuilders().get(count);
                standardParameterBuilder.addRemovedParameters(columnIndex);
            }
            count++;
        }
    }
    
    private int getColumnIndex(final GroupedParameterBuilder parameterBuilder, final InsertSQLStatementContext sqlStatementContext, final String shadowColumnName) {
        List<String> columnNames;
        if (parameterBuilder.getDerivedColumnName().isPresent()) {
            columnNames = new ArrayList<>(sqlStatementContext.getColumnNames());
            columnNames.remove(parameterBuilder.getDerivedColumnName().get());
        } else {
            columnNames = sqlStatementContext.getColumnNames();
        }
        return columnNames.indexOf(shadowColumnName);
    }
}
