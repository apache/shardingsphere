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

package org.apache.shardingsphere.infra.binder.engine.type;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.dal.ExplainStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.dal.OptimizeTableStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.dal.ShowColumnsStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.dal.ShowCreateTableStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.dal.ShowIndexStatementBinder;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ExplainStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.OptimizeTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowColumnsStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowCreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowIndexStatement;

/**
 * DAL statement bind engine.
 */
@RequiredArgsConstructor
public final class DALStatementBindEngine {
    
    private final ShardingSphereMetaData metaData;
    
    private final String currentDatabaseName;
    
    private final HintValueContext hintValueContext;
    
    /**
     * Bind DAL statement.
     *
     * @param statement to be bound DAL statement
     * @return bound DAL statement
     */
    public DALStatement bind(final DALStatement statement) {
        SQLStatementBinderContext binderContext = new SQLStatementBinderContext(metaData, currentDatabaseName, hintValueContext, statement);
        if (statement instanceof OptimizeTableStatement) {
            return new OptimizeTableStatementBinder().bind((OptimizeTableStatement) statement, binderContext);
        }
        if (statement instanceof ShowCreateTableStatement) {
            return new ShowCreateTableStatementBinder().bind((ShowCreateTableStatement) statement, binderContext);
        }
        if (statement instanceof ShowColumnsStatement) {
            return new ShowColumnsStatementBinder().bind((ShowColumnsStatement) statement, binderContext);
        }
        if (statement instanceof ShowIndexStatement) {
            return new ShowIndexStatementBinder().bind((ShowIndexStatement) statement, binderContext);
        }
        if (statement instanceof ExplainStatement) {
            return new ExplainStatementBinder(metaData, currentDatabaseName, hintValueContext).bind((ExplainStatement) statement, binderContext);
        }
        return statement;
    }
}
