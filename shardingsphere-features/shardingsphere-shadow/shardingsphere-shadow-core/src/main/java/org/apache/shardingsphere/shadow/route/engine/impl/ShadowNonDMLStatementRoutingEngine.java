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
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.shadow.api.shadow.ShadowOperationType;
import org.apache.shardingsphere.shadow.api.shadow.note.NoteShadowAlgorithm;
import org.apache.shardingsphere.shadow.condition.ShadowDetermineCondition;
import org.apache.shardingsphere.shadow.route.engine.ShadowRouteEngine;
import org.apache.shardingsphere.shadow.route.engine.determiner.ShadowDeterminerFactory;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.CommentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.AbstractSQLStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Shadow non-DML statement routing engine.
 */
@RequiredArgsConstructor
public final class ShadowNonDMLStatementRoutingEngine implements ShadowRouteEngine {
    
    private final SQLStatementContext<?> sqlStatementContext;
    
    @Override
    public void route(final RouteContext routeContext, final ShadowRule shadowRule, final ConfigurationProperties props) {
        if (Boolean.parseBoolean(String.valueOf(props.getProps().get(ConfigurationPropertyKey.SQL_COMMENT_PARSE_ENABLED.getKey())))) {
            findShadowDataSourceMappings(shadowRule).ifPresent(stringStringMap -> shadowNonDMLStatementRouteDecorate(routeContext, stringStringMap));
        }
    }
    
    private Optional<Map<String, String>> findShadowDataSourceMappings(final ShadowRule shadowRule) {
        Optional<Collection<String>> sqlNotes = parseSqlNotes();
        if (!sqlNotes.isPresent()) {
            return Optional.empty();
        }
        Optional<Collection<NoteShadowAlgorithm<Comparable<?>>>> noteShadowAlgorithms = shadowRule.getAllNoteShadowAlgorithms();
        if (!noteShadowAlgorithms.isPresent()) {
            return Optional.empty();
        }
        if (isMatchAnyNoteShadowAlgorithms(noteShadowAlgorithms.get(), createShadowDetermineCondition(sqlNotes.get()), shadowRule)) {
            return Optional.of(shadowRule.getAllShadowDataSourceMappings());
        }
        return Optional.empty();
    }
    
    private Optional<Collection<String>> parseSqlNotes() {
        Collection<String> result = ((AbstractSQLStatement) sqlStatementContext.getSqlStatement()).getCommentSegments().stream().map(CommentSegment::getText)
                .collect(Collectors.toCollection(LinkedList::new));
        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }
    
    private ShadowDetermineCondition createShadowDetermineCondition(final Collection<String> sqlNotes) {
        ShadowDetermineCondition result = new ShadowDetermineCondition("", ShadowOperationType.NON_DML);
        return result.initSqlNotes(sqlNotes);
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
