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

package org.apache.shardingsphere.core;

import org.apache.shardingsphere.api.hint.HintManager;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.constant.properties.ShardingProperties;
import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import org.apache.shardingsphere.core.metadata.ShardingMetaData;
import org.apache.shardingsphere.core.parse.cache.ParsingResultCache;
import org.apache.shardingsphere.core.rewrite.SQLBuilder;
import org.apache.shardingsphere.core.rewrite.SQLRewriteEngine;
import org.apache.shardingsphere.core.route.PreparedStatementRoutingEngine;
import org.apache.shardingsphere.core.route.RouteUnit;
import org.apache.shardingsphere.core.route.SQLLogger;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.SQLUnit;
import org.apache.shardingsphere.core.route.type.TableUnit;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Sharding engine for prepared statement.
 *
 * @author zhangliang
 */
public final class PreparedStatementShardingEngine {
    
    private final String sql; 
    
    private final ShardingRule shardingRule;
    
    private final ShardingProperties shardingProperties;
    
    private final ShardingMetaData metaData;
    
    private final DatabaseType databaseType;
    
    private final PreparedStatementRoutingEngine routingEngine;
    
    public PreparedStatementShardingEngine(final String sql, final ShardingRule shardingRule, final ShardingProperties shardingProperties, 
                                           final ShardingMetaData metaData, final DatabaseType databaseType, final ParsingResultCache cache) {
        this.sql = sql;
        this.shardingRule = shardingRule;
        this.shardingProperties = shardingProperties;
        this.metaData = metaData;
        this.databaseType = databaseType;
        routingEngine = new PreparedStatementRoutingEngine(sql, shardingRule, metaData, databaseType, cache);
    }
    
    /**
     * Shard.
     * 
     * @param parameters SQL parameters
     * @return SQL route result
     */
    public SQLRouteResult shard(final List<Object> parameters) {
        List<Object> rewriteParameters = new ArrayList<>(parameters);
        SQLRouteResult result = routingEngine.route(rewriteParameters);
        result.getRouteUnits().addAll(HintManager.isDatabaseShardingOnly() ? convert(rewriteParameters, result) : rewriteAndConvert(rewriteParameters, result));
        if (shardingProperties.getValue(ShardingPropertiesConstant.SQL_SHOW)) {
            SQLLogger.logSQL(sql, result.getSqlStatement(), result.getRouteUnits());
        }
        return result;
    }
    
    private Collection<RouteUnit> convert(final List<Object> parameters, final SQLRouteResult sqlRouteResult) {
        Collection<RouteUnit> result = new LinkedHashSet<>();
        for (TableUnit each : sqlRouteResult.getRoutingResult().getTableUnits().getTableUnits()) {
            result.add(new RouteUnit(each.getDataSourceName(), new SQLUnit(sql, parameters)));
        }
        return result;
    }
    
    private Collection<RouteUnit> rewriteAndConvert(final List<Object> parameters, final SQLRouteResult sqlRouteResult) {
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, sql, databaseType, sqlRouteResult.getSqlStatement(), parameters, sqlRouteResult.getOptimizeResult());
        SQLBuilder sqlBuilder = rewriteEngine.rewrite(sqlRouteResult.getRoutingResult().isSingleRouting());
        Collection<RouteUnit> result = new LinkedHashSet<>();
        for (TableUnit each : sqlRouteResult.getRoutingResult().getTableUnits().getTableUnits()) {
            result.add(new RouteUnit(each.getDataSourceName(), rewriteEngine.generateSQL(each, sqlBuilder, metaData.getDataSource())));
        }
        return result;
    }
}
