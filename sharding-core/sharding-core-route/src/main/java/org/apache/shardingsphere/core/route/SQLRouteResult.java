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

package org.apache.shardingsphere.core.route;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.core.optimize.GeneratedKey;
import org.apache.shardingsphere.core.optimize.result.OptimizeResult;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.old.parser.context.limit.Limit;
import org.apache.shardingsphere.core.route.type.RoutingResult;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * SQL route result.
 * 
 * @author gaohongtao
 * @author zhangliang
 * @author zhaojun
 */
@RequiredArgsConstructor
@Getter
@Setter
public final class SQLRouteResult {
    
    private final SQLStatement sqlStatement;
    
    private final GeneratedKey generatedKey;
    
    // For multiple thread read cached sqlStatement, clone limit on SQLRouteResult, because limit will be modified after cache
    // TODO need more good design here
    private Limit limit;
    
    private RoutingResult routingResult;
    
    private OptimizeResult optimizeResult;
    
    private final Collection<RouteUnit> routeUnits = new LinkedHashSet<>();
    
    public SQLRouteResult(final SQLStatement sqlStatement) {
        this(sqlStatement, null);
    }
}
