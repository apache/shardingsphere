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

import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.dal.AnalyzeTableStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.dal.ExplainStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.dal.FlushStatementBinder;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.AnalyzeTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ExplainStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.FlushStatement;

/**
 * DAL statement bind engine.
 */
public final class DALStatementBindEngine {
    
    /**
     * Bind DAL statement.
     *
     * @param statement to be bound DAL statement
     * @param binderContext binder context
     * @return bound DAL statement
     */
    public DALStatement bind(final DALStatement statement, final SQLStatementBinderContext binderContext) {
        if (statement instanceof AnalyzeTableStatement) {
            return new AnalyzeTableStatementBinder().bind((AnalyzeTableStatement) statement, binderContext);
        }
        if (statement instanceof ExplainStatement) {
            return new ExplainStatementBinder().bind((ExplainStatement) statement, binderContext);
        }
        if (statement instanceof FlushStatement) {
            return new FlushStatementBinder().bind((FlushStatement) statement, binderContext);
        }
        return statement;
    }
}
