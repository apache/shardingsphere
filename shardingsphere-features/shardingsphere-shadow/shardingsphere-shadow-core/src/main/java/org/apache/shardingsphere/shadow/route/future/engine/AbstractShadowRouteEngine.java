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

package org.apache.shardingsphere.shadow.route.future.engine;

import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.shadow.api.shadow.column.ColumnShadowAlgorithm;
import org.apache.shardingsphere.shadow.api.shadow.note.NoteShadowAlgorithm;
import org.apache.shardingsphere.shadow.route.future.engine.determiner.ShadowDetermineCondition;
import org.apache.shardingsphere.shadow.route.future.engine.determiner.ShadowDeterminerFactory;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Abstract shadow route engine.
 */
public abstract class AbstractShadowRouteEngine implements ShadowRouteEngine {
    
    @Override
    public void route(final RouteContext routeContext, final ShadowRule shadowRule) {
        if (isShadow(shadowRule)) {
            shadowDMLStatementRouteDecorate(routeContext, shadowRule);
        }
    }
    
    private boolean isShadow(final ShadowRule shadowRule) {
        Collection<String> relatedShadowTables = getRelatedShadowTables(getAllTables(), shadowRule);
        for (String each : relatedShadowTables) {
            Optional<Collection<ShadowAlgorithm>> relatedShadowAlgorithms = shadowRule.getRelatedShadowAlgorithms(each);
            if (relatedShadowAlgorithms.isPresent() && isShadowTable(relatedShadowAlgorithms.get(), shadowRule, each)) {
                return true;
            }
        }
        return false;
    }
    
    private Collection<String> getRelatedShadowTables(final Collection<SimpleTableSegment> simpleTableSegments, final ShadowRule shadowRule) {
        return shadowRule.getRelatedShadowTables(simpleTableSegments.stream().map(each -> each.getTableName().getIdentifier().getValue()).collect(Collectors.toCollection(LinkedList::new)));
    }
    
    private boolean isShadowTable(final Collection<ShadowAlgorithm> shadowAlgorithms, final ShadowRule shadowRule, final String tableName) {
        ShadowDetermineCondition shadowDetermineCondition = createShadowDetermineCondition();
        for (ShadowAlgorithm each : shadowAlgorithms) {
            if (isShadowAlgorithm(shadowDetermineCondition, each, shadowRule, tableName)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isShadowAlgorithm(final ShadowDetermineCondition shadowDetermineCondition, final ShadowAlgorithm shadowAlgorithm, final ShadowRule shadowRule, final String tableName) {
        if (shadowAlgorithm instanceof NoteShadowAlgorithm) {
            return isShadowNoteAlgorithm(shadowDetermineCondition, shadowAlgorithm, shadowRule, tableName);
        } else if (shadowAlgorithm instanceof ColumnShadowAlgorithm) {
            return isShadowColumnAlgorithm(shadowDetermineCondition, shadowAlgorithm, shadowRule, tableName);
        } else {
            return false;
        }
    }
    
    private boolean isShadowColumnAlgorithm(final ShadowDetermineCondition shadowDetermineCondition, final ShadowAlgorithm shadowAlgorithm, final ShadowRule shadowRule, final String tableName) {
        if (!shadowDetermineCondition.isColumnValuesMappingsInitialized()) {
            Optional<Map<String, Collection<Comparable<?>>>> columnValuesMappings = parseColumnValuesMappings();
            if (!columnValuesMappings.isPresent()) {
                return false;
            }
            shadowDetermineCondition.initColumnValuesMappings(columnValuesMappings.get());
        }
        return ShadowDeterminerFactory.newInstance(shadowAlgorithm).isShadow(shadowDetermineCondition, shadowRule, tableName);
    }
    
    private boolean isShadowNoteAlgorithm(final ShadowDetermineCondition shadowDetermineCondition, final ShadowAlgorithm shadowAlgorithm, final ShadowRule shadowRule, final String tableName) {
        if (!shadowDetermineCondition.isSqlNotesInitialized()) {
            Optional<Collection<String>> sqlNotes = parseSqlNotes();
            if (!sqlNotes.isPresent()) {
                return false;
            }
            shadowDetermineCondition.initSqlNotes(sqlNotes.get());
        }
        return ShadowDeterminerFactory.newInstance(shadowAlgorithm).isShadow(shadowDetermineCondition, shadowRule, tableName);
    }
    
    /**
     * Parse column values mappings.
     *
     * @return column values mappings
     */
    protected abstract Optional<Map<String, Collection<Comparable<?>>>> parseColumnValuesMappings();
    
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
    
    private void shadowDMLStatementRouteDecorate(final RouteContext routeContext, final ShadowRule shadowRule) {
        Collection<RouteUnit> toBeAdded = new LinkedList<>();
        routeContext.getRouteUnits().forEach(each -> toBeAdded.add(createActualShadowRouteUnit(each, shadowRule)));
        routeContext.getRouteUnits().clear();
        routeContext.getRouteUnits().addAll(toBeAdded);
    }
    
    private RouteUnit createActualShadowRouteUnit(final RouteUnit routeUnit, final ShadowRule shadowRule) {
        return new RouteUnit(new RouteMapper(routeUnit.getDataSourceMapper().getLogicName(), shadowRule.getShadowDataSourceMappings().get(routeUnit.getDataSourceMapper().getActualName())),
                routeUnit.getTableMappers());
    }
}
