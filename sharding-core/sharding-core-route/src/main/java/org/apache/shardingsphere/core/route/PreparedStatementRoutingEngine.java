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

import org.apache.shardingsphere.core.route.router.masterslave.MasterSlaveRouteDecorator;
import org.apache.shardingsphere.core.route.router.sharding.ShardingRouter;
import org.apache.shardingsphere.core.rule.MasterSlaveRule;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.SQLParseEngine;
import org.apache.shardingsphere.underlying.common.metadata.ShardingSphereMetaData;

import java.util.List;

/**
 * PreparedStatement routing engine.
 * 
 * @author zhangliang
 * @author panjuan
 */
public final class PreparedStatementRoutingEngine {
    
    private final String logicSQL;
    
    private final ShardingRule shardingRule;
    
    private final ShardingRouter shardingRouter;
    
    public PreparedStatementRoutingEngine(final String logicSQL, final ShardingRule shardingRule, final ShardingSphereMetaData metaData, final SQLParseEngine sqlParseEngine) {
        this.logicSQL = logicSQL;
        this.shardingRule = shardingRule;
        shardingRouter = new ShardingRouter(shardingRule, metaData, sqlParseEngine);
    }
    
    /**
     * SQL route.
     * 
     * @param parameters SQL parameters
     * @return route result
     */
    public ShardingRouteResult route(final List<Object> parameters) {
        ShardingRouteResult result = shardingRouter.route(logicSQL, parameters, true);
        for (MasterSlaveRule each : shardingRule.getMasterSlaveRules()) {
            result = (ShardingRouteResult) new MasterSlaveRouteDecorator(each).decorate(result);
        }
        return result;
    }
}
