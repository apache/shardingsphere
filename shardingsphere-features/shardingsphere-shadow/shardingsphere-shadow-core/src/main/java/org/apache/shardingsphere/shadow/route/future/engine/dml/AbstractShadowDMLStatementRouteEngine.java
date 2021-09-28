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

package org.apache.shardingsphere.shadow.route.future.engine.dml;

import lombok.Getter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.shadow.api.shadow.column.ColumnShadowAlgorithm;
import org.apache.shardingsphere.shadow.api.shadow.note.NoteShadowAlgorithm;
import org.apache.shardingsphere.shadow.route.future.engine.ShadowRouteEngine;
import org.apache.shardingsphere.shadow.route.future.engine.determiner.ShadowColumnCondition;
import org.apache.shardingsphere.shadow.route.future.engine.determiner.ShadowDetermineCondition;
import org.apache.shardingsphere.shadow.route.future.engine.determiner.ShadowDeterminerFactory;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;
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
        for (String each : relatedShadowTables) {
            if (isShadowTable(shadowRule, each)) {
                return shadowRule.getRelatedShadowDataSourceMappings(each);
            }
        }
        return Optional.empty();
    }
    
    private boolean isShadowTable(final ShadowRule shadowRule, final String tableName) {
        Optional<Collection<ShadowAlgorithm>> relatedShadowAlgorithms = shadowRule.getRelatedShadowAlgorithms(tableName);
        return relatedShadowAlgorithms.isPresent() && isMatchAnyShadowAlgorithms(relatedShadowAlgorithms.get(), shadowRule, tableName);
    }
    
    private boolean isMatchAnyShadowAlgorithms(final Collection<ShadowAlgorithm> shadowAlgorithms, final ShadowRule shadowRule, final String tableName) {
        ShadowDetermineCondition shadowDetermineCondition = createShadowDetermineCondition();
        for (ShadowAlgorithm each : shadowAlgorithms) {
            if (isMatchShadowAlgorithm(shadowDetermineCondition, each, shadowRule, tableName)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isMatchShadowAlgorithm(final ShadowDetermineCondition shadowDetermineCondition, final ShadowAlgorithm shadowAlgorithm, final ShadowRule shadowRule, final String tableName) {
        if (shadowAlgorithm instanceof NoteShadowAlgorithm) {
            return isMatchNoteAlgorithm(shadowDetermineCondition, shadowAlgorithm, shadowRule, tableName);
        } else if (shadowAlgorithm instanceof ColumnShadowAlgorithm) {
            return isMatchColumnAlgorithm(shadowDetermineCondition, shadowAlgorithm, shadowRule, tableName);
        } else {
            return false;
        }
    }
    
    private boolean isMatchColumnAlgorithm(final ShadowDetermineCondition shadowDetermineCondition, final ShadowAlgorithm shadowAlgorithm, final ShadowRule shadowRule, final String tableName) {
        if (!shadowDetermineCondition.isShadowColumnConditionsInitialized()) {
            Optional<Collection<ShadowColumnCondition>> shadowColumnConditions = parseShadowColumnConditions();
            if (!shadowColumnConditions.isPresent()) {
                return false;
            }
            shadowDetermineCondition.initShadowColumnCondition(shadowColumnConditions.get());
        }
        return ShadowDeterminerFactory.newInstance(shadowAlgorithm).isShadow(shadowDetermineCondition, shadowRule, tableName);
    }
    
    private boolean isMatchNoteAlgorithm(final ShadowDetermineCondition shadowDetermineCondition, final ShadowAlgorithm shadowAlgorithm, final ShadowRule shadowRule, final String tableName) {
        if (!shadowDetermineCondition.isSqlNotesInitialized()) {
            Optional<Collection<String>> sqlNotes = parseSqlNotes();
            if (!sqlNotes.isPresent()) {
                return false;
            }
            shadowDetermineCondition.initSqlNotes(sqlNotes.get());
        }
        return ShadowDeterminerFactory.newInstance(shadowAlgorithm).isShadow(shadowDetermineCondition, shadowRule, tableName);
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
     * Create shadow determine condition.
     *
     * @return  new instance of shadow determine condition
     */
    protected abstract ShadowDetermineCondition createShadowDetermineCondition();
    
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
