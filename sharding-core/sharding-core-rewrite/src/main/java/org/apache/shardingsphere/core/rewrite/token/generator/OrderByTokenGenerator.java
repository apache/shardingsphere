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

package org.apache.shardingsphere.core.rewrite.token.generator;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.rewrite.token.pojo.OrderByToken;
import org.apache.shardingsphere.core.rule.ShardingRule;

/**
 * Order by token generator.
 *
 * @author zhangliang
 */
public final class OrderByTokenGenerator implements OptionalSQLTokenGenerator<ShardingRule> {
    
    @Override
    public Optional<OrderByToken> generateSQLToken(final SQLStatement sqlStatement, final ShardingRule rule) {
        if (!(sqlStatement instanceof SelectStatement)) {
            return Optional.absent();
        }
        if (!((SelectStatement) sqlStatement).getGroupByItems().isEmpty() && ((SelectStatement) sqlStatement).getOrderByItems().isEmpty()) {
            return Optional.of(new OrderByToken(((SelectStatement) sqlStatement).getGroupByLastIndex() + 1));
        }
        return Optional.absent();
    }
}
