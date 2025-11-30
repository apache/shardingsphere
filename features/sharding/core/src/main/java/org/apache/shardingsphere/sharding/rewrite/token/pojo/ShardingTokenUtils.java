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

package org.apache.shardingsphere.sharding.rewrite.token.pojo;

import com.cedarsoftware.util.CaseInsensitiveMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Collection;
import java.util.Map;

/**
 * Sharding token utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingTokenUtils {
    
    /**
     * Get logic and actual table map.
     *
     * @param routeUnit route unit
     * @param sqlStatementContext SQL statement context
     * @param rule sharding rule
     * @return key is logic table name, values is actual table belong to this data source
     */
    public static Map<String, String> getLogicAndActualTableMap(final RouteUnit routeUnit, final SQLStatementContext sqlStatementContext, final ShardingRule rule) {
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        Map<String, String> result = new CaseInsensitiveMap<>();
        for (RouteMapper each : routeUnit.getTableMappers()) {
            result.put(each.getLogicName(), each.getActualName());
            result.putAll(rule.getLogicAndActualTablesFromBindingTable(routeUnit.getDataSourceMapper().getLogicName(), each.getLogicName(), each.getActualName(), tableNames));
        }
        return result;
    }
}
