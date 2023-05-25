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

package org.apache.shardingsphere.shadow.route.engine.dml;

import org.apache.shardingsphere.infra.binder.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.shadow.api.shadow.ShadowOperationType;
import org.apache.shardingsphere.shadow.condition.ShadowColumnCondition;
import org.apache.shardingsphere.shadow.exception.syntax.UnsupportedShadowInsertValueException;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Shadow insert statement routing engine.
 */
public final class ShadowInsertStatementRoutingEngine extends AbstractShadowDMLStatementRouteEngine {
    
    private final InsertStatementContext sqlStatementContext;
    
    public ShadowInsertStatementRoutingEngine(final InsertStatementContext sqlStatementContext) {
        super(sqlStatementContext, ShadowOperationType.INSERT);
        this.sqlStatementContext = sqlStatementContext;
    }
    
    @Override
    protected Collection<ShadowColumnCondition> getShadowColumnConditions(final String shadowColumnName) {
        Collection<ShadowColumnCondition> result = new LinkedList<>();
        int columnIndex = 0;
        for (String each : sqlStatementContext.getInsertColumnNames()) {
            if (!shadowColumnName.equals(each)) {
                columnIndex++;
                continue;
            }
            Collection<Comparable<?>> columnValues = getColumnValues(sqlStatementContext.getInsertValueContexts(), columnIndex);
            columnIndex++;
            result.add(new ShadowColumnCondition(getSingleTableName(), each, columnValues));
        }
        return result;
    }
    
    private Collection<Comparable<?>> getColumnValues(final List<InsertValueContext> insertValueContexts, final int columnIndex) {
        Collection<Comparable<?>> result = new LinkedList<>();
        for (InsertValueContext each : insertValueContexts) {
            Object columnValue = each.getLiteralValue(columnIndex).orElseThrow(() -> new UnsupportedShadowInsertValueException(columnIndex));
            ShardingSpherePreconditions.checkState(columnValue instanceof Comparable<?>, () -> new UnsupportedShadowInsertValueException(columnIndex));
            result.add((Comparable<?>) columnValue);
        }
        return result;
    }
}
