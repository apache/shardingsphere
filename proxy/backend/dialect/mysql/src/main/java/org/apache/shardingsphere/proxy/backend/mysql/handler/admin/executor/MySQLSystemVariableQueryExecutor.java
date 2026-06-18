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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultColumnMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultMetaData;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataMergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminQueryExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.sysvar.MySQLSystemVariable;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.sysvar.MySQLSystemVariableScope;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * MySQL system variable query executor.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLSystemVariableQueryExecutor implements DatabaseAdminQueryExecutor {
    
    private final List<ExpressionProjectionSegment> projections;
    
    private final List<MySQLSystemVariable> variables;
    
    @Getter
    private QueryResultMetaData queryResultMetaData;
    
    @Getter
    private MergedResult mergedResult;
    
    @Override
    public void execute(final ConnectionSession connectionSession, final ShardingSphereMetaData metaData) {
        List<RawQueryResultColumnMetaData> columnMetaData = new ArrayList<>(projections.size());
        List<Object> columnsOfRow = new ArrayList<>(projections.size());
        for (int i = 0; i < projections.size(); i++) {
            ExpressionProjectionSegment projection = projections.get(i);
            VariableSegment variableSegment = (VariableSegment) projection.getExpr();
            MySQLSystemVariableScope scope = variableSegment.getScope().map(MySQLSystemVariableScope::valueFrom).orElse(MySQLSystemVariableScope.DEFAULT);
            columnsOfRow.add(variables.get(i).getValue(scope, connectionSession));
            String name = projection.getAliasName().orElseGet(() -> "@@" + variableSegment.getScope().map(s -> s + ".").orElse("") + variableSegment.getVariable());
            columnMetaData.add(new RawQueryResultColumnMetaData("", name, name, Types.VARCHAR, "VARCHAR", 1024, 0));
        }
        queryResultMetaData = new RawQueryResultMetaData(columnMetaData);
        mergedResult = new LocalDataMergedResult(Collections.singleton(new LocalDataQueryResultRow(columnsOfRow.toArray())));
    }
    
    /**
     * Try to get {@link MySQLSystemVariableQueryExecutor} for select statement.
     *
     * @param selectStatement select statement
     * @return {@link MySQLSystemVariableQueryExecutor}
     */
    public static Optional<DatabaseAdminExecutor> tryGetSystemVariableQueryExecutor(final SelectStatement selectStatement) {
        Collection<ProjectionSegment> projections = selectStatement.getProjections().getProjections();
        List<ExpressionProjectionSegment> expressionProjectionSegments = new ArrayList<>(projections.size());
        List<MySQLSystemVariable> variables = new ArrayList<>(projections.size());
        for (ProjectionSegment each : projections) {
            if (!(each instanceof ExpressionProjectionSegment)) {
                return Optional.empty();
            }
            ExpressionProjectionSegment expression = (ExpressionProjectionSegment) each;
            if (!(expression.getExpr() instanceof VariableSegment)) {
                return Optional.empty();
            }
            expressionProjectionSegments.add(expression);
            VariableSegment variable = (VariableSegment) expression.getExpr();
            Optional<MySQLSystemVariable> systemVariable = MySQLSystemVariable.findSystemVariable(variable.getVariable());
            if (!systemVariable.isPresent()) {
                return Optional.empty();
            }
            variables.add(systemVariable.get());
        }
        return Optional.of(new MySQLSystemVariableQueryExecutor(expressionProjectionSegments, variables));
    }
}
