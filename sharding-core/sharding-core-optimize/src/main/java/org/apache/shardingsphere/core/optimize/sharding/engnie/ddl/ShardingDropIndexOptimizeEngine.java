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
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.optimize.api.segment.Tables;
import org.apache.shardingsphere.core.optimize.sharding.engnie.ShardingOptimizeEngine;
import org.apache.shardingsphere.core.optimize.sharding.statement.ddl.ShardingDropIndexOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.core.parse.sql.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Drop index optimize engine for sharding.
 *
 * @author panjuan
 */
public final class ShardingDropIndexOptimizeEngine implements ShardingOptimizeEngine<DropIndexStatement> {
    
    @Override
    public ShardingDropIndexOptimizedStatement optimize(final ShardingRule shardingRule,
                                                        final TableMetas tableMetas, final String sql, final List<Object> parameters, final DropIndexStatement sqlStatement) {
        Tables tables = new Tables(sqlStatement);
        return new ShardingDropIndexOptimizedStatement(sqlStatement, tables, getTableNames(tableMetas, sqlStatement, tables));
    }
    
    private Collection<String> getTableNames(final TableMetas tableMetas, final DropIndexStatement sqlStatement, final Tables tables) {
        return tables.isEmpty() ? getTableNames(tableMetas, sqlStatement) : Collections.singletonList(tables.getSingleTableName());
    }
    
    private Collection<String> getTableNames(final TableMetas tableMetas, final DropIndexStatement sqlStatement) {
        Collection<String> result = new LinkedList<>();
        for (IndexSegment each : sqlStatement.getIndexes()) {
            Optional<String> tableName = findLogicTableName(tableMetas, each.getName());
            Preconditions.checkState(tableName.isPresent(), "Cannot find table for index name `%s` from sharding rule.", each.getName());
            result.add(tableName.get());
        }
        return result;
    }
    
    private Optional<String> findLogicTableName(final TableMetas tableMetas, final String logicIndexName) {
        for (String each : tableMetas.getAllTableNames()) {
            if (tableMetas.get(each).containsIndex(logicIndexName)) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
}
