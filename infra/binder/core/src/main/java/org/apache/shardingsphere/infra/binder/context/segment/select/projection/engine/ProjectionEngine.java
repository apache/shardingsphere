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

package org.apache.shardingsphere.infra.binder.context.segment.select.projection.engine;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.DerivedColumn;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationDistinctProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ColumnProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ExpressionProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ParameterMarkerProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ShorthandProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.SubqueryProjection;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.Paren;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Projection engine.
 */
@RequiredArgsConstructor
public final class ProjectionEngine {
    
    private final DatabaseType databaseType;
    
    private int aggregationAverageDerivedColumnCount;
    
    private int aggregationDistinctDerivedColumnCount;
    
    private int expressionDerivedColumnCount;
    
    /**
     * Create projection.
     *
     * @param projectionSegment projection segment
     * @param expressionDerivedAggregations expression derived aggregations map to capture local state
     * @return projection
     */
    public Optional<Projection> createProjection(final ProjectionSegment projectionSegment, final Map<ExpressionProjection, List<AggregationProjection>> expressionDerivedAggregations) {
        if (projectionSegment instanceof ShorthandProjectionSegment) {
            return Optional.of(createProjection((ShorthandProjectionSegment) projectionSegment, expressionDerivedAggregations));
        }
        if (projectionSegment instanceof ColumnProjectionSegment) {
            return Optional.of(createProjection((ColumnProjectionSegment) projectionSegment));
        }
        if (projectionSegment instanceof ExpressionProjectionSegment) {
            return Optional.of(createProjection((ExpressionProjectionSegment) projectionSegment, expressionDerivedAggregations));
        }
        if (projectionSegment instanceof AggregationDistinctProjectionSegment) {
            return Optional.of(createProjection((AggregationDistinctProjectionSegment) projectionSegment));
        }
        if (projectionSegment instanceof AggregationProjectionSegment) {
            return Optional.of(createProjection((AggregationProjectionSegment) projectionSegment));
        }
        if (projectionSegment instanceof SubqueryProjectionSegment) {
            return Optional.of(createProjection((SubqueryProjectionSegment) projectionSegment, expressionDerivedAggregations));
        }
        if (projectionSegment instanceof ParameterMarkerExpressionSegment) {
            return Optional.of(createProjection((ParameterMarkerExpressionSegment) projectionSegment));
        }
        return Optional.empty();
    }
    
    private ParameterMarkerProjection createProjection(final ParameterMarkerExpressionSegment projectionSegment) {
        return new ParameterMarkerProjection(projectionSegment.getParameterMarkerIndex(), projectionSegment.getParameterMarkerType(), projectionSegment.getAlias().orElse(null));
    }
    
    private SubqueryProjection createProjection(final SubqueryProjectionSegment projectionSegment, final Map<ExpressionProjection, List<AggregationProjection>> expressionDerivedAggregations) {
        Projection subqueryProjection = createProjection(projectionSegment.getSubquery().getSelect().getProjections().getProjections().iterator().next(), expressionDerivedAggregations)
                .orElseThrow(() -> new IllegalArgumentException("Subquery projection must have at least one projection column."));
        return new SubqueryProjection(projectionSegment, subqueryProjection, projectionSegment.getAlias().orElse(null), databaseType);
    }
    
    private ShorthandProjection createProjection(final ShorthandProjectionSegment projectionSegment, final Map<ExpressionProjection, List<AggregationProjection>> expressionDerivedAggregations) {
        IdentifierValue owner = projectionSegment.getOwner().map(OwnerSegment::getIdentifier).orElse(null);
        Collection<Projection> projections = new LinkedHashSet<>(projectionSegment.getActualProjectionSegments().size(), 1F);
        projectionSegment.getActualProjectionSegments().forEach(each -> createProjection(each, expressionDerivedAggregations).ifPresent(projections::add));
        return new ShorthandProjection(owner, projections);
    }
    
    private ColumnProjection createProjection(final ColumnProjectionSegment projectionSegment) {
        IdentifierValue owner = projectionSegment.getColumn().getOwner().isPresent() ? projectionSegment.getColumn().getOwner().get().getIdentifier() : null;
        IdentifierValue alias = projectionSegment.getAliasName().isPresent() ? projectionSegment.getAlias().orElse(null) : null;
        return new ColumnProjection(owner, projectionSegment.getColumn().getIdentifier(), alias, databaseType, projectionSegment.getColumn().getLeftParentheses().orElse(null),
                projectionSegment.getColumn().getRightParentheses().orElse(null), projectionSegment.getColumn().getColumnBoundInfo(), true);
    }
    
    private ExpressionProjection createProjection(final ExpressionProjectionSegment projectionSegment, final Map<ExpressionProjection, List<AggregationProjection>> expressionDerivedAggregations) {
        ExpressionProjection result = new ExpressionProjection(projectionSegment, projectionSegment.getAlias().orElse(null), databaseType);
        
        List<AggregationProjectionSegment> extractedSegments = new ArrayList<>();
        extractAggregationSegments(projectionSegment.getExpr(), extractedSegments);
        
        if (!extractedSegments.isEmpty()) {
            List<AggregationProjection> derivedAggregations = new ArrayList<>();
            Map<String, AggregationProjection> uniqueAggregations = new LinkedHashMap<>();
            
            for (AggregationProjectionSegment aggrSegment : extractedSegments) {
                String aggrText = aggrSegment.getText();
                
                if (uniqueAggregations.containsKey(aggrText)) {
                    continue;
                }
                
                AggregationProjection derivedAggr = createDerivedAggregationProjection(aggrSegment);
                
                uniqueAggregations.put(aggrText, derivedAggr);
                derivedAggregations.add(derivedAggr);
            }
            expressionDerivedAggregations.put(result, derivedAggregations);
        }
        
        return result;
    }
    
    private Projection createProjection(final AggregationDistinctProjectionSegment projectionSegment) {
        if (projectionSegment.getWindow().isPresent()) {
            return createExpressionProjection(projectionSegment);
        }
        IdentifierValue alias =
                projectionSegment.getAlias().orElseGet(() -> new IdentifierValue(DerivedColumn.AGGREGATION_DISTINCT_DERIVED.getDerivedColumnAlias(aggregationDistinctDerivedColumnCount++)));
        AggregationDistinctProjection result = new AggregationDistinctProjection(
                projectionSegment.getStartIndex(), projectionSegment.getStopIndex(), projectionSegment.getType(), projectionSegment, alias,
                projectionSegment.getDistinctInnerExpression(), databaseType, projectionSegment.getSeparator().orElse(null));
        if (AggregationType.AVG == result.getType()) {
            appendAverageDistinctDerivedProjection(result);
        }
        return result;
    }
    
    private Projection createProjection(final AggregationProjectionSegment projectionSegment) {
        if (projectionSegment.getWindow().isPresent()) {
            return createExpressionProjection(projectionSegment);
        }
        AggregationProjection result =
                new AggregationProjection(projectionSegment.getType(), projectionSegment, projectionSegment.getAlias().orElse(null), databaseType,
                        projectionSegment.getSeparator().orElse(null));
        if (AggregationType.AVG == result.getType()) {
            appendAverageDerivedProjection(result);
            // TODO replace avg to constant, avoid calculate useless avg
        }
        return result;
    }
    
    private AggregationProjection createDerivedAggregationProjection(final AggregationProjectionSegment aggrSegment) {
        IdentifierValue alias = new IdentifierValue(DerivedColumn.EXPRESSION_DERIVED.getDerivedColumnAlias(expressionDerivedColumnCount++));
        AggregationProjection result = new AggregationProjection(aggrSegment.getType(), aggrSegment, alias, databaseType, aggrSegment.getSeparator().orElse(null));
        if (AggregationType.AVG == result.getType()) {
            appendAverageDerivedProjection(result);
        }
        return result;
    }
    
    private void extractAggregationSegments(final ExpressionSegment segment, final List<AggregationProjectionSegment> extractedSegments) {
        if (segment == null) {
            return;
        }
        
        if (segment instanceof AggregationProjectionSegment) {
            if (segment instanceof AggregationDistinctProjectionSegment) {
                return;
            }
            if (!((AggregationProjectionSegment) segment).getWindow().isPresent()) {
                extractedSegments.add((AggregationProjectionSegment) segment);
            }
            return;
        }
        
        if (segment instanceof FunctionSegment) {
            String functionName = ((FunctionSegment) segment).getFunctionName();
            if ("IFNULL".equalsIgnoreCase(functionName) || "COALESCE".equalsIgnoreCase(functionName)) {
                for (ExpressionSegment param : ((FunctionSegment) segment).getParameters()) {
                    extractAggregationSegments(param, extractedSegments);
                }
            }
        }
    }
    
    private ExpressionProjection createExpressionProjection(final AggregationProjectionSegment projectionSegment) {
        ExpressionProjectionSegment expressionSegment =
                new ExpressionProjectionSegment(projectionSegment.getStartIndex(), projectionSegment.getStopIndex(), projectionSegment.getExpression(), projectionSegment);
        return new ExpressionProjection(expressionSegment, projectionSegment.getAlias().orElse(null), databaseType);
    }
    
    private void appendAverageDistinctDerivedProjection(final AggregationDistinctProjection averageDistinctProjection) {
        String distinctInnerExpression = averageDistinctProjection.getDistinctInnerExpression();
        String countAlias = DerivedColumn.AVG_COUNT_ALIAS.getDerivedColumnAlias(aggregationAverageDerivedColumnCount);
        String innerExpression = averageDistinctProjection.getExpression().substring(averageDistinctProjection.getExpression().indexOf(Paren.PARENTHESES.getLeftParen()));
        AggregationProjectionSegment countExpression = new AggregationProjectionSegment(0, 0, AggregationType.COUNT, AggregationType.COUNT.name() + innerExpression);
        AggregationDistinctProjection countDistinctProjection =
                new AggregationDistinctProjection(0, 0, AggregationType.COUNT, countExpression, new IdentifierValue(countAlias), distinctInnerExpression, databaseType);
        String sumAlias = DerivedColumn.AVG_SUM_ALIAS.getDerivedColumnAlias(aggregationAverageDerivedColumnCount);
        AggregationProjectionSegment sumExpression = new AggregationProjectionSegment(0, 0, AggregationType.SUM, AggregationType.SUM.name() + innerExpression);
        AggregationDistinctProjection sumDistinctProjection =
                new AggregationDistinctProjection(0, 0, AggregationType.SUM, sumExpression, new IdentifierValue(sumAlias), distinctInnerExpression, databaseType);
        averageDistinctProjection.getDerivedAggregationProjections().add(countDistinctProjection);
        averageDistinctProjection.getDerivedAggregationProjections().add(sumDistinctProjection);
        aggregationAverageDerivedColumnCount++;
    }
    
    private void appendAverageDerivedProjection(final AggregationProjection averageProjection) {
        String countAlias = DerivedColumn.AVG_COUNT_ALIAS.getDerivedColumnAlias(aggregationAverageDerivedColumnCount);
        String innerExpression = averageProjection.getExpression().substring(averageProjection.getExpression().indexOf(Paren.PARENTHESES.getLeftParen()));
        AggregationProjectionSegment countExpression = new AggregationProjectionSegment(0, 0, AggregationType.COUNT, AggregationType.COUNT.name() + innerExpression);
        AggregationProjection countProjection = new AggregationProjection(AggregationType.COUNT, countExpression, new IdentifierValue(countAlias), databaseType);
        String sumAlias = DerivedColumn.AVG_SUM_ALIAS.getDerivedColumnAlias(aggregationAverageDerivedColumnCount);
        AggregationProjectionSegment sumExpression = new AggregationProjectionSegment(0, 0, AggregationType.SUM, AggregationType.SUM.name() + innerExpression);
        AggregationProjection sumProjection = new AggregationProjection(AggregationType.SUM, sumExpression, new IdentifierValue(sumAlias), databaseType);
        averageProjection.getDerivedAggregationProjections().add(countProjection);
        averageProjection.getDerivedAggregationProjections().add(sumProjection);
        aggregationAverageDerivedColumnCount++;
    }
}
