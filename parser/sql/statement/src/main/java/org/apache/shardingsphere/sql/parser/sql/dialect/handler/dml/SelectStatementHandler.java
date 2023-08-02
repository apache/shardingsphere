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
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.LockSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.ModelSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.WindowSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.SQLStatementHandler;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dml.OpenGaussSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dml.SQL92SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerSelectStatement;

import java.util.Optional;

/**
 * Select statement helper class for different dialect SQL statements.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SelectStatementHandler implements SQLStatementHandler {
    
    /**
     * Get limit segment.
     *
     * @param selectStatement select statement
     * @return limit segment
     */
    public static Optional<LimitSegment> getLimitSegment(final SelectStatement selectStatement) {
        if (selectStatement instanceof MySQLSelectStatement) {
            return ((MySQLSelectStatement) selectStatement).getLimit();
        }
        if (selectStatement instanceof PostgreSQLSelectStatement) {
            return ((PostgreSQLSelectStatement) selectStatement).getLimit();
        }
        if (selectStatement instanceof SQL92SelectStatement) {
            return ((SQL92SelectStatement) selectStatement).getLimit();
        }
        if (selectStatement instanceof SQLServerSelectStatement) {
            return ((SQLServerSelectStatement) selectStatement).getLimit();
        }
        if (selectStatement instanceof OpenGaussSelectStatement) {
            return ((OpenGaussSelectStatement) selectStatement).getLimit();
        }
        return Optional.empty();
    }
    
    /**
     * Set limit segment.
     *
     * @param selectStatement select statement
     * @param  limitSegment limit segment
     */
    public static void setLimitSegment(final SelectStatement selectStatement, final LimitSegment limitSegment) {
        if (selectStatement instanceof MySQLSelectStatement) {
            ((MySQLSelectStatement) selectStatement).setLimit(limitSegment);
        }
        if (selectStatement instanceof PostgreSQLSelectStatement) {
            ((PostgreSQLSelectStatement) selectStatement).setLimit(limitSegment);
        }
        if (selectStatement instanceof SQL92SelectStatement) {
            ((SQL92SelectStatement) selectStatement).setLimit(limitSegment);
        }
        if (selectStatement instanceof SQLServerSelectStatement) {
            ((SQLServerSelectStatement) selectStatement).setLimit(limitSegment);
        }
        if (selectStatement instanceof OpenGaussSelectStatement) {
            ((OpenGaussSelectStatement) selectStatement).setLimit(limitSegment);
        }
    }
    
    /**
     * Get lock segment.
     *
     * @param selectStatement select statement
     * @return lock segment
     */
    public static Optional<LockSegment> getLockSegment(final SelectStatement selectStatement) {
        if (selectStatement instanceof MySQLSelectStatement) {
            return ((MySQLSelectStatement) selectStatement).getLock();
        }
        if (selectStatement instanceof OracleSelectStatement) {
            return ((OracleSelectStatement) selectStatement).getLock();
        }
        if (selectStatement instanceof PostgreSQLSelectStatement) {
            return ((PostgreSQLSelectStatement) selectStatement).getLock();
        }
        if (selectStatement instanceof OpenGaussSelectStatement) {
            return ((OpenGaussSelectStatement) selectStatement).getLock();
        }
        return Optional.empty();
    }
    
    /**
     * Set lock segment.
     * 
     * @param selectStatement select statement
     * @param lockSegment lock segment
     */
    public static void setLockSegment(final SelectStatement selectStatement, final LockSegment lockSegment) {
        if (selectStatement instanceof MySQLSelectStatement) {
            ((MySQLSelectStatement) selectStatement).setLock(lockSegment);
        }
        if (selectStatement instanceof OracleSelectStatement) {
            ((OracleSelectStatement) selectStatement).setLock(lockSegment);
        }
        if (selectStatement instanceof PostgreSQLSelectStatement) {
            ((PostgreSQLSelectStatement) selectStatement).setLock(lockSegment);
        }
        if (selectStatement instanceof OpenGaussSelectStatement) {
            ((OpenGaussSelectStatement) selectStatement).setLock(lockSegment);
        }
    }
    
    /**
     * Get window segment.
     *
     * @param selectStatement select statement
     * @return window segment
     */
    public static Optional<WindowSegment> getWindowSegment(final SelectStatement selectStatement) {
        if (selectStatement instanceof MySQLSelectStatement) {
            return ((MySQLSelectStatement) selectStatement).getWindow();
        }
        if (selectStatement instanceof PostgreSQLSelectStatement) {
            return ((PostgreSQLSelectStatement) selectStatement).getWindow();
        }
        if (selectStatement instanceof OpenGaussSelectStatement) {
            return ((OpenGaussSelectStatement) selectStatement).getWindow();
        }
        return Optional.empty();
    }
    
    /**
     * Set window segment.
     *
     * @param selectStatement select statement
     * @param windowSegment window segment
     */
    public static void setWindowSegment(final SelectStatement selectStatement, final WindowSegment windowSegment) {
        if (selectStatement instanceof MySQLSelectStatement) {
            ((MySQLSelectStatement) selectStatement).setWindow(windowSegment);
        }
        if (selectStatement instanceof PostgreSQLSelectStatement) {
            ((PostgreSQLSelectStatement) selectStatement).setWindow(windowSegment);
        }
        if (selectStatement instanceof OpenGaussSelectStatement) {
            ((OpenGaussSelectStatement) selectStatement).setWindow(windowSegment);
        }
    }
    
    /**
     * Get with segment.
     *
     * @param selectStatement select statement
     * @return with segment
     */
    public static Optional<WithSegment> getWithSegment(final SelectStatement selectStatement) {
        if (selectStatement instanceof OracleSelectStatement) {
            return ((OracleSelectStatement) selectStatement).getWithSegment();
        }
        if (selectStatement instanceof SQLServerSelectStatement) {
            return ((SQLServerSelectStatement) selectStatement).getWithSegment();
        }
        return Optional.empty();
    }
    
    /**
     * Set with segment.
     * 
     * @param selectStatement select statement
     * @param withSegment with segment
     */
    public static void setWithSegment(final SelectStatement selectStatement, final WithSegment withSegment) {
        if (selectStatement instanceof OracleSelectStatement) {
            ((OracleSelectStatement) selectStatement).setWithSegment(withSegment);
        }
        if (selectStatement instanceof SQLServerSelectStatement) {
            ((SQLServerSelectStatement) selectStatement).setWithSegment(withSegment);
        }
    }
    
    /**
     * Get model segment.
     *
     * @param selectStatement select statement
     * @return model segment
     */
    public static Optional<ModelSegment> getModelSegment(final SelectStatement selectStatement) {
        if (selectStatement instanceof OracleSelectStatement) {
            return ((OracleSelectStatement) selectStatement).getModelSegment();
        }
        return Optional.empty();
    }
    
    /**
     * Set model segment.
     * 
     * @param selectStatement select statement
     * @param modelSegment model segment
     */
    public static void setModelSegment(final SelectStatement selectStatement, final ModelSegment modelSegment) {
        if (selectStatement instanceof OracleSelectStatement) {
            ((OracleSelectStatement) selectStatement).setModelSegment(modelSegment);
        }
    }
}
