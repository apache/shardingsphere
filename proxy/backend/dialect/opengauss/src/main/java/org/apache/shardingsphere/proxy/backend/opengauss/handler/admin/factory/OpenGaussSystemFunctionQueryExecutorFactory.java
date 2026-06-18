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

package org.apache.shardingsphere.proxy.backend.opengauss.handler.admin.factory;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.opengauss.handler.admin.executor.OpenGaussSelectPasswordDeadlineExecutor;
import org.apache.shardingsphere.proxy.backend.opengauss.handler.admin.executor.OpenGaussSelectPasswordNotifyTimeExecutor;
import org.apache.shardingsphere.proxy.backend.opengauss.handler.admin.executor.OpenGaussSelectVersionExecutor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;

import java.util.Collection;
import java.util.Optional;

/**
 * System function query executor factory for openGauss.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OpenGaussSystemFunctionQueryExecutorFactory {
    
    /**
     * Create new instance of system function query executor.
     *
     * @param sqlStatementContext select statement context
     * @return created instance
     */
    public static Optional<DatabaseAdminExecutor> newInstance(final SelectStatementContext sqlStatementContext) {
        Collection<ProjectionSegment> projections = sqlStatementContext.getSqlStatement().getProjections().getProjections();
        if (1 != projections.size() || !(projections.iterator().next() instanceof ExpressionProjectionSegment)) {
            return Optional.empty();
        }
        String functionName = ((ExpressionProjectionSegment) projections.iterator().next()).getText();
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
