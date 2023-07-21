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

package org.apache.shardingsphere.infra.binder.segment.select.projection.engine;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.segment.select.projection.DerivedColumn;
import org.apache.shardingsphere.infra.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.AggregationDistinctProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.DerivedProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ExpressionProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ParameterMarkerProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ShorthandProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.SubqueryProjection;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.exception.SchemaNotFoundException;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sql.parser.sql.common.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.sql.common.enums.JoinType;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Projection engine.
 */
@RequiredArgsConstructor
public final class ProjectionEngine {
    
    private final String databaseName;
    
    private final Map<String, ShardingSphereSchema> schemas;
    
    private final DatabaseType databaseType;
    
    private int aggregationAverageDerivedColumnCount;
    
    private int aggregationDistinctDerivedColumnCount;
    
    /**
     * Create projection.
     *
     * @param table table segment
     * @param projectionSegment projection segment
     * @return projection
     */
    public Optional<Projection> createProjection(final TableSegment table, final ProjectionSegment projectionSegment) {
        if (projectionSegment instanceof ShorthandProjectionSegment) {
            return Optional.of(createProjection(table, (ShorthandProjectionSegment) projectionSegment));
        }
        if (projectionSegment instanceof ColumnProjectionSegment) {
            return Optional.of(createProjection((ColumnProjectionSegment) projectionSegment));
        }
        if (projectionSegment instanceof ExpressionProjectionSegment) {
            return Optional.of(createProjection((ExpressionProjectionSegment) projectionSegment));
        }
        if (projectionSegment instanceof AggregationDistinctProjectionSegment) {
            return Optional.of(createProjection((AggregationDistinctProjectionSegment) projectionSegment));
        }
        if (projectionSegment instanceof AggregationProjectionSegment) {
            return Optional.of(createProjection((AggregationProjectionSegment) projectionSegment));
        }
        if (projectionSegment instanceof SubqueryProjectionSegment) {
            return Optional.of(createProjection(table, (SubqueryProjectionSegment) projectionSegment));
        }
        if (projectionSegment instanceof ParameterMarkerExpressionSegment) {
            return Optional.of(createProjection((ParameterMarkerExpressionSegment) projectionSegment));
        }
        return Optional.empty();
    }
    
    private ParameterMarkerProjection createProjection(final ParameterMarkerExpressionSegment projectionSegment) {
        return new ParameterMarkerProjection(projectionSegment.getParameterMarkerIndex(), projectionSegment.getParameterMarkerType(), projectionSegment.getAlias().orElse(null));
    }
    
    private SubqueryProjection createProjection(final TableSegment table, final SubqueryProjectionSegment projectionSegment) {
        Projection subqueryProjection = createProjection(table, projectionSegment.getSubquery().getSelect().getProjections().getProjections().iterator().next())
                .orElseThrow(() -> new IllegalArgumentException("Subquery projection must have at least one projection column."));
        return new SubqueryProjection(projectionSegment.getText(), subqueryProjection, projectionSegment.getAlias().orElse(null), databaseType);
    }
    
    private ShorthandProjection createProjection(final TableSegment table, final ShorthandProjectionSegment projectionSegment) {
        IdentifierValue owner = projectionSegment.getOwner().map(OwnerSegment::getIdentifier).orElse(null);
        Collection<Projection> projections = new LinkedHashSet<>();
        projections.addAll(getShorthandColumnsFromSimpleTableSegment(table, owner));
        projections.addAll(getShorthandColumnsFromSubqueryTableSegment(table, owner));
        projections.addAll(getShorthandColumnsFromJoinTableSegment(table, owner, projectionSegment));
        return new ShorthandProjection(owner, projections);
    }
    
    private ColumnProjection createProjection(final ColumnProjectionSegment projectionSegment) {
        IdentifierValue owner = projectionSegment.getColumn().getOwner().isPresent() ? projectionSegment.getColumn().getOwner().get().getIdentifier() : null;
        return new ColumnProjection(owner, projectionSegment.getColumn().getIdentifier(), projectionSegment.getAliasName().isPresent() ? projectionSegment.getAlias().orElse(null) : null);
    }
    
    private ExpressionProjection createProjection(final ExpressionProjectionSegment projectionSegment) {
        return new ExpressionProjection(projectionSegment.getText(), projectionSegment.getAlias().orElse(null));
    }
    
    private AggregationDistinctProjection createProjection(final AggregationDistinctProjectionSegment projectionSegment) {
        String innerExpression = projectionSegment.getInnerExpression();
        IdentifierValue alias =
                projectionSegment.getAlias().orElseGet(() -> new IdentifierValue(DerivedColumn.AGGREGATION_DISTINCT_DERIVED.getDerivedColumnAlias(aggregationDistinctDerivedColumnCount++)));
        AggregationDistinctProjection result = new AggregationDistinctProjection(
                projectionSegment.getStartIndex(), projectionSegment.getStopIndex(), projectionSegment.getType(), innerExpression, alias, projectionSegment.getDistinctExpression(), databaseType);
        if (AggregationType.AVG == result.getType()) {
            appendAverageDistinctDerivedProjection(result);
        }
        return result;
    }
    
    private AggregationProjection createProjection(final AggregationProjectionSegment projectionSegment) {
        String innerExpression = projectionSegment.getInnerExpression();
        AggregationProjection result = new AggregationProjection(projectionSegment.getType(), innerExpression, projectionSegment.getAlias().orElse(null), databaseType);
        if (AggregationType.AVG == result.getType()) {
            appendAverageDerivedProjection(result);
            // TODO replace avg to constant, avoid calculate useless avg
        }
        return result;
    }
    
    private Collection<ColumnProjection> getShorthandColumnsFromSimpleTableSegment(final TableSegment table, final IdentifierValue owner) {
        if (!(table instanceof SimpleTableSegment)) {
            return Collections.emptyList();
        }
        String tableName = ((SimpleTableSegment) table).getTableName().getIdentifier().getValue();
        String tableAlias = table.getAliasName().orElse(tableName);
        String schemaName = ((SimpleTableSegment) table).getOwner().map(optional -> optional.getIdentifier().getValue())
                .orElseGet(() -> DatabaseTypeEngine.getDefaultSchemaName(databaseType, databaseName)).toLowerCase();
        ShardingSphereSchema schema = schemas.get(schemaName);
        ShardingSpherePreconditions.checkNotNull(schema, () -> new SchemaNotFoundException(schemaName));
        Collection<ColumnProjection> result = new LinkedList<>();
        if (null == owner) {
            schema.getVisibleColumnNames(tableName).stream().map(each -> new ColumnProjection(table.getAlias()
                    .orElse(((SimpleTableSegment) table).getTableName().getIdentifier()), new IdentifierValue(each, databaseType.getQuoteCharacter()), null)).forEach(result::add);
        } else if (owner.getValue().equalsIgnoreCase(tableAlias)) {
            schema.getVisibleColumnNames(tableName).stream().map(each -> new ColumnProjection(owner, new IdentifierValue(each, databaseType.getQuoteCharacter()), null)).forEach(result::add);
        }
        return result;
    }
    
    private Collection<Projection> getShorthandColumnsFromSubqueryTableSegment(final TableSegment table, final IdentifierValue owner) {
        if (!(table instanceof SubqueryTableSegment) || isOwnerNotSameWithTableAlias(owner, table)) {
            return Collections.emptyList();
        }
        SelectStatement subSelectStatement = ((SubqueryTableSegment) table).getSubquery().getSelect();
        Collection<Projection> projections = subSelectStatement.getProjections().getProjections().stream().map(each -> createProjection(subSelectStatement.getFrom(), each).orElse(null))
                .filter(Objects::nonNull).collect(Collectors.toList());
        IdentifierValue subqueryTableAlias = table.getAlias().orElse(null);
        return getSubqueryTableActualProjections(projections, subqueryTableAlias);
    }
    
    private boolean isOwnerNotSameWithTableAlias(final IdentifierValue owner, final TableSegment table) {
        return null != owner && table.getAliasName().isPresent() && !table.getAliasName().get().equals(owner.getValue());
    }
    
    private Collection<Projection> getSubqueryTableActualProjections(final Collection<Projection> projections, final IdentifierValue subqueryTableAlias) {
        Collection<Projection> result = new LinkedList<>();
        for (Projection each : projections) {
            if (each instanceof ShorthandProjection) {
                result.addAll(getSubqueryTableActualProjections(((ShorthandProjection) each).getActualColumns(), subqueryTableAlias));
            } else if (!(each instanceof DerivedProjection)) {
                IdentifierValue originalOwner = each instanceof ColumnProjection ? ((ColumnProjection) each).getOriginalOwner() : null;
                IdentifierValue originalName = each instanceof ColumnProjection ? ((ColumnProjection) each).getOriginalName() : null;
                result.add(each.transformSubqueryProjection(subqueryTableAlias, originalOwner, originalName));
            }
        }
        return result;
    }
    
    private Collection<Projection> getShorthandColumnsFromJoinTableSegment(final TableSegment table, final IdentifierValue owner, final ProjectionSegment projectionSegment) {
        if (!(table instanceof JoinTableSegment)) {
            return Collections.emptyList();
        }
        JoinTableSegment joinTable = (JoinTableSegment) table;
        Collection<Projection> result = new LinkedList<>();
        Collection<Projection> remainingProjections = new LinkedList<>();
        for (Projection each : getOriginalProjections(joinTable, projectionSegment)) {
            Collection<Projection> actualProjections = getActualProjections(Collections.singletonList(each));
            if (joinTable.getUsing().isEmpty() && !joinTable.isNatural() || null != owner && each.getColumnName().contains(owner.getValue())) {
                result.addAll(actualProjections);
            } else {
                remainingProjections.addAll(actualProjections);
            }
        }
        result.addAll(getUsingActualProjections(remainingProjections, joinTable.getUsing(), joinTable.isNatural()));
        return result;
    }
    
    private Collection<Projection> getOriginalProjections(final JoinTableSegment joinTable, final ProjectionSegment projectionSegment) {
        Collection<Projection> result = new LinkedList<>();
        if (databaseType instanceof MySQLDatabaseType && (!joinTable.getUsing().isEmpty() || joinTable.isNatural()) && JoinType.RIGHT.name().equalsIgnoreCase(joinTable.getJoinType())) {
            createProjection(joinTable.getRight(), projectionSegment).ifPresent(result::add);
            createProjection(joinTable.getLeft(), projectionSegment).ifPresent(result::add);
            return result;
        }
        createProjection(joinTable.getLeft(), projectionSegment).ifPresent(result::add);
        createProjection(joinTable.getRight(), projectionSegment).ifPresent(result::add);
        return result;
    }
    
    private Collection<Projection> getActualProjections(final Collection<Projection> projections) {
        Collection<Projection> result = new LinkedList<>();
        for (Projection each : projections) {
            if (each instanceof ShorthandProjection) {
                result.addAll(((ShorthandProjection) each).getActualColumns());
            } else if (!(each instanceof DerivedProjection)) {
                result.add(each);
            }
        }
        return result;
    }
    
    private Collection<Projection> getUsingActualProjections(final Collection<Projection> actualProjections, final Collection<ColumnSegment> usingColumns, final boolean natural) {
        if (usingColumns.isEmpty() && !natural) {
            return Collections.emptyList();
        }
        Collection<String> usingColumnNames = usingColumns.isEmpty() ? getUsingColumnNamesByNaturalJoin(actualProjections) : getUsingColumnNames(usingColumns);
        Collection<Projection> result = new LinkedList<>();
        if (databaseType instanceof MySQLDatabaseType) {
            result.addAll(getJoinUsingColumnsByOriginalColumnSequence(actualProjections, usingColumnNames));
        } else {
            result.addAll(getJoinUsingColumnsByUsingColumnSequence(actualProjections, usingColumnNames));
        }
        result.addAll(getRemainingColumns(actualProjections, usingColumnNames));
        return result;
    }
    
    private Collection<String> getUsingColumnNamesByNaturalJoin(final Collection<Projection> actualProjections) {
        Collection<String> result = new LinkedHashSet<>();
        Map<String, Projection> uniqueProjections = new LinkedHashMap<>(actualProjections.size(), 1F);
        for (Projection each : actualProjections) {
            Projection previousProjection = uniqueProjections.put(each.getColumnLabel().toLowerCase(), each);
            if (null != previousProjection) {
                result.add(previousProjection.getColumnLabel().toLowerCase());
            }
        }
        return result;
    }
    
    private Collection<String> getUsingColumnNames(final Collection<ColumnSegment> usingColumns) {
        Collection<String> result = new LinkedHashSet<>();
        for (ColumnSegment each : usingColumns) {
            result.add(each.getIdentifier().getValue().toLowerCase());
        }
        return result;
    }
    
    private Collection<Projection> getJoinUsingColumnsByOriginalColumnSequence(final Collection<Projection> actualProjections, final Collection<String> usingColumnNames) {
        Collection<Projection> result = new LinkedList<>();
        for (Projection each : actualProjections) {
            if (result.size() == usingColumnNames.size()) {
                return result;
            }
            if (usingColumnNames.contains(each.getColumnLabel().toLowerCase())) {
                result.add(each);
            }
        }
        return result;
    }
    
    private Collection<Projection> getJoinUsingColumnsByUsingColumnSequence(final Collection<Projection> actualProjections, final Collection<String> usingColumnNames) {
        Collection<Projection> result = new LinkedList<>();
        for (String each : usingColumnNames) {
            for (Projection projection : actualProjections) {
                if (each.equalsIgnoreCase(projection.getColumnLabel())) {
                    result.add(projection);
                    break;
                }
            }
        }
        return result;
    }
    
    private Collection<Projection> getRemainingColumns(final Collection<Projection> actualProjections, final Collection<String> usingColumnNames) {
        Collection<Projection> result = new LinkedList<>();
        for (Projection each : actualProjections) {
            if (usingColumnNames.contains(each.getColumnLabel().toLowerCase())) {
                continue;
            }
            result.add(each);
        }
        return result;
    }
    
    private void appendAverageDistinctDerivedProjection(final AggregationDistinctProjection averageDistinctProjection) {
        String innerExpression = averageDistinctProjection.getInnerExpression();
        String distinctInnerExpression = averageDistinctProjection.getDistinctInnerExpression();
        String countAlias = DerivedColumn.AVG_COUNT_ALIAS.getDerivedColumnAlias(aggregationAverageDerivedColumnCount);
        AggregationDistinctProjection countDistinctProjection = new AggregationDistinctProjection(
                0, 0, AggregationType.COUNT, innerExpression, new IdentifierValue(countAlias), distinctInnerExpression, databaseType);
        String sumAlias = DerivedColumn.AVG_SUM_ALIAS.getDerivedColumnAlias(aggregationAverageDerivedColumnCount);
        AggregationDistinctProjection sumDistinctProjection = new AggregationDistinctProjection(
                0, 0, AggregationType.SUM, innerExpression, new IdentifierValue(sumAlias), distinctInnerExpression, databaseType);
        averageDistinctProjection.getDerivedAggregationProjections().add(countDistinctProjection);
        averageDistinctProjection.getDerivedAggregationProjections().add(sumDistinctProjection);
        aggregationAverageDerivedColumnCount++;
    }
    
    private void appendAverageDerivedProjection(final AggregationProjection averageProjection) {
        String innerExpression = averageProjection.getInnerExpression();
        String countAlias = DerivedColumn.AVG_COUNT_ALIAS.getDerivedColumnAlias(aggregationAverageDerivedColumnCount);
        AggregationProjection countProjection = new AggregationProjection(AggregationType.COUNT, innerExpression, new IdentifierValue(countAlias), databaseType);
        String sumAlias = DerivedColumn.AVG_SUM_ALIAS.getDerivedColumnAlias(aggregationAverageDerivedColumnCount);
        AggregationProjection sumProjection = new AggregationProjection(AggregationType.SUM, innerExpression, new IdentifierValue(sumAlias), databaseType);
        averageProjection.getDerivedAggregationProjections().add(countProjection);
        averageProjection.getDerivedAggregationProjections().add(sumProjection);
        aggregationAverageDerivedColumnCount++;
    }
}
