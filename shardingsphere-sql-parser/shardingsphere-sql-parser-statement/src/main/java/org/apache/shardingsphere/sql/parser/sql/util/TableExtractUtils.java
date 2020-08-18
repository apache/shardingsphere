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

package org.apache.shardingsphere.sql.parser.sql.util;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.JoinSpecificationSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.JoinedTableSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.TableFactorSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.TableReferenceSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateCompareRightValue;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.OwnerAvailable;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

public final class TableExtractUtils {
    /**
     * Get table that should be rewrited from SelectStatement.
     *
     * @param selectStatement SelectStatement.
     * @return SimpleTableSegment collection
     */
    public static Collection<SimpleTableSegment> getTableFromSelect(final SelectStatement selectStatement) {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        Collection<TableSegment> realTables = new LinkedList<>();
        Collection<TableSegment> allTables = new LinkedList<>();
        for (TableReferenceSegment each : selectStatement.getTableReferences()) {
            allTables.addAll(getTablesFromTableReference(each));
            realTables.addAll(getRealTablesFromTableReference(each));
        }
        if (selectStatement.getWhere().isPresent()) {
            allTables.addAll(getAllTablesFromWhere(selectStatement.getWhere().get(), realTables));
        }
        result.addAll(getAllTablesFromProjections(selectStatement.getProjections(), realTables));
        if (selectStatement.getGroupBy().isPresent()) {
            result.addAll(getAllTablesFromOrderByItems(selectStatement.getGroupBy().get().getGroupByItems(), realTables));
        }
        if (selectStatement.getOrderBy().isPresent()) {
            result.addAll(getAllTablesFromOrderByItems(selectStatement.getOrderBy().get().getOrderByItems(), realTables));
        }
        for (TableSegment each : allTables) {
            if (each instanceof SubqueryTableSegment) {
                result.addAll(getTableFromSelect(((SubqueryTableSegment) each).getSubquery().getSelect()));
            } else {
                result.add((SimpleTableSegment) each);
            }
        }
        return result;
    }
    
    /**
     * Get real table that should be rewrited from SelectStatement.
     *
     * @param selectStatement SelectStatement.
     * @return SimpleTableSegment collection
     */
    public static Collection<SimpleTableSegment> getSimpleTableFromSelect(final SelectStatement selectStatement) {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        Collection<TableSegment> realTables = new LinkedList<>();
        for (TableReferenceSegment each : selectStatement.getTableReferences()) {
            realTables.addAll(getRealTablesFromTableReference(each));
        }
        for (TableSegment each : realTables) {
            if (each instanceof SubqueryTableSegment) {
                result.addAll(getSimpleTableFromSelect(((SubqueryTableSegment) each).getSubquery().getSelect()));
            } else {
                result.add((SimpleTableSegment) each);
            }
        }
        return result;
    }
    
    private static Collection<TableSegment> getTablesFromTableReference(final TableReferenceSegment tableReferenceSegment) {
        Collection<TableSegment> result = new LinkedList<>();
        if (null != tableReferenceSegment.getTableFactor()) {
            result.addAll(getTablesFromTableFactor(tableReferenceSegment.getTableFactor()));
        }
        if (null != tableReferenceSegment.getJoinedTables()) {
            for (JoinedTableSegment each : tableReferenceSegment.getJoinedTables()) {
                result.addAll(getTablesFromJoinTable(each, result));
            }
        }
        return result;
    }
    
    private static Collection<TableSegment> getRealTablesFromTableReference(final TableReferenceSegment tableReferenceSegment) {
        Collection<TableSegment> result = new LinkedList<>();
        if (null != tableReferenceSegment.getTableFactor()) {
            result.addAll(getRealTablesFromTableFactor(tableReferenceSegment.getTableFactor()));
        }
        if (null != tableReferenceSegment.getJoinedTables()) {
            for (JoinedTableSegment each : tableReferenceSegment.getJoinedTables()) {
                result.addAll(getRealTablesFromJoinTable(each));
            }
        }
        return result;
    }
    
    private static Collection<TableSegment> getTablesFromTableFactor(final TableFactorSegment tableFactorSegment) {
        Collection<TableSegment> result = new LinkedList<>();
        if (null != tableFactorSegment.getTable() && tableFactorSegment.getTable() instanceof SimpleTableSegment) {
            result.add(tableFactorSegment.getTable());
        }
        if (null != tableFactorSegment.getTable() && tableFactorSegment.getTable() instanceof SubqueryTableSegment) {
            result.add(tableFactorSegment.getTable());
        }
        if (null != tableFactorSegment.getTableReferences() && !tableFactorSegment.getTableReferences().isEmpty()) {
            for (TableReferenceSegment each: tableFactorSegment.getTableReferences()) {
                result.addAll(getTablesFromTableReference(each));
            }
        }
        return result;
    }
    
    private static Collection<TableSegment> getRealTablesFromTableFactor(final TableFactorSegment tableFactorSegment) {
        Collection<TableSegment> result = new LinkedList<>();
        if (null != tableFactorSegment.getTable() && tableFactorSegment.getTable() instanceof SimpleTableSegment) {
            result.add(tableFactorSegment.getTable());
        }
        if (null != tableFactorSegment.getTable() && tableFactorSegment.getTable() instanceof SubqueryTableSegment) {
            result.add(tableFactorSegment.getTable());
        }
        if (null != tableFactorSegment.getTableReferences() && !tableFactorSegment.getTableReferences().isEmpty()) {
            for (TableReferenceSegment each: tableFactorSegment.getTableReferences()) {
                result.addAll(getRealTablesFromTableReference(each));
            }
        }
        return result;
    }
    
    private static Collection<TableSegment> getTablesFromJoinTable(final JoinedTableSegment joinedTableSegment, final Collection<TableSegment> tableSegments) {
        Collection<TableSegment> result = new LinkedList<>();
        Collection<TableSegment> realTables = new LinkedList<>();
        realTables.addAll(tableSegments);
        if (null != joinedTableSegment.getTableFactor()) {
            result.addAll(getTablesFromTableFactor(joinedTableSegment.getTableFactor()));
            realTables.addAll(getTablesFromTableFactor(joinedTableSegment.getTableFactor()));
        }
        if (null != joinedTableSegment.getJoinSpecification()) {
            result.addAll(getTablesFromJoinSpecification(joinedTableSegment.getJoinSpecification(), realTables));
        }
        return result;
    }
    
    private static Collection<TableSegment> getRealTablesFromJoinTable(final JoinedTableSegment joinedTableSegment) {
        Collection<TableSegment> result = new LinkedList<>();
        if (null != joinedTableSegment.getTableFactor()) {
            result.addAll(getTablesFromTableFactor(joinedTableSegment.getTableFactor()));
        }
        return result;
    }
    
    private static Collection<SimpleTableSegment> getTablesFromJoinSpecification(final JoinSpecificationSegment joinSpecificationSegment, final Collection<TableSegment> tableSegments) {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        Collection<AndPredicate> andPredicates = joinSpecificationSegment.getAndPredicates();
        for (AndPredicate each : andPredicates) {
            for (PredicateSegment e : each.getPredicates()) {
                if (null != e.getColumn() && (e.getColumn().getOwner().isPresent())) {
                    OwnerSegment ownerSegment = e.getColumn().getOwner().get();
                    if (isTable(ownerSegment, tableSegments)) {
                        result.add(new SimpleTableSegment(ownerSegment.getStartIndex(), ownerSegment.getStopIndex(), ownerSegment.getIdentifier()));
                    }
                }
                if (null != e.getRightValue() && (e.getRightValue() instanceof ColumnSegment) && ((ColumnSegment) e.getRightValue()).getOwner().isPresent()) {
                    OwnerSegment ownerSegment = ((ColumnSegment) e.getRightValue()).getOwner().get();
                    if (isTable(ownerSegment, tableSegments)) {
                        result.add(new SimpleTableSegment(ownerSegment.getStartIndex(), ownerSegment.getStopIndex(), ownerSegment.getIdentifier()));
                    }
                }
            }
        }
        return result;
    }
    
    private static Collection<SimpleTableSegment> getAllTablesFromWhere(final WhereSegment where, final Collection<TableSegment> tableSegments) {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        for (AndPredicate each : where.getAndPredicates()) {
            for (PredicateSegment predicate : each.getPredicates()) {
                result.addAll(getAllTablesFromPredicate(predicate, tableSegments));
            }
        }
        return result;
    }
    
    private static Collection<SimpleTableSegment> getAllTablesFromPredicate(final PredicateSegment predicate, final Collection<TableSegment> tableSegments) {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        if (predicate.getColumn().getOwner().isPresent() && isTable(predicate.getColumn().getOwner().get(), tableSegments)) {
            OwnerSegment segment = predicate.getColumn().getOwner().get();
            result.add(new SimpleTableSegment(segment.getStartIndex(), segment.getStopIndex(), segment.getIdentifier()));
        }
        if (predicate.getRightValue() instanceof PredicateCompareRightValue) {
            if (((PredicateCompareRightValue) predicate.getRightValue()).getExpression() instanceof SubqueryExpressionSegment) {
                result.addAll(TableExtractUtils.getTableFromSelect(((SubqueryExpressionSegment) ((PredicateCompareRightValue) predicate.getRightValue()).getExpression()).getSubquery().getSelect()));
            }
        } else {
            if (predicate.getRightValue() instanceof ColumnSegment) {
                Preconditions.checkState(((ColumnSegment) predicate.getRightValue()).getOwner().isPresent());
                OwnerSegment segment = ((ColumnSegment) predicate.getRightValue()).getOwner().get();
                result.add(new SimpleTableSegment(segment.getStartIndex(), segment.getStopIndex(), segment.getIdentifier()));
            }
        }
        return result;
    }
    
    private static Collection<SimpleTableSegment> getAllTablesFromProjections(final ProjectionsSegment projections, final Collection<TableSegment> tableSegments) {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        if (null == projections || projections.getProjections().isEmpty()) {
            return result;
        }
        for (ProjectionSegment each : projections.getProjections()) {
            if (each instanceof SubqueryProjectionSegment) {
                result.addAll(getTableFromSelect(((SubqueryProjectionSegment) each).getSubquery().getSelect()));
            } else {
                Optional<SimpleTableSegment> table = getTableSegment(each, tableSegments);
                table.ifPresent(result::add);
            }
        }
        return result;
    }
    
    private static Optional<SimpleTableSegment> getTableSegment(final ProjectionSegment each, final Collection<TableSegment> tableSegments) {
        Optional<OwnerSegment> owner = getTableOwner(each);
        if (owner.isPresent() && isTable(owner.get(), tableSegments)) {
            return Optional .of(new SimpleTableSegment(owner.get().getStartIndex(), owner.get().getStopIndex(), owner.get().getIdentifier()));
        }
        return Optional.empty();
    }
    
    private static Optional<OwnerSegment> getTableOwner(final ProjectionSegment each) {
        if (each instanceof OwnerAvailable) {
            return ((OwnerAvailable) each).getOwner();
        }
        if (each instanceof ColumnProjectionSegment) {
            return ((ColumnProjectionSegment) each).getColumn().getOwner();
        }
        return Optional.empty();
    }
    
    private static Collection<SimpleTableSegment> getAllTablesFromOrderByItems(final Collection<OrderByItemSegment> orderByItems, final Collection<TableSegment> tableSegments) {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        for (OrderByItemSegment each : orderByItems) {
            if (each instanceof ColumnOrderByItemSegment) {
                Optional<OwnerSegment> owner = ((ColumnOrderByItemSegment) each).getColumn().getOwner();
                if (owner.isPresent() && isTable(owner.get(), tableSegments)) {
                    Preconditions.checkState(((ColumnOrderByItemSegment) each).getColumn().getOwner().isPresent());
                    OwnerSegment segment = ((ColumnOrderByItemSegment) each).getColumn().getOwner().get();
                    result.add(new SimpleTableSegment(segment.getStartIndex(), segment.getStopIndex(), segment.getIdentifier()));
                }
            }
        }
        return result;
    }
    
    private static boolean isTable(final OwnerSegment owner, final Collection<TableSegment> tables) {
        for (TableSegment each : tables) {
            if (owner.getIdentifier().getValue().equals(each.getAlias().orElse(null))) {
                return false;
            }
        }
        return true;
    }
}
