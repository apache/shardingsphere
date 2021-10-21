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

package org.apache.shardingsphere.shadow.route.engine.dml;

import lombok.Getter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.shadow.api.shadow.ShadowOperationType;
import org.apache.shardingsphere.shadow.api.shadow.column.ColumnShadowAlgorithm;
import org.apache.shardingsphere.shadow.api.shadow.note.NoteShadowAlgorithm;
import org.apache.shardingsphere.shadow.condition.ShadowColumnCondition;
import org.apache.shardingsphere.shadow.condition.ShadowDetermineCondition;
import org.apache.shardingsphere.shadow.route.engine.ShadowRouteEngine;
import org.apache.shardingsphere.shadow.route.engine.determiner.ShadowDeterminerFactory;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * Abstract shadow DML statement route engine.
 */
public abstract class AbstractShadowDMLStatementRouteEngine implements ShadowRouteEngine {
    
    @Getter
    private final Map<String, String> tableAliasNameMappings = new LinkedHashMap<>();
    
    @Override
    public void route(final RouteContext routeContext, final ShadowRule shadowRule) {
        findShadowDataSourceMappings(shadowRule).ifPresent(shadowDataSourceMappings -> shadowDMLStatementRouteDecorate(routeContext, shadowDataSourceMappings));
    }
    
    private Optional<Map<String, String>> findShadowDataSourceMappings(final ShadowRule shadowRule) {
        Collection<String> relatedShadowTables = getRelatedShadowTables(getAllTables(), shadowRule);
        ShadowOperationType shadowOperationType = getShadowOperationType();
        for (String each : relatedShadowTables) {
            if (isShadowTable(each, shadowRule, shadowOperationType)) {
                return shadowRule.getRelatedShadowDataSourceMappings(each);
            }
        }
        return Optional.empty();
    }
    
    private boolean isShadowTable(final String tableName, final ShadowRule shadowRule, final ShadowOperationType shadowOperationType) {
        ShadowDetermineCondition shadowDetermineCondition = new ShadowDetermineCondition(tableName, shadowOperationType);
        Optional<Collection<NoteShadowAlgorithm<Comparable<?>>>> noteShadowAlgorithms = shadowRule.getRelatedNoteShadowAlgorithms(tableName);
        Optional<Collection<String>> sqlNotes = parseSqlNotes();
        if (noteShadowAlgorithms.isPresent() && sqlNotes.isPresent() && isMatchAnyNoteShadowAlgorithms(noteShadowAlgorithms.get(), shadowDetermineCondition.initSqlNotes(sqlNotes.get()), shadowRule)) {
            return true;
        }
        Optional<Collection<ColumnShadowAlgorithm<Comparable<?>>>> columnShadowAlgorithms = shadowRule.getRelatedColumnShadowAlgorithms(tableName, shadowOperationType);
        Optional<Collection<ShadowColumnCondition>> shadowColumnConditions = parseShadowColumnConditions();
        return columnShadowAlgorithms.isPresent() && shadowColumnConditions.isPresent() && isMatchAnyColumnShadowAlgorithms(columnShadowAlgorithms.get(),
                shadowDetermineCondition.initShadowColumnCondition(shadowColumnConditions.get()), shadowRule);
    }
    
    private boolean isMatchAnyNoteShadowAlgorithms(final Collection<NoteShadowAlgorithm<Comparable<?>>> shadowAlgorithms, final ShadowDetermineCondition shadowCondition, final ShadowRule shadowRule) {
        for (NoteShadowAlgorithm<Comparable<?>> each : shadowAlgorithms) {
            if (isMatchNoteShadowAlgorithm(each, shadowCondition, shadowRule)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isMatchNoteShadowAlgorithm(final NoteShadowAlgorithm<Comparable<?>> noteShadowAlgorithm, final ShadowDetermineCondition shadowCondition, final ShadowRule shadowRule) {
        return ShadowDeterminerFactory.newInstance(noteShadowAlgorithm).isShadow(shadowCondition, shadowRule);
    }
    
    private boolean isMatchAnyColumnShadowAlgorithms(final Collection<ColumnShadowAlgorithm<Comparable<?>>> shadowAlgorithms, final ShadowDetermineCondition shadowCondition,
                                                     final ShadowRule shadowRule) {
        for (ColumnShadowAlgorithm<Comparable<?>> each : shadowAlgorithms) {
            if (isMatchColumnShadowAlgorithm(each, shadowCondition, shadowRule)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isMatchColumnShadowAlgorithm(final ColumnShadowAlgorithm<Comparable<?>> columnShadowAlgorithm, final ShadowDetermineCondition shadowCondition, final ShadowRule shadowRule) {
        return ShadowDeterminerFactory.newInstance(columnShadowAlgorithm).isShadow(shadowCondition, shadowRule);
    }
    
    private Collection<String> getRelatedShadowTables(final Collection<SimpleTableSegment> simpleTableSegments, final ShadowRule shadowRule) {
        Collection<String> tableNames = new LinkedHashSet<>();
        for (SimpleTableSegment each : simpleTableSegments) {
            String tableName = each.getTableName().getIdentifier().getValue();
            String alias = each.getAlias().isPresent() ? each.getAlias().get() : tableName;
            tableNames.add(tableName);
            tableAliasNameMappings.put(alias, tableName);
        }
        return shadowRule.getRelatedShadowTables(tableNames);
    }
    
    /**
     * Parse shadow column conditions.
     *
     * @return shadow column condition
     */
    protected abstract Optional<Collection<ShadowColumnCondition>> parseShadowColumnConditions();
    
    /**
     * Parse sql notes.
     *
     * @return sql notes
     */
    protected abstract Optional<Collection<String>> parseSqlNotes();
    
    /**
     * get shadow operation type.
     *
     * @return  shadow operation type
     */
    protected abstract ShadowOperationType getShadowOperationType();
    
    /**
     * Get all tables.
     *
     * @return all tables
     */
    protected abstract Collection<SimpleTableSegment> getAllTables();
    
    /**
     * Get single table tame.
     *
     * @return table tame
     */
    protected String getSingleTableName() {
        return tableAliasNameMappings.entrySet().iterator().next().getValue();
    }
    
    private void shadowDMLStatementRouteDecorate(final RouteContext routeContext, final Map<String, String> shadowDataSourceMappings) {
        Collection<RouteUnit> routeUnits = routeContext.getRouteUnits();
        Collection<RouteUnit> toBeAdded = new LinkedList<>();
        for (RouteUnit each : routeUnits) {
            RouteMapper routeMapper = each.getDataSourceMapper();
            if (null != shadowDataSourceMappings.get(routeMapper.getActualName())) {
                toBeAdded.add(new RouteUnit(new RouteMapper(routeMapper.getLogicName(), shadowDataSourceMappings.get(routeMapper.getActualName())), each.getTableMappers()));
            }
        }
        routeUnits.clear();
        routeUnits.addAll(toBeAdded);
    }
}
