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

package org.apache.shardingsphere.readwritesplitting.route.qualified.type;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.readwritesplitting.route.qualified.QualifiedReadwriteSplittingDataSourceRouter;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingDataSourceGroupRule;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;

/**
 * Qualified data source primary router for readwrite-splitting.
 */
public final class QualifiedReadwriteSplittingPrimaryDataSourceRouter implements QualifiedReadwriteSplittingDataSourceRouter {
    
    @Override
    public boolean isQualified(final SQLStatementContext sqlStatementContext, final ReadwriteSplittingDataSourceGroupRule rule, final HintValueContext hintValueContext) {
        return isPrimaryRoute(sqlStatementContext, hintValueContext);
    }
    
    private boolean isPrimaryRoute(final SQLStatementContext sqlStatementContext, final HintValueContext hintValueContext) {
        return isWriteRouteStatement(sqlStatementContext) || isHintWriteRouteOnly(hintValueContext);
    }
    
    private boolean isWriteRouteStatement(final SQLStatementContext sqlStatementContext) {
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        return containsLockSegment(sqlStatement) || containsLastInsertIdProjection(sqlStatementContext) || !(sqlStatement instanceof SelectStatement);
    }
    
    private boolean containsLockSegment(final SQLStatement sqlStatement) {
        return sqlStatement instanceof SelectStatement && ((SelectStatement) sqlStatement).getLock().isPresent();
    }
    
    private boolean containsLastInsertIdProjection(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext && ((SelectStatementContext) sqlStatementContext).getProjectionsContext().isContainsLastInsertIdProjection();
    }
    
    private boolean isHintWriteRouteOnly(final HintValueContext hintValueContext) {
        return HintManager.isWriteRouteOnly() || hintValueContext.isWriteRouteOnly();
    }
    
    @Override
    public String route(final ReadwriteSplittingDataSourceGroupRule rule) {
        return rule.getWriteDataSource();
    }
}
