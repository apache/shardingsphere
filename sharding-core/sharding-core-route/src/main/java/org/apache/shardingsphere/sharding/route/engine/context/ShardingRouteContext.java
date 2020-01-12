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

import com.google.common.base.Optional;
import lombok.Getter;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.keygen.GeneratedKey;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.underlying.route.context.RouteContext;
import org.apache.shardingsphere.underlying.route.context.RouteResult;

/**
 * SQL route context.
 * 
 * @author gaohongtao
 * @author zhangliang
 * @author zhaojun
 */
@Getter
public final class ShardingRouteContext extends RouteContext {
    
    private final ShardingConditions shardingConditions;
    
    private final GeneratedKey generatedKey;
    
    public ShardingRouteContext(final SQLStatementContext sqlStatementContext, final RouteResult routeResult, final ShardingConditions shardingConditions, final GeneratedKey generatedKey) {
        super(sqlStatementContext, routeResult);
        this.shardingConditions = shardingConditions;
        this.generatedKey = generatedKey;
    }
    
    public ShardingRouteContext(final SQLStatementContext sqlStatementContext, final RouteResult routeResult, final ShardingConditions shardingConditions) {
        this(sqlStatementContext, routeResult, shardingConditions, null);
    }
    
    /**
     * Get generated key.
     * 
     * @return generated key
     */
    public Optional<GeneratedKey> getGeneratedKey() {
        return Optional.fromNullable(generatedKey);
    }
}
