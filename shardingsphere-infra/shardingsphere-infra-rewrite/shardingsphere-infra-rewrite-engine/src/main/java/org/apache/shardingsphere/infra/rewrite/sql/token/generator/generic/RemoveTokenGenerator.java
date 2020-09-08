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

package org.apache.shardingsphere.infra.rewrite.sql.token.generator.generic;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic.RemoveToken;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.RemoveAvailable;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowColumnsStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowTableStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowTablesStatement;

import java.util.Collection;
import java.util.Collections;

/**
 * Remove token generator.
 */
public final class RemoveTokenGenerator implements CollectionSQLTokenGenerator {
    
    @Override
    public boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        if (sqlStatementContext.getSqlStatement() instanceof MySQLShowTablesStatement) {
            return ((MySQLShowTablesStatement) sqlStatementContext.getSqlStatement()).getFromSchema().isPresent();
        }
        if (sqlStatementContext.getSqlStatement() instanceof MySQLShowTableStatusStatement) {
            return ((MySQLShowTableStatusStatement) sqlStatementContext.getSqlStatement()).getFromSchema().isPresent();
        }
        if (sqlStatementContext.getSqlStatement() instanceof MySQLShowColumnsStatement) {
            return ((MySQLShowColumnsStatement) sqlStatementContext.getSqlStatement()).getFromSchema().isPresent();
        }
        return false;
    }
    
    @Override
    public Collection<RemoveToken> generateSQLTokens(final SQLStatementContext sqlStatementContext) {
        if (sqlStatementContext.getSqlStatement() instanceof MySQLShowTablesStatement) {
            Preconditions.checkState(((MySQLShowTablesStatement) sqlStatementContext.getSqlStatement()).getFromSchema().isPresent());
            RemoveAvailable removeAvailable = ((MySQLShowTablesStatement) sqlStatementContext.getSqlStatement()).getFromSchema().get();
            return Collections.singletonList(new RemoveToken(removeAvailable.getStartIndex(), removeAvailable.getStopIndex()));
        }
        if (sqlStatementContext.getSqlStatement() instanceof MySQLShowTableStatusStatement) {
            Preconditions.checkState(((MySQLShowTableStatusStatement) sqlStatementContext.getSqlStatement()).getFromSchema().isPresent());
            RemoveAvailable removeAvailable = ((MySQLShowTableStatusStatement) sqlStatementContext.getSqlStatement()).getFromSchema().get();
            return Collections.singletonList(new RemoveToken(removeAvailable.getStartIndex(), removeAvailable.getStopIndex()));
        }
        if (sqlStatementContext.getSqlStatement() instanceof MySQLShowColumnsStatement) {
            Preconditions.checkState(((MySQLShowColumnsStatement) sqlStatementContext.getSqlStatement()).getFromSchema().isPresent());
            RemoveAvailable removeAvailable = ((MySQLShowColumnsStatement) sqlStatementContext.getSqlStatement()).getFromSchema().get();
            return Collections.singletonList(new RemoveToken(removeAvailable.getStartIndex(), removeAvailable.getStopIndex()));
        }
        return Collections.emptyList();
    }
}
