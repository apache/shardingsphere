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
     * @param sqlStatement SQL statement
     * @return optimize engine instance
     */
    public static OptimizeEngine newInstance(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof SelectStatement) {
            return new ShardingSelectOptimizeEngine();
        }
        if (sqlStatement instanceof InsertStatement) {
            return new ShardingInsertOptimizeEngine();
        }
        if (sqlStatement instanceof UpdateStatement) {
            return new ShardingUpdateOptimizeEngine();
        }
        if (sqlStatement instanceof DeleteStatement) {
            return new ShardingDeleteOptimizeEngine();
        }
        if (sqlStatement instanceof DropIndexStatement) {
            return new ShardingDropIndexOptimizeEngine();
        }
        return new TransparentOptimizeEngine();
    }
}
