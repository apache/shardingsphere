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

package org.apache.shardingsphere.masterslave.route.engine;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.rule.MasterSlaveRule;
import org.apache.shardingsphere.masterslave.route.engine.impl.MasterSlaveDataSourceRouter;
import org.apache.shardingsphere.masterslave.route.log.MasterSlaveSQLLogger;
import org.apache.shardingsphere.sql.parser.SQLParseEngine;
import org.apache.shardingsphere.underlying.route.DateNodeRouter;
import org.apache.shardingsphere.underlying.route.context.RouteContext;
import org.apache.shardingsphere.underlying.route.context.RouteResult;
import org.apache.shardingsphere.underlying.route.context.RouteUnit;

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
    public RouteContext route(final String sql, final List<Object> parameters, final boolean useCache) {
        String dataSourceName = new MasterSlaveDataSourceRouter(masterSlaveRule).route(parseEngine.parse(sql, useCache));
        if (showSQL) {
            MasterSlaveSQLLogger.logSQL(sql, dataSourceName);
        }
        RouteResult routeResult = new RouteResult();
        routeResult.getRouteUnits().add(new RouteUnit(dataSourceName));
        return new RouteContext(null, routeResult);
    }
}
