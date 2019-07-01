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

import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.optimize.engine.OptimizeEngine;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.condition.engine.WhereClauseShardingConditionEngine;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.ShardingSelectOptimizedStatement;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.pagination.Pagination;
import org.apache.shardingsphere.core.parse.sql.statement.dml.DMLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Where clause optimize engine for sharding.
 *
 * @author zhangliang
 */
public class ShardingWhereOptimizeEngine implements OptimizeEngine {
    
    private final DMLStatement dmlStatement;
    
    private final List<Object> parameters;
    
    private final WhereClauseShardingConditionEngine shardingConditionEngine;
    
    public ShardingWhereOptimizeEngine(final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData, final DMLStatement dmlStatement, final List<Object> parameters) {
        this.dmlStatement = dmlStatement;
        this.parameters = parameters;
        shardingConditionEngine = new WhereClauseShardingConditionEngine(shardingRule, shardingTableMetaData);
    }
    
    @Override
    public ShardingSelectOptimizedStatement optimize() {
        List<ShardingCondition> shardingConditions = new ArrayList<>(shardingConditionEngine.createShardingConditions(dmlStatement, parameters));
        ShardingSelectOptimizedStatement result = new ShardingSelectOptimizedStatement(dmlStatement, shardingConditions);
        setPagination(result);
        return result;
    }
    
    private void setPagination(final ShardingSelectOptimizedStatement optimizedStatement) {
        if (dmlStatement instanceof SelectStatement) {
            SelectStatement selectStatement = (SelectStatement) dmlStatement;
            if (null != selectStatement.getOffset() || null != selectStatement.getRowCount()) {
                optimizedStatement.setPagination(new Pagination(selectStatement.getOffset(), selectStatement.getRowCount(), parameters));
            }
        }
    }
}
