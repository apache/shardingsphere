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
import org.apache.shardingsphere.infra.binder.engine.statement.ddl.AlterIndexStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.ddl.AlterTableStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.ddl.AlterViewStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.ddl.CommentStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.ddl.CreateIndexStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.ddl.CreateTableStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.ddl.CreateViewStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.ddl.CursorStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.ddl.DropIndexStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.ddl.DropTableStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.ddl.DropViewStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.ddl.PrepareStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.ddl.RenameTableStatementBinder;
import org.apache.shardingsphere.infra.binder.engine.statement.ddl.TruncateStatementBinder;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CommentStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CursorStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.PrepareStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.AlterIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.DropIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.AlterTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.DropTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.RenameTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.CreateViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.DropViewStatement;

/**
 * DDL statement bind engine.
 */
public final class DDLStatementBindEngine {
    
    /**
     * Bind DDL statement.
     *
     * @param statement to be bound DDL statement
     * @param binderContext binder context
     * @return bound DDL statement
     */
    public DDLStatement bind(final DDLStatement statement, final SQLStatementBinderContext binderContext) {
        if (statement instanceof CursorStatement) {
            return new CursorStatementBinder().bind((CursorStatement) statement, binderContext);
        }
        if (statement instanceof CreateTableStatement) {
            return new CreateTableStatementBinder().bind((CreateTableStatement) statement, binderContext);
        }
        if (statement instanceof AlterTableStatement) {
            return new AlterTableStatementBinder().bind((AlterTableStatement) statement, binderContext);
        }
        if (statement instanceof DropTableStatement) {
            return new DropTableStatementBinder().bind((DropTableStatement) statement, binderContext);
        }
        if (statement instanceof RenameTableStatement) {
            return new RenameTableStatementBinder().bind((RenameTableStatement) statement, binderContext);
        }
        if (statement instanceof CreateIndexStatement) {
            return new CreateIndexStatementBinder().bind((CreateIndexStatement) statement, binderContext);
        }
        if (statement instanceof AlterIndexStatement) {
            return new AlterIndexStatementBinder().bind((AlterIndexStatement) statement, binderContext);
        }
        if (statement instanceof DropIndexStatement) {
            return new DropIndexStatementBinder().bind((DropIndexStatement) statement, binderContext);
        }
        if (statement instanceof CreateViewStatement) {
            return new CreateViewStatementBinder().bind((CreateViewStatement) statement, binderContext);
        }
        if (statement instanceof AlterViewStatement) {
            return new AlterViewStatementBinder().bind((AlterViewStatement) statement, binderContext);
        }
        if (statement instanceof DropViewStatement) {
            return new DropViewStatementBinder().bind((DropViewStatement) statement, binderContext);
        }
        if (statement instanceof TruncateStatement) {
            return new TruncateStatementBinder().bind((TruncateStatement) statement, binderContext);
        }
        if (statement instanceof CommentStatement) {
            return new CommentStatementBinder().bind((CommentStatement) statement, binderContext);
        }
        if (statement instanceof PrepareStatement) {
            return new PrepareStatementBinder().bind((PrepareStatement) statement, binderContext);
        }
        return statement;
    }
}
