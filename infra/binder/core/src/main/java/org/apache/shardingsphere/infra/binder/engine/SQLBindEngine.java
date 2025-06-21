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
import org.apache.shardingsphere.infra.binder.context.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.engine.type.DALStatementBindEngine;
import org.apache.shardingsphere.infra.binder.engine.type.DCLStatementBindEngine;
import org.apache.shardingsphere.infra.binder.engine.type.DDLStatementBindEngine;
import org.apache.shardingsphere.infra.binder.engine.type.DMLStatementBindEngine;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DMLStatement;

import java.util.List;
import java.util.Optional;

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
     * @param databaseType database type
     * @param sqlStatement SQL statement
     * @param params parameters
     * @return SQL statement context
     */
    public SQLStatementContext bind(final DatabaseType databaseType, final SQLStatement sqlStatement, final List<Object> params) {
        SQLStatement boundSQLStatement = isNeedBind() ? bind(databaseType, sqlStatement) : sqlStatement;
        return SQLStatementContextFactory.newInstance(metaData, databaseType, boundSQLStatement, params, currentDatabaseName);
    }
    
    private SQLStatement bind(final DatabaseType databaseType, final SQLStatement statement) {
        SQLStatementBinderContext binderContext = new SQLStatementBinderContext(metaData, currentDatabaseName, hintValueContext, databaseType, statement);
        Optional<DialectSQLBindEngine> dialectSQLBindEngine = DatabaseTypedSPILoader.findService(DialectSQLBindEngine.class, databaseType);
        if (dialectSQLBindEngine.isPresent()) {
            Optional<SQLStatement> boundSQLStatement = dialectSQLBindEngine.get().bind(statement, binderContext);
            if (boundSQLStatement.isPresent()) {
                return boundSQLStatement.get();
            }
        }
        if (statement instanceof DMLStatement) {
            return new DMLStatementBindEngine().bind((DMLStatement) statement, binderContext);
        }
        if (statement instanceof DDLStatement) {
            return new DDLStatementBindEngine().bind((DDLStatement) statement, binderContext);
        }
        if (statement instanceof DALStatement) {
            return new DALStatementBindEngine().bind((DALStatement) statement, binderContext);
        }
        if (statement instanceof DCLStatement) {
            return new DCLStatementBindEngine().bind((DCLStatement) statement, binderContext);
        }
        return statement;
    }
    
    private boolean isNeedBind() {
        return !hintValueContext.findHintDataSourceName().isPresent() && !HintManager.getDataSourceName().isPresent();
    }
}
