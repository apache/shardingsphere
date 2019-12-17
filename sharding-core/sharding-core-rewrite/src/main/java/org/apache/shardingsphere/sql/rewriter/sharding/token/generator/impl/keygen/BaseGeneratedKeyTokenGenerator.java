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

package org.apache.shardingsphere.sql.rewriter.sharding.token.generator.impl.keygen;

import com.google.common.base.Preconditions;
import lombok.Setter;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.route.router.sharding.keygen.GeneratedKey;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.rewriter.sharding.aware.SQLRouteResultAware;
import org.apache.shardingsphere.sql.rewriter.sql.token.generator.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.sql.rewriter.sql.token.pojo.SQLToken;

/**
 * Base generated key token generator.
 *
 * @author zhangliang
 */
@Setter
public abstract class BaseGeneratedKeyTokenGenerator implements OptionalSQLTokenGenerator, SQLRouteResultAware {
    
    private SQLRouteResult sqlRouteResult;
    
    @Override
    public final boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof InsertSQLStatementContext && sqlRouteResult.getGeneratedKey().isPresent()
                && sqlRouteResult.getGeneratedKey().get().isGenerated() && isGenerateSQLToken((InsertStatement) sqlStatementContext.getSqlStatement());
    }
    
    protected abstract boolean isGenerateSQLToken(InsertStatement insertStatement);
    
    @Override
    public final SQLToken generateSQLToken(final SQLStatementContext sqlStatementContext) {
        Preconditions.checkState(sqlRouteResult.getGeneratedKey().isPresent());
        return generateSQLToken(sqlStatementContext, sqlRouteResult.getGeneratedKey().get());
    }
    
    protected abstract SQLToken generateSQLToken(SQLStatementContext sqlStatementContext, GeneratedKey generatedKey);
}
