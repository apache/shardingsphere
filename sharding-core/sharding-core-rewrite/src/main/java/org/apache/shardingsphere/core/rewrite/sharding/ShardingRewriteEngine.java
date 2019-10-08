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

package org.apache.shardingsphere.core.rewrite.sharding;

import org.apache.shardingsphere.core.rewrite.SQLRewriteBuilder;
import org.apache.shardingsphere.core.route.SQLUnit;
import org.apache.shardingsphere.core.route.type.RoutingUnit;

import java.util.Map;

/**
 * SQL rewrite engine for sharding.
 * 
 * @author zhangliang
 */
public final class ShardingRewriteEngine {
    
    /**
     * Generate SQL.
     *
     * @param sqlRewriteBuilder SQL rewrite builder 
     * @param routingUnit routing unit
     * @param logicAndActualTables logic and actual tables
     * @return SQL unit
     */
    public SQLUnit generateSQL(final SQLRewriteBuilder sqlRewriteBuilder, final RoutingUnit routingUnit, final Map<String, String> logicAndActualTables) {
        return new SQLUnit(sqlRewriteBuilder.getSQLBuilder().toSQL(routingUnit, logicAndActualTables), sqlRewriteBuilder.getParameterBuilder().getParameters(routingUnit));
    }
}
