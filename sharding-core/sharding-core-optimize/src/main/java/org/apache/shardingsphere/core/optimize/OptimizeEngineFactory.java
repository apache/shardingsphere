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

package org.apache.shardingsphere.core.optimize;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.optimize.engine.OptimizeEngine;
import org.apache.shardingsphere.core.optimize.engine.encrypt.EncryptInsertOptimizeEngine;
import org.apache.shardingsphere.core.optimize.engine.sharding.ddl.DropIndexOptimizeEngine;
import org.apache.shardingsphere.core.optimize.engine.sharding.dml.ShardingInsertOptimizeEngine;
import org.apache.shardingsphere.core.optimize.engine.sharding.dml.ShardingSelectOptimizeEngine;
import org.apache.shardingsphere.core.optimize.engine.sharding.dml.ShardingWhereOptimizeEngine;
import org.apache.shardingsphere.core.optimize.engine.transparent.TransparentOptimizeEngine;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.DMLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.List;

/**
 * Optimize engine factory.
 *
 * @author zhangliang
 * @author maxiaoguang
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OptimizeEngineFactory {
    
    /**
     * Create sharding optimize engine instance.
     * 
     * @param shardingRule sharding rule
     * @param sqlStatement SQL statement
     * @param parameters parameters
     * @param shardingTableMetaData sharding table metadata
     * @return optimize engine instance
     */
    public static OptimizeEngine newInstance(final ShardingRule shardingRule, final SQLStatement sqlStatement, final List<Object> parameters, final ShardingTableMetaData shardingTableMetaData) {
        if (sqlStatement instanceof SelectStatement) {
            return new ShardingSelectOptimizeEngine(shardingRule, shardingTableMetaData, (DMLStatement) sqlStatement, parameters);
        }
        if (sqlStatement instanceof InsertStatement) {
            return new ShardingInsertOptimizeEngine(shardingRule, (InsertStatement) sqlStatement, parameters);
        }
        if (sqlStatement instanceof DMLStatement) {
            return new ShardingWhereOptimizeEngine(shardingRule, shardingTableMetaData, (DMLStatement) sqlStatement, parameters);
        }
        if (sqlStatement instanceof DropIndexStatement) {
            return new DropIndexOptimizeEngine((DropIndexStatement) sqlStatement, shardingTableMetaData);
        }
        return new TransparentOptimizeEngine(sqlStatement);
    }
    
    /**
     * Create encrypt optimize engine instance.
     * 
     * @param encryptRule encrypt rule
     * @param sqlStatement SQL statement
     * @param parameters parameters
     * @return optimize engine instance
     */
    public static OptimizeEngine newInstance(final EncryptRule encryptRule, final SQLStatement sqlStatement, final List<Object> parameters) {
        if (sqlStatement instanceof InsertStatement) {
            return new EncryptInsertOptimizeEngine(encryptRule, (InsertStatement) sqlStatement, parameters);
        }
        return new TransparentOptimizeEngine(sqlStatement);
    }
}
