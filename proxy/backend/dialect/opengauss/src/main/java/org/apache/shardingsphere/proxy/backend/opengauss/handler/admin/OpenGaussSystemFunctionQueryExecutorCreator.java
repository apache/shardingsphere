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

package org.apache.shardingsphere.proxy.backend.opengauss.handler.admin;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.opengauss.handler.admin.executor.OpenGaussSelectPasswordDeadlineExecutor;
import org.apache.shardingsphere.proxy.backend.opengauss.handler.admin.executor.OpenGaussSelectPasswordNotifyTimeExecutor;
import org.apache.shardingsphere.proxy.backend.opengauss.handler.admin.executor.OpenGaussSelectVersionExecutor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;

import java.util.Collection;
import java.util.Optional;

/**
 * OpenGauss system function query executor creator.
 */
@RequiredArgsConstructor
public final class OpenGaussSystemFunctionQueryExecutorCreator {
    
    private final SQLStatementContext sqlStatementContext;
    
    private String functionName;
    
    /**
     * Accept.
     *
     * @return true or false
     */
    public boolean accept() {
        if (!(sqlStatementContext.getSqlStatement() instanceof SelectStatement)) {
            return false;
        }
        SelectStatement selectStatement = (SelectStatement) sqlStatementContext.getSqlStatement();
        Collection<ProjectionSegment> projections = selectStatement.getProjections().getProjections();
        if (1 == projections.size() && projections.iterator().next() instanceof ExpressionProjectionSegment) {
            functionName = ((ExpressionProjectionSegment) projections.iterator().next()).getText();
            return OpenGaussSelectVersionExecutor.accept(functionName)
                    || OpenGaussSelectPasswordDeadlineExecutor.accept(functionName)
                    || OpenGaussSelectPasswordNotifyTimeExecutor.accept(functionName);
        }
        return false;
    }
    
    /**
     * Create.
     *
     * @return database admin executor
     */
    public Optional<DatabaseAdminExecutor> create() {
        if (OpenGaussSelectVersionExecutor.accept(functionName)) {
            return Optional.of(new OpenGaussSelectVersionExecutor());
        }
        if (OpenGaussSelectPasswordDeadlineExecutor.accept(functionName)) {
            return Optional.of(new OpenGaussSelectPasswordDeadlineExecutor(functionName));
        }
        if (OpenGaussSelectPasswordNotifyTimeExecutor.accept(functionName)) {
            return Optional.of(new OpenGaussSelectPasswordNotifyTimeExecutor());
        }
        return Optional.empty();
    }
}
