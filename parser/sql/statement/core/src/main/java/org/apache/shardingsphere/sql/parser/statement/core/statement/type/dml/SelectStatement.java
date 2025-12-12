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

package org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.SubqueryType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.hint.WithTableHintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.HavingSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.LockSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.ModelSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WindowSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.AllowNotUseDatabaseSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.WithSQLStatementAttribute;

import java.util.Optional;

/**
 * Select statement.
 */
@Getter
@Setter
public final class SelectStatement extends DMLStatement {
    
    private ProjectionsSegment projections;
    
    private TableSegment from;
    
    private WhereSegment where;
    
    private GroupBySegment groupBy;
    
    private HavingSegment having;
    
    private OrderBySegment orderBy;
    
    private CombineSegment combine;
    
    private WithSegment with;
    
    private SubqueryType subqueryType;
    
    private LimitSegment limit;
    
    private LockSegment lock;
    
    private WindowSegment window;
    
    private TableSegment into;
    
    private ModelSegment model;
    
    private WithTableHintSegment withTableHint;
    
    public SelectStatement(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    /**
     * Get from.
     *
     * @return from table segment
     */
    public Optional<TableSegment> getFrom() {
        return Optional.ofNullable(from);
    }
    
    /**
     * Get where.
     *
     * @return where segment
     */
    public Optional<WhereSegment> getWhere() {
        return Optional.ofNullable(where);
    }
    
    /**
     * Get group by segment.
     *
     * @return group by segment
     */
    public Optional<GroupBySegment> getGroupBy() {
        return Optional.ofNullable(groupBy);
    }
    
    /**
     * Get having segment.
     *
     * @return having segment
     */
    public Optional<HavingSegment> getHaving() {
        return Optional.ofNullable(having);
    }
    
    /**
     * Get order by segment.
     *
     * @return order by segment
     */
    public Optional<OrderBySegment> getOrderBy() {
        return Optional.ofNullable(orderBy);
    }
    
    /**
     * Get combine.
     *
     * @return combine
     */
    public Optional<CombineSegment> getCombine() {
        return Optional.ofNullable(combine);
    }
    
    /**
     * Get with.
     *
     * @return with
     */
    public Optional<WithSegment> getWith() {
        return Optional.ofNullable(with);
    }
    
    /**
     * Get subquery type.
     *
     * @return subquery type
     */
    public Optional<SubqueryType> getSubqueryType() {
        return Optional.ofNullable(subqueryType);
    }
    
    /**
     * Get limit.
     *
     * @return limit
     */
    public Optional<LimitSegment> getLimit() {
        return Optional.ofNullable(limit);
    }
    
    /**
     * Get lock.
     *
     * @return lock
     */
    public Optional<LockSegment> getLock() {
        return Optional.ofNullable(lock);
    }
    
    /**
     * Get window.
     *
     * @return window
     */
    public Optional<WindowSegment> getWindow() {
        return Optional.ofNullable(window);
    }
    
    /**
     * Get into.
     *
     * @return into table
     */
    public Optional<TableSegment> getInto() {
        return Optional.ofNullable(into);
    }
    
    /**
     * Get model.
     *
     * @return model
     */
    public Optional<ModelSegment> getModel() {
        return Optional.ofNullable(model);
    }
    
    /**
     * Get with table hint.
     *
     * @return with table hint.
     */
    public Optional<WithTableHintSegment> getWithTableHint() {
        return Optional.ofNullable(withTableHint);
    }
    
    @Override
    public SQLStatementAttributes getAttributes() {
        return new SQLStatementAttributes(new WithSQLStatementAttribute(with), new AllowNotUseDatabaseSQLStatementAttribute(null == from));
    }
}
