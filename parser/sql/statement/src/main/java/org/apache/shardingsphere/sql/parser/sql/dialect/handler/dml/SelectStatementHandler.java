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
     * @param selectStatement select statement
     * @return limit segment
     */
    public static Optional<LimitSegment> getLimitSegment(final SimpleSelectStatement selectStatement) {
        if (selectStatement instanceof MySQLSimpleSelectStatement) {
            return ((MySQLSimpleSelectStatement) selectStatement).getLimit();
        }
        if (selectStatement instanceof PostgreSQLSimpleSelectStatement) {
            return ((PostgreSQLSimpleSelectStatement) selectStatement).getLimit();
        }
        if (selectStatement instanceof SQL92SimpleSelectStatement) {
            return ((SQL92SimpleSelectStatement) selectStatement).getLimit();
        }
        if (selectStatement instanceof SQLServerSimpleSelectStatement) {
            return ((SQLServerSimpleSelectStatement) selectStatement).getLimit();
        }
        if (selectStatement instanceof OpenGaussSimpleSelectStatement) {
            return ((OpenGaussSimpleSelectStatement) selectStatement).getLimit();
        }
        return Optional.empty();
    }
    
    /**
     * Set limit segment.
     *
     * @param selectStatement select statement
     * @param  limitSegment limit segment
     */
    public static void setLimitSegment(final SimpleSelectStatement selectStatement, final LimitSegment limitSegment) {
        if (selectStatement instanceof MySQLSimpleSelectStatement) {
            ((MySQLSimpleSelectStatement) selectStatement).setLimit(limitSegment);
        }
        if (selectStatement instanceof PostgreSQLSimpleSelectStatement) {
            ((PostgreSQLSimpleSelectStatement) selectStatement).setLimit(limitSegment);
        }
        if (selectStatement instanceof SQL92SimpleSelectStatement) {
            ((SQL92SimpleSelectStatement) selectStatement).setLimit(limitSegment);
        }
        if (selectStatement instanceof SQLServerSimpleSelectStatement) {
            ((SQLServerSimpleSelectStatement) selectStatement).setLimit(limitSegment);
        }
        if (selectStatement instanceof OpenGaussSimpleSelectStatement) {
            ((OpenGaussSimpleSelectStatement) selectStatement).setLimit(limitSegment);
        }
    }
    
    /**
     * Get lock segment.
     *
     * @param selectStatement select statement
     * @return lock segment
     */
    public static Optional<LockSegment> getLockSegment(final SimpleSelectStatement selectStatement) {
        if (selectStatement instanceof MySQLSimpleSelectStatement) {
            return ((MySQLSimpleSelectStatement) selectStatement).getLock();
        }
        if (selectStatement instanceof OracleSimpleSelectStatement) {
            return ((OracleSimpleSelectStatement) selectStatement).getLock();
        }
        if (selectStatement instanceof PostgreSQLSimpleSelectStatement) {
            return ((PostgreSQLSimpleSelectStatement) selectStatement).getLock();
        }
        if (selectStatement instanceof OpenGaussSimpleSelectStatement) {
            return ((OpenGaussSimpleSelectStatement) selectStatement).getLock();
        }
        return Optional.empty();
    }
    
    /**
     * Set lock segment.
     *
     * @param selectStatement select statement
     * @param lockSegment lock segment
     */
    public static void setLockSegment(final SimpleSelectStatement selectStatement, final LockSegment lockSegment) {
        if (selectStatement instanceof MySQLSimpleSelectStatement) {
            ((MySQLSimpleSelectStatement) selectStatement).setLock(lockSegment);
        }
        if (selectStatement instanceof OracleSimpleSelectStatement) {
            ((OracleSimpleSelectStatement) selectStatement).setLock(lockSegment);
        }
        if (selectStatement instanceof PostgreSQLSimpleSelectStatement) {
            ((PostgreSQLSimpleSelectStatement) selectStatement).setLock(lockSegment);
        }
        if (selectStatement instanceof OpenGaussSimpleSelectStatement) {
            ((OpenGaussSimpleSelectStatement) selectStatement).setLock(lockSegment);
        }
    }
    
    /**
     * Get window segment.
     *
     * @param selectStatement select statement
     * @return window segment
     */
    public static Optional<WindowSegment> getWindowSegment(final SimpleSelectStatement selectStatement) {
        if (selectStatement instanceof MySQLSimpleSelectStatement) {
            return ((MySQLSimpleSelectStatement) selectStatement).getWindow();
        }
        if (selectStatement instanceof PostgreSQLSimpleSelectStatement) {
            return ((PostgreSQLSimpleSelectStatement) selectStatement).getWindow();
        }
        if (selectStatement instanceof OpenGaussSimpleSelectStatement) {
            return ((OpenGaussSimpleSelectStatement) selectStatement).getWindow();
        }
        return Optional.empty();
    }
    
    /**
     * Set window segment.
     *
     * @param selectStatement select statement
     * @param windowSegment window segment
     */
    public static void setWindowSegment(final SimpleSelectStatement selectStatement, final WindowSegment windowSegment) {
        if (selectStatement instanceof MySQLSimpleSelectStatement) {
            ((MySQLSimpleSelectStatement) selectStatement).setWindow(windowSegment);
        }
        if (selectStatement instanceof PostgreSQLSimpleSelectStatement) {
            ((PostgreSQLSimpleSelectStatement) selectStatement).setWindow(windowSegment);
        }
        if (selectStatement instanceof OpenGaussSimpleSelectStatement) {
            ((OpenGaussSimpleSelectStatement) selectStatement).setWindow(windowSegment);
        }
    }
    
    /**
     * Get with segment.
     *
     * @param selectStatement select statement
     * @return with segment
     */
    public static Optional<WithSegment> getWithSegment(final SimpleSelectStatement selectStatement) {
        if (selectStatement instanceof OracleSimpleSelectStatement) {
            return ((OracleSimpleSelectStatement) selectStatement).getWithSegment();
        }
        if (selectStatement instanceof SQLServerSimpleSelectStatement) {
            return ((SQLServerSimpleSelectStatement) selectStatement).getWithSegment();
        }
        if (selectStatement instanceof MySQLSimpleSelectStatement) {
            return ((MySQLSimpleSelectStatement) selectStatement).getWithSegment();
        }
        return Optional.empty();
    }
    
    /**
     * Set with segment.
     *
     * @param selectStatement select statement
     * @param withSegment with segment
     */
    public static void setWithSegment(final SimpleSelectStatement selectStatement, final WithSegment withSegment) {
        if (selectStatement instanceof OracleSimpleSelectStatement) {
            ((OracleSimpleSelectStatement) selectStatement).setWithSegment(withSegment);
        }
        if (selectStatement instanceof SQLServerSimpleSelectStatement) {
            ((SQLServerSimpleSelectStatement) selectStatement).setWithSegment(withSegment);
        }
        if (selectStatement instanceof MySQLSimpleSelectStatement) {
            ((MySQLSimpleSelectStatement) selectStatement).setWithSegment(withSegment);
        }
    }
    
    /**
     * Get model segment.
     *
     * @param selectStatement select statement
     * @return model segment
     */
    public static Optional<ModelSegment> getModelSegment(final SimpleSelectStatement selectStatement) {
        if (selectStatement instanceof OracleSimpleSelectStatement) {
            return ((OracleSimpleSelectStatement) selectStatement).getModelSegment();
        }
        return Optional.empty();
    }
    
    /**
     * Set model segment.
     *
     * @param selectStatement select statement
     * @param modelSegment model segment
     */
    public static void setModelSegment(final SimpleSelectStatement selectStatement, final ModelSegment modelSegment) {
        if (selectStatement instanceof OracleSimpleSelectStatement) {
            ((OracleSimpleSelectStatement) selectStatement).setModelSegment(modelSegment);
        }
    }
    
    /**
     * Get into segment.
     *
     * @param selectStatement select statement
     * @return into table segment
     */
    public static Optional<TableSegment> getIntoSegment(final SimpleSelectStatement selectStatement) {
        if (selectStatement instanceof SQLServerSimpleSelectStatement) {
            return ((SQLServerSimpleSelectStatement) selectStatement).getIntoSegment();
        } else if (selectStatement instanceof PostgreSQLSimpleSelectStatement) {
            return ((PostgreSQLSimpleSelectStatement) selectStatement).getIntoSegment();
        } else if (selectStatement instanceof OpenGaussSimpleSelectStatement) {
            return ((OpenGaussSimpleSelectStatement) selectStatement).getIntoSegment();
        }
        return Optional.empty();
    }
    
    /**
     * Set into segment.
     *
     * @param selectStatement select statement
     * @param intoSegment table into segment
     */
    public static void setIntoSegment(final SimpleSelectStatement selectStatement, final TableSegment intoSegment) {
        if (selectStatement instanceof SQLServerSimpleSelectStatement) {
            ((SQLServerSimpleSelectStatement) selectStatement).setIntoSegment(intoSegment);
        } else if (selectStatement instanceof PostgreSQLSimpleSelectStatement) {
            ((PostgreSQLSimpleSelectStatement) selectStatement).setIntoSegment(intoSegment);
        } else if (selectStatement instanceof OpenGaussSimpleSelectStatement) {
            ((OpenGaussSimpleSelectStatement) selectStatement).setIntoSegment(intoSegment);
        }
    }
}
