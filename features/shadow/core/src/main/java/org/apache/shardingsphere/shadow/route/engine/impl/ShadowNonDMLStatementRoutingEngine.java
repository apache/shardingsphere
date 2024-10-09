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
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.shadow.condition.ShadowDetermineCondition;
import org.apache.shardingsphere.shadow.route.engine.ShadowRouteEngine;
import org.apache.shardingsphere.shadow.route.engine.determiner.HintShadowAlgorithmDeterminer;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.spi.ShadowOperationType;
import org.apache.shardingsphere.shadow.spi.hint.HintShadowAlgorithm;

import java.util.Collections;
import java.util.Map;

/**
 * Shadow non-DML statement routing engine.
 */
@RequiredArgsConstructor
public final class ShadowNonDMLStatementRoutingEngine implements ShadowRouteEngine {
    
    private final HintValueContext hintValueContext;
    
    @Override
    public void route(final RouteContext routeContext, final ShadowRule rule) {
        decorateRouteContext(routeContext, rule, findShadowDataSourceMappings(rule));
    }
    
    private Map<String, String> findShadowDataSourceMappings(final ShadowRule rule) {
        if (!hintValueContext.isShadow()) {
            return Collections.emptyMap();
        }
        if (isMatchAnyNoteShadowAlgorithms(rule, new ShadowDetermineCondition("", ShadowOperationType.HINT_MATCH))) {
            return rule.getAllShadowDataSourceMappings();
        }
        return Collections.emptyMap();
    }
    
    private boolean isMatchAnyNoteShadowAlgorithms(final ShadowRule rule, final ShadowDetermineCondition shadowCondition) {
        for (HintShadowAlgorithm<Comparable<?>> each : rule.getAllHintShadowAlgorithms()) {
            if (HintShadowAlgorithmDeterminer.isShadow(each, shadowCondition, rule, hintValueContext.isShadow())) {
                return true;
            }
        }
        return false;
    }
}
