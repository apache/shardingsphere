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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.TableAvailable;
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
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.CommentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.AbstractSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Abstract shadow DML statement route engine.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public abstract class AbstractShadowDMLStatementRouteEngine implements ShadowRouteEngine {
    
    private final SQLStatementContext sqlStatementContext;
    
    private final ShadowOperationType operationType;
    
    private final Map<String, String> tableAliasNameMappings = new LinkedHashMap<>();
    
    @Override
    public final void route(final RouteContext routeContext, final ShadowRule rule) {
        tableAliasNameMappings.putAll(getTableAliasNameMappings(((TableAvailable) sqlStatementContext).getAllTables()));
        decorateRouteContext(routeContext, rule, findShadowDataSourceMappings(rule));
    }
    
    private Map<String, String> getTableAliasNameMappings(final Collection<SimpleTableSegment> tableSegments) {
        Map<String, String> result = new LinkedHashMap<>();
        for (SimpleTableSegment each : tableSegments) {
            String tableName = each.getTableName().getIdentifier().getValue();
            String alias = each.getAliasName().isPresent() ? each.getAliasName().get() : tableName;
            result.put(alias, tableName);
        }
        return result;
    }
    
    private Map<String, String> findShadowDataSourceMappings(final ShadowRule rule) {
        Collection<String> relatedShadowTables = rule.getRelatedShadowTables(tableAliasNameMappings.values());
        Collection<String> sqlComments = getSQLComments();
        if (relatedShadowTables.isEmpty() && isMatchDefaultAlgorithm(rule, sqlComments)) {
            return rule.getAllShadowDataSourceMappings();
        }
        Map<String, String> result = findBySQLComments(rule, sqlComments, relatedShadowTables);
        return result.isEmpty() ? findByShadowColumn(rule, relatedShadowTables) : result;
    }
    
    private Collection<String> getSQLComments() {
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        return ((AbstractSQLStatement) sqlStatement).getCommentSegments().stream().map(CommentSegment::getText).collect(Collectors.toList());
    }
    
    @SuppressWarnings("unchecked")
    private boolean isMatchDefaultAlgorithm(final ShadowRule rule, final Collection<String> sqlComments) {
        Optional<ShadowAlgorithm> defaultAlgorithm = rule.getDefaultShadowAlgorithm();
        if (defaultAlgorithm.isPresent() && defaultAlgorithm.get() instanceof HintShadowAlgorithm<?>) {
            ShadowDetermineCondition determineCondition = new ShadowDetermineCondition("", ShadowOperationType.HINT_MATCH);
            return HintShadowAlgorithmDeterminer.isShadow((HintShadowAlgorithm<Comparable<?>>) defaultAlgorithm.get(), determineCondition.initSQLComments(sqlComments), rule);
        }
        return false;
    }
    
    private Map<String, String> findBySQLComments(final ShadowRule rule, final Collection<String> sqlComments, final Collection<String> relatedShadowTables) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String each : relatedShadowTables) {
            if (isContainsShadowInSQLComments(rule, each, sqlComments, new ShadowDetermineCondition(each, operationType))) {
                result.putAll(rule.getRelatedShadowDataSourceMappings(each));
                return result;
            }
        }
        return result;
    }
    
    private boolean isContainsShadowInSQLComments(final ShadowRule rule, final String tableName, final Collection<String> sqlComments, final ShadowDetermineCondition shadowCondition) {
        ShadowDetermineCondition shadowConditionWithComments = shadowCondition.initSQLComments(sqlComments);
        for (HintShadowAlgorithm<Comparable<?>> each : rule.getRelatedHintShadowAlgorithms(tableName)) {
            if (HintShadowAlgorithmDeterminer.isShadow(each, shadowConditionWithComments, rule)) {
                return true;
            }
        }
        return false;
    }
    
    private Map<String, String> findByShadowColumn(final ShadowRule rule, final Collection<String> relatedShadowTables) {
        for (String each : relatedShadowTables) {
            Collection<String> relatedShadowColumnNames = rule.getRelatedShadowColumnNames(operationType, each);
            if (!relatedShadowColumnNames.isEmpty() && isMatchAnyColumnShadowAlgorithms(rule, each, relatedShadowColumnNames)) {
                return rule.getRelatedShadowDataSourceMappings(each);
            }
        }
        return Collections.emptyMap();
    }
    
    private boolean isMatchAnyColumnShadowAlgorithms(final ShadowRule rule, final String shadowTable, final Collection<String> shadowColumnNames) {
        for (String each : shadowColumnNames) {
            if (isMatchAnyColumnShadowAlgorithms(rule, shadowTable, each)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isMatchAnyColumnShadowAlgorithms(final ShadowRule rule, final String shadowTable, final String shadowColumn) {
        Collection<ColumnShadowAlgorithm<Comparable<?>>> columnShadowAlgorithms = rule.getRelatedColumnShadowAlgorithms(operationType, shadowTable, shadowColumn);
        if (columnShadowAlgorithms.isEmpty()) {
            return false;
        }
        for (ShadowColumnCondition each : getShadowColumnConditions(shadowColumn)) {
            if (isMatchColumnShadowAlgorithm(shadowTable, columnShadowAlgorithms, each)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isMatchColumnShadowAlgorithm(final String shadowTable, final Collection<ColumnShadowAlgorithm<Comparable<?>>> algorithms, final ShadowColumnCondition condition) {
        for (ColumnShadowAlgorithm<Comparable<?>> each : algorithms) {
            if (ColumnShadowAlgorithmDeterminer.isShadow(each, new ShadowDetermineCondition(shadowTable, operationType).initShadowColumnCondition(condition))) {
                return true;
            }
        }
        return false;
    }
    
    protected abstract Collection<ShadowColumnCondition> getShadowColumnConditions(String shadowColumnName);
    
    protected final String getSingleTableName() {
        return tableAliasNameMappings.entrySet().iterator().next().getValue();
    }
}
