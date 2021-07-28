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

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.StandardParameterBuilder;
import org.apache.shardingsphere.shadow.rewrite.parameter.ShadowParameterRewriter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;

import java.util.List;

/**
 * Update value parameter rewriter for shadow.
 */
public final class ShadowUpdateValueParameterRewriter extends ShadowParameterRewriter<UpdateStatementContext> {
    
    @Override
    protected boolean isNeedRewriteForShadow(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof UpdateStatementContext && isContainShadowColumn(((UpdateStatementContext) sqlStatementContext).getSqlStatement());
    }
    
    private boolean isContainShadowColumn(final UpdateStatement updateStatement) {
        return updateStatement.getSetAssignment().getAssignments().stream().anyMatch(each -> each.getColumn().getIdentifier().getValue().equals(getShadowColumn()));
    }
    
    @Override
    public void rewrite(final ParameterBuilder parameterBuilder, final UpdateStatementContext updateStatementContext, final List<Object> parameters) {
        doShadowRewrite(parameterBuilder, updateStatementContext.getSqlStatement());
    }
    
    private void doShadowRewrite(final ParameterBuilder parameterBuilder, final UpdateStatement sqlStatement) {
        if (parameterBuilder instanceof StandardParameterBuilder) {
            ((StandardParameterBuilder) parameterBuilder).addRemovedParameters(getShadowColumnIndex(sqlStatement));
        }
    }
    
    private int getShadowColumnIndex(final UpdateStatement sqlStatement) {
        int count = 0;
        for (AssignmentSegment each : sqlStatement.getSetAssignment().getAssignments()) {
            if (each.getColumn().getIdentifier().getValue().equals(getShadowColumn())) {
                return count;
            }
            count++;
        }
        return count;
    }
}
