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

package org.apache.shardingsphere.core.optimize.sharding;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.optimize.api.engine.OptimizeEngine;
import org.apache.shardingsphere.core.optimize.sharding.engnie.ddl.ShardingDropIndexOptimizeEngine;
import org.apache.shardingsphere.core.optimize.sharding.engnie.dml.ShardingDeleteOptimizeEngine;
import org.apache.shardingsphere.core.optimize.sharding.engnie.dml.ShardingInsertOptimizeEngine;
import org.apache.shardingsphere.core.optimize.sharding.engnie.dml.ShardingSelectOptimizeEngine;
import org.apache.shardingsphere.core.optimize.sharding.engnie.dml.ShardingUpdateOptimizeEngine;
import org.apache.shardingsphere.core.optimize.transparent.engine.TransparentOptimizeEngine;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.DeleteStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.UpdateStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.List;

/**
 * Optimize engine factory for sharding.
 *
 * @author zhangliang
 * @author maxiaoguang
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingOptimizeEngineFactory {
    
    /**
     * Create sharding optimize engine instance.
     * 
     * @param shardingRule sharding rule
     * @param shardingTableMetaData sharding table metadata
     * @param sqlStatement SQL statement
     * @param parameters parameters
     * @return optimize engine instance
     */
    public static OptimizeEngine newInstance(final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData, final SQLStatement sqlStatement, final List<Object> parameters) {
        if (sqlStatement instanceof SelectStatement) {
            return new ShardingSelectOptimizeEngine(shardingRule, shardingTableMetaData, (SelectStatement) sqlStatement, parameters);
        }
        if (sqlStatement instanceof InsertStatement) {
            return new ShardingInsertOptimizeEngine(shardingRule, shardingTableMetaData, (InsertStatement) sqlStatement, parameters);
        }
        if (sqlStatement instanceof UpdateStatement) {
            return new ShardingUpdateOptimizeEngine(shardingRule, shardingTableMetaData, (UpdateStatement) sqlStatement, parameters);
        }
        if (sqlStatement instanceof DeleteStatement) {
            return new ShardingDeleteOptimizeEngine(shardingRule, shardingTableMetaData, (DeleteStatement) sqlStatement, parameters);
        }
        if (sqlStatement instanceof DropIndexStatement) {
            return new ShardingDropIndexOptimizeEngine((DropIndexStatement) sqlStatement, shardingTableMetaData);
        }
        return new TransparentOptimizeEngine(sqlStatement);
    }
}
