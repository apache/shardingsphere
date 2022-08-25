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
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.InsertMultiTableElementSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OutputSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.SQLStatementHandler;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.MySQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.OpenGaussStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dml.OpenGaussInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.OracleStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.PostgreSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.SQLServerStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerInsertStatement;

import java.util.Optional;

/**
 * Insert statement handler for different dialect SQL statements.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InsertStatementHandler implements SQLStatementHandler {
    
    /**
     * Get On duplicate key columns segment.
     *
     * @param insertStatement insert statement
     * @return on duplicate key columns segment
     */
    public static Optional<OnDuplicateKeyColumnsSegment> getOnDuplicateKeyColumnsSegment(final InsertStatement insertStatement) {
        if (insertStatement instanceof MySQLStatement) {
            return ((MySQLInsertStatement) insertStatement).getOnDuplicateKeyColumns();
        }
        if (insertStatement instanceof OpenGaussStatement) {
            return ((OpenGaussInsertStatement) insertStatement).getOnDuplicateKeyColumns();
        }
        return Optional.empty();
    }
    
    /**
     * Get set assignment segment.
     *
     * @param insertStatement insert statement
     * @return set assignment segment
     */
    public static Optional<SetAssignmentSegment> getSetAssignmentSegment(final InsertStatement insertStatement) {
        return insertStatement instanceof MySQLStatement ? ((MySQLInsertStatement) insertStatement).getSetAssignment() : Optional.empty();
    }
    
    /**
     * Get with segment.
     *
     * @param insertStatement insert statement
     * @return with segment
     */
    public static Optional<WithSegment> getWithSegment(final InsertStatement insertStatement) {
        if (insertStatement instanceof PostgreSQLStatement) {
            return ((PostgreSQLInsertStatement) insertStatement).getWithSegment();
        }
        if (insertStatement instanceof SQLServerStatement) {
            return ((SQLServerInsertStatement) insertStatement).getWithSegment();
        }
        if (insertStatement instanceof OpenGaussStatement) {
            return ((OpenGaussInsertStatement) insertStatement).getWithSegment();
        }
        return Optional.empty();
    }
    
    /**
     * Get output segment.
     * 
     * @param insertStatement insert statement
     * @return output segment
     */
    public static Optional<OutputSegment> getOutputSegment(final InsertStatement insertStatement) {
        if (insertStatement instanceof SQLServerStatement) {
            return ((SQLServerInsertStatement) insertStatement).getOutputSegment();
        }
        return Optional.empty();
    }
    
    /**
     * Get insert multi table element segment.
     *
     * @param insertStatement insert statement
     * @return insert multi table element segment
     */
    public static Optional<InsertMultiTableElementSegment> getInsertMultiTableElementSegment(final InsertStatement insertStatement) {
        if (insertStatement instanceof OracleStatement) {
            return ((OracleInsertStatement) insertStatement).getInsertMultiTableElementSegment();
        }
        return Optional.empty();
    }
    
    /**
     * Get select subquery segment.
     *
     * @param insertStatement insert statement
     * @return select subquery segment
     */
    public static Optional<SubquerySegment> getSelectSubquery(final InsertStatement insertStatement) {
        if (insertStatement instanceof OracleStatement) {
            return ((OracleInsertStatement) insertStatement).getSelectSubquery();
        }
        return Optional.empty();
    }
}
