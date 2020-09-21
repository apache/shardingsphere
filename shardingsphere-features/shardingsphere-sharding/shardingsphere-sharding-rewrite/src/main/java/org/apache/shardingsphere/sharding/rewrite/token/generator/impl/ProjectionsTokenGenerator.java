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
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.RouteContextAware;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.rewrite.token.generator.IgnoreForSingleRoute;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.ProjectionsToken;
import org.apache.shardingsphere.sql.parser.binder.segment.select.projection.Projection;
import org.apache.shardingsphere.sql.parser.binder.segment.select.projection.impl.AggregationDistinctProjection;
import org.apache.shardingsphere.sql.parser.binder.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.sql.parser.binder.segment.select.projection.impl.DerivedProjection;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Projections token generator.
 */
@Setter
public final class ProjectionsTokenGenerator implements OptionalSQLTokenGenerator<SelectStatementContext>, IgnoreForSingleRoute, RouteContextAware {
    
    private RouteContext routeContext;
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext && !getDerivedProjectionTexts((SelectStatementContext) sqlStatementContext).isEmpty();
    }
    
    @Override
    public ProjectionsToken generateSQLToken(final SelectStatementContext selectStatementContext) {
        Map<RouteUnit, Collection<String>> derivedProjectionTexts = getDerivedProjectionTexts(selectStatementContext);
        return new ProjectionsToken(selectStatementContext.getProjectionsContext().getStopIndex() + 1 + " ".length(), derivedProjectionTexts);
    }
    
    private Map<RouteUnit, Collection<String>> getDerivedProjectionTexts(final SelectStatementContext selectStatementContext) {
        Map<RouteUnit, Collection<String>> result = new HashMap<>();
        for (RouteUnit routeUnit : routeContext.getRouteResult().getRouteUnits()) {
            result.put(routeUnit, new LinkedList<>());
            for (Projection each : selectStatementContext.getProjectionsContext().getProjections()) {
                if (each instanceof AggregationProjection && !((AggregationProjection) each).getDerivedAggregationProjections().isEmpty()) {
                    result.get(routeUnit).addAll(((AggregationProjection) each).getDerivedAggregationProjections().stream().map(this::getDerivedProjectionText).collect(Collectors.toList()));
                } else if (each instanceof DerivedProjection && ((DerivedProjection) each).getDerivedProjection() instanceof ColumnOrderByItemSegment) {
                    TableExtractor tableExtractor = new TableExtractor();
                    tableExtractor.extractTablesFromSelect(selectStatementContext.getSqlStatement());
                    result.get(routeUnit).add(getDerivedProjectionTextFromColumnOrderByItemSegment((DerivedProjection) each, tableExtractor, routeUnit));
                } else if (each instanceof DerivedProjection) {
                    result.get(routeUnit).add(getDerivedProjectionText(each));
                }
            }
        }
        return result;
    }
    
    private String getDerivedProjectionText(final Projection projection) {
        Preconditions.checkState(projection.getAlias().isPresent());
        if (projection instanceof AggregationDistinctProjection) {
            return ((AggregationDistinctProjection) projection).getDistinctInnerExpression() + " AS " + projection.getAlias().get() + " ";
        }
        return projection.getExpression() + " AS " + projection.getAlias().get() + " ";
    }
    
    private String getDerivedProjectionTextFromColumnOrderByItemSegment(final DerivedProjection projection, final TableExtractor tableExtractor, final RouteUnit routeUnit) {
        Preconditions.checkState(projection.getAlias().isPresent());
        Preconditions.checkState(projection.getDerivedProjection() instanceof ColumnOrderByItemSegment);
        ColumnOrderByItemSegment columnOrderByItemSegment = (ColumnOrderByItemSegment) projection.getDerivedProjection();
        ColumnOrderByItemSegment newColumnOrderByItem = generateNewColumnOrderByItem(columnOrderByItemSegment, routeUnit, tableExtractor);
        String result = new StringBuilder().append(newColumnOrderByItem.getText()).append(" AS ").append(projection.getAlias().get()).append(" ").toString();
        return result;
    }
    
    private Optional<String> getActualTables(final RouteUnit routeUnit, final String logicalTableName) {
        for (RouteMapper each : routeUnit.getTableMappers()) {
            if (each.getLogicName().equalsIgnoreCase(logicalTableName)) {
                return Optional.of(each.getActualName());
            }
        }
        return Optional.empty();
    }

    private ColumnOrderByItemSegment generateNewColumnOrderByItem(final ColumnOrderByItemSegment old, final RouteUnit routeUnit, final TableExtractor tableExtractor) {
        Optional<OwnerSegment> ownerSegment = old.getColumn().getOwner();
        if (!ownerSegment.isPresent()) {
            return old;
        }
        if (!tableExtractor.needRewrite(ownerSegment.get())) {
            return old;
        }
        Optional<String> actualTableName = getActualTables(routeUnit, ownerSegment.get().getIdentifier().getValue());
        Preconditions.checkState(actualTableName.isPresent());
        ColumnSegment newColumnSegment = new ColumnSegment(0, 0, old.getColumn().getIdentifier());
        String newOwnerString = String.format("%s%s%s", ownerSegment.get().getIdentifier().getQuoteCharacter().getStartDelimiter(), actualTableName.get(),
                ownerSegment.get().getIdentifier().getQuoteCharacter().getEndDelimiter());
        IdentifierValue newOwnerIdentifier = new IdentifierValue(newOwnerString);
        OwnerSegment newOwner = new OwnerSegment(0, 0, newOwnerIdentifier);
        newColumnSegment.setOwner(newOwner);
        ColumnOrderByItemSegment result = new ColumnOrderByItemSegment(newColumnSegment, old.getOrderDirection());
        return result;
    }
}
