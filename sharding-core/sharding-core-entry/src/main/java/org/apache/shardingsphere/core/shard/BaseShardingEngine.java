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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.rule.MasterSlaveRule;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.encrypt.rewrite.context.EncryptSQLRewriteContextDecorator;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.masterslave.route.engine.MasterSlaveRouteDecorator;
import org.apache.shardingsphere.sharding.rewrite.context.ShardingSQLRewriteContextDecorator;
import org.apache.shardingsphere.sharding.route.engine.ShardingRouteDecorator;
import org.apache.shardingsphere.sql.parser.SQLParserEngine;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.underlying.common.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;
import org.apache.shardingsphere.underlying.executor.context.ExecutionContext;
import org.apache.shardingsphere.underlying.executor.context.ExecutionUnit;
import org.apache.shardingsphere.underlying.executor.context.SQLUnit;
import org.apache.shardingsphere.underlying.executor.log.SQLLogger;
import org.apache.shardingsphere.underlying.rewrite.SQLRewriteEntry;
import org.apache.shardingsphere.underlying.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.underlying.rewrite.engine.SQLRewriteEngine;
import org.apache.shardingsphere.underlying.rewrite.engine.SQLRewriteResult;
import org.apache.shardingsphere.underlying.rewrite.engine.SQLRouteRewriteEngine;
import org.apache.shardingsphere.underlying.route.DataNodeRouter;
import org.apache.shardingsphere.underlying.route.context.RouteContext;
import org.apache.shardingsphere.underlying.route.context.RouteUnit;
import org.apache.shardingsphere.underlying.route.hook.SPIRoutingHook;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;

/**
 * Base sharding engine.
 */
@RequiredArgsConstructor
public abstract class BaseShardingEngine {
    
    private final Collection<BaseRule> rules;
    
    private final ConfigurationProperties properties;
    
    private final ShardingSphereMetaData metaData;
    
    private final DataNodeRouter dataNodeRouter;
    
    private final SQLRewriteEntry sqlRewriteEntry;
    
    private final SPIRoutingHook routingHook;
    
    public BaseShardingEngine(final Collection<BaseRule> rules, final ConfigurationProperties properties, final ShardingSphereMetaData metaData, final SQLParserEngine sqlParserEngine) {
        this.rules = rules;
        this.properties = properties;
        this.metaData = metaData;
        dataNodeRouter = new DataNodeRouter(metaData, properties, sqlParserEngine);
        sqlRewriteEntry = new SQLRewriteEntry(metaData.getSchema(), properties);
        routingHook = new SPIRoutingHook();
    }
    
    /**
     * Shard.
     *
     * @param sql SQL
     * @param parameters SQL parameters
     * @return execution context
     */
    public ExecutionContext shard(final String sql, final List<Object> parameters) {
        List<Object> clonedParameters = cloneParameters(parameters);
        RouteContext routeContext = executeRoute(sql, clonedParameters);
        ExecutionContext result = new ExecutionContext(routeContext.getSqlStatementContext());
        result.getExecutionUnits().addAll(executeRewrite(sql, clonedParameters, routeContext));
        if (properties.<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW)) {
            SQLLogger.logSQL(sql, properties.<Boolean>getValue(ConfigurationPropertyKey.SQL_SIMPLE), result.getSqlStatementContext(), result.getExecutionUnits());
        }
        return result;
    }
    
    protected abstract List<Object> cloneParameters(List<Object> parameters);
    
    private RouteContext executeRoute(final String sql, final List<Object> clonedParameters) {
        routingHook.start(sql);
        try {
            registerRouteDecorator();
            RouteContext result = route(dataNodeRouter, sql, clonedParameters);
            routingHook.finishSuccess(result, metaData.getSchema());
            return result;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            routingHook.finishFailure(ex);
            throw ex;
        }
    }
    
    private void registerRouteDecorator() {
        for (BaseRule each : rules) {
            if (each instanceof ShardingRule) {
                dataNodeRouter.registerDecorator(each, new ShardingRouteDecorator());
            } else if (each instanceof MasterSlaveRule) {
                dataNodeRouter.registerDecorator(each, new MasterSlaveRouteDecorator());
            }
        }
    }
    
    protected abstract RouteContext route(DataNodeRouter dataNodeRouter, String sql, List<Object> parameters);
    
    private Collection<ExecutionUnit> executeRewrite(final String sql, final List<Object> parameters, final RouteContext routeContext) {
        registerRewriteDecorator();
        SQLRewriteContext sqlRewriteContext = sqlRewriteEntry.createSQLRewriteContext(sql, parameters, routeContext.getSqlStatementContext(), routeContext);
        return routeContext.getRouteResult().getRouteUnits().isEmpty() ? rewrite(sqlRewriteContext) : rewrite(routeContext, sqlRewriteContext);
    }
    
    private void registerRewriteDecorator() {
        for (BaseRule each : rules) {
            if (each instanceof ShardingRule) {
                sqlRewriteEntry.registerDecorator(each, new ShardingSQLRewriteContextDecorator());
            } else if (each instanceof EncryptRule) {
                sqlRewriteEntry.registerDecorator(each, new EncryptSQLRewriteContextDecorator());
            }
        }
    }
    
    private Collection<ExecutionUnit> rewrite(final SQLRewriteContext sqlRewriteContext) {
        SQLRewriteResult sqlRewriteResult = new SQLRewriteEngine().rewrite(sqlRewriteContext);
        String dataSourceName = metaData.getDataSources().getAllInstanceDataSourceNames().iterator().next();
        return Collections.singletonList(new ExecutionUnit(dataSourceName, new SQLUnit(sqlRewriteResult.getSql(), sqlRewriteResult.getParameters())));
    }
    
    private Collection<ExecutionUnit> rewrite(final RouteContext routeContext, final SQLRewriteContext sqlRewriteContext) {
        Collection<ExecutionUnit> result = new LinkedHashSet<>();
        for (Entry<RouteUnit, SQLRewriteResult> entry : new SQLRouteRewriteEngine().rewrite(sqlRewriteContext, routeContext.getRouteResult()).entrySet()) {
            result.add(new ExecutionUnit(entry.getKey().getDataSourceMapper().getActualName(), new SQLUnit(entry.getValue().getSql(), entry.getValue().getParameters())));
        }
        return result;
    }
}
