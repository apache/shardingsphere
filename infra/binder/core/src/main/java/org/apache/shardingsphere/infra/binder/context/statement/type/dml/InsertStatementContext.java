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

package org.apache.shardingsphere.infra.binder.context.statement.type.dml;

import lombok.Getter;
import org.apache.shardingsphere.infra.binder.context.available.WhereContextAvailable;
import org.apache.shardingsphere.infra.binder.context.aware.ParameterAware;
import org.apache.shardingsphere.infra.binder.context.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.context.segment.insert.values.InsertSelectContext;
import org.apache.shardingsphere.infra.binder.context.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.context.segment.insert.values.OnDuplicateUpdateContext;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Insert SQL statement context.
 */
public final class InsertStatementContext implements SQLStatementContext, ParameterAware, WhereContextAvailable {
    
    @Getter
    private final InsertStatementBaseContext baseContext;
    
    private InsertStatementBindingContext bindingContext;
    
    public InsertStatementContext(final InsertStatement sqlStatement, final ShardingSphereMetaData metaData, final String currentDatabaseName) {
        baseContext = new InsertStatementBaseContext(sqlStatement, metaData, currentDatabaseName);
        bindingContext = new InsertStatementBindingContext(baseContext, Collections.emptyList(), metaData, currentDatabaseName);
    }
    
    /**
     * Get column names for descending order.
     *
     * @return column names for descending order
     */
    public Iterator<String> getDescendingColumnNames() {
        return baseContext.getDescendingColumnNames();
    }
    
    /**
     * Get grouped parameters.
     *
     * @return grouped parameters
     */
    public List<List<Object>> getGroupedParameters() {
        return bindingContext.getGroupedParameters();
    }
    
    /**
     * Get on duplicate key update parameters.
     *
     * @return on duplicate key update parameters
     */
    public List<Object> getOnDuplicateKeyUpdateParameters() {
        return bindingContext.getOnDuplicateKeyUpdateParameters();
    }
    
    /**
     * Get generated key context.
     *
     * @return generated key context
     */
    public Optional<GeneratedKeyContext> getGeneratedKeyContext() {
        return bindingContext.getGeneratedKeyContext();
    }
    
    /**
     * Judge whether contains insert columns.
     *
     * @return contains insert columns or not
     */
    public boolean containsInsertColumns() {
        return baseContext.containsInsertColumns();
    }
    
    /**
     * Get value list count.
     *
     * @return value list count
     */
    public int getValueListCount() {
        return baseContext.getValueListCount();
    }
    
    /**
     * Get insert column names.
     *
     * @return column names collection
     */
    public List<String> getInsertColumnNames() {
        return baseContext.getInsertColumnNames();
    }
    
    @Override
    public InsertStatement getSqlStatement() {
        return baseContext.getSqlStatement();
    }
    
    @Override
    public TablesContext getTablesContext() {
        return baseContext.getTablesContext();
    }
    
    @Override
    public void bindParameters(final List<Object> params) {
        if (!params.isEmpty()) {
            bindingContext = new InsertStatementBindingContext(baseContext, params, baseContext.getMetaData(), baseContext.getCurrentDatabaseName());
        }
    }
    
    @Override
    public Collection<WhereSegment> getWhereSegments() {
        return null == bindingContext.getInsertSelectContext() ? Collections.emptyList() : bindingContext.getInsertSelectContext().getSelectStatementContext().getWhereSegments();
    }
    
    @Override
    public Collection<ColumnSegment> getColumnSegments() {
        return null == bindingContext.getInsertSelectContext() ? Collections.emptyList() : bindingContext.getInsertSelectContext().getSelectStatementContext().getColumnSegments();
    }
    
    @Override
    public Collection<BinaryOperationExpression> getJoinConditions() {
        return null == bindingContext.getInsertSelectContext() ? Collections.emptyList() : bindingContext.getInsertSelectContext().getSelectStatementContext().getJoinConditions();
    }
    
    /**
     * Get insert select context.
     *
     * @return insert select context
     */
    public InsertSelectContext getInsertSelectContext() {
        return bindingContext.getInsertSelectContext();
    }
    
    /**
     * Get insert value contexts.
     *
     * @return insert value contexts
     */
    public List<InsertValueContext> getInsertValueContexts() {
        return bindingContext.getInsertValueContexts();
    }
    
    /**
     * Get column names.
     *
     * @return column names
     */
    public List<String> getColumnNames() {
        return baseContext.getColumnNames();
    }
    
    /**
     * Get on duplicate key update value context.
     *
     * @return on duplicate key update value context
     */
    public OnDuplicateUpdateContext getOnDuplicateKeyUpdateValueContext() {
        return bindingContext.getOnDuplicateKeyUpdateValueContext();
    }
}
