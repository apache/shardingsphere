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

package org.apache.shardingsphere.core.optimize.sharding.engnie.dml;

import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.optimize.encrypt.condition.EncryptConditions;
import org.apache.shardingsphere.core.optimize.encrypt.condition.engine.WhereClauseEncryptConditionEngine;
import org.apache.shardingsphere.core.optimize.sharding.engnie.ShardingOptimizeEngine;
import org.apache.shardingsphere.core.optimize.sharding.segment.condition.ShardingConditions;
import org.apache.shardingsphere.core.optimize.sharding.segment.condition.engine.WhereClauseShardingConditionEngine;
import org.apache.shardingsphere.core.optimize.sharding.statement.dml.ShardingConditionOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.UpdateStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.List;

/**
 * Update optimize engine for sharding.
 *
 * @author zhangliang
 */
public final class ShardingUpdateOptimizeEngine implements ShardingOptimizeEngine<UpdateStatement> {
    
    @Override
    public ShardingConditionOptimizedStatement optimize(final ShardingRule shardingRule,
                                                        final TableMetas tableMetas, final String sql, final List<Object> parameters, final UpdateStatement sqlStatement) {
        WhereClauseShardingConditionEngine shardingConditionEngine = new WhereClauseShardingConditionEngine(shardingRule, tableMetas);
        WhereClauseEncryptConditionEngine encryptConditionEngine = new WhereClauseEncryptConditionEngine(shardingRule.getEncryptRule(), tableMetas);
        return new ShardingConditionOptimizedStatement(sqlStatement,
                new ShardingConditions(shardingConditionEngine.createShardingConditions(sqlStatement, parameters)),
                new EncryptConditions(encryptConditionEngine.createEncryptConditions(sqlStatement)));
    }
}
