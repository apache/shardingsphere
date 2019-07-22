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

package org.apache.shardingsphere.core.optimize.transparent.engine;

import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.optimize.api.engine.OptimizeEngine;
import org.apache.shardingsphere.core.optimize.transparent.statement.TransparentOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.rule.BaseRule;

import java.util.List;

/**
 * Transparent optimize engine.
 *
 * @author panjuan
 * @author zhangliang
 */
public final class TransparentOptimizeEngine implements OptimizeEngine<BaseRule, SQLStatement> {
    
    @Override
    public TransparentOptimizedStatement optimize(final BaseRule rule, 
                                                  final ShardingTableMetaData shardingTableMetaData, final String sql, final List<Object> parameters, final SQLStatement sqlStatement) {
        return new TransparentOptimizedStatement(sqlStatement);
    }
}
