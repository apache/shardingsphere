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
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.GenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.SQLStatementHandler;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLGenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dml.OpenGaussGenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleGenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLGenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dml.SQL92GenericSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerGenericSelectStatement;

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
    public static Optional<LimitSegment> getLimitSegment(final GenericSelectStatement selectStatement) {
        if (selectStatement instanceof MySQLGenericSelectStatement) {
            return ((MySQLGenericSelectStatement) selectStatement).getLimit();
        }
        if (selectStatement instanceof PostgreSQLGenericSelectStatement) {
            return ((PostgreSQLGenericSelectStatement) selectStatement).getLimit();
        }
        if (selectStatement instanceof SQL92GenericSelectStatement) {
            return ((SQL92GenericSelectStatement) selectStatement).getLimit();
        }
        if (selectStatement instanceof SQLServerGenericSelectStatement) {
            return ((SQLServerGenericSelectStatement) selectStatement).getLimit();
        }
        if (selectStatement instanceof OpenGaussGenericSelectStatement) {
            return ((OpenGaussGenericSelectStatement) selectStatement).getLimit();
        }
        return Optional.empty();
    }
    
    /**
     * Set limit segment.
     *
     * @param selectStatement select statement
     * @param  limitSegment limit segment
     */
    public static void setLimitSegment(final GenericSelectStatement selectStatement, final LimitSegment limitSegment) {
        if (selectStatement instanceof MySQLGenericSelectStatement) {
            ((MySQLGenericSelectStatement) selectStatement).setLimit(limitSegment);
        }
        if (selectStatement instanceof PostgreSQLGenericSelectStatement) {
            ((PostgreSQLGenericSelectStatement) selectStatement).setLimit(limitSegment);
        }
        if (selectStatement instanceof SQL92GenericSelectStatement) {
            ((SQL92GenericSelectStatement) selectStatement).setLimit(limitSegment);
        }
        if (selectStatement instanceof SQLServerGenericSelectStatement) {
            ((SQLServerGenericSelectStatement) selectStatement).setLimit(limitSegment);
        }
        if (selectStatement instanceof OpenGaussGenericSelectStatement) {
            ((OpenGaussGenericSelectStatement) selectStatement).setLimit(limitSegment);
        }
    }
    
    /**
     * Get lock segment.
     *
     * @param selectStatement select statement
     * @return lock segment
     */
    public static Optional<LockSegment> getLockSegment(final GenericSelectStatement selectStatement) {
        if (selectStatement instanceof MySQLGenericSelectStatement) {
            return ((MySQLGenericSelectStatement) selectStatement).getLock();
        }
        if (selectStatement instanceof OracleGenericSelectStatement) {
            return ((OracleGenericSelectStatement) selectStatement).getLock();
        }
        if (selectStatement instanceof PostgreSQLGenericSelectStatement) {
            return ((PostgreSQLGenericSelectStatement) selectStatement).getLock();
        }
        if (selectStatement instanceof OpenGaussGenericSelectStatement) {
            return ((OpenGaussGenericSelectStatement) selectStatement).getLock();
        }
        return Optional.empty();
    }
    
    /**
     * Set lock segment.
     *
     * @param selectStatement select statement
     * @param lockSegment lock segment
     */
    public static void setLockSegment(final GenericSelectStatement selectStatement, final LockSegment lockSegment) {
        if (selectStatement instanceof MySQLGenericSelectStatement) {
            ((MySQLGenericSelectStatement) selectStatement).setLock(lockSegment);
        }
        if (selectStatement instanceof OracleGenericSelectStatement) {
            ((OracleGenericSelectStatement) selectStatement).setLock(lockSegment);
        }
        if (selectStatement instanceof PostgreSQLGenericSelectStatement) {
            ((PostgreSQLGenericSelectStatement) selectStatement).setLock(lockSegment);
        }
        if (selectStatement instanceof OpenGaussGenericSelectStatement) {
            ((OpenGaussGenericSelectStatement) selectStatement).setLock(lockSegment);
        }
    }
    
    /**
     * Get window segment.
     *
     * @param selectStatement select statement
     * @return window segment
     */
    public static Optional<WindowSegment> getWindowSegment(final GenericSelectStatement selectStatement) {
        if (selectStatement instanceof MySQLGenericSelectStatement) {
            return ((MySQLGenericSelectStatement) selectStatement).getWindow();
        }
        if (selectStatement instanceof PostgreSQLGenericSelectStatement) {
            return ((PostgreSQLGenericSelectStatement) selectStatement).getWindow();
        }
        if (selectStatement instanceof OpenGaussGenericSelectStatement) {
            return ((OpenGaussGenericSelectStatement) selectStatement).getWindow();
        }
        return Optional.empty();
    }
    
    /**
     * Set window segment.
     *
     * @param selectStatement select statement
     * @param windowSegment window segment
     */
    public static void setWindowSegment(final GenericSelectStatement selectStatement, final WindowSegment windowSegment) {
        if (selectStatement instanceof MySQLGenericSelectStatement) {
            ((MySQLGenericSelectStatement) selectStatement).setWindow(windowSegment);
        }
        if (selectStatement instanceof PostgreSQLGenericSelectStatement) {
            ((PostgreSQLGenericSelectStatement) selectStatement).setWindow(windowSegment);
        }
        if (selectStatement instanceof OpenGaussGenericSelectStatement) {
            ((OpenGaussGenericSelectStatement) selectStatement).setWindow(windowSegment);
        }
    }
    
    /**
     * Get with segment.
     *
     * @param selectStatement select statement
     * @return with segment
     */
    public static Optional<WithSegment> getWithSegment(final GenericSelectStatement selectStatement) {
        if (selectStatement instanceof OracleGenericSelectStatement) {
            return ((OracleGenericSelectStatement) selectStatement).getWithSegment();
        }
        if (selectStatement instanceof SQLServerGenericSelectStatement) {
            return ((SQLServerGenericSelectStatement) selectStatement).getWithSegment();
        }
        if (selectStatement instanceof MySQLGenericSelectStatement) {
            return ((MySQLGenericSelectStatement) selectStatement).getWithSegment();
        }
        return Optional.empty();
    }
    
    /**
     * Set with segment.
     *
     * @param selectStatement select statement
     * @param withSegment with segment
     */
    public static void setWithSegment(final GenericSelectStatement selectStatement, final WithSegment withSegment) {
        if (selectStatement instanceof OracleGenericSelectStatement) {
            ((OracleGenericSelectStatement) selectStatement).setWithSegment(withSegment);
        }
        if (selectStatement instanceof SQLServerGenericSelectStatement) {
            ((SQLServerGenericSelectStatement) selectStatement).setWithSegment(withSegment);
        }
        if (selectStatement instanceof MySQLGenericSelectStatement) {
            ((MySQLGenericSelectStatement) selectStatement).setWithSegment(withSegment);
        }
    }
    
    /**
     * Get model segment.
     *
     * @param selectStatement select statement
     * @return model segment
     */
    public static Optional<ModelSegment> getModelSegment(final GenericSelectStatement selectStatement) {
        if (selectStatement instanceof OracleGenericSelectStatement) {
            return ((OracleGenericSelectStatement) selectStatement).getModelSegment();
        }
        return Optional.empty();
    }
    
    /**
     * Set model segment.
     *
     * @param selectStatement select statement
     * @param modelSegment model segment
     */
    public static void setModelSegment(final GenericSelectStatement selectStatement, final ModelSegment modelSegment) {
        if (selectStatement instanceof OracleGenericSelectStatement) {
            ((OracleGenericSelectStatement) selectStatement).setModelSegment(modelSegment);
        }
    }
    
    /**
     * Get into segment.
     *
     * @param selectStatement select statement
     * @return into table segment
     */
    public static Optional<TableSegment> getIntoSegment(final GenericSelectStatement selectStatement) {
        if (selectStatement instanceof SQLServerGenericSelectStatement) {
            return ((SQLServerGenericSelectStatement) selectStatement).getIntoSegment();
        } else if (selectStatement instanceof PostgreSQLGenericSelectStatement) {
            return ((PostgreSQLGenericSelectStatement) selectStatement).getIntoSegment();
        } else if (selectStatement instanceof OpenGaussGenericSelectStatement) {
            return ((OpenGaussGenericSelectStatement) selectStatement).getIntoSegment();
        }
        return Optional.empty();
    }
    
    /**
     * Set into segment.
     *
     * @param selectStatement select statement
     * @param intoSegment table into segment
     */
    public static void setIntoSegment(final GenericSelectStatement selectStatement, final TableSegment intoSegment) {
        if (selectStatement instanceof SQLServerGenericSelectStatement) {
            ((SQLServerGenericSelectStatement) selectStatement).setIntoSegment(intoSegment);
        } else if (selectStatement instanceof PostgreSQLGenericSelectStatement) {
            ((PostgreSQLGenericSelectStatement) selectStatement).setIntoSegment(intoSegment);
        } else if (selectStatement instanceof OpenGaussGenericSelectStatement) {
            ((OpenGaussGenericSelectStatement) selectStatement).setIntoSegment(intoSegment);
        }
    }
}
