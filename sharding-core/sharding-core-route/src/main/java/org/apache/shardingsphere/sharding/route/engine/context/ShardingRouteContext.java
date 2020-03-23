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

package org.apache.shardingsphere.sharding.route.engine.context;

import lombok.Getter;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sql.parser.binder.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.underlying.route.context.RouteContext;
import org.apache.shardingsphere.underlying.route.context.RouteResult;

import java.util.List;
import java.util.Optional;

/**
 * SQL route context.
 */
@Getter
public final class ShardingRouteContext extends RouteContext {
    
    private final ShardingConditions shardingConditions;
    
    private final GeneratedKeyContext generatedKey;
    
    public ShardingRouteContext(final SQLStatementContext sqlStatementContext, 
                                final List<Object> parameters, final RouteResult routeResult, final ShardingConditions shardingConditions, final GeneratedKeyContext generatedKey) {
        super(sqlStatementContext, parameters, routeResult);
        this.shardingConditions = shardingConditions;
        this.generatedKey = generatedKey;
    }
    
    public ShardingRouteContext(final SQLStatementContext sqlStatementContext, final List<Object> parameters, final RouteResult routeResult, final ShardingConditions shardingConditions) {
        this(sqlStatementContext, parameters, routeResult, shardingConditions, null);
    }
    
    /**
     * Get generated key.
     * 
     * @return generated key
     */
    public Optional<GeneratedKeyContext> getGeneratedKey() {
        return Optional.ofNullable(generatedKey);
    }
}
