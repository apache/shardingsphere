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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Abstract shadow DML statement route engine.
 */
@Getter
public abstract class AbstractShadowDMLStatementRouteEngine implements ShadowRouteEngine {
    
    private final Map<String, String> tableAliasNameMappings = new LinkedHashMap<>();
    
    @Override
    public final void route(final RouteContext routeContext, final ShadowRule rule) {
        tableAliasNameMappings.putAll(getTableAliasNameMappings(getAllTables()));
        decorateRouteContext(routeContext, rule, findShadowDataSourceMappings(rule));
    }
    
    private Map<String, String> getTableAliasNameMappings(final Collection<SimpleTableSegment> tableSegments) {
        Map<String, String> result = new LinkedHashMap<>();
        for (SimpleTableSegment each : tableSegments) {
            String tableName = each.getTableName().getIdentifier().getValue();
            String alias = each.getAlias().isPresent() ? each.getAlias().get() : tableName;
            result.put(alias, tableName);
        }
        return result;
    }
    
    private Map<String, String> findShadowDataSourceMappings(final ShadowRule rule) {
        Collection<String> relatedShadowTables = rule.getRelatedShadowTables(tableAliasNameMappings.values());
        if (relatedShadowTables.isEmpty() && isMatchDefaultAlgorithm(rule)) {
            return rule.getAllShadowDataSourceMappings();
        }
        ShadowOperationType shadowOperationType = getShadowOperationType();
        Map<String, String> result = findBySQLComments(rule, relatedShadowTables, shadowOperationType);
        return result.isEmpty() ? findByShadowColumn(rule, relatedShadowTables, shadowOperationType) : result;
    }
    
    @SuppressWarnings("unchecked")
    private boolean isMatchDefaultAlgorithm(final ShadowRule rule) {
        Optional<Collection<String>> sqlComments = parseSQLComments();
        if (!sqlComments.isPresent()) {
            return false;
        }
        Optional<ShadowAlgorithm> defaultAlgorithm = rule.getDefaultShadowAlgorithm();
        if (defaultAlgorithm.isPresent() && defaultAlgorithm.get() instanceof HintShadowAlgorithm<?>) {
            ShadowDetermineCondition determineCondition = new ShadowDetermineCondition("", ShadowOperationType.HINT_MATCH);
            return HintShadowAlgorithmDeterminer.isShadow((HintShadowAlgorithm<Comparable<?>>) defaultAlgorithm.get(), determineCondition.initSQLComments(sqlComments.get()), rule);
        }
        return false;
    }
    
    private Map<String, String> findBySQLComments(final ShadowRule rule, final Collection<String> relatedShadowTables, final ShadowOperationType shadowOperationType) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String each : relatedShadowTables) {
            if (isContainsShadowInSQLComments(rule, each, new ShadowDetermineCondition(each, shadowOperationType))) {
                result.putAll(rule.getRelatedShadowDataSourceMappings(each));
                return result;
            }
        }
        return result;
    }
    
    private boolean isContainsShadowInSQLComments(final ShadowRule rule, final String tableName, final ShadowDetermineCondition shadowCondition) {
        return parseSQLComments().filter(each -> isMatchAnyHintShadowAlgorithms(rule, tableName, shadowCondition.initSQLComments(each))).isPresent();
    }
    
    private boolean isMatchAnyHintShadowAlgorithms(final ShadowRule rule, final String tableName, final ShadowDetermineCondition shadowCondition) {
        for (HintShadowAlgorithm<Comparable<?>> each : rule.getRelatedHintShadowAlgorithms(tableName)) {
            if (HintShadowAlgorithmDeterminer.isShadow(each, shadowCondition, rule)) {
                return true;
            }
        }
        return false;
    }
    
    private Map<String, String> findByShadowColumn(final ShadowRule rule, final Collection<String> relatedShadowTables, final ShadowOperationType shadowOperationType) {
        for (String each : relatedShadowTables) {
            Collection<String> relatedShadowColumnNames = rule.getRelatedShadowColumnNames(shadowOperationType, each);
            if (!relatedShadowColumnNames.isEmpty() && isMatchAnyColumnShadowAlgorithms(rule, each, relatedShadowColumnNames, shadowOperationType)) {
                return rule.getRelatedShadowDataSourceMappings(each);
            }
        }
        return Collections.emptyMap();
    }
    
    private boolean isMatchAnyColumnShadowAlgorithms(final ShadowRule rule, final String shadowTable, final Collection<String> shadowColumnNames, final ShadowOperationType shadowOperation) {
        for (String each : shadowColumnNames) {
            if (isMatchAnyColumnShadowAlgorithms(rule, shadowTable, each, shadowOperation)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isMatchAnyColumnShadowAlgorithms(final ShadowRule rule, final String shadowTable, final String shadowColumn, final ShadowOperationType shadowOperationType) {
        Collection<ColumnShadowAlgorithm<Comparable<?>>> columnShadowAlgorithms = rule.getRelatedColumnShadowAlgorithms(shadowOperationType, shadowTable, shadowColumn);
        if (columnShadowAlgorithms.isEmpty()) {
            return false;
        }
        Iterator<Optional<ShadowColumnCondition>> iterator = getShadowColumnConditionIterator(shadowColumn);
        ShadowDetermineCondition shadowDetermineCondition;
        while (iterator.hasNext()) {
            Optional<ShadowColumnCondition> next = iterator.next();
            if (!next.isPresent()) {
                continue;
            }
            for (ColumnShadowAlgorithm<Comparable<?>> each : columnShadowAlgorithms) {
                shadowDetermineCondition = new ShadowDetermineCondition(shadowTable, shadowOperationType);
                if (ColumnShadowAlgorithmDeterminer.isShadow(each, shadowDetermineCondition.initShadowColumnCondition(next.get()))) {
                    return true;
                }
            }
        }
        return false;
    }
    
    protected abstract Collection<SimpleTableSegment> getAllTables();
    
    protected abstract ShadowOperationType getShadowOperationType();
    
    protected abstract Optional<Collection<String>> parseSQLComments();
    
    protected abstract Iterator<Optional<ShadowColumnCondition>> getShadowColumnConditionIterator(String shadowColumn);
    
    protected final String getSingleTableName() {
        return tableAliasNameMappings.entrySet().iterator().next().getValue();
    }
}
