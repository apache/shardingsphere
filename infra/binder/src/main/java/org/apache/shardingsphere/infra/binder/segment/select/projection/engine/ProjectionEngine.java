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
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ExpressionProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ParameterMarkerProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.ShorthandProjection;
import org.apache.shardingsphere.infra.binder.segment.select.projection.impl.SubqueryProjection;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.exception.SchemaNotFoundException;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sql.parser.sql.common.constant.AggregationType;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

import java.util.Collection;
import java.util.Collections;
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
            return Optional.of(createProjection((SubqueryProjectionSegment) projectionSegment));
        }
        if (projectionSegment instanceof ParameterMarkerExpressionSegment) {
            return Optional.of(createProjection((ParameterMarkerExpressionSegment) projectionSegment));
        }
        // TODO subquery
        return Optional.empty();
    }
    
    private ParameterMarkerProjection createProjection(final ParameterMarkerExpressionSegment projectionSegment) {
        return new ParameterMarkerProjection(projectionSegment.getParameterMarkerIndex(), projectionSegment.getParameterMarkerType(), projectionSegment.getAlias().orElse(null));
    }
    
    private SubqueryProjection createProjection(final SubqueryProjectionSegment projectionSegment) {
        return new SubqueryProjection(projectionSegment.getText(), projectionSegment.getAlias().orElse(null));
    }
    
    private ShorthandProjection createProjection(final TableSegment table, final ShorthandProjectionSegment projectionSegment) {
        String owner = projectionSegment.getOwner().map(optional -> optional.getIdentifier().getValue()).orElse(null);
        Collection<Projection> projections = new LinkedHashSet<>();
        projections.addAll(getShorthandColumnsFromSimpleTableSegment(table, owner));
        projections.addAll(getShorthandColumnsFromSubqueryTableSegment(table));
        projections.addAll(getShorthandColumnsFromJoinTableSegment(table, projectionSegment));
        return new ShorthandProjection(owner, projections);
    }
    
    private ColumnProjection createProjection(final ColumnProjectionSegment projectionSegment) {
        String owner = projectionSegment.getColumn().getOwner().isPresent() ? projectionSegment.getColumn().getOwner().get().getIdentifier().getValue() : null;
        return new ColumnProjection(owner, projectionSegment.getColumn().getIdentifier().getValue(), projectionSegment.getAlias().orElse(null));
    }
    
    private ExpressionProjection createProjection(final ExpressionProjectionSegment projectionSegment) {
        return new ExpressionProjection(projectionSegment.getText(), projectionSegment.getAlias().orElse(null));
    }
    
    private AggregationDistinctProjection createProjection(final AggregationDistinctProjectionSegment projectionSegment) {
        String innerExpression = projectionSegment.getInnerExpression();
        String alias = projectionSegment.getAlias().orElseGet(() -> DerivedColumn.AGGREGATION_DISTINCT_DERIVED.getDerivedColumnAlias(aggregationDistinctDerivedColumnCount++));
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
    
    private Collection<ColumnProjection> getShorthandColumnsFromSimpleTableSegment(final TableSegment table, final String owner) {
        if (!(table instanceof SimpleTableSegment)) {
            return Collections.emptyList();
        }
        String tableName = ((SimpleTableSegment) table).getTableName().getIdentifier().getValue();
        String tableAlias = table.getAlias().orElse(tableName);
        String schemaName = ((SimpleTableSegment) table).getOwner().map(optional -> optional.getIdentifier().getValue())
                .orElseGet(() -> DatabaseTypeEngine.getDefaultSchemaName(databaseType, databaseName)).toLowerCase();
        ShardingSphereSchema schema = schemas.get(schemaName);
        ShardingSpherePreconditions.checkNotNull(schema, () -> new SchemaNotFoundException(schemaName));
        Collection<ColumnProjection> result = new LinkedList<>();
        if (null == owner) {
            schema.getVisibleColumnNames(tableName).stream().map(each -> new ColumnProjection(tableAlias, each, null)).forEach(result::add);
        } else if (owner.equalsIgnoreCase(tableAlias)) {
            schema.getVisibleColumnNames(tableName).stream().map(each -> new ColumnProjection(owner, each, null)).forEach(result::add);
        }
        return result;
    }
    
    private Collection<Projection> getShorthandColumnsFromSubqueryTableSegment(final TableSegment table) {
        if (!(table instanceof SubqueryTableSegment)) {
            return Collections.emptyList();
        }
        SelectStatement subSelectStatement = ((SubqueryTableSegment) table).getSubquery().getSelect();
        Collection<Projection> projections = subSelectStatement.getProjections().getProjections().stream().map(each -> createProjection(subSelectStatement.getFrom(), each).orElse(null))
                .filter(Objects::nonNull).collect(Collectors.toList());
        return getResultSetProjections(projections);
    }
    
    private Collection<Projection> getShorthandColumnsFromJoinTableSegment(final TableSegment table, final ProjectionSegment projectionSegment) {
        if (!(table instanceof JoinTableSegment)) {
            return Collections.emptyList();
        }
        Collection<Projection> projections = new LinkedList<>();
        createProjection(((JoinTableSegment) table).getLeft(), projectionSegment).ifPresent(projections::add);
        createProjection(((JoinTableSegment) table).getRight(), projectionSegment).ifPresent(projections::add);
        return getResultSetProjections(projections);
    }
    
    private Collection<Projection> getResultSetProjections(final Collection<Projection> projections) {
        Collection<Projection> result = new LinkedList<>();
        for (Projection each : projections) {
            if (each instanceof ColumnProjection) {
                result.add(each);
            } else if (each instanceof ExpressionProjection) {
                result.add(each);
            } else if (each instanceof ShorthandProjection) {
                result.addAll(((ShorthandProjection) each).getResultSetColumns().values());
            }
        }
        return result;
    }
    
    private void appendAverageDistinctDerivedProjection(final AggregationDistinctProjection averageDistinctProjection) {
        String innerExpression = averageDistinctProjection.getInnerExpression();
        String distinctInnerExpression = averageDistinctProjection.getDistinctInnerExpression();
        String countAlias = DerivedColumn.AVG_COUNT_ALIAS.getDerivedColumnAlias(aggregationAverageDerivedColumnCount);
        AggregationDistinctProjection countDistinctProjection = new AggregationDistinctProjection(
                0, 0, AggregationType.COUNT, innerExpression, countAlias, distinctInnerExpression, databaseType);
        String sumAlias = DerivedColumn.AVG_SUM_ALIAS.getDerivedColumnAlias(aggregationAverageDerivedColumnCount);
        AggregationDistinctProjection sumDistinctProjection = new AggregationDistinctProjection(
                0, 0, AggregationType.SUM, innerExpression, sumAlias, distinctInnerExpression, databaseType);
        averageDistinctProjection.getDerivedAggregationProjections().add(countDistinctProjection);
        averageDistinctProjection.getDerivedAggregationProjections().add(sumDistinctProjection);
        aggregationAverageDerivedColumnCount++;
    }
    
    private void appendAverageDerivedProjection(final AggregationProjection averageProjection) {
        String innerExpression = averageProjection.getInnerExpression();
        String countAlias = DerivedColumn.AVG_COUNT_ALIAS.getDerivedColumnAlias(aggregationAverageDerivedColumnCount);
        AggregationProjection countProjection = new AggregationProjection(AggregationType.COUNT, innerExpression, countAlias, databaseType);
        String sumAlias = DerivedColumn.AVG_SUM_ALIAS.getDerivedColumnAlias(aggregationAverageDerivedColumnCount);
        AggregationProjection sumProjection = new AggregationProjection(AggregationType.SUM, innerExpression, sumAlias, databaseType);
        averageProjection.getDerivedAggregationProjections().add(countProjection);
        averageProjection.getDerivedAggregationProjections().add(sumProjection);
        aggregationAverageDerivedColumnCount++;
    }
}
