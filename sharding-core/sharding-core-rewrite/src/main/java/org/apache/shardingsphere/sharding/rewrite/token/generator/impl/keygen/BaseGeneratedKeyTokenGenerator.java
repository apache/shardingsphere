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

import com.google.common.base.Preconditions;
import lombok.Setter;
import org.apache.shardingsphere.sharding.rewrite.aware.ShardingRouteContextAware;
import org.apache.shardingsphere.sharding.route.engine.context.ShardingRouteContext;
import org.apache.shardingsphere.sharding.route.engine.keygen.GeneratedKey;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.underlying.rewrite.sql.token.generator.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.underlying.rewrite.sql.token.pojo.SQLToken;

/**
 * Base generated key token generator.
 */
@Setter
public abstract class BaseGeneratedKeyTokenGenerator implements OptionalSQLTokenGenerator<InsertStatementContext>, ShardingRouteContextAware {
    
    private ShardingRouteContext shardingRouteContext;
    
    @Override
    public final boolean isGenerateSQLToken(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof InsertStatementContext && shardingRouteContext.getGeneratedKey().isPresent()
                && shardingRouteContext.getGeneratedKey().get().isGenerated() && isGenerateSQLToken(((InsertStatementContext) sqlStatementContext).getSqlStatement());
    }
    
    protected abstract boolean isGenerateSQLToken(InsertStatement insertStatement);
    
    @Override
    public final SQLToken generateSQLToken(final InsertStatementContext insertStatementContext) {
        Preconditions.checkState(shardingRouteContext.getGeneratedKey().isPresent());
        return generateSQLToken(insertStatementContext, shardingRouteContext.getGeneratedKey().get());
    }
    
    protected abstract SQLToken generateSQLToken(InsertStatementContext insertStatementContext, GeneratedKey generatedKey);
}
