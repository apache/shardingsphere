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
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SimpleSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.SQLStatementHandler;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSimpleSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.dml.OpenGaussSimpleSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleSimpleSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLSimpleSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dml.SQL92SimpleSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerSimpleSelectStatement;

import java.util.Optional;

/**
 * Select statement helper class for different dialect SQL statements.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SelectStatementHandler implements SQLStatementHandler {
    
    /**
     * Get limit segment.
     *
     * @param simpleSelectStatement select statement
     * @return limit segment
     */
    public static Optional<LimitSegment> getLimitSegment(final SimpleSelectStatement simpleSelectStatement) {
        if (simpleSelectStatement instanceof MySQLSimpleSelectStatement) {
            return ((MySQLSimpleSelectStatement) simpleSelectStatement).getLimit();
        }
        if (simpleSelectStatement instanceof PostgreSQLSimpleSelectStatement) {
            return ((PostgreSQLSimpleSelectStatement) simpleSelectStatement).getLimit();
        }
        if (simpleSelectStatement instanceof SQL92SimpleSelectStatement) {
            return ((SQL92SimpleSelectStatement) simpleSelectStatement).getLimit();
        }
        if (simpleSelectStatement instanceof SQLServerSimpleSelectStatement) {
            return ((SQLServerSimpleSelectStatement) simpleSelectStatement).getLimit();
        }
        if (simpleSelectStatement instanceof OpenGaussSimpleSelectStatement) {
            return ((OpenGaussSimpleSelectStatement) simpleSelectStatement).getLimit();
        }
        return Optional.empty();
    }
    
    /**
     * Set limit segment.
     *
     * @param simpleSelectStatement select statement
     * @param  limitSegment limit segment
     */
    public static void setLimitSegment(final SimpleSelectStatement simpleSelectStatement, final LimitSegment limitSegment) {
        if (simpleSelectStatement instanceof MySQLSimpleSelectStatement) {
            ((MySQLSimpleSelectStatement) simpleSelectStatement).setLimit(limitSegment);
        }
        if (simpleSelectStatement instanceof PostgreSQLSimpleSelectStatement) {
            ((PostgreSQLSimpleSelectStatement) simpleSelectStatement).setLimit(limitSegment);
        }
        if (simpleSelectStatement instanceof SQL92SimpleSelectStatement) {
            ((SQL92SimpleSelectStatement) simpleSelectStatement).setLimit(limitSegment);
        }
        if (simpleSelectStatement instanceof SQLServerSimpleSelectStatement) {
            ((SQLServerSimpleSelectStatement) simpleSelectStatement).setLimit(limitSegment);
        }
        if (simpleSelectStatement instanceof OpenGaussSimpleSelectStatement) {
            ((OpenGaussSimpleSelectStatement) simpleSelectStatement).setLimit(limitSegment);
        }
    }
    
    /**
     * Get lock segment.
     *
     * @param simpleSelectStatement select statement
     * @return lock segment
     */
    public static Optional<LockSegment> getLockSegment(final SimpleSelectStatement simpleSelectStatement) {
        if (simpleSelectStatement instanceof MySQLSimpleSelectStatement) {
            return ((MySQLSimpleSelectStatement) simpleSelectStatement).getLock();
        }
        if (simpleSelectStatement instanceof OracleSimpleSelectStatement) {
            return ((OracleSimpleSelectStatement) simpleSelectStatement).getLock();
        }
        if (simpleSelectStatement instanceof PostgreSQLSimpleSelectStatement) {
            return ((PostgreSQLSimpleSelectStatement) simpleSelectStatement).getLock();
        }
        if (simpleSelectStatement instanceof OpenGaussSimpleSelectStatement) {
            return ((OpenGaussSimpleSelectStatement) simpleSelectStatement).getLock();
        }
        return Optional.empty();
    }
    
    /**
     * Set lock segment.
     *
     * @param simpleSelectStatement select statement
     * @param lockSegment lock segment
     */
    public static void setLockSegment(final SimpleSelectStatement simpleSelectStatement, final LockSegment lockSegment) {
        if (simpleSelectStatement instanceof MySQLSimpleSelectStatement) {
            ((MySQLSimpleSelectStatement) simpleSelectStatement).setLock(lockSegment);
        }
        if (simpleSelectStatement instanceof OracleSimpleSelectStatement) {
            ((OracleSimpleSelectStatement) simpleSelectStatement).setLock(lockSegment);
        }
        if (simpleSelectStatement instanceof PostgreSQLSimpleSelectStatement) {
            ((PostgreSQLSimpleSelectStatement) simpleSelectStatement).setLock(lockSegment);
        }
        if (simpleSelectStatement instanceof OpenGaussSimpleSelectStatement) {
            ((OpenGaussSimpleSelectStatement) simpleSelectStatement).setLock(lockSegment);
        }
    }
    
    /**
     * Get window segment.
     *
     * @param simpleSelectStatement select statement
     * @return window segment
     */
    public static Optional<WindowSegment> getWindowSegment(final SimpleSelectStatement simpleSelectStatement) {
        if (simpleSelectStatement instanceof MySQLSimpleSelectStatement) {
            return ((MySQLSimpleSelectStatement) simpleSelectStatement).getWindow();
        }
        if (simpleSelectStatement instanceof PostgreSQLSimpleSelectStatement) {
            return ((PostgreSQLSimpleSelectStatement) simpleSelectStatement).getWindow();
        }
        if (simpleSelectStatement instanceof OpenGaussSimpleSelectStatement) {
            return ((OpenGaussSimpleSelectStatement) simpleSelectStatement).getWindow();
        }
        return Optional.empty();
    }
    
    /**
     * Set window segment.
     *
     * @param simpleSelectStatement select statement
     * @param windowSegment window segment
     */
    public static void setWindowSegment(final SimpleSelectStatement simpleSelectStatement, final WindowSegment windowSegment) {
        if (simpleSelectStatement instanceof MySQLSimpleSelectStatement) {
            ((MySQLSimpleSelectStatement) simpleSelectStatement).setWindow(windowSegment);
        }
        if (simpleSelectStatement instanceof PostgreSQLSimpleSelectStatement) {
            ((PostgreSQLSimpleSelectStatement) simpleSelectStatement).setWindow(windowSegment);
        }
        if (simpleSelectStatement instanceof OpenGaussSimpleSelectStatement) {
            ((OpenGaussSimpleSelectStatement) simpleSelectStatement).setWindow(windowSegment);
        }
    }
    
    /**
     * Get with segment.
     *
     * @param simpleSelectStatement select statement
     * @return with segment
     */
    public static Optional<WithSegment> getWithSegment(final SimpleSelectStatement simpleSelectStatement) {
        if (simpleSelectStatement instanceof OracleSimpleSelectStatement) {
            return ((OracleSimpleSelectStatement) simpleSelectStatement).getWithSegment();
        }
        if (simpleSelectStatement instanceof SQLServerSimpleSelectStatement) {
            return ((SQLServerSimpleSelectStatement) simpleSelectStatement).getWithSegment();
        }
        if (simpleSelectStatement instanceof MySQLSimpleSelectStatement) {
            return ((MySQLSimpleSelectStatement) simpleSelectStatement).getWithSegment();
        }
        return Optional.empty();
    }
    
    /**
     * Set with segment.
     *
     * @param simpleSelectStatement select statement
     * @param withSegment with segment
     */
    public static void setWithSegment(final SimpleSelectStatement simpleSelectStatement, final WithSegment withSegment) {
        if (simpleSelectStatement instanceof OracleSimpleSelectStatement) {
            ((OracleSimpleSelectStatement) simpleSelectStatement).setWithSegment(withSegment);
        }
        if (simpleSelectStatement instanceof SQLServerSimpleSelectStatement) {
            ((SQLServerSimpleSelectStatement) simpleSelectStatement).setWithSegment(withSegment);
        }
        if (simpleSelectStatement instanceof MySQLSimpleSelectStatement) {
            ((MySQLSimpleSelectStatement) simpleSelectStatement).setWithSegment(withSegment);
        }
    }
    
    /**
     * Get model segment.
     *
     * @param simpleSelectStatement select statement
     * @return model segment
     */
    public static Optional<ModelSegment> getModelSegment(final SimpleSelectStatement simpleSelectStatement) {
        if (simpleSelectStatement instanceof OracleSimpleSelectStatement) {
            return ((OracleSimpleSelectStatement) simpleSelectStatement).getModelSegment();
        }
        return Optional.empty();
    }
    
    /**
     * Set model segment.
     *
     * @param simpleSelectStatement select statement
     * @param modelSegment model segment
     */
    public static void setModelSegment(final SimpleSelectStatement simpleSelectStatement, final ModelSegment modelSegment) {
        if (simpleSelectStatement instanceof OracleSimpleSelectStatement) {
            ((OracleSimpleSelectStatement) simpleSelectStatement).setModelSegment(modelSegment);
        }
    }
    
    /**
     * Get into segment.
     *
     * @param simpleSelectStatement select statement
     * @return into table segment
     */
    public static Optional<TableSegment> getIntoSegment(final SimpleSelectStatement simpleSelectStatement) {
        if (simpleSelectStatement instanceof SQLServerSimpleSelectStatement) {
            return ((SQLServerSimpleSelectStatement) simpleSelectStatement).getIntoSegment();
        } else if (simpleSelectStatement instanceof PostgreSQLSimpleSelectStatement) {
            return ((PostgreSQLSimpleSelectStatement) simpleSelectStatement).getIntoSegment();
        } else if (simpleSelectStatement instanceof OpenGaussSimpleSelectStatement) {
            return ((OpenGaussSimpleSelectStatement) simpleSelectStatement).getIntoSegment();
        }
        return Optional.empty();
    }
    
    /**
     * Set into segment.
     *
     * @param simpleSelectStatement select statement
     * @param intoSegment table into segment
     */
    public static void setIntoSegment(final SimpleSelectStatement simpleSelectStatement, final TableSegment intoSegment) {
        if (simpleSelectStatement instanceof SQLServerSimpleSelectStatement) {
            ((SQLServerSimpleSelectStatement) simpleSelectStatement).setIntoSegment(intoSegment);
        } else if (simpleSelectStatement instanceof PostgreSQLSimpleSelectStatement) {
            ((PostgreSQLSimpleSelectStatement) simpleSelectStatement).setIntoSegment(intoSegment);
        } else if (simpleSelectStatement instanceof OpenGaussSimpleSelectStatement) {
            ((OpenGaussSimpleSelectStatement) simpleSelectStatement).setIntoSegment(intoSegment);
        }
    }
}
