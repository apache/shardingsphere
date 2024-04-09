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
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OutputSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.SQLStatementHandler;
import org.apache.shardingsphere.sql.parser.sql.dialect.segment.sqlserver.hint.OptionHintSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.MySQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLUpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleUpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.SQLServerStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerUpdateStatement;

import java.util.Optional;

/**
 * Update statement helper class for different dialect SQL statements.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UpdateStatementHandler implements SQLStatementHandler {
    
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
    
    /**
     * Get with segment.
     *
     * @param updateStatement update statement
     * @return with segment
     */
    public static Optional<WithSegment> getWithSegment(final UpdateStatement updateStatement) {
        if (updateStatement instanceof SQLServerStatement) {
            return ((SQLServerUpdateStatement) updateStatement).getWithSegment();
        }
        return Optional.empty();
    }
    
    /**
     * Get delete where segment.
     *
     * @param updateStatement update statement
     * @return delete where segment
     */
    public static Optional<WhereSegment> getDeleteWhereSegment(final UpdateStatement updateStatement) {
        if (updateStatement instanceof OracleUpdateStatement) {
            return Optional.ofNullable(((OracleUpdateStatement) updateStatement).getDeleteWhere());
        }
        return Optional.empty();
    }
    
    /**
     * Get option hint segment.
     *
     * @param updateStatement update statement
     * @return option hint segment
     */
    public static Optional<OptionHintSegment> getOptionHintSegment(final UpdateStatement updateStatement) {
        if (updateStatement instanceof SQLServerStatement) {
            return ((SQLServerUpdateStatement) updateStatement).getOptionHintSegment();
        }
        return Optional.empty();
    }
    
    /**
     * Set order by segment.
     * 
     * @param updateStatement update statement
     * @param orderBySegment order by segment
     */
    public static void setOrderBySegment(final UpdateStatement updateStatement, final OrderBySegment orderBySegment) {
        if (updateStatement instanceof MySQLStatement) {
            ((MySQLUpdateStatement) updateStatement).setOrderBy(orderBySegment);
        }
    }
    
    /**
     * Set limit segment.
     *
     * @param updateStatement update statement
     * @param limitSegment limit segment
     */
    public static void setLimitSegment(final UpdateStatement updateStatement, final LimitSegment limitSegment) {
        if (updateStatement instanceof MySQLStatement) {
            ((MySQLUpdateStatement) updateStatement).setLimit(limitSegment);
        }
    }
    
    /**
     * Set with segment.
     *
     * @param updateStatement update statement
     * @param withSegment with segment
     */
    public static void setWithSegment(final UpdateStatement updateStatement, final WithSegment withSegment) {
        if (updateStatement instanceof SQLServerStatement) {
            ((SQLServerUpdateStatement) updateStatement).setWithSegment(withSegment);
        }
    }
    
    /**
     * Set delete where segment.
     *
     * @param updateStatement update statement
     * @param deleteWhereSegment delete where segment
     */
    public static void setDeleteWhereSegment(final UpdateStatement updateStatement, final WhereSegment deleteWhereSegment) {
        if (updateStatement instanceof OracleUpdateStatement) {
            ((OracleUpdateStatement) updateStatement).setDeleteWhere(deleteWhereSegment);
        }
    }
    
    /**
     * Get output segment.
     *
     * @param updateStatement update statement
     * @return output segment
     */
    public static Optional<OutputSegment> getOutputSegment(final UpdateStatement updateStatement) {
        if (updateStatement instanceof SQLServerStatement) {
            return ((SQLServerUpdateStatement) updateStatement).getOutputSegment();
        }
        return Optional.empty();
    }
    
    /**
     * Set output segment.
     *
     * @param updateStatement update statement
     * @param outputSegment output segment
     */
    public static void setOutputSegment(final UpdateStatement updateStatement, final OutputSegment outputSegment) {
        if (updateStatement instanceof SQLServerStatement) {
            ((SQLServerUpdateStatement) updateStatement).setOutputSegment(outputSegment);
        }
    }
}
