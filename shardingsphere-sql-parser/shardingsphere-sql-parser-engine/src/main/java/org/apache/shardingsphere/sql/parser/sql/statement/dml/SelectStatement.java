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

package org.apache.shardingsphere.sql.parser.sql.statement.dml;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.sql.predicate.PredicateExtractor;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.LockSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.OwnerAvailable;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.generic.TableSegmentsAvailable;
import org.apache.shardingsphere.sql.parser.sql.statement.generic.WhereSegmentAvailable;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Select statement.
 */
@Getter
@Setter
public final class SelectStatement extends DMLStatement implements TableSegmentsAvailable, WhereSegmentAvailable {
    
    private final Collection<TableSegment> tables = new LinkedList<>();
    
    private ProjectionsSegment projections;
    
    private WhereSegment where;
    
    private GroupBySegment groupBy;
    
    private OrderBySegment orderBy;
    
    private LimitSegment limit;
    
    private SelectStatement parentStatement;
    
    private LockSegment lock;
    
    /**
     * Get group by segment.
     * 
     * @return group by segment
     */
    public Optional<GroupBySegment> getGroupBy() {
        return Optional.fromNullable(groupBy);
    }
    
    /**
     * Get order by segment.
     *
     * @return order by segment
     */
    public Optional<OrderBySegment> getOrderBy() {
        return Optional.fromNullable(orderBy);
    }
    
    /**
     * Get order by segment.
     *
     * @return order by segment
     */
    public Optional<LimitSegment> getLimit() {
        return Optional.fromNullable(limit);
    }
    
    /**
     * Get lock segment.
     *
     * @return lock segment
     */
    public Optional<LockSegment> getLock() {
        return Optional.fromNullable(lock);
    }
    
    @Override
    public Optional<WhereSegment> getWhere() {
        return Optional.fromNullable(where);
    }
    
    @Override
    public Collection<TableSegment> getAllTables() {
        Collection<TableSegment> result = new LinkedList<>(tables);
        if (null != where) {
            result.addAll(getAllTablesFromWhere());
        }
        result.addAll(getAllTablesFromProjections());
        if (null != groupBy) {
            result.addAll(getAllTablesFromOrderByItems(groupBy.getGroupByItems()));
        }
        if (null != orderBy) {
            result.addAll(getAllTablesFromOrderByItems(orderBy.getOrderByItems()));
        }
        return result;
    }
    
    private Collection<TableSegment> getAllTablesFromWhere() {
        Collection<TableSegment> result = new LinkedList<>();
        for (AndPredicate each : where.getAndPredicates()) {
            for (PredicateSegment predicate : each.getPredicates()) {
                result.addAll(new PredicateExtractor(tables, predicate).extractTables());
            }
        }
        return result;
    }
    
    private Collection<TableSegment> getAllTablesFromProjections() {
        Collection<TableSegment> result = new LinkedList<>();
        for (ProjectionSegment each : projections.getProjections()) {
            if (each instanceof OwnerAvailable) {
                Optional<OwnerSegment> owner = ((OwnerAvailable) each).getOwner();
                if (owner.isPresent() && isTable(owner.get())) {
                    result.add(new TableSegment(owner.get().getStartIndex(), owner.get().getStopIndex(), owner.get().getIdentifier()));
                }
            }
        }
        return result;
    }
    
    private Collection<TableSegment> getAllTablesFromOrderByItems(final Collection<OrderByItemSegment> orderByItems) {
        Collection<TableSegment> result = new LinkedList<>();
        for (OrderByItemSegment each : orderByItems) {
            if (each instanceof ColumnOrderByItemSegment) {
                Optional<OwnerSegment> owner = ((ColumnOrderByItemSegment) each).getColumn().getOwner();
                if (owner.isPresent() && isTable(owner.get())) {
                    Preconditions.checkState(((ColumnOrderByItemSegment) each).getColumn().getOwner().isPresent());
                    OwnerSegment segment = ((ColumnOrderByItemSegment) each).getColumn().getOwner().get();
                    result.add(new TableSegment(segment.getStartIndex(), segment.getStopIndex(), segment.getIdentifier()));
                }
            }
        }
        return result;
    }
    
    private boolean isTable(final OwnerSegment owner) {
        for (TableSegment each : tables) {
            if (owner.getIdentifier().getValue().equals(each.getAlias().orNull())) {
                return false;
            }
        }
        return true;
    }
}
