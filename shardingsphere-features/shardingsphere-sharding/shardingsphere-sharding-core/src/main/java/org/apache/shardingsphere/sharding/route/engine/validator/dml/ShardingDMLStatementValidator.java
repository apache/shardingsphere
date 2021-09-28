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

package org.apache.shardingsphere.sharding.route.engine.validator.dml;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.sharding.route.engine.validator.ShardingStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Collection;

/**
 * Sharding dml statement validator.
 */
public abstract class ShardingDMLStatementValidator<T extends SQLStatement> implements ShardingStatementValidator<T> {
    
    /**
     * Validate multiple table.
     *
     * @param shardingRule sharding rule
     * @param sqlStatementContext sqlStatementContext
     */
    protected void validateMultipleTable(final ShardingRule shardingRule, final SQLStatementContext<T> sqlStatementContext) {
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        boolean isAllShardingTables = shardingRule.isAllShardingTables(tableNames) && (1 == tableNames.size() || shardingRule.isAllBindingTables(tableNames));
        boolean isAllBroadcastTables = shardingRule.isAllBroadcastTables(tableNames);
        boolean isAllSingleTables = !shardingRule.tableRuleExists(tableNames);
        if (!(isAllShardingTables || isAllBroadcastTables || isAllSingleTables)) {
            throw new ShardingSphereException("Cannot support Multiple-Table for '%s'.", tableNames);
        }
    }
}
