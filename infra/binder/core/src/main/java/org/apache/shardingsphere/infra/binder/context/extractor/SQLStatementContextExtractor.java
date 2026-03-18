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

package org.apache.shardingsphere.infra.binder.context.extractor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.available.WhereContextAvailable;
import org.apache.shardingsphere.infra.binder.context.segment.insert.values.InsertSelectContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.AlterViewStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CreateViewStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.metadata.database.schema.util.IndexMetaDataUtils;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.IndexSQLStatementAttribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * SQL statement context extractor.
 */
@HighFrequencyInvocation
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLStatementContextExtractor {
    
    /**
     * Get table names.
     *
     * @param database database
     * @param sqlStatementContext SQL statement context
     * @return table names
     */
    public static Collection<String> getTableNames(final ShardingSphereDatabase database, final SQLStatementContext sqlStatementContext) {
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        if (!tableNames.isEmpty()) {
            return tableNames;
        }
        return sqlStatementContext.getSqlStatement().getAttributes().findAttribute(IndexSQLStatementAttribute.class)
                .map(optional -> getTableNames(database, sqlStatementContext.getSqlStatement().getDatabaseType(), optional.getIndexes())).orElse(Collections.emptyList());
    }
    
    private static Collection<String> getTableNames(final ShardingSphereDatabase database, final DatabaseType databaseType, final Collection<IndexSegment> indexes) {
        Collection<String> result = new LinkedList<>();
        for (QualifiedTable each : IndexMetaDataUtils.getTableNames(database, databaseType, indexes)) {
            result.add(each.getTableName());
        }
        return result;
    }
    
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
            return result;
        }
        if (sqlStatementContext instanceof InsertStatementContext && null != ((InsertStatementContext) sqlStatementContext).getInsertSelectContext()) {
            InsertSelectContext insertSelectContext = ((InsertStatementContext) sqlStatementContext).getInsertSelectContext();
            result.add(insertSelectContext.getSelectStatementContext());
            result.addAll(insertSelectContext.getSelectStatementContext().getSubqueryContexts().values());
            insertSelectContext.getSelectStatementContext().getSubqueryContexts().values().forEach(each -> result.addAll(getAllSubqueryContexts(each)));
            return result;
        }
        if (sqlStatementContext instanceof CreateViewStatementContext) {
            CreateViewStatementContext createViewStatementContext = (CreateViewStatementContext) sqlStatementContext;
            result.add(createViewStatementContext.getSelectStatementContext());
            result.addAll(createViewStatementContext.getSelectStatementContext().getSubqueryContexts().values());
            createViewStatementContext.getSelectStatementContext().getSubqueryContexts().values().forEach(each -> result.addAll(getAllSubqueryContexts(each)));
            return result;
        }
        if (sqlStatementContext instanceof AlterViewStatementContext && ((AlterViewStatementContext) sqlStatementContext).getSelectStatementContext().isPresent()) {
            AlterViewStatementContext alterViewStatementContext = (AlterViewStatementContext) sqlStatementContext;
            result.add(alterViewStatementContext.getSelectStatementContext().get());
            result.addAll(alterViewStatementContext.getSelectStatementContext().get().getSubqueryContexts().values());
            alterViewStatementContext.getSelectStatementContext().get().getSubqueryContexts().values().forEach(each -> result.addAll(getAllSubqueryContexts(each)));
            return result;
        }
        return result;
    }
    
    /**
     * Get all where segments.
     *
     * @param sqlStatementContext SQL statement context
     * @return all where segments
     */
    public static Collection<WhereSegment> getAllWhereSegments(final SQLStatementContext sqlStatementContext) {
        if (!(sqlStatementContext instanceof WhereContextAvailable)) {
            return Collections.emptySet();
        }
        Collection<SelectStatementContext> allSubqueryContexts = getAllSubqueryContexts(sqlStatementContext);
        return getWhereSegments((WhereContextAvailable) sqlStatementContext, allSubqueryContexts);
    }
    
    /**
     * Get all where segments.
     *
     * @param whereContextAvailable where available
     * @param allSubqueryContexts all subquery contexts
     * @return all where segments
     */
    public static Collection<WhereSegment> getWhereSegments(final WhereContextAvailable whereContextAvailable, final Collection<SelectStatementContext> allSubqueryContexts) {
        Map<Integer, WhereSegment> uniqueWhereSegments = new LinkedHashMap<>(whereContextAvailable.getWhereSegments().size() + allSubqueryContexts.size(), 1F);
        whereContextAvailable.getWhereSegments().forEach(each -> uniqueWhereSegments.put(each.getStartIndex() + each.getStopIndex(), each));
        allSubqueryContexts.forEach(each -> each.getWhereSegments().forEach(where -> uniqueWhereSegments.put(where.getStartIndex() + where.getStopIndex(), where)));
        return new ArrayList<>(uniqueWhereSegments.values());
    }
    
    /**
     * Get all column segments.
     *
     * @param whereContextAvailable where available
     * @param allSubqueryContexts all subquery contexts
     * @return all column segments
     */
    public static Collection<ColumnSegment> getColumnSegments(final WhereContextAvailable whereContextAvailable, final Collection<SelectStatementContext> allSubqueryContexts) {
        Collection<ColumnSegment> result = new LinkedList<>(whereContextAvailable.getColumnSegments());
        allSubqueryContexts.forEach(each -> result.addAll(each.getColumnSegments()));
        return result;
    }
    
    /**
     * Get all join conditions.
     *
     * @param whereContextAvailable where available
     * @param allSubqueryContexts all subquery contexts
     * @return all join conditions
     */
    public static Collection<BinaryOperationExpression> getJoinConditions(final WhereContextAvailable whereContextAvailable, final Collection<SelectStatementContext> allSubqueryContexts) {
        Collection<BinaryOperationExpression> result = new LinkedList<>(whereContextAvailable.getJoinConditions());
        allSubqueryContexts.forEach(each -> result.addAll(each.getJoinConditions()));
        return result;
    }
}
