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
import org.apache.shardingsphere.infra.binder.engine.statement.dml.DeleteStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.dml.InsertStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.dml.SelectStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.dml.UpdateStatementBinder;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.UpdateStatement;

/**
 * DML statement bind engine.
 */
@RequiredArgsConstructor
public final class DMLStatementBindEngine {
    
    private final ShardingSphereMetaData metaData;
    
    private final String currentDatabaseName;
    
    /**
     * Bind DML statement.
     *
     * @param statement to be bound DML statement
     * @return bound DML statement
     */
    public DMLStatement bind(final DMLStatement statement) {
        SQLStatementBinderContext binderContext = new SQLStatementBinderContext(statement, metaData, currentDatabaseName);
        if (statement instanceof SelectStatement) {
            return new SelectStatementBinder().bind((SelectStatement) statement, binderContext);
        }
        if (statement instanceof InsertStatement) {
            return new InsertStatementBinder().bind((InsertStatement) statement, binderContext);
        }
        if (statement instanceof UpdateStatement) {
            return new UpdateStatementBinder().bind((UpdateStatement) statement, binderContext);
        }
        if (statement instanceof DeleteStatement) {
            return new DeleteStatementBinder().bind((DeleteStatement) statement, binderContext);
        }
        return statement;
    }
}
