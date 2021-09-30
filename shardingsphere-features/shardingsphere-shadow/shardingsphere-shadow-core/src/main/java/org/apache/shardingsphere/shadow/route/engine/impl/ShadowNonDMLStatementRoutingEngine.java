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

package org.apache.shardingsphere.shadow.route.engine.impl;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.shadow.api.shadow.ShadowOperationType;
import org.apache.shardingsphere.shadow.condition.ShadowDetermineCondition;
import org.apache.shardingsphere.shadow.route.engine.ShadowRouteEngine;
import org.apache.shardingsphere.shadow.route.engine.determiner.ShadowDeterminerFactory;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;
import org.apache.shardingsphere.sql.parser.sql.common.statement.AbstractSQLStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * Shadow non-DML statement routing engine.
 */
@RequiredArgsConstructor
public final class ShadowNonDMLStatementRoutingEngine implements ShadowRouteEngine {
    
    private final SQLStatementContext<?> sqlStatementContext;
    
    @Override
    public void route(final RouteContext routeContext, final ShadowRule shadowRule) {
        findShadowDataSourceMappings(shadowRule).ifPresent(stringStringMap -> shadowNonDMLStatementRouteDecorate(routeContext, stringStringMap));
    }
    
    private Optional<Map<String, String>> findShadowDataSourceMappings(final ShadowRule shadowRule) {
        Optional<Collection<String>> sqlNotes = parseSqlNotes();
        if (sqlNotes.isPresent() && isShadowSqlNotes(shadowRule, sqlNotes.get())) {
            return Optional.of(shadowRule.getAllShadowDataSourceMappings());
        }
        return Optional.empty();
    }
    
    private Optional<Collection<String>> parseSqlNotes() {
        Collection<String> result = new LinkedList<>();
        ((AbstractSQLStatement) sqlStatementContext.getSqlStatement()).getCommentSegments().forEach(each -> result.add(each.getText()));
        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }
    
    private boolean isShadowSqlNotes(final ShadowRule shadowRule, final Collection<String> sqlNotes) {
        Optional<Collection<ShadowAlgorithm>> relatedNoteShadowAlgorithms = shadowRule.getRelatedNoteShadowAlgorithms();
        return relatedNoteShadowAlgorithms.filter(shadowAlgorithms -> isMatchNoteAlgorithms(shadowAlgorithms, shadowRule, sqlNotes)).isPresent();
    }
    
    private boolean isMatchNoteAlgorithms(final Collection<ShadowAlgorithm> shadowAlgorithms, final ShadowRule shadowRule, final Collection<String> sqlNotes) {
        for (ShadowAlgorithm each : shadowAlgorithms) {
            if (isMatchNoteAlgorithm(each, shadowRule, sqlNotes)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isMatchNoteAlgorithm(final ShadowAlgorithm shadowAlgorithm, final ShadowRule shadowRule, final Collection<String> sqlNotes) {
        return ShadowDeterminerFactory.newInstance(shadowAlgorithm).isShadow(createShadowDetermineCondition(sqlNotes), shadowRule, "");
    }
    
    private ShadowDetermineCondition createShadowDetermineCondition(final Collection<String> sqlNotes) {
        ShadowDetermineCondition shadowDetermineCondition = new ShadowDetermineCondition(ShadowOperationType.NON_DML);
        shadowDetermineCondition.initSqlNotes(sqlNotes);
        return shadowDetermineCondition;
    }
    
    private void shadowNonDMLStatementRouteDecorate(final RouteContext routeContext, final Map<String, String> shadowDataSourceMappings) {
        Collection<RouteUnit> routeUnits = routeContext.getRouteUnits();
        Collection<RouteUnit> toBeAdded = new LinkedList<>();
        for (RouteUnit each : routeUnits) {
            RouteMapper dataSourceMapper = each.getDataSourceMapper();
            String shadowActualName = shadowDataSourceMappings.get(dataSourceMapper.getActualName());
            if (null != shadowActualName) {
                toBeAdded.add(new RouteUnit(new RouteMapper(dataSourceMapper.getLogicName(), shadowActualName), each.getTableMappers()));
            }
        }
        routeUnits.clear();
        routeUnits.addAll(toBeAdded);
    }
}
