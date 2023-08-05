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
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.ReturningSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.InsertMultiTableElementSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OutputSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.SQLStatementHandler;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dml.OpenGaussInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLInsertStatement;
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
        if (insertStatement instanceof MySQLInsertStatement) {
            return ((MySQLInsertStatement) insertStatement).getOnDuplicateKeyColumns();
        }
        if (insertStatement instanceof OpenGaussInsertStatement) {
            return ((OpenGaussInsertStatement) insertStatement).getOnDuplicateKeyColumns();
        }
        if (insertStatement instanceof PostgreSQLInsertStatement) {
            return ((PostgreSQLInsertStatement) insertStatement).getOnDuplicateKeyColumns();
        }
        return Optional.empty();
    }
    
    /**
     * Set on duplicate key columns segment.
     * 
     * @param insertStatement insert statement
     * @param onDuplicateKeyColumnsSegment on duplicate key columns segment
     */
    public static void setOnDuplicateKeyColumnsSegment(final InsertStatement insertStatement, final OnDuplicateKeyColumnsSegment onDuplicateKeyColumnsSegment) {
        if (insertStatement instanceof MySQLInsertStatement) {
            ((MySQLInsertStatement) insertStatement).setOnDuplicateKeyColumns(onDuplicateKeyColumnsSegment);
        }
        if (insertStatement instanceof OpenGaussInsertStatement) {
            ((OpenGaussInsertStatement) insertStatement).setOnDuplicateKeyColumnsSegment(onDuplicateKeyColumnsSegment);
        }
        if (insertStatement instanceof PostgreSQLInsertStatement) {
            ((PostgreSQLInsertStatement) insertStatement).setOnDuplicateKeyColumnsSegment(onDuplicateKeyColumnsSegment);
        }
    }
    
    /**
     * Get set assignment segment.
     *
     * @param insertStatement insert statement
     * @return set assignment segment
     */
    public static Optional<SetAssignmentSegment> getSetAssignmentSegment(final InsertStatement insertStatement) {
        return insertStatement instanceof MySQLInsertStatement ? ((MySQLInsertStatement) insertStatement).getSetAssignment() : Optional.empty();
    }
    
    /**
     * Set set assignment segment.
     * 
     * @param insertStatement insert statement
     * @param setAssignmentSegment set assignment segment
     */
    public static void setSetAssignmentSegment(final InsertStatement insertStatement, final SetAssignmentSegment setAssignmentSegment) {
        if (insertStatement instanceof MySQLInsertStatement) {
            ((MySQLInsertStatement) insertStatement).setSetAssignment(setAssignmentSegment);
        }
    }
    
    /**
     * Get with segment.
     *
     * @param insertStatement insert statement
     * @return with segment
     */
    public static Optional<WithSegment> getWithSegment(final InsertStatement insertStatement) {
        if (insertStatement instanceof PostgreSQLInsertStatement) {
            return ((PostgreSQLInsertStatement) insertStatement).getWithSegment();
        }
        if (insertStatement instanceof SQLServerInsertStatement) {
            return ((SQLServerInsertStatement) insertStatement).getWithSegment();
        }
        if (insertStatement instanceof OpenGaussInsertStatement) {
            return ((OpenGaussInsertStatement) insertStatement).getWithSegment();
        }
        return Optional.empty();
    }
    
    /**
     * Set with segment.
     * 
     * @param insertStatement insert statement
     * @param withSegment with segment
     */
    public static void setWithSegment(final InsertStatement insertStatement, final WithSegment withSegment) {
        if (insertStatement instanceof PostgreSQLInsertStatement) {
            ((PostgreSQLInsertStatement) insertStatement).setWithSegment(withSegment);
        }
        if (insertStatement instanceof SQLServerInsertStatement) {
            ((SQLServerInsertStatement) insertStatement).setWithSegment(withSegment);
        }
        if (insertStatement instanceof OpenGaussInsertStatement) {
            ((OpenGaussInsertStatement) insertStatement).setWithSegment(withSegment);
        }
    }
    
    /**
     * Get output segment.
     * 
     * @param insertStatement insert statement
     * @return output segment
     */
    public static Optional<OutputSegment> getOutputSegment(final InsertStatement insertStatement) {
        if (insertStatement instanceof SQLServerInsertStatement) {
            return ((SQLServerInsertStatement) insertStatement).getOutputSegment();
        }
        return Optional.empty();
    }
    
    /**
     * Set output segment.
     * 
     * @param insertStatement insert statement
     * @param outputSegment output segment
     */
    public static void setOutputSegment(final InsertStatement insertStatement, final OutputSegment outputSegment) {
        if (insertStatement instanceof SQLServerInsertStatement) {
            ((SQLServerInsertStatement) insertStatement).setOutputSegment(outputSegment);
        }
    }
    
    /**
     * Get insert multi table element segment.
     *
     * @param insertStatement insert statement
     * @return insert multi table element segment
     */
    public static Optional<InsertMultiTableElementSegment> getInsertMultiTableElementSegment(final InsertStatement insertStatement) {
        if (insertStatement instanceof OracleInsertStatement) {
            return ((OracleInsertStatement) insertStatement).getInsertMultiTableElementSegment();
        }
        return Optional.empty();
    }
    
    /**
     * Set insert multi table element segment.
     * 
     * @param insertStatement insert statement
     * @param insertMultiTableElementSegment insert multi table element segment
     */
    public static void setInsertMultiTableElementSegment(final InsertStatement insertStatement, final InsertMultiTableElementSegment insertMultiTableElementSegment) {
        if (insertStatement instanceof OracleInsertStatement) {
            ((OracleInsertStatement) insertStatement).setInsertMultiTableElementSegment(insertMultiTableElementSegment);
        }
    }
    
    /**
     * Get returning segment of insert statement.
     *
     * @param insertStatement insert statement
     * @return returning segment
     */
    public static Optional<ReturningSegment> getReturningSegment(final InsertStatement insertStatement) {
        if (insertStatement instanceof PostgreSQLInsertStatement) {
            return ((PostgreSQLInsertStatement) insertStatement).getReturningSegment();
        }
        if (insertStatement instanceof OpenGaussInsertStatement) {
            return ((OpenGaussInsertStatement) insertStatement).getReturningSegment();
        }
        return Optional.empty();
    }
    
    /**
     * Set returning segment of insert statement.
     * 
     * @param insertStatement insert statement
     * @param returningSegment returning segment
     */
    public static void setReturningSegment(final InsertStatement insertStatement, final ReturningSegment returningSegment) {
        if (insertStatement instanceof PostgreSQLInsertStatement) {
            ((PostgreSQLInsertStatement) insertStatement).setReturningSegment(returningSegment);
        }
        if (insertStatement instanceof OpenGaussInsertStatement) {
            ((OpenGaussInsertStatement) insertStatement).setReturningSegment(returningSegment);
        }
    }
}
