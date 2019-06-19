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

package org.apache.shardingsphere.core.parse.optimizer;

import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.aware.EncryptRuleAware;
import org.apache.shardingsphere.core.parse.aware.ShardingRuleAware;
import org.apache.shardingsphere.core.parse.aware.ShardingTableMetaDataAware;
import org.apache.shardingsphere.core.parse.rule.registry.statement.SQLStatementRule;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.rule.BaseRule;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.rule.ShardingRule;

/**
 * SQL statement optimizer engine.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class SQLStatementOptimizerEngine {
    
    private final BaseRule rule;
    
    private final ShardingTableMetaData shardingTableMetaData;
    
    /**
     * Optimize SQL statement.
     *
     * @param sqlStatementRule SQL statement rule
     * @param sqlStatement SQL statement
     */
    public void optimize(final SQLStatementRule sqlStatementRule, final SQLStatement sqlStatement) {
        Optional<SQLStatementOptimizer> optimizer = sqlStatementRule.getOptimizer();
        if (optimizer.isPresent()) {
            if (optimizer.get() instanceof ShardingRuleAware && rule instanceof ShardingRule) {
                ((ShardingRuleAware) optimizer.get()).setShardingRule((ShardingRule) rule);
            }
            if (optimizer.get() instanceof EncryptRuleAware && rule instanceof EncryptRule) {
                ((EncryptRuleAware) optimizer.get()).setEncryptRule((EncryptRule) rule);
            }
            if (optimizer.get() instanceof ShardingTableMetaDataAware) {
                ((ShardingTableMetaDataAware) optimizer.get()).setShardingTableMetaData(shardingTableMetaData);
            }
            optimizer.get().optimize(sqlStatement);
        }
    }
}
