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

package org.apache.shardingsphere.core.optimize.engine.encrypt;

import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.optimize.engine.OptimizeEngine;
import org.apache.shardingsphere.core.optimize.statement.encrypt.EncryptWhereOptimizedStatement;
import org.apache.shardingsphere.core.optimize.statement.encrypt.condition.EncryptConditions;
import org.apache.shardingsphere.core.optimize.statement.encrypt.condition.engine.WhereClauseEncryptConditionEngine;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.rule.EncryptRule;

/**
 * Where optimize engine for encrypt.
 *
 * @author zhangliang
 */
public final class EncryptWhereOptimizeEngine implements OptimizeEngine {
    
    private final SQLStatement sqlStatement;
    
    private final WhereClauseEncryptConditionEngine encryptConditionEngine;
    
    public EncryptWhereOptimizeEngine(final EncryptRule encryptRule, final ShardingTableMetaData shardingTableMetaData, final SQLStatement sqlStatement) {
        this.sqlStatement = sqlStatement;
        encryptConditionEngine = new WhereClauseEncryptConditionEngine(encryptRule, shardingTableMetaData);
    }
    
    @Override
    public EncryptWhereOptimizedStatement optimize() {
        return new EncryptWhereOptimizedStatement(sqlStatement, new EncryptConditions(encryptConditionEngine.createEncryptConditions(sqlStatement)));
    }
}
