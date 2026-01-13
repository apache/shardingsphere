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

package org.apache.shardingsphere.sharding.rewrite.token.generator.impl;

import com.google.common.base.Preconditions;
import lombok.Setter;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.NullsOrderType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationDistinctProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.DerivedProjection;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.ExpressionProjection;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.aware.RouteContextAware;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.rewrite.token.generator.IgnoreForSingleRoute;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.ProjectionsToken;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Sharding projections token generator.
 */
@HighFrequencyInvocation
@Setter
public final class ShardingProjectionsTokenGenerator implements OptionalSQLTokenGenerator<SelectStatementContext>, IgnoreForSingleRoute, RouteContextAware {
    
    private RouteContext routeContext;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext && containsDerivedProjections((SelectStatementContext) sqlStatementContext);
    }
    
    private boolean containsDerivedProjections(final SelectStatementContext selectStatementContext) {
        for (Projection each : selectStatementContext.getProjectionsContext().getProjections()) {
            if (each instanceof AggregationProjection && !((AggregationProjection) each).getDerivedAggregationProjections().isEmpty() || each instanceof DerivedProjection) {
                return true;
            }
            if (each instanceof ExpressionProjection && !((ExpressionProjection) each).getExpressionSegment().getAggregationProjectionSegments().isEmpty()) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public ProjectionsToken generateSQLToken(final SelectStatementContext selectStatementContext) {
        Map<RouteUnit, Collection<String>> derivedProjectionTexts = getDerivedProjectionTexts(selectStatementContext);
        return new ProjectionsToken(selectStatementContext.getProjectionsContext().getStopIndex() + 1 + " ".length(), derivedProjectionTexts);
    }
    
    private Map<RouteUnit, Collection<String>> getDerivedProjectionTexts(final SelectStatementContext selectStatementContext) {
        Map<RouteUnit, Collection<String>> result = new HashMap<>(routeContext.getRouteUnits().size(), 1F);
        for (RouteUnit each : routeContext.getRouteUnits()) {
            Collection<String> projectionTexts = getDerivedProjectionTexts(selectStatementContext, each);
            result.put(each, projectionTexts);
        }
        return result;
    }
    
    private Collection<String> getDerivedProjectionTexts(final SelectStatementContext selectStatementContext, final RouteUnit routeUnit) {
        Collection<String> result = new LinkedList<>();
        Collection<String> existingAliases = new LinkedList<>();
        for (Projection p : selectStatementContext.getProjectionsContext().getProjections()) {
            p.getAlias().ifPresent(a -> existingAliases.add(a.getValue()));
            if (p instanceof AggregationProjection) {
                ((AggregationProjection) p).getDerivedAggregationProjections()
                        .forEach(d -> d.getAlias().ifPresent(a -> existingAliases.add(a.getValue())));
            }
        }
        for (AggregationProjection each : selectStatementContext.getProjectionsContext().getExpandAggregationProjections()) {
            String alias = each.getAlias().map(IdentifierValue::getValue).orElse("");
            if (!alias.isEmpty() && !existingAliases.contains(alias)) {
                result.add(getDerivedProjectionText(each));
                existingAliases.add(alias);
            }
            for (AggregationProjection derived : each.getDerivedAggregationProjections()) {
                String dAlias = derived.getAlias().map(IdentifierValue::getValue).orElse("");
                if (!dAlias.isEmpty() && !existingAliases.contains(dAlias)) {
                    result.add(getDerivedProjectionText(derived));
                    existingAliases.add(dAlias);
                }
            }
        }
        for (Projection each : selectStatementContext.getProjectionsContext().getProjections()) {
            if (each instanceof DerivedProjection && ((DerivedProjection) each).getDerivedProjectionSegment() instanceof ColumnOrderByItemSegment) {
                TableExtractor tableExtractor = new TableExtractor();
                tableExtractor.extractTablesFromSelect(selectStatementContext.getSqlStatement());
                result.add(getDerivedProjectionText((DerivedProjection) each, tableExtractor, routeUnit, selectStatementContext.getSqlStatement().getDatabaseType()));
            } else if (each instanceof DerivedProjection) {
                result.add(getDerivedProjectionText(each));
            }
        }
        return result;
    }
    private String getDerivedProjectionText(final Projection projection) {
        Preconditions.checkState(projection.getAlias().isPresent());
        String projectionExpression = projection instanceof AggregationDistinctProjection ? ((AggregationDistinctProjection) projection).getDistinctInnerExpression() : projection.getExpression();
        return projectionExpression + " AS " + projection.getAlias().get().getValue() + " ";
    }
    
    private String getDerivedProjectionText(final DerivedProjection projection, final TableExtractor tableExtractor, final RouteUnit routeUnit, final DatabaseType databaseType) {
        Preconditions.checkState(projection.getAlias().isPresent());
        Preconditions.checkState(projection.getDerivedProjectionSegment() instanceof ColumnOrderByItemSegment);
        ColumnOrderByItemSegment columnOrderByItemSegment = (ColumnOrderByItemSegment) projection.getDerivedProjectionSegment();
        ColumnOrderByItemSegment newColumnOrderByItem = generateNewColumnOrderByItem(columnOrderByItemSegment, routeUnit, tableExtractor, databaseType);
        return newColumnOrderByItem.getText() + " AS " + projection.getAlias().get().getValue() + " ";
    }
    
    private ColumnOrderByItemSegment generateNewColumnOrderByItem(final ColumnOrderByItemSegment old,
                                                                  final RouteUnit routeUnit, final TableExtractor tableExtractor, final DatabaseType databaseType) {
        Optional<OwnerSegment> ownerSegment = old.getColumn().getOwner();
        if (!ownerSegment.isPresent() || !tableExtractor.needRewrite(ownerSegment.get())) {
            return old;
        }
        String actualTableName = getActualTableName(routeUnit, ownerSegment.get().getIdentifier().getValue());
        OwnerSegment newOwner = new OwnerSegment(0, 0, new IdentifierValue(ownerSegment.get().getIdentifier().getQuoteCharacter().wrap(actualTableName)));
        ColumnSegment newColumnSegment = new ColumnSegment(0, 0, old.getColumn().getIdentifier());
        newColumnSegment.setOwner(newOwner);
        NullsOrderType nullsOrderType = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData().getDefaultNullsOrderType().getResolvedOrderType(old.getOrderDirection().name());
        return new ColumnOrderByItemSegment(newColumnSegment, old.getOrderDirection(), nullsOrderType);
    }
    
    private String getActualTableName(final RouteUnit routeUnit, final String logicalTableName) {
        for (RouteMapper each : routeUnit.getTableMappers()) {
            if (each.getLogicName().equalsIgnoreCase(logicalTableName)) {
                return each.getActualName();
            }
        }
        throw new IllegalStateException(String.format("Cannot find actual table name with logic table name '%s'", logicalTableName));
    }
}
