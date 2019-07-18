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

package org.apache.shardingsphere.core.optimize.sharding.statement.dml;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.optimize.api.statement.WhereOptimizedStatement;
import org.apache.shardingsphere.core.optimize.encrypt.statement.condition.EncryptConditions;
import org.apache.shardingsphere.core.optimize.sharding.statement.ShardingOptimizedStatement;
import org.apache.shardingsphere.core.optimize.sharding.statement.dml.condition.ShardingConditions;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;

/**
 * Where optimized statement for sharding.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public class ShardingWhereOptimizedStatement implements ShardingOptimizedStatement, WhereOptimizedStatement {
    
    private final SQLStatement sqlStatement;
    
    @Getter
    private final ShardingConditions shardingConditions;
    
    @Getter
    private final EncryptConditions encryptConditions;
    
    @Override
    public final SQLStatement getSQLStatement() {
        return sqlStatement;
    }
}
