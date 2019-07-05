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

package org.apache.shardingsphere.core.optimize.statement.sharding.dml.select;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.ShardingWhereOptimizedStatement;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.condition.ShardingConditions;
import org.apache.shardingsphere.core.parse.sql.context.condition.AndCondition;
import org.apache.shardingsphere.core.parse.sql.context.selectitem.SelectItem;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Select optimized statement for sharding.
 *
 * @author zhangliang
 */
@Getter
@Setter
public final class ShardingSelectOptimizedStatement extends ShardingWhereOptimizedStatement {
    
    private final Collection<SelectItem> items = new LinkedHashSet<>();
    
    private Pagination pagination;
    
    public ShardingSelectOptimizedStatement(final SQLStatement sqlStatement, final List<ShardingCondition> shardingConditions, final AndCondition encryptConditions) {
        super(sqlStatement, new ShardingConditions(shardingConditions), encryptConditions);
    }
}
