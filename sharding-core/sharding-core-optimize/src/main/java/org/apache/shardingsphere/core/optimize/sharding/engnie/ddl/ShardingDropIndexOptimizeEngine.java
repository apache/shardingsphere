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

package org.apache.shardingsphere.core.optimize.sharding.engnie.ddl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.optimize.api.segment.Tables;
import org.apache.shardingsphere.core.optimize.sharding.engnie.ShardingOptimizeEngine;
import org.apache.shardingsphere.core.optimize.sharding.statement.ddl.ShardingDropIndexOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.statement.ddl.DropIndexStatement;

/**
 * Drop index optimize engine for sharding.
 *
 * @author panjuan
 */
@RequiredArgsConstructor
public final class ShardingDropIndexOptimizeEngine implements ShardingOptimizeEngine {
    
    private final DropIndexStatement dropIndexStatement;
    
    private final ShardingTableMetaData shardingTableMetaData;
    
    @Override
    public ShardingDropIndexOptimizedStatement optimize() {
        Tables tables = new Tables(dropIndexStatement);
        return new ShardingDropIndexOptimizedStatement(dropIndexStatement, tables, getTableName(tables));
    }
    
    // TODO simplify from tables
    private String getTableName(final Tables tables) {
        if (!tables.isEmpty()) {
            return tables.getSingleTableName();
        }
        Optional<String> tableName = shardingTableMetaData.getLogicTableName(dropIndexStatement.getIndexName());
        Preconditions.checkState(tableName.isPresent(), "Cannot find table for index name `%s` from sharding rule.", dropIndexStatement.getIndexName());
        return tableName.get();
    }
}
