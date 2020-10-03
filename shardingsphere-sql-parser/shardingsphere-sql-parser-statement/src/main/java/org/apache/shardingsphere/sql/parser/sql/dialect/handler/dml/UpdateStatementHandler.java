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

package org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.MySQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLUpdateStatement;

import java.util.Optional;

/**
 * Update statement helper class for different dialect SQL statements.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UpdateStatementHandler {
    
    /**
     * Get order by segment.
     *
     * @param updateStatement update statement
     * @return order by segment
     */
    public static Optional<OrderBySegment> getOrderBySegment(final UpdateStatement updateStatement) {
        if (updateStatement instanceof MySQLStatement) {
            return ((MySQLUpdateStatement) updateStatement).getOrderBy();
        }
        return Optional.empty();
    }
    
    /**
     * Get limit segment.
     *
     * @param updateStatement update statement
     * @return limit segment
     */
    public static Optional<LimitSegment> getLimitSegment(final UpdateStatement updateStatement) {
        if (updateStatement instanceof MySQLStatement) {
            return ((MySQLUpdateStatement) updateStatement).getLimit();
        }
        return Optional.empty();
    }
}
