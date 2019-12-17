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
import org.apache.shardingsphere.core.constant.properties.ShardingProperties;
import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import org.apache.shardingsphere.core.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.core.metadata.table.TableMetaData;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.route.RouteUnit;
import org.apache.shardingsphere.core.route.SQLLogger;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.SQLUnit;
import org.apache.shardingsphere.core.route.hook.SPIRoutingHook;
import org.apache.shardingsphere.core.route.type.RoutingUnit;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.relation.metadata.RelationMetaData;
import org.apache.shardingsphere.sql.parser.relation.metadata.RelationMetas;
import org.apache.shardingsphere.sql.rewriter.context.SQLRewriteContext;
import org.apache.shardingsphere.sql.rewriter.engine.SQLRewriteResult;
import org.apache.shardingsphere.sql.rewriter.encrypt.context.EncryptSQLRewriteContextDecorator;
import org.apache.shardingsphere.sql.rewriter.sharding.context.ShardingSQLRewriteContextDecorator;
import org.apache.shardingsphere.sql.rewriter.sharding.engine.ShardingSQLRewriteEngine;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Base sharding engine.
 *
 * @author zhangliang
 * @author panjuan
 */
@RequiredArgsConstructor
public abstract class BaseShardingEngine {
    
    private final ShardingRule shardingRule;
    
    private final ShardingProperties shardingProperties;
    
    private final ShardingSphereMetaData metaData;
    
    private final SPIRoutingHook routingHook = new SPIRoutingHook();
    
    /**
     * Shard.
     *
     * @param sql SQL
     * @param parameters parameters of SQL
     * @return SQL route result
     */
    public SQLRouteResult shard(final String sql, final List<Object> parameters) {
        List<Object> clonedParameters = cloneParameters(parameters);
        SQLRouteResult result = executeRoute(sql, clonedParameters);
        result.getRouteUnits().addAll(HintManager.isDatabaseShardingOnly() ? convert(sql, clonedParameters, result) : rewriteAndConvert(sql, clonedParameters, result));
        boolean showSQL = shardingProperties.getValue(ShardingPropertiesConstant.SQL_SHOW);
        if (showSQL) {
            boolean showSimple = shardingProperties.getValue(ShardingPropertiesConstant.SQL_SIMPLE);
            SQLLogger.logSQL(sql, showSimple, result.getSqlStatementContext(), result.getRouteUnits());
        }
        return result;
    }
    
    protected abstract List<Object> cloneParameters(List<Object> parameters);
    
    protected abstract SQLRouteResult route(String sql, List<Object> parameters);
    
    private SQLRouteResult executeRoute(final String sql, final List<Object> clonedParameters) {
        routingHook.start(sql);
        try {
            SQLRouteResult result = route(sql, clonedParameters);
            routingHook.finishSuccess(result, metaData.getTables());
            return result;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            routingHook.finishFailure(ex);
            throw ex;
        }
    }
    
    private Collection<RouteUnit> convert(final String sql, final List<Object> parameters, final SQLRouteResult sqlRouteResult) {
        Collection<RouteUnit> result = new LinkedHashSet<>();
        for (RoutingUnit each : sqlRouteResult.getRoutingResult().getRoutingUnits()) {
            result.add(new RouteUnit(each.getDataSourceName(), new SQLUnit(sql, parameters)));
        }
        return result;
    }
    
    private Collection<RouteUnit> rewriteAndConvert(final String sql, final List<Object> parameters, final SQLRouteResult sqlRouteResult) {
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(getRelationMetas(metaData.getTables()), sqlRouteResult.getSqlStatementContext(), sql, parameters);
        new ShardingSQLRewriteContextDecorator(shardingRule, sqlRouteResult).decorate(sqlRewriteContext);
        boolean isQueryWithCipherColumn = shardingProperties.<Boolean>getValue(ShardingPropertiesConstant.QUERY_WITH_CIPHER_COLUMN);
        new EncryptSQLRewriteContextDecorator(shardingRule.getEncryptRule(), isQueryWithCipherColumn).decorate(sqlRewriteContext);
        sqlRewriteContext.generateSQLTokens();
        Collection<RouteUnit> result = new LinkedHashSet<>();
        for (RoutingUnit each : sqlRouteResult.getRoutingResult().getRoutingUnits()) {
            ShardingSQLRewriteEngine sqlRewriteEngine = new ShardingSQLRewriteEngine(shardingRule, sqlRouteResult.getShardingConditions(), each);
            SQLRewriteResult sqlRewriteResult = sqlRewriteEngine.rewrite(sqlRewriteContext);
            result.add(new RouteUnit(each.getDataSourceName(), new SQLUnit(sqlRewriteResult.getSql(), sqlRewriteResult.getParameters())));
        }
        return result;
    }
    
    private RelationMetas getRelationMetas(final TableMetas tableMetas) {
        Map<String, RelationMetaData> result = new HashMap<>(tableMetas.getAllTableNames().size());
        for (String each : tableMetas.getAllTableNames()) {
            TableMetaData tableMetaData = tableMetas.get(each);
            result.put(each, new RelationMetaData(tableMetaData.getColumns().keySet()));
        }
        return new RelationMetas(result);
    }
}
