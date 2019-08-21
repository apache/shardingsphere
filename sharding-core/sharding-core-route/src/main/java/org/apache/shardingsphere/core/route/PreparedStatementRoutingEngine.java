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

import org.apache.shardingsphere.core.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.core.parse.SQLParseEngine;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.route.router.masterslave.ShardingMasterSlaveRouter;
import org.apache.shardingsphere.core.route.router.sharding.ShardingRouter;
import org.apache.shardingsphere.core.route.router.sharding.ShardingRouterFactory;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.spi.database.DatabaseType;

import java.util.List;

/**
 * PreparedStatement routing engine.
 * 
 * @author zhangliang
 * @author panjuan
 */
public final class PreparedStatementRoutingEngine {
    
    private final String logicSQL;
    
    private final ShardingRouter shardingRouter;
    
    private final ShardingMasterSlaveRouter masterSlaveRouter;
    
    private SQLStatement sqlStatement;
    
    public PreparedStatementRoutingEngine(final String logicSQL, final ShardingRule shardingRule,
                                          final ShardingSphereMetaData metaData, final DatabaseType databaseType, final SQLParseEngine sqlParseEngine) {
        this.logicSQL = logicSQL;
        shardingRouter = ShardingRouterFactory.newInstance(shardingRule, metaData, databaseType, sqlParseEngine);
        masterSlaveRouter = new ShardingMasterSlaveRouter(shardingRule.getMasterSlaveRules());
    }
    
    /**
     * SQL route.
     * 
     * <p>First routing time will parse SQL, after second time will reuse first parsed result.</p>
     * 
     * @param parameters parameters of SQL placeholder
     * @return route result
     */
    public SQLRouteResult route(final List<Object> parameters) {
        if (null == sqlStatement) {
            sqlStatement = shardingRouter.parse(logicSQL, true);
        }
        return masterSlaveRouter.route(shardingRouter.route(logicSQL, parameters, sqlStatement));
    }
}
