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

package org.apache.shardingsphere.encrypt.rewrite.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.context.segment.insert.values.InsertSelectContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.WhereAvailable;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Encrypt predicate segment utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EncryptPredicateSegmentUtils {
    
    /**
     * Get all subquery contexts.
     *
     * @param sqlStatementContext SQL statement context
     * @return all subquery contexts
     */
    public static Collection<SelectStatementContext> getAllSubqueryContexts(final SQLStatementContext sqlStatementContext) {
        Collection<SelectStatementContext> result = new LinkedList<>();
        if (sqlStatementContext instanceof SelectStatementContext) {
            result.addAll(((SelectStatementContext) sqlStatementContext).getSubqueryContexts().values());
            ((SelectStatementContext) sqlStatementContext).getSubqueryContexts().values().forEach(each -> result.addAll(getAllSubqueryContexts(each)));
        }
        if (sqlStatementContext instanceof InsertStatementContext && null != ((InsertStatementContext) sqlStatementContext).getInsertSelectContext()) {
            InsertSelectContext insertSelectContext = ((InsertStatementContext) sqlStatementContext).getInsertSelectContext();
            result.add(insertSelectContext.getSelectStatementContext());
            result.addAll(insertSelectContext.getSelectStatementContext().getSubqueryContexts().values());
            insertSelectContext.getSelectStatementContext().getSubqueryContexts().values().forEach(each -> result.addAll(getAllSubqueryContexts(each)));
        }
        return result;
    }
    
    /**
     * Get all where segments.
     *
     * @param whereAvailable where available
     * @param allSubqueryContexts all subquery contexts
     * @return all where segments
     */
    public static Collection<WhereSegment> getWhereSegments(final WhereAvailable whereAvailable, final Collection<SelectStatementContext> allSubqueryContexts) {
        Collection<WhereSegment> result = new LinkedList<>(whereAvailable.getWhereSegments());
        allSubqueryContexts.forEach(each -> result.addAll(each.getWhereSegments()));
        return result;
    }
    
    /**
     * Get all column segments.
     *
     * @param whereAvailable where available
     * @param allSubqueryContexts all subquery contexts
     * @return all column segments
     */
    public static Collection<ColumnSegment> getColumnSegments(final WhereAvailable whereAvailable, final Collection<SelectStatementContext> allSubqueryContexts) {
        Collection<ColumnSegment> result = new LinkedList<>(whereAvailable.getColumnSegments());
        allSubqueryContexts.forEach(each -> result.addAll(each.getColumnSegments()));
        return result;
    }
    
    /**
     * Get all join conditions.
     *
     * @param whereAvailable where available
     * @param allSubqueryContexts all subquery contexts
     * @return all join conditions
     */
    public static Collection<BinaryOperationExpression> getJoinConditions(final WhereAvailable whereAvailable, final Collection<SelectStatementContext> allSubqueryContexts) {
        Collection<BinaryOperationExpression> result = new LinkedList<>(whereAvailable.getJoinConditions());
        allSubqueryContexts.forEach(each -> result.addAll(each.getJoinConditions()));
        return result;
    }
}
