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

package org.apache.shardingsphere.shadow.route.finder.dml;

import lombok.Getter;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.TableAvailable;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.shadow.condition.ShadowColumnCondition;
import org.apache.shardingsphere.shadow.condition.ShadowCondition;
import org.apache.shardingsphere.shadow.route.determiner.ColumnShadowAlgorithmDeterminer;
import org.apache.shardingsphere.shadow.route.determiner.HintShadowAlgorithmDeterminer;
import org.apache.shardingsphere.shadow.route.finder.ShadowDataSourceMappingsFinder;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;
import org.apache.shardingsphere.shadow.spi.ShadowOperationType;
import org.apache.shardingsphere.shadow.spi.column.ColumnShadowAlgorithm;
import org.apache.shardingsphere.shadow.spi.hint.HintShadowAlgorithm;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Abstract shadow DML statement data source mappings finder.
 */
@HighFrequencyInvocation
public abstract class AbstractShadowDMLStatementDataSourceMappingsFinder implements ShadowDataSourceMappingsFinder {
    
    private final ShadowOperationType operationType;
    
    private final boolean isShadow;
    
    @Getter
    private final Map<String, String> tableAliasAndNameMappings;
    
    protected AbstractShadowDMLStatementDataSourceMappingsFinder(final SQLStatementContext sqlStatementContext, final HintValueContext hintValueContext, final ShadowOperationType operationType) {
        this.operationType = operationType;
        isShadow = hintValueContext.isShadow();
        tableAliasAndNameMappings = getTableAliasAndNameMappings(((TableAvailable) sqlStatementContext).getTablesContext().getSimpleTables());
    }
    
    private Map<String, String> getTableAliasAndNameMappings(final Collection<SimpleTableSegment> tableSegments) {
        Map<String, String> result = new LinkedHashMap<>(tableSegments.size(), 1F);
        for (SimpleTableSegment each : tableSegments) {
            String tableName = each.getTableName().getIdentifier().getValue();
            String alias = each.getAliasName().isPresent() ? each.getAliasName().get() : tableName;
            result.put(alias, tableName);
        }
        return result;
    }
    
    @Override
    public Map<String, String> find(final ShadowRule rule) {
        Collection<String> shadowTables = rule.filterShadowTables(tableAliasAndNameMappings.values());
        if (shadowTables.isEmpty() && isMatchDefaultAlgorithm(rule)) {
            return rule.getAllShadowDataSourceMappings();
        }
        Map<String, String> result = findBySQLHints(rule, shadowTables);
        return result.isEmpty() ? findByShadowColumn(rule, shadowTables) : result;
    }
    
    @SuppressWarnings("unchecked")
    private boolean isMatchDefaultAlgorithm(final ShadowRule rule) {
        Optional<ShadowAlgorithm> defaultAlgorithm = rule.getDefaultShadowAlgorithm();
        if (defaultAlgorithm.isPresent() && defaultAlgorithm.get() instanceof HintShadowAlgorithm<?>) {
            ShadowCondition shadowCondition = new ShadowCondition("", ShadowOperationType.HINT_MATCH);
            return HintShadowAlgorithmDeterminer.isShadow((HintShadowAlgorithm<Comparable<?>>) defaultAlgorithm.get(), shadowCondition, rule, isShadow);
        }
        return false;
    }
    
    private Map<String, String> findBySQLHints(final ShadowRule rule, final Collection<String> relatedShadowTables) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String each : relatedShadowTables) {
            if (isContainsShadowInSQLHints(rule, each, new ShadowCondition(each, operationType))) {
                result.putAll(rule.getShadowDataSourceMappings(each));
                return result;
            }
        }
        return result;
    }
    
    private boolean isContainsShadowInSQLHints(final ShadowRule rule, final String tableName, final ShadowCondition shadowCondition) {
        for (HintShadowAlgorithm<Comparable<?>> each : rule.getHintShadowAlgorithms(tableName)) {
            if (HintShadowAlgorithmDeterminer.isShadow(each, shadowCondition, rule, isShadow)) {
                return true;
            }
        }
        return false;
    }
    
    private Map<String, String> findByShadowColumn(final ShadowRule rule, final Collection<String> relatedShadowTables) {
        for (String each : relatedShadowTables) {
            Collection<String> shadowColumnNames = rule.getShadowColumnNames(operationType, each);
            if (!shadowColumnNames.isEmpty() && isMatchAnyColumnShadowAlgorithms(rule, each, shadowColumnNames)) {
                return rule.getShadowDataSourceMappings(each);
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
        Collection<ColumnShadowAlgorithm<Comparable<?>>> columnShadowAlgorithms = rule.getColumnShadowAlgorithms(operationType, shadowTable, shadowColumn);
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
            if (ColumnShadowAlgorithmDeterminer.isShadow(each, new ShadowCondition(shadowTable, operationType, condition))) {
                return true;
            }
        }
        return false;
    }
    
    protected abstract Collection<ShadowColumnCondition> getShadowColumnConditions(String shadowColumnName);
    
    protected final String getSingleTableName() {
        return tableAliasAndNameMappings.entrySet().iterator().next().getValue();
    }
}
