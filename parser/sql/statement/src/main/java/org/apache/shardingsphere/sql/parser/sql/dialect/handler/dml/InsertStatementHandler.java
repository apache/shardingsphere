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
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OutputSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.SQLStatementHandler;
import org.apache.shardingsphere.sql.parser.sql.dialect.segment.oracle.table.MultiTableConditionalIntoSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.segment.oracle.table.MultiTableInsertIntoSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.segment.oracle.table.MultiTableInsertType;
import org.apache.shardingsphere.sql.parser.sql.dialect.segment.sqlserver.exec.ExecSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.segment.sqlserver.hint.WithTableHintSegment;
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
     * Get multi table insert type.
     *
     * @param insertStatement insert statement
     * @return multi table insert type
     */
    public static Optional<MultiTableInsertType> getMultiTableInsertType(final InsertStatement insertStatement) {
        if (insertStatement instanceof OracleInsertStatement) {
            return ((OracleInsertStatement) insertStatement).getMultiTableInsertType();
        }
        return Optional.empty();
    }
    
    /**
     * Set multi table insert type.
     *
     * @param insertStatement insert into statement
     * @param multiTableInsertType multi table insert type
     */
    public static void setMultiTableInsertType(final InsertStatement insertStatement, final MultiTableInsertType multiTableInsertType) {
        if (insertStatement instanceof OracleInsertStatement) {
            ((OracleInsertStatement) insertStatement).setMultiTableInsertType(multiTableInsertType);
        }
    }
    
    /**
     * Get multi table insert into segment.
     *
     * @param insertStatement insert statement
     * @return multi table insert into segment
     */
    public static Optional<MultiTableInsertIntoSegment> getMultiTableInsertIntoSegment(final InsertStatement insertStatement) {
        if (insertStatement instanceof OracleInsertStatement) {
            return ((OracleInsertStatement) insertStatement).getMultiTableInsertIntoSegment();
        }
        return Optional.empty();
    }
    
    /**
     * Set multi table insert into segment.
     * 
     * @param insertStatement insert into statement
     * @param multiTableInsertIntoSegment multi table insert into segment
     */
    public static void setMultiTableInsertIntoSegment(final InsertStatement insertStatement, final MultiTableInsertIntoSegment multiTableInsertIntoSegment) {
        if (insertStatement instanceof OracleInsertStatement) {
            ((OracleInsertStatement) insertStatement).setMultiTableInsertIntoSegment(multiTableInsertIntoSegment);
        }
    }
    
    /**
     * Get multi table conditional into segment.
     *
     * @param insertStatement insert statement
     * @return multi table conditional into segment
     */
    public static Optional<MultiTableConditionalIntoSegment> getMultiTableConditionalIntoSegment(final InsertStatement insertStatement) {
        if (insertStatement instanceof OracleInsertStatement) {
            return ((OracleInsertStatement) insertStatement).getMultiTableConditionalIntoSegment();
        }
        return Optional.empty();
    }
    
    /**
     * Set multi table conditional into segment.
     *
     * @param insertStatement insert into statement
     * @param multiTableConditionalIntoSegment multi table conditional into segment
     */
    public static void setMultiTableConditionalIntoSegment(final InsertStatement insertStatement, final MultiTableConditionalIntoSegment multiTableConditionalIntoSegment) {
        if (insertStatement instanceof OracleInsertStatement) {
            ((OracleInsertStatement) insertStatement).setMultiTableConditionalIntoSegment(multiTableConditionalIntoSegment);
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
    
    /**
     * Get where segment.
     *
     * @param insertStatement insert statement
     * @return where segment
     */
    public static Optional<WhereSegment> getWhereSegment(final InsertStatement insertStatement) {
        if (insertStatement instanceof OracleInsertStatement) {
            return ((OracleInsertStatement) insertStatement).getWhere();
        }
        return Optional.empty();
    }
    
    /**
     * Set where segment.
     * 
     * @param insertStatement insert statement
     * @param whereSegment where segment
     */
    public static void setWhereSegment(final InsertStatement insertStatement, final WhereSegment whereSegment) {
        if (insertStatement instanceof OracleInsertStatement) {
            ((OracleInsertStatement) insertStatement).setWhere(whereSegment);
        }
    }
    
    /**
     * Get execute segment.
     *
     * @param insertStatement insert statement
     * @return execute segment
     */
    public static Optional<ExecSegment> getExecSegment(final InsertStatement insertStatement) {
        if (insertStatement instanceof SQLServerInsertStatement) {
            return ((SQLServerInsertStatement) insertStatement).getExecSegment();
        }
        return Optional.empty();
    }
    
    /**
     * Set execute segment.
     * 
     * @param insertStatement insert statement
     * @param execSegment execute segment
     */
    public static void setExecSegment(final InsertStatement insertStatement, final ExecSegment execSegment) {
        if (insertStatement instanceof SQLServerInsertStatement) {
            ((SQLServerInsertStatement) insertStatement).setExecSegment(execSegment);
        }
    }
    
    /**
     * Get with table hint segment.
     *
     * @param insertStatement insert statement
     * @return with table hint segment
     */
    public static Optional<WithTableHintSegment> getWithTableHintSegment(final InsertStatement insertStatement) {
        if (insertStatement instanceof SQLServerInsertStatement) {
            return ((SQLServerInsertStatement) insertStatement).getWithTableHintSegment();
        }
        return Optional.empty();
    }
    
    /**
     * Get rowSet function segment.
     *
     * @param insertStatement insert statement
     * @return rowSet function segment
     */
    public static Optional<FunctionSegment> getRowSetFunctionSegment(final InsertStatement insertStatement) {
        if (insertStatement instanceof SQLServerInsertStatement) {
            return ((SQLServerInsertStatement) insertStatement).getRowSetFunctionSegment();
        }
        return Optional.empty();
    }
}
