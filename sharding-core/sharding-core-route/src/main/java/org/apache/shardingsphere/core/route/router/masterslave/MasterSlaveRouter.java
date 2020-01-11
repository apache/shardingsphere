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

package org.apache.shardingsphere.core.route.router.masterslave;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.underlying.route.result.RouteResult;
import org.apache.shardingsphere.core.route.SQLLogger;
import org.apache.shardingsphere.core.route.router.DateNodeRouter;
import org.apache.shardingsphere.underlying.route.result.RoutingResult;
import org.apache.shardingsphere.underlying.route.result.RoutingUnit;
import org.apache.shardingsphere.core.rule.MasterSlaveRule;
import org.apache.shardingsphere.sql.parser.SQLParseEngine;

import java.util.List;

/**
 * Master slave router interface.
 * 
 * @author zhangliang
 * @author panjuan
 */
@RequiredArgsConstructor
public final class MasterSlaveRouter implements DateNodeRouter {
    
    private final MasterSlaveRule masterSlaveRule;
    
    private final SQLParseEngine parseEngine;
    
    private final boolean showSQL;
    
    @Override
    public RouteResult route(final String sql, final List<Object> parameters, final boolean useCache) {
        String dataSourceName = new MasterSlaveDataSourceRouter(masterSlaveRule).route(parseEngine.parse(sql, useCache));
        if (showSQL) {
            SQLLogger.logSQL(sql, dataSourceName);
        }
        RouteResult result = new RouteResult(null);
        RoutingResult routingResult = new RoutingResult();
        routingResult.getRoutingUnits().add(new RoutingUnit(dataSourceName));
        result.setRoutingResult(routingResult);
        return result;
    }
}
