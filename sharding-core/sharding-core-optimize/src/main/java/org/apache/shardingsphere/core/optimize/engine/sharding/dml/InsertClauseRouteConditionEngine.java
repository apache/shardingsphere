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

package org.apache.shardingsphere.core.optimize.engine.sharding.dml;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.optimize.condition.RouteCondition;
import org.apache.shardingsphere.core.optimize.keygen.GeneratedKey;
import org.apache.shardingsphere.core.parse.sql.context.condition.AndCondition;
import org.apache.shardingsphere.core.parse.sql.context.condition.Condition;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.strategy.route.value.ListRouteValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Route condition engine for insert clause.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class InsertClauseRouteConditionEngine {
    
    private final ShardingRule shardingRule;
    
    /**
     * Create route conditions.
     * 
     * @param insertStatement insert statement
     * @param parameters SQL parameters
     * @param generatedKey generated key
     * @return route conditions
     */
    public List<RouteCondition> createRouteConditions(final InsertStatement insertStatement, final List<Object> parameters, final GeneratedKey generatedKey) {
        List<RouteCondition> result = new ArrayList<>(insertStatement.getShardingConditions().getOrConditions().size());
        String tableName = insertStatement.getTables().getSingleTableName();
        Iterator<Comparable<?>> generatedValues = null == generatedKey ? Collections.<Comparable<?>>emptyList().iterator() : generatedKey.getGeneratedValues().iterator();
        for (AndCondition each : insertStatement.getShardingConditions().getOrConditions()) {
            RouteCondition routeCondition = new RouteCondition();
            routeCondition.getRouteValues().addAll(getRouteValues(each, parameters));
            if (isNeedAppendGeneratedKeyCondition(generatedKey, tableName)) {
                routeCondition.getRouteValues().add(new ListRouteValue<>(generatedKey.getColumnName(), tableName, Collections.<Comparable<?>>singletonList(generatedValues.next())));
            }
            result.add(routeCondition);
        }
        return result;
    }
    
    private boolean isNeedAppendGeneratedKeyCondition(final GeneratedKey generatedKey, final String tableName) {
        return null != generatedKey && generatedKey.isGenerated() && shardingRule.isShardingColumn(generatedKey.getColumnName(), tableName);
    }
    
    private Collection<ListRouteValue> getRouteValues(final AndCondition andCondition, final List<Object> parameters) {
        Collection<ListRouteValue> result = new LinkedList<>();
        for (Condition each : andCondition.getConditions()) {
            result.add(new ListRouteValue<>(each.getColumn().getName(), each.getColumn().getTableName(), each.getConditionValues(parameters)));
        }
        return result;
    }
}
