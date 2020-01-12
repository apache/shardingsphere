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

package org.apache.shardingsphere.core.shard;

import org.apache.shardingsphere.core.route.ShardingRouteContext;
import org.apache.shardingsphere.core.route.router.masterslave.MasterSlaveRouteDecorator;
import org.apache.shardingsphere.core.route.router.sharding.ShardingRouter;
import org.apache.shardingsphere.core.rule.MasterSlaveRule;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.SQLParseEngine;
import org.apache.shardingsphere.underlying.common.constant.properties.ShardingSphereProperties;
import org.apache.shardingsphere.underlying.common.metadata.ShardingSphereMetaData;

import java.util.ArrayList;
import java.util.List;

/**
 * Sharding engine for prepared query.
 * 
 * <pre>
 *     Prepared query:  
 *       for JDBC is PreparedStatement; 
 *       for MyQL is COM_STMT; 
 *       for PostgreSQL is Extended Query;
 * </pre>
 * 
 * @author zhangliang
 */
public final class PreparedQueryShardingEngine extends BaseShardingEngine {
    
    private final ShardingRule shardingRule;
    
    private final ShardingRouter shardingRouter;
    
    public PreparedQueryShardingEngine(final ShardingRule shardingRule, final ShardingSphereProperties properties, final ShardingSphereMetaData metaData, final SQLParseEngine sqlParseEngine) {
        super(shardingRule, properties, metaData);
        this.shardingRule = shardingRule;
        shardingRouter = new ShardingRouter(shardingRule, metaData, sqlParseEngine);
    }
    
    @Override
    protected List<Object> cloneParameters(final List<Object> parameters) {
        return new ArrayList<>(parameters);
    }
    
    @Override
    protected ShardingRouteContext route(final String sql, final List<Object> parameters) {
        ShardingRouteContext result = shardingRouter.route(sql, parameters, true);
        for (MasterSlaveRule each : shardingRule.getMasterSlaveRules()) {
            result = (ShardingRouteContext) new MasterSlaveRouteDecorator(each).decorate(result);
        }
        return result;
    }
}
