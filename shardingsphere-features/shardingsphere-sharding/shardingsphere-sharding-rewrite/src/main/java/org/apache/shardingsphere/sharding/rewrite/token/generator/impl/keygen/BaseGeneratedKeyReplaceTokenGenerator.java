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

package org.apache.shardingsphere.sharding.rewrite.token.generator.impl.keygen;

import org.apache.shardingsphere.infra.rewrite.sql.token.generator.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.statement.dml.ReplaceStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.ReplaceStatement;

/**
 * Base generated key token generator for replace clause.
 */
public abstract class BaseGeneratedKeyReplaceTokenGenerator implements OptionalSQLTokenGenerator<ReplaceStatementContext> {
    
    @Override
    public final boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof ReplaceStatementContext && ((ReplaceStatementContext) sqlStatementContext).getGeneratedKeyContext().isPresent()
                && ((ReplaceStatementContext) sqlStatementContext).getGeneratedKeyContext().get().isGenerated()
                && isGenerateSQLToken(((ReplaceStatementContext) sqlStatementContext).getSqlStatement());
    }
    
    protected abstract boolean isGenerateSQLToken(ReplaceStatement replaceStatement);
}
