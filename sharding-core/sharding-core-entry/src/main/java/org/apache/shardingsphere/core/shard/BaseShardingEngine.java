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
import org.apache.shardingsphere.api.hint.HintManager;
import org.apache.shardingsphere.core.rule.MasterSlaveRule;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.shard.log.ShardingSQLLogger;
import org.apache.shardingsphere.encrypt.rewrite.context.EncryptSQLRewriteContextDecorator;
import org.apache.shardingsphere.masterslave.route.engine.MasterSlaveRouteDecorator;
import org.apache.shardingsphere.sharding.execute.context.ShardingExecutionContext;
import org.apache.shardingsphere.sharding.rewrite.context.ShardingSQLRewriteContextDecorator;
import org.apache.shardingsphere.sharding.rewrite.engine.ShardingSQLRewriteEngine;
import org.apache.shardingsphere.sharding.route.engine.ShardingRouter;
import org.apache.shardingsphere.sharding.route.engine.context.ShardingRouteContext;
import org.apache.shardingsphere.sharding.route.hook.SPIRoutingHook;
import org.apache.shardingsphere.sql.parser.SQLParserEngine;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.underlying.common.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;
import org.apache.shardingsphere.underlying.executor.context.ExecutionContext;
import org.apache.shardingsphere.underlying.executor.context.ExecutionUnit;
import org.apache.shardingsphere.underlying.executor.context.SQLUnit;
import org.apache.shardingsphere.underlying.rewrite.SQLRewriteEntry;
import org.apache.shardingsphere.underlying.rewrite.context.SQLRewriteContext;
import org.apache.shardingsphere.underlying.rewrite.context.SQLRewriteContextDecorator;
import org.apache.shardingsphere.underlying.rewrite.engine.SQLRewriteResult;
import org.apache.shardingsphere.underlying.route.context.RouteUnit;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Base sharding engine.
 */
@RequiredArgsConstructor
public abstract class BaseShardingEngine {
    
    private final ShardingRule shardingRule;
    
    private final ConfigurationProperties properties;
    
    private final ShardingSphereMetaData metaData;
    
    private final ShardingRouter shardingRouter;
    
    private final SPIRoutingHook routingHook;
    
    public BaseShardingEngine(final ShardingRule shardingRule, final ConfigurationProperties properties, final ShardingSphereMetaData metaData, final SQLParserEngine sqlParserEngine) {
        this.shardingRule = shardingRule;
        this.properties = properties;
        this.metaData = metaData;
        shardingRouter = new ShardingRouter(shardingRule, properties, metaData, sqlParserEngine);
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
        ShardingRouteContext shardingRouteContext = executeRoute(sql, clonedParameters);
        ShardingExecutionContext result = new ShardingExecutionContext(shardingRouteContext.getSqlStatementContext(), shardingRouteContext.getGeneratedKey().orElse(null));
        result.getExecutionUnits().addAll(HintManager.isDatabaseShardingOnly() ? convert(sql, clonedParameters, shardingRouteContext) : rewriteAndConvert(sql, clonedParameters, shardingRouteContext));
        if (properties.<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW)) {
            ShardingSQLLogger.logSQL(sql, properties.<Boolean>getValue(ConfigurationPropertyKey.SQL_SIMPLE), result.getSqlStatementContext(), result.getExecutionUnits());
        }
        return result;
    }
    
    protected abstract List<Object> cloneParameters(List<Object> parameters);
    
    protected abstract ShardingRouteContext route(ShardingRouter shardingRouter, String sql, List<Object> parameters);
    
    private ShardingRouteContext executeRoute(final String sql, final List<Object> clonedParameters) {
        routingHook.start(sql);
        try {
            ShardingRouteContext result = decorate(route(shardingRouter, sql, clonedParameters));
            routingHook.finishSuccess(result, metaData.getSchema());
            return result;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            routingHook.finishFailure(ex);
            throw ex;
        }
    }
    
    private ShardingRouteContext decorate(final ShardingRouteContext shardingRouteContext) {
        ShardingRouteContext result = shardingRouteContext;
        for (MasterSlaveRule each : shardingRule.getMasterSlaveRules()) {
            result = (ShardingRouteContext) new MasterSlaveRouteDecorator(each).decorate(result);
        }
        return result;
    }
    
    private Collection<ExecutionUnit> convert(final String sql, final List<Object> parameters, final ShardingRouteContext shardingRouteContext) {
        Collection<ExecutionUnit> result = new LinkedHashSet<>();
        for (RouteUnit each : shardingRouteContext.getRouteResult().getRouteUnits()) {
            result.add(new ExecutionUnit(each.getActualDataSourceName(), new SQLUnit(sql, parameters)));
        }
        return result;
    }
    
    private Collection<ExecutionUnit> rewriteAndConvert(final String sql, final List<Object> parameters, final ShardingRouteContext shardingRouteContext) {
        Collection<ExecutionUnit> result = new LinkedHashSet<>();
        SQLRewriteContext sqlRewriteContext = new SQLRewriteEntry(metaData.getSchema(), properties).createSQLRewriteContext(
                sql, parameters, shardingRouteContext.getSqlStatementContext(), createSQLRewriteContextDecorator(shardingRouteContext));
        for (RouteUnit each : shardingRouteContext.getRouteResult().getRouteUnits()) {
            ShardingSQLRewriteEngine sqlRewriteEngine = new ShardingSQLRewriteEngine(shardingRule, shardingRouteContext.getShardingConditions(), each);
            SQLRewriteResult sqlRewriteResult = sqlRewriteEngine.rewrite(sqlRewriteContext);
            result.add(new ExecutionUnit(each.getActualDataSourceName(), new SQLUnit(sqlRewriteResult.getSql(), sqlRewriteResult.getParameters())));
        }
        return result;
    }
    
    private Map<BaseRule, SQLRewriteContextDecorator> createSQLRewriteContextDecorator(final ShardingRouteContext shardingRouteContext) {
        Map<BaseRule, SQLRewriteContextDecorator> result = new LinkedHashMap<>(2, 1);
        result.put(shardingRule, new ShardingSQLRewriteContextDecorator(shardingRouteContext));
        if (!shardingRule.getEncryptRule().getEncryptTableNames().isEmpty()) {
            result.put(shardingRule.getEncryptRule(), new EncryptSQLRewriteContextDecorator());
        }
        return result;
    }
}
