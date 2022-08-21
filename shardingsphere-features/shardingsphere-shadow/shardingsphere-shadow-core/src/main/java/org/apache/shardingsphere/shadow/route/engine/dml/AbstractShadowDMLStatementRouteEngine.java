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
import org.apache.shardingsphere.shadow.api.shadow.ShadowOperationType;
import org.apache.shardingsphere.shadow.api.shadow.column.ColumnShadowAlgorithm;
import org.apache.shardingsphere.shadow.api.shadow.hint.HintShadowAlgorithm;
import org.apache.shardingsphere.shadow.condition.ShadowColumnCondition;
import org.apache.shardingsphere.shadow.condition.ShadowDetermineCondition;
import org.apache.shardingsphere.shadow.route.engine.ShadowRouteEngine;
import org.apache.shardingsphere.shadow.route.engine.determiner.ColumnShadowAlgorithmDeterminer;
import org.apache.shardingsphere.shadow.route.engine.determiner.HintShadowAlgorithmDeterminer;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;

/**
 * Abstract shadow DML statement route engine.
 */
@Getter
public abstract class AbstractShadowDMLStatementRouteEngine implements ShadowRouteEngine {
    
    private final Map<String, String> tableAliasNameMappings = new LinkedHashMap<>();
    
    @Override
    public void route(final RouteContext routeContext, final ShadowRule shadowRule) {
        decorateRouteContext(routeContext, shadowRule, findShadowDataSourceMappings(shadowRule));
    }
    
    private Map<String, String> findShadowDataSourceMappings(final ShadowRule shadowRule) {
        Collection<String> relatedShadowTables = getRelatedShadowTables(getAllTables(), shadowRule);
        if (relatedShadowTables.isEmpty() && isMatchDefaultShadowAlgorithm(shadowRule)) {
            return shadowRule.getAllShadowDataSourceMappings();
        }
        ShadowOperationType shadowOperationType = getShadowOperationType();
        Map<String, String> result = findBySQLComments(relatedShadowTables, shadowRule, shadowOperationType);
        if (!result.isEmpty()) {
            return result;
        }
        return findByShadowColumn(relatedShadowTables, shadowRule, shadowOperationType);
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
    
    @SuppressWarnings("unchecked")
    private boolean isMatchDefaultShadowAlgorithm(final ShadowRule shadowRule) {
        Optional<Collection<String>> sqlComments = parseSQLComments();
        if (!sqlComments.isPresent()) {
            return false;
        }
        Optional<ShadowAlgorithm> defaultShadowAlgorithm = shadowRule.getDefaultShadowAlgorithm();
        if (defaultShadowAlgorithm.isPresent()) {
            ShadowAlgorithm shadowAlgorithm = defaultShadowAlgorithm.get();
            if (shadowAlgorithm instanceof HintShadowAlgorithm<?>) {
                ShadowDetermineCondition shadowDetermineCondition = new ShadowDetermineCondition("", ShadowOperationType.HINT_MATCH);
                return HintShadowAlgorithmDeterminer.isShadow((HintShadowAlgorithm<Comparable<?>>) shadowAlgorithm, shadowDetermineCondition.initSQLComments(sqlComments.get()), shadowRule);
            }
        }
        return false;
    }
    
    private Map<String, String> findBySQLComments(final Collection<String> relatedShadowTables, final ShadowRule shadowRule, final ShadowOperationType shadowOperationType) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String each : relatedShadowTables) {
            if (isContainsShadowInSQLComments(each, shadowRule, new ShadowDetermineCondition(each, shadowOperationType))) {
                result.putAll(shadowRule.getRelatedShadowDataSourceMappings(each));
                return result;
            }
        }
        return result;
    }
    
    private boolean isContainsShadowInSQLComments(final String tableName, final ShadowRule shadowRule, final ShadowDetermineCondition shadowCondition) {
        return parseSQLComments().filter(each -> isMatchAnyHintShadowAlgorithms(shadowRule.getRelatedHintShadowAlgorithms(tableName), shadowCondition.initSQLComments(each), shadowRule)).isPresent();
    }
    
    private boolean isMatchAnyHintShadowAlgorithms(final Collection<HintShadowAlgorithm<Comparable<?>>> shadowAlgorithms, final ShadowDetermineCondition shadowCondition, final ShadowRule shadowRule) {
        for (HintShadowAlgorithm<Comparable<?>> each : shadowAlgorithms) {
            if (HintShadowAlgorithmDeterminer.isShadow(each, shadowCondition, shadowRule)) {
                return true;
            }
        }
        return false;
    }
    
    private Map<String, String> findByShadowColumn(final Collection<String> relatedShadowTables, final ShadowRule shadowRule, final ShadowOperationType shadowOperationType) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String each : relatedShadowTables) {
            Collection<ColumnShadowAlgorithm<Comparable<?>>> columnShadowAlgorithms = shadowRule.getRelatedColumnShadowAlgorithms(each, shadowOperationType);
            if (!columnShadowAlgorithms.isEmpty() && isMatchAnyColumnShadowAlgorithms(each, columnShadowAlgorithms, shadowRule, shadowOperationType)) {
                return shadowRule.getRelatedShadowDataSourceMappings(each);
            }
        }
        return result;
    }
    
    private boolean isMatchAnyColumnShadowAlgorithms(final String shadowTable, final Collection<ColumnShadowAlgorithm<Comparable<?>>> columnShadowAlgorithms, final ShadowRule shadowRule,
                                                     final ShadowOperationType shadowOperationType) {
        Iterator<Optional<ShadowColumnCondition>> iterator = getShadowColumnConditionIterator();
        ShadowDetermineCondition shadowDetermineCondition;
        while (iterator.hasNext()) {
            Optional<ShadowColumnCondition> next = iterator.next();
            if (next.isPresent()) {
                for (ColumnShadowAlgorithm<Comparable<?>> each : columnShadowAlgorithms) {
                    shadowDetermineCondition = new ShadowDetermineCondition(shadowTable, shadowOperationType);
                    if (ColumnShadowAlgorithmDeterminer.isShadow(each, shadowDetermineCondition.initShadowColumnCondition(next.get()))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Get all tables.
     *
     * @return all tables
     */
    protected abstract Collection<SimpleTableSegment> getAllTables();
    
    /**
     * get shadow operation type.
     *
     * @return shadow operation type
     */
    protected abstract ShadowOperationType getShadowOperationType();
    
    /**
     * Parse SQL Comments.
     *
     * @return SQL comments
     */
    protected abstract Optional<Collection<String>> parseSQLComments();
    
    /**
     * Get shadow column condition iterator.
     *
     * @return shadow column condition iterator
     */
    protected abstract Iterator<Optional<ShadowColumnCondition>> getShadowColumnConditionIterator();
    
    /**
     * Get single table tame.
     *
     * @return table tame
     */
    protected String getSingleTableName() {
        return tableAliasNameMappings.entrySet().iterator().next().getValue();
    }
}
