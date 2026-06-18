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

import lombok.Builder;
import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.SubqueryType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.hint.WithTableHintSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.outfile.OutfileSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.HavingSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.HierarchicalQuerySegment;
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
public final class SelectStatement extends DMLStatement {
    
    private final ProjectionsSegment projections;
    
    private final TableSegment from;
    
    private final WhereSegment where;
    
    private final HierarchicalQuerySegment hierarchicalQuery;
    
    private final GroupBySegment groupBy;
    
    private final HavingSegment having;
    
    private final OrderBySegment orderBy;
    
    private final CombineSegment combine;
    
    private final WithSegment with;
    
    private final SubqueryType subqueryType;
    
    private final LimitSegment limit;
    
    private final LockSegment lock;
    
    private final WindowSegment window;
    
    private final TableSegment into;
    
    private final ModelSegment model;
    
    private final OutfileSegment outfile;
    
    private final WithTableHintSegment withTableHint;
    
    private final SQLStatementAttributes attributes;
    
    @Builder
    private SelectStatement(final DatabaseType databaseType, final ProjectionsSegment projections, final TableSegment from, final WhereSegment where,
                            final HierarchicalQuerySegment hierarchicalQuery, final GroupBySegment groupBy, final HavingSegment having, final OrderBySegment orderBy,
                            final CombineSegment combine, final WithSegment with, final SubqueryType subqueryType, final LimitSegment limit, final LockSegment lock,
                            final WindowSegment window, final TableSegment into, final ModelSegment model, final OutfileSegment outfile,
                            final WithTableHintSegment withTableHint) {
        super(databaseType);
        this.projections = projections;
        this.from = from;
        this.where = where;
        this.hierarchicalQuery = hierarchicalQuery;
        this.groupBy = groupBy;
        this.having = having;
        this.orderBy = orderBy;
        this.combine = combine;
        this.with = with;
        this.subqueryType = subqueryType;
        this.limit = limit;
        this.lock = lock;
        this.window = window;
        this.into = into;
        this.model = model;
        this.outfile = outfile;
        this.withTableHint = withTableHint;
        attributes = new SQLStatementAttributes(new WithSQLStatementAttribute(with), new AllowNotUseDatabaseSQLStatementAttribute(null == from));
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
     * Get hierarchical query.
     *
     * @return hierarchical query segment
     */
    public Optional<HierarchicalQuerySegment> getHierarchicalQuery() {
        return Optional.ofNullable(hierarchicalQuery);
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
     * Get select statement with subquery type.
     *
     * @param subqueryType subquery type
     * @return select statement
     */
    public SelectStatement withSubqueryType(final SubqueryType subqueryType) {
        return builder().databaseType(getDatabaseType()).projections(projections).from(from).where(where).hierarchicalQuery(hierarchicalQuery)
                .groupBy(groupBy).having(having).orderBy(orderBy).combine(combine).with(with).subqueryType(subqueryType).limit(limit).lock(lock).window(window)
                .into(into).model(model).outfile(outfile).withTableHint(withTableHint).build();
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
     * Get outfile.
     *
     * @return outfile segment
     */
    public Optional<OutfileSegment> getOutfile() {
        return Optional.ofNullable(outfile);
    }
    
    /**
     * Get with table hint.
     *
     * @return with table hint.
     */
    public Optional<WithTableHintSegment> getWithTableHint() {
        return Optional.ofNullable(withTableHint);
    }
}
