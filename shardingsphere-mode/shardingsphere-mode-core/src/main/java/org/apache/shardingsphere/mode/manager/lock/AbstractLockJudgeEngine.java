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

import lombok.Getter;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract lock judge engine.
 */
public abstract class AbstractLockJudgeEngine implements LockJudgeEngine {
    
    private static final Set<Class<? extends SQLStatement>> IGNORABLE_SQL_STATEMENT_CLASSES_STOP_WRITING = Collections.newSetFromMap(new ConcurrentHashMap<>());
    
    @Getter
    private LockContext lockContext;
    
    @Override
    public void init(final LockContext lockContext) {
        this.lockContext = lockContext;
    }
    
    /**
     * Is write statement.
     *
     * @param sqlStatement sql statement
     * @return is write statement or not
     */
    protected boolean isWriteStatement(final SQLStatement sqlStatement) {
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
