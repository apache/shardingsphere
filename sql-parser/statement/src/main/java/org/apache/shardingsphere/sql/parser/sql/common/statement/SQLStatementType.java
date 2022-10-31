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

package org.apache.shardingsphere.sql.parser.sql.common.statement;

import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SQL statement type.
 */
public enum SQLStatementType {
    
    DML, DDL, TCL, DCL, DAL, RL;
    
    private static final Collection<Class<? extends SQLStatement>> INVOLVE_DATA_CHANGES_STATEMENTS = Collections.newSetFromMap(new ConcurrentHashMap<>());
    
    private static final Collection<Class<? extends SQLStatement>> NOT_INVOLVE_DATA_CHANGES_STATEMENTS = Collections.newSetFromMap(new ConcurrentHashMap<>());
    
    /**
     * Judge whether involves data changes.
     *
     * @param sqlStatement SQL statement
     * @return is statement involves data changes or not
     */
    public static boolean involvesDataChanges(final SQLStatement sqlStatement) {
        Class<? extends SQLStatement> sqlStatementClass = sqlStatement.getClass();
        if (NOT_INVOLVE_DATA_CHANGES_STATEMENTS.contains(sqlStatementClass)) {
            return false;
        }
        if (INVOLVE_DATA_CHANGES_STATEMENTS.contains(sqlStatementClass)) {
            return true;
        }
        if (sqlStatement instanceof SelectStatement) {
            NOT_INVOLVE_DATA_CHANGES_STATEMENTS.add(sqlStatementClass);
            return false;
        }
        if (sqlStatement instanceof DMLStatement || sqlStatement instanceof DDLStatement) {
            INVOLVE_DATA_CHANGES_STATEMENTS.add(sqlStatementClass);
            return true;
        }
        NOT_INVOLVE_DATA_CHANGES_STATEMENTS.add(sqlStatementClass);
        return false;
    }
}
