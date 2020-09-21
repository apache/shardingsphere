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

package org.apache.shardingsphere.sql.parser.sql.dialect.helper.dml;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.LockSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.MySQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.OracleStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.PostgreSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.SQL92Statement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dml.SQL92SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.SQLServerStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerSelectStatement;

import java.util.Optional;

/**
 * SelectStatement helper class for different dialect SQLStatements.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SelectStatementHelper {

    /**
     * Get LimitSegment.
     *
     * @param selectStatement SelectStatement
     * @return LimitSegment
     */
    public static Optional<LimitSegment> getLimitSegment(final SelectStatement selectStatement) {
        if (selectStatement instanceof MySQLStatement) {
            return ((MySQLSelectStatement) selectStatement).getLimit();
        }
        if (selectStatement instanceof PostgreSQLStatement) {
            return ((PostgreSQLSelectStatement) selectStatement).getLimit();
        }
        if (selectStatement instanceof SQL92Statement) {
            return ((SQL92SelectStatement) selectStatement).getLimit();
        }
        if (selectStatement instanceof SQLServerStatement) {
            return ((SQLServerSelectStatement) selectStatement).getLimit();
        }
        return Optional.empty();
    }

    /**
     * Get LockSegment.
     *
     * @param selectStatement SelectStatement
     * @return LockSegment
     */
    public static Optional<LockSegment> getLockSegment(final SelectStatement selectStatement) {
        if (selectStatement instanceof MySQLStatement) {
            return ((MySQLSelectStatement) selectStatement).getLock();
        }
        if (selectStatement instanceof OracleStatement) {
            return ((OracleSelectStatement) selectStatement).getLock();
        }
        if (selectStatement instanceof PostgreSQLStatement) {
            return ((PostgreSQLSelectStatement) selectStatement).getLock();
        }
        return Optional.empty();
    }
}
