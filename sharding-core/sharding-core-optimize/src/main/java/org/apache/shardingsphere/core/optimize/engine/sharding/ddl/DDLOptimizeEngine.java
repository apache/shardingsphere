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

package org.apache.shardingsphere.core.optimize.engine.sharding.ddl;

import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.optimize.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimize.condition.ShardingConditions;
import org.apache.shardingsphere.core.optimize.engine.OptimizeEngine;
import org.apache.shardingsphere.core.optimize.result.OptimizeResult;
import org.apache.shardingsphere.core.parse.sql.statement.ddl.DDLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.ddl.DropIndexStatement;

import java.util.Collections;

/**
 * DDL OptimizeEngine.
 *
 * @author panjuan
 */
@RequiredArgsConstructor
public final class DDLOptimizeEngine implements OptimizeEngine {
    
    private final DDLStatement ddlStatement;
    
    private final ShardingTableMetaData shardingTableMetaData;
    
    @Override
    public OptimizeResult optimize() {
        OptimizeResult result = new OptimizeResult(new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        if (isDropIndexWithoutTable()) {
            setLogicTableName(result);
        }
        return result;
    }
    
    private boolean isDropIndexWithoutTable() {
        return ddlStatement instanceof DropIndexStatement && ddlStatement.getTables().isEmpty();
    }
    
    private void setLogicTableName(final OptimizeResult result) {
        Optional<String> logicTableName = shardingTableMetaData.getLogicTableName(ddlStatement.getIndexName());
        if (logicTableName.isPresent()) {
            result.setLogicTableNameForDropIndex(logicTableName.get());
        }
    }
}
