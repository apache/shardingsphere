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

package org.apache.shardingsphere.infra.binder.context.statement.dml;

import com.cedarsoftware.util.CaseInsensitiveMap;
import lombok.Getter;
import org.apache.shardingsphere.infra.binder.context.aware.ParameterAware;
import org.apache.shardingsphere.infra.binder.context.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.context.segment.insert.keygen.engine.GeneratedKeyContextEngine;
import org.apache.shardingsphere.infra.binder.context.segment.insert.values.InsertSelectContext;
import org.apache.shardingsphere.infra.binder.context.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.context.segment.insert.values.OnDuplicateUpdateContext;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.TableAvailable;
import org.apache.shardingsphere.infra.binder.context.type.WhereAvailable;
import org.apache.shardingsphere.infra.binder.context.type.WithAvailable;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.statement.core.enums.SubqueryType;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.InsertStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Insert SQL statement context.
 */
public final class InsertStatementContext extends CommonSQLStatementContext implements TableAvailable, ParameterAware, WhereAvailable, WithAvailable {
    
    private final ShardingSphereMetaData metaData;
    
    private final String currentDatabaseName;
    
    private final Map<String, Integer> insertColumnNamesAndIndexes;
    
    private final List<List<ExpressionSegment>> valueExpressions;
    
    @Getter
    private final TablesContext tablesContext;
    
    @Getter
    private final List<String> columnNames;
    
    @Getter
    private List<InsertValueContext> insertValueContexts;
    
    @Getter
    private InsertSelectContext insertSelectContext;
    
    @Getter
    private OnDuplicateUpdateContext onDuplicateKeyUpdateValueContext;
    
    private GeneratedKeyContext generatedKeyContext;
    
    public InsertStatementContext(final ShardingSphereMetaData metaData, final List<Object> params, final InsertStatement sqlStatement, final String currentDatabaseName) {
        super(sqlStatement);
        this.metaData = metaData;
        this.currentDatabaseName = currentDatabaseName;
        valueExpressions = getAllValueExpressions(sqlStatement);
        AtomicInteger parametersOffset = new AtomicInteger(0);
        insertValueContexts = getInsertValueContexts(params, parametersOffset, valueExpressions);
        insertSelectContext = getInsertSelectContext(metaData, params, parametersOffset, currentDatabaseName).orElse(null);
        onDuplicateKeyUpdateValueContext = getOnDuplicateKeyUpdateValueContext(params, parametersOffset).orElse(null);
        tablesContext = new TablesContext(getAllSimpleTableSegments());
        List<String> insertColumnNames = getInsertColumnNames();
        ShardingSphereSchema schema = getSchema(metaData, currentDatabaseName);
        columnNames = containsInsertColumns()
                ? insertColumnNames
                : sqlStatement.getTable().map(optional -> schema.getVisibleColumnNames(optional.getTableName().getIdentifier().getValue())).orElseGet(Collections::emptyList);
        insertColumnNamesAndIndexes = createInsertColumnNamesAndIndexes(insertColumnNames);
        generatedKeyContext = new GeneratedKeyContextEngine(sqlStatement, schema).createGenerateKeyContext(insertColumnNamesAndIndexes, insertValueContexts, params).orElse(null);
    }
    
    private Map<String, Integer> createInsertColumnNamesAndIndexes(final List<String> insertColumnNames) {
        if (containsInsertColumns()) {
            Map<String, Integer> result = new CaseInsensitiveMap<>(insertColumnNames.size(), 1F);
            int index = 0;
            for (String each : insertColumnNames) {
                result.put(each, index++);
            }
            return result;
        }
        return Collections.emptyMap();
    }
    
    private List<InsertValueContext> getInsertValueContexts(final List<Object> params, final AtomicInteger paramsOffset, final List<List<ExpressionSegment>> valueExpressions) {
        List<InsertValueContext> result = new LinkedList<>();
        for (Collection<ExpressionSegment> each : valueExpressions) {
            InsertValueContext insertValueContext = new InsertValueContext(each, params, paramsOffset.get());
            result.add(insertValueContext);
            paramsOffset.addAndGet(insertValueContext.getParameterCount());
        }
        return result;
    }
    
    private Optional<InsertSelectContext> getInsertSelectContext(final ShardingSphereMetaData metaData, final List<Object> params,
                                                                 final AtomicInteger paramsOffset, final String currentDatabaseName) {
        if (!getSqlStatement().getInsertSelect().isPresent()) {
            return Optional.empty();
        }
        SubquerySegment insertSelectSegment = getSqlStatement().getInsertSelect().get();
        SelectStatementContext selectStatementContext = new SelectStatementContext(metaData, params, insertSelectSegment.getSelect(), currentDatabaseName, Collections.emptyList());
        selectStatementContext.setSubqueryType(SubqueryType.INSERT_SELECT);
        setCombineSelectSubqueryType(selectStatementContext);
        setProjectionSelectSubqueryType(selectStatementContext);
        InsertSelectContext insertSelectContext = new InsertSelectContext(selectStatementContext, params, paramsOffset.get());
        paramsOffset.addAndGet(insertSelectContext.getParameterCount());
        return Optional.of(insertSelectContext);
    }
    
    private void setCombineSelectSubqueryType(final SelectStatementContext selectStatementContext) {
        if (selectStatementContext.getSqlStatement().getCombine().isPresent()) {
            CombineSegment combineSegment = selectStatementContext.getSqlStatement().getCombine().get();
            Optional.ofNullable(selectStatementContext.getSubqueryContexts().get(combineSegment.getLeft().getStartIndex()))
                    .ifPresent(optional -> optional.setSubqueryType(SubqueryType.INSERT_SELECT));
            Optional.ofNullable(selectStatementContext.getSubqueryContexts().get(combineSegment.getRight().getStartIndex()))
                    .ifPresent(optional -> optional.setSubqueryType(SubqueryType.INSERT_SELECT));
        }
    }
    
    private void setProjectionSelectSubqueryType(final SelectStatementContext selectStatementContext) {
        for (Entry<Integer, SelectStatementContext> entry : selectStatementContext.getSubqueryContexts().entrySet()) {
            if (entry.getKey() >= selectStatementContext.getProjectionsContext().getStartIndex() && entry.getKey() <= selectStatementContext.getProjectionsContext().getStopIndex()) {
                entry.getValue().setSubqueryType(SubqueryType.INSERT_SELECT);
            }
        }
    }
    
    private Optional<OnDuplicateUpdateContext> getOnDuplicateKeyUpdateValueContext(final List<Object> params, final AtomicInteger parametersOffset) {
        Optional<OnDuplicateKeyColumnsSegment> onDuplicateKeyColumnsSegment = getSqlStatement().getOnDuplicateKeyColumns();
        if (!onDuplicateKeyColumnsSegment.isPresent()) {
            return Optional.empty();
        }
        Collection<ColumnAssignmentSegment> onDuplicateKeyColumns = onDuplicateKeyColumnsSegment.get().getColumns();
        OnDuplicateUpdateContext onDuplicateUpdateContext = new OnDuplicateUpdateContext(onDuplicateKeyColumns, params, parametersOffset.get());
        parametersOffset.addAndGet(onDuplicateUpdateContext.getParameterCount());
        return Optional.of(onDuplicateUpdateContext);
    }
    
    private Collection<SimpleTableSegment> getAllSimpleTableSegments() {
        TableExtractor tableExtractor = new TableExtractor();
        tableExtractor.extractTablesFromInsert(getSqlStatement());
        return tableExtractor.getRewriteTables();
    }
    
    private ShardingSphereSchema getSchema(final ShardingSphereMetaData metaData, final String currentDatabaseName) {
        String databaseName = tablesContext.getDatabaseName().orElse(currentDatabaseName);
        ShardingSpherePreconditions.checkNotNull(databaseName, NoDatabaseSelectedException::new);
        ShardingSphereDatabase database = metaData.getDatabase(databaseName);
        ShardingSpherePreconditions.checkNotNull(database, () -> new UnknownDatabaseException(databaseName));
        String defaultSchema = new DatabaseTypeRegistry(getDatabaseType()).getDefaultSchemaName(databaseName);
        return tablesContext.getSchemaName().map(database::getSchema).orElseGet(() -> database.getSchema(defaultSchema));
    }
    
    /**
     * Get column names for descending order.
     *
     * @return column names for descending order
     */
    public Iterator<String> getDescendingColumnNames() {
        return new LinkedList<>(columnNames).descendingIterator();
    }
    
    /**
     * Get grouped parameters.
     *
     * @return grouped parameters
     */
    public List<List<Object>> getGroupedParameters() {
        List<List<Object>> result = new LinkedList<>();
        for (InsertValueContext each : insertValueContexts) {
            result.add(each.getParameters());
        }
        if (null != insertSelectContext && !insertSelectContext.getParameters().isEmpty()) {
            result.add(insertSelectContext.getParameters());
        }
        return result;
    }
    
    /**
     * Get on duplicate key update parameters.
     *
     * @return on duplicate key update parameters
     */
    public List<Object> getOnDuplicateKeyUpdateParameters() {
        return null == onDuplicateKeyUpdateValueContext ? new ArrayList<>() : onDuplicateKeyUpdateValueContext.getParameters();
    }
    
    /**
     * Get generated key context.
     *
     * @return generated key context
     */
    public Optional<GeneratedKeyContext> getGeneratedKeyContext() {
        return Optional.ofNullable(generatedKeyContext);
    }
    
    /**
     * Judge whether contains insert columns.
     *
     * @return contains insert columns or not
     */
    public boolean containsInsertColumns() {
        InsertStatement insertStatement = getSqlStatement();
        return !insertStatement.getColumns().isEmpty() || insertStatement.getSetAssignment().isPresent();
    }
    
    /**
     * Get value list count.
     *
     * @return value list count
     */
    public int getValueListCount() {
        InsertStatement insertStatement = getSqlStatement();
        return insertStatement.getSetAssignment().isPresent() ? 1 : insertStatement.getValues().size();
    }
    
    /**
     * Get insert column names.
     *
     * @return column names collection
     */
    public List<String> getInsertColumnNames() {
        return getSqlStatement().getSetAssignment().map(this::getColumnNamesForSetAssignment).orElseGet(() -> getColumnNamesForInsertColumns(getSqlStatement().getColumns()));
    }
    
    private List<String> getColumnNamesForSetAssignment(final SetAssignmentSegment setAssignment) {
        List<String> result = new LinkedList<>();
        for (ColumnAssignmentSegment each : setAssignment.getAssignments()) {
            result.add(each.getColumns().get(0).getIdentifier().getValue().toLowerCase());
        }
        return result;
    }
    
    private List<String> getColumnNamesForInsertColumns(final Collection<ColumnSegment> columns) {
        List<String> result = new LinkedList<>();
        for (ColumnSegment each : columns) {
            result.add(each.getIdentifier().getValue().toLowerCase());
        }
        return result;
    }
    
    private List<List<ExpressionSegment>> getAllValueExpressions(final InsertStatement insertStatement) {
        Optional<SetAssignmentSegment> setAssignment = insertStatement.getSetAssignment();
        return setAssignment
                .map(optional -> Collections.singletonList(getAllValueExpressionsFromSetAssignment(optional))).orElseGet(() -> getAllValueExpressionsFromValues(insertStatement.getValues()));
    }
    
    private List<ExpressionSegment> getAllValueExpressionsFromSetAssignment(final SetAssignmentSegment setAssignment) {
        List<ExpressionSegment> result = new ArrayList<>(setAssignment.getAssignments().size());
        for (ColumnAssignmentSegment each : setAssignment.getAssignments()) {
            result.add(each.getValue());
        }
        return result;
    }
    
    private List<List<ExpressionSegment>> getAllValueExpressionsFromValues(final Collection<InsertValuesSegment> values) {
        List<List<ExpressionSegment>> result = new ArrayList<>(values.size());
        for (InsertValuesSegment each : values) {
            result.add(each.getValues());
        }
        return result;
    }
    
    @Override
    public InsertStatement getSqlStatement() {
        return (InsertStatement) super.getSqlStatement();
    }
    
    @Override
    public void setUpParameters(final List<Object> params) {
        AtomicInteger parametersOffset = new AtomicInteger(0);
        insertValueContexts = getInsertValueContexts(params, parametersOffset, valueExpressions);
        insertSelectContext = getInsertSelectContext(metaData, params, parametersOffset, currentDatabaseName).orElse(null);
        onDuplicateKeyUpdateValueContext = getOnDuplicateKeyUpdateValueContext(params, parametersOffset).orElse(null);
        ShardingSphereSchema schema = getSchema(metaData, currentDatabaseName);
        generatedKeyContext = new GeneratedKeyContextEngine(getSqlStatement(), schema).createGenerateKeyContext(insertColumnNamesAndIndexes, insertValueContexts, params).orElse(null);
    }
    
    @Override
    public Collection<WhereSegment> getWhereSegments() {
        return null == insertSelectContext ? Collections.emptyList() : insertSelectContext.getSelectStatementContext().getWhereSegments();
    }
    
    @Override
    public Collection<ColumnSegment> getColumnSegments() {
        return null == insertSelectContext ? Collections.emptyList() : insertSelectContext.getSelectStatementContext().getColumnSegments();
    }
    
    @Override
    public Collection<BinaryOperationExpression> getJoinConditions() {
        return null == insertSelectContext ? Collections.emptyList() : insertSelectContext.getSelectStatementContext().getJoinConditions();
    }
    
    @Override
    public Optional<WithSegment> getWith() {
        return getSqlStatement().getWithSegment();
    }
}
