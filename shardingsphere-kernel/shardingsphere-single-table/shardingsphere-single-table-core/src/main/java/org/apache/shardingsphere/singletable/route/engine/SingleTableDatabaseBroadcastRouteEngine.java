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

package org.apache.shardingsphere.singletable.route.engine;

import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;

import java.util.Collections;

/**
 * Single table databases broadcast route engine.
 */
public final class SingleTableDatabaseBroadcastRouteEngine implements SingleTableRouteEngine {
    
    @Override
    public void route(final RouteContext routeContext, final SingleTableRule singleTableRule) {
        for (String each : singleTableRule.getDataSourceNames()) {
            routeContext.getRouteUnits().add(new RouteUnit(new RouteMapper(each, each), Collections.emptyList()));
        }
    }
}
