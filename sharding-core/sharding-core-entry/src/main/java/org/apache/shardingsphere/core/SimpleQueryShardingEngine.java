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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.api.hint.HintManager;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.constant.properties.ShardingProperties;
import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import org.apache.shardingsphere.core.metadata.ShardingMetaData;
import org.apache.shardingsphere.core.parse.cache.ParsingResultCache;
import org.apache.shardingsphere.core.rewrite.SQLBuilder;
import org.apache.shardingsphere.core.rewrite.SQLRewriteEngine;
import org.apache.shardingsphere.core.route.RouteUnit;
import org.apache.shardingsphere.core.route.SQLLogger;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.SQLUnit;
import org.apache.shardingsphere.core.route.StatementRoutingEngine;
import org.apache.shardingsphere.core.route.type.TableUnit;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

/**
 * Sharding engine for simple query.
 * 
 * <pre>
 *     Simple query:  
 *       for JDBC is Statement; 
 *       for MyQL is COM_QUERY; 
 *       for PostgreSQL is Simple Query;
 * </pre>
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class SimpleQueryShardingEngine {
    
    private final ShardingRule shardingRule;
    
    private final ShardingProperties shardingProperties;
    
    private final ShardingMetaData metaData;
    
    private final DatabaseType databaseType;
    
    private final ParsingResultCache cache;
    
    /**
     * Shard.
     *
     * @param sql SQL
     * @return SQL route result
     */
    public SQLRouteResult shard(final String sql) {
        SQLRouteResult result = new StatementRoutingEngine(shardingRule, metaData, databaseType, cache).route(sql);
        result.getRouteUnits().addAll(HintManager.isDatabaseShardingOnly() ? convert(sql, result) : rewriteAndConvert(sql, result));
        if (shardingProperties.getValue(ShardingPropertiesConstant.SQL_SHOW)) {
            SQLLogger.logSQL(sql, result.getSqlStatement(), result.getRouteUnits());
        }
        return result;
    }
    
    private Collection<RouteUnit> convert(final String sql, final SQLRouteResult sqlRouteResult) {
        Collection<RouteUnit> result = new LinkedHashSet<>();
        for (TableUnit each : sqlRouteResult.getRoutingResult().getTableUnits().getTableUnits()) {
            result.add(new RouteUnit(each.getDataSourceName(), new SQLUnit(sql, Collections.emptyList())));
        }
        return result;
    }
    
    private Collection<RouteUnit> rewriteAndConvert(final String sql, final SQLRouteResult sqlRouteResult) {
        SQLRewriteEngine rewriteEngine = new SQLRewriteEngine(shardingRule, sql, databaseType, sqlRouteResult.getSqlStatement(), Collections.emptyList(), sqlRouteResult.getOptimizeResult());
        SQLBuilder sqlBuilder = rewriteEngine.rewrite(sqlRouteResult.getRoutingResult().isSingleRouting());
        Collection<RouteUnit> result = new LinkedHashSet<>();
        for (TableUnit each : sqlRouteResult.getRoutingResult().getTableUnits().getTableUnits()) {
            result.add(new RouteUnit(each.getDataSourceName(), rewriteEngine.generateSQL(each, sqlBuilder, metaData.getDataSource())));
        }
        return result;
    }
}
