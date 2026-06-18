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

package org.apache.shardingsphere.sql.parser.engine.mysql.visitor.statement.type;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.LCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.LockContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableLockContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.UnlockContext;
import org.apache.shardingsphere.sql.parser.engine.mysql.visitor.statement.MySQLStatementVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.lcl.LockStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.lcl.UnlockStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * LCL statement visitor for MySQL.
 */
public final class MySQLLCLStatementVisitor extends MySQLStatementVisitor implements LCLStatementVisitor {
    
    public MySQLLCLStatementVisitor(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    @Override
    public ASTNode visitLock(final LockContext ctx) {
        return new LockStatement(getDatabaseType(), null == ctx.tableLock() ? Collections.emptyList() : getLockTables(ctx.tableLock()));
    }
    
    private Collection<SimpleTableSegment> getLockTables(final List<TableLockContext> tableLockContexts) {
        Collection<SimpleTableSegment> result = new LinkedList<>();
        for (TableLockContext each : tableLockContexts) {
            SimpleTableSegment simpleTableSegment = (SimpleTableSegment) visit(each.tableName());
            if (null != each.alias()) {
                simpleTableSegment.setAlias((AliasSegment) visit(each.alias()));
            }
            result.add(simpleTableSegment);
        }
        return result;
    }
    
    @Override
    public ASTNode visitUnlock(final UnlockContext ctx) {
        return new UnlockStatement(getDatabaseType());
    }
}
