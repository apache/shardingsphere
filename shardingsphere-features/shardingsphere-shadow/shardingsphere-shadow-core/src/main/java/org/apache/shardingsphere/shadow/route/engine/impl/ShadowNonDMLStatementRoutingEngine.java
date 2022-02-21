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
import org.apache.shardingsphere.shadow.api.shadow.ShadowOperationType;
import org.apache.shardingsphere.shadow.api.shadow.hint.HintShadowAlgorithm;
import org.apache.shardingsphere.shadow.condition.ShadowDetermineCondition;
import org.apache.shardingsphere.shadow.route.engine.ShadowRouteEngine;
import org.apache.shardingsphere.shadow.route.engine.determiner.HintShadowAlgorithmDeterminer;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.CommentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.AbstractSQLStatement;

import java.util.Collection;
import java.util.LinkedHashMap;
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
    public void route(final RouteContext routeContext, final ShadowRule shadowRule) {
        shadowRouteDecorate(routeContext, shadowRule, findShadowDataSourceMappings(shadowRule));
    }
    
    private Map<String, String> findShadowDataSourceMappings(final ShadowRule shadowRule) {
        Map<String, String> result = new LinkedHashMap<>();
        Optional<Collection<String>> sqlComments = parseSQLComments();
        if (!sqlComments.isPresent()) {
            return result;
        }
        if (isMatchAnyNoteShadowAlgorithms(shadowRule, createShadowDetermineCondition(sqlComments.get()))) {
            return shadowRule.getAllShadowDataSourceMappings();
        }
        return result;
    }
    
    private Optional<Collection<String>> parseSQLComments() {
        Collection<String> result = ((AbstractSQLStatement) sqlStatementContext.getSqlStatement()).getCommentSegments().stream().map(CommentSegment::getText)
                .collect(Collectors.toCollection(LinkedList::new));
        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }
    
    private ShadowDetermineCondition createShadowDetermineCondition(final Collection<String> sqlComments) {
        ShadowDetermineCondition result = new ShadowDetermineCondition("", ShadowOperationType.HINT_MATCH);
        return result.initSQLComments(sqlComments);
    }
    
    private boolean isMatchAnyNoteShadowAlgorithms(final ShadowRule shadowRule, final ShadowDetermineCondition shadowCondition) {
        for (HintShadowAlgorithm<Comparable<?>> each : shadowRule.getAllHintShadowAlgorithms()) {
            if (HintShadowAlgorithmDeterminer.isShadow(each, shadowCondition, shadowRule)) {
                return true;
            }
        }
        return false;
    }
}
