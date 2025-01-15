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

package org.apache.shardingsphere.infra.binder.engine;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.engine.type.DALStatementBindEngine;
import org.apache.shardingsphere.infra.binder.engine.type.DCLStatementBindEngine;
import org.apache.shardingsphere.infra.binder.engine.type.DDLStatementBindEngine;
import org.apache.shardingsphere.infra.binder.engine.type.DMLStatementBindEngine;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.DMLStatement;

import java.util.List;

/**
 * SQL bind engine.
 */
@RequiredArgsConstructor
public final class SQLBindEngine {
    
    private final ShardingSphereMetaData metaData;
    
    private final String currentDatabaseName;
    
    private final HintValueContext hintValueContext;
    
    /**
     * Bind SQL statement.
     *
     * @param sqlStatement SQL statement
     * @param params parameters
     * @return SQL statement context
     */
    public SQLStatementContext bind(final SQLStatement sqlStatement, final List<Object> params) {
        SQLStatement boundSQLStatement = isNeedBind() ? bindSQLStatement(sqlStatement) : sqlStatement;
        return SQLStatementContextFactory.newInstance(metaData, boundSQLStatement, params, currentDatabaseName);
    }
    
    private boolean isNeedBind() {
        return !hintValueContext.findHintDataSourceName().isPresent() && !HintManager.getDataSourceName().isPresent();
    }
    
    private SQLStatement bindSQLStatement(final SQLStatement statement) {
        if (statement instanceof DMLStatement) {
            return new DMLStatementBindEngine(metaData, currentDatabaseName, hintValueContext).bind((DMLStatement) statement);
        }
        if (statement instanceof DDLStatement) {
            return new DDLStatementBindEngine(metaData, currentDatabaseName, hintValueContext).bind((DDLStatement) statement);
        }
        if (statement instanceof DALStatement) {
            return new DALStatementBindEngine(metaData, currentDatabaseName, hintValueContext).bind((DALStatement) statement);
        }
        if (statement instanceof DCLStatement) {
            return new DCLStatementBindEngine(metaData, currentDatabaseName, hintValueContext).bind((DCLStatement) statement);
        }
        return statement;
    }
}
