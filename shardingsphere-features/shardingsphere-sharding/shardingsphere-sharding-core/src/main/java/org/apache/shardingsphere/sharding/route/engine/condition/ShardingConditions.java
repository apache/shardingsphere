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

package org.apache.shardingsphere.sharding.route.engine.condition;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.List;

/**
 * Sharding conditions.
 */
@RequiredArgsConstructor
@Getter
@ToString
public final class ShardingConditions {
    
    private final List<ShardingCondition> conditions;
    
    private final boolean needMerge;
    
    public ShardingConditions(final List<ShardingCondition> conditions, final SQLStatementContext<?> sqlStatementContext, final ShardingRule rule) {
        this.conditions = conditions;
        this.needMerge = isNeedMerge(sqlStatementContext, rule);
    }
    
    /**
     * Judge sharding conditions is always false or not.
     *
     * @return sharding conditions is always false or not
     */
    public boolean isAlwaysFalse() {
        if (conditions.isEmpty()) {
            return false;
        }
        for (ShardingCondition each : conditions) {
            if (!(each instanceof AlwaysFalseShardingCondition)) {
                return false;
            }
        }
        return true;
    }
    
    private boolean isNeedMerge(final SQLStatementContext<?> sqlStatementContext, final ShardingRule rule) {
        boolean selectContainsSubquery = sqlStatementContext instanceof SelectStatementContext && ((SelectStatementContext) sqlStatementContext).isContainsSubquery();
        boolean insertSelectContainsSubquery = sqlStatementContext instanceof InsertStatementContext && null != ((InsertStatementContext) sqlStatementContext).getInsertSelectContext()
                && ((InsertStatementContext) sqlStatementContext).getInsertSelectContext().getSelectStatementContext().isContainsSubquery();
        return (selectContainsSubquery || insertSelectContainsSubquery) && !rule.getShardingLogicTableNames(sqlStatementContext.getTablesContext().getTableNames()).isEmpty();
    }
    
    /**
     * Merge sharding conditions.
     */
    public void merge() {
        if (conditions.size() > 1) {
            ShardingCondition shardingCondition = conditions.remove(conditions.size() - 1);
            conditions.clear();
            conditions.add(shardingCondition);
        }
    }
}
