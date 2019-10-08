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

package org.apache.shardingsphere.core.route.router.sharding.validator.impl;

import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.core.optimize.segment.table.TablesContext;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.UpdateStatement;
import org.apache.shardingsphere.core.route.router.sharding.validator.ShardingStatementValidator;
import org.apache.shardingsphere.core.rule.ShardingRule;

/**
 * Sharding update statement validator.
 *
 * @author zhangliang
 */
public final class ShardingUpdateStatementValidator implements ShardingStatementValidator<UpdateStatement> {
    
    @Override
    public void validate(final ShardingRule shardingRule, final UpdateStatement sqlStatement) {
        String tableName = new TablesContext(sqlStatement).getSingleTableName();
        for (AssignmentSegment each : sqlStatement.getSetAssignment().getAssignments()) {
            if (shardingRule.isShardingColumn(each.getColumn().getName(), tableName)) {
                throw new ShardingException("Can not update sharding key, logic table: [%s], column: [%s].", tableName, each);
            }
        }
    }
}
