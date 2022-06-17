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

package org.apache.shardingsphere.mode.manager.lock;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lock judge engine for ShardingSphere.
 */
@RequiredArgsConstructor
public final class ShardingSphereLockJudgeEngine implements LockJudgeEngine {
    
    private static final Set<Class<? extends SQLStatement>> IGNORABLE_SQL_STATEMENT_CLASSES_STOP_WRITING = Collections.newSetFromMap(new ConcurrentHashMap<>());
    
    private LockContext lockContext;
    
    @Override
    public void init(final LockContext lockContext) {
        this.lockContext = lockContext;
    }
    
    /**
     * Is locked.
     *
     * @param databaseName database name
     * @param sqlStatementContext sql statement context
     * @return is locked or not
     */
    @Override
    public boolean isLocked(final String databaseName, final SQLStatementContext<?> sqlStatementContext) {
        if (isWriteStatement(sqlStatementContext.getSqlStatement())) {
            return lockContext.isLocked(databaseName);
        }
        return false;
    }
    
    private boolean isWriteStatement(final SQLStatement sqlStatement) {
        Class<? extends SQLStatement> sqlStatementClass = sqlStatement.getClass();
        if (IGNORABLE_SQL_STATEMENT_CLASSES_STOP_WRITING.contains(sqlStatementClass)) {
            return false;
        }
        if (sqlStatement instanceof SelectStatement) {
            catchIgnorable(sqlStatementClass);
            return false;
        }
        if (sqlStatement instanceof DMLStatement) {
            return true;
        }
        if (sqlStatement instanceof DDLStatement) {
            return true;
        }
        catchIgnorable(sqlStatementClass);
        return false;
    }
    
    private void catchIgnorable(final Class<? extends SQLStatement> sqlStatementClass) {
        IGNORABLE_SQL_STATEMENT_CLASSES_STOP_WRITING.add(sqlStatementClass);
    }
}
