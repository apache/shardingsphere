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

package org.apache.shardingsphere.sql.parser.sql.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.MySQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.PostgreSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.SQLServerStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerInsertStatement;

import java.util.Optional;

/**
 * SQL statement utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLStatementUtils {

    /**
     * Get OnDuplicateKeyColumnsSegment.
     *
     * @param insertStatement InsertStatement
     * @return OnDuplicateKeyColumnsSegment
     */
    public static Optional<OnDuplicateKeyColumnsSegment> getOnDuplicateKeyColumnsSegment(final InsertStatement insertStatement) {
        if (insertStatement instanceof MySQLStatement) {
            return ((MySQLInsertStatement) insertStatement).getOnDuplicateKeyColumns();
        }
        return Optional.empty();
    }

    /**
     * Get SetAssignmentSegment.
     *
     * @param insertStatement InsertStatement
     * @return SetAssignmentSegment
     */
    public static Optional<SetAssignmentSegment> getSetAssignmentSegment(final InsertStatement insertStatement) {
        if (insertStatement instanceof MySQLStatement) {
            return ((MySQLInsertStatement) insertStatement).getSetAssignment();
        }
        return Optional.empty();
    }

    /**
     * Get WithSegment.
     *
     * @param insertStatement InsertStatement
     * @return WithSegment
     */
    public static Optional<WithSegment> getWithSegment(final InsertStatement insertStatement) {
        if (insertStatement instanceof PostgreSQLStatement) {
            return ((PostgreSQLInsertStatement) insertStatement).getWithSegment();
        }
        if (insertStatement instanceof SQLServerStatement) {
            return ((SQLServerInsertStatement) insertStatement).getWithSegment();
        }
        return Optional.empty();
    }
}
