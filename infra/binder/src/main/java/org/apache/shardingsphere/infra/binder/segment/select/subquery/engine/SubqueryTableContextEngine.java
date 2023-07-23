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

package org.apache.shardingsphere.infra.binder.segment.select.subquery.engine;

import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.segment.select.subquery.SubqueryTableContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Subquery table context engine.
 */
public final class SubqueryTableContextEngine {
    
    /**
     * Create subquery table contexts.
     *
     * @param subqueryContext subquery context
     * @param aliasName subquery alias name
     * @return subquery table context map
     */
    public Map<String, SubqueryTableContext> createSubqueryTableContexts(final SelectStatementContext subqueryContext, final String aliasName) {
        Map<String, SubqueryTableContext> result = new LinkedHashMap<>();
        TableSegment tableSegment = subqueryContext.getSqlStatement().getFrom();
        for (Projection each : subqueryContext.getProjectionsContext().getExpandProjections()) {
            if (!(each instanceof ColumnProjection)) {
                continue;
            }
            String columnName = ((ColumnProjection) each).getName().getValue();
            if (tableSegment instanceof SimpleTableSegment) {
                String tableName = ((SimpleTableSegment) tableSegment).getTableName().getIdentifier().getValue();
                result.computeIfAbsent(tableName.toLowerCase(), unused -> new SubqueryTableContext(tableName, aliasName)).getColumnNames().add(columnName);
            }
            if (tableSegment instanceof JoinTableSegment && ((ColumnProjection) each).getOwner().isPresent()) {
                Optional<String> tableName = getTableNameByOwner(subqueryContext.getTablesContext().getSimpleTableSegments(), ((ColumnProjection) each).getOwner().get().getValue());
                tableName.ifPresent(optional -> result.computeIfAbsent(optional.toLowerCase(), unused -> new SubqueryTableContext(optional, aliasName)).getColumnNames().add(columnName));
            }
        }
        return result;
    }
    
    private Optional<String> getTableNameByOwner(final Collection<SimpleTableSegment> simpleTableSegments, final String owner) {
        for (SimpleTableSegment each : simpleTableSegments) {
            String tableNameOrAlias = each.getAliasName().orElseGet(() -> each.getTableName().getIdentifier().getValue());
            if (tableNameOrAlias.equalsIgnoreCase(owner)) {
                return Optional.of(each.getTableName().getIdentifier().getValue());
            }
        }
        return Optional.empty();
    }
}
