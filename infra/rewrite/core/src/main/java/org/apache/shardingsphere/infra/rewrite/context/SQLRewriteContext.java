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

package org.apache.shardingsphere.infra.rewrite.context;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.GroupedParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.StandardParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.SQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.SQLTokenGenerators;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * SQL rewrite context.
 */
@Getter
public final class SQLRewriteContext {
    
    private final ShardingSphereDatabase database;
    
    private final SQLStatementContext sqlStatementContext;
    
    private final String sql;
    
    private final List<Object> parameters;
    
    private final ParameterBuilder parameterBuilder;
    
    private final List<SQLToken> sqlTokens = new LinkedList<>();
    
    @Getter(AccessLevel.NONE)
    private final SQLTokenGenerators sqlTokenGenerators = new SQLTokenGenerators();
    
    private final ConnectionContext connectionContext;
    
    public SQLRewriteContext(final ShardingSphereDatabase database, final QueryContext queryContext) {
        this.database = database;
        sqlStatementContext = queryContext.getSqlStatementContext();
        sql = queryContext.getSql();
        parameters = queryContext.getParameters();
        connectionContext = queryContext.getConnectionContext();
        parameterBuilder = containsGroupedParameter(sqlStatementContext) ? buildGroupedParameterBuilder(sqlStatementContext) : new StandardParameterBuilder(parameters);
    }
    
    private boolean containsGroupedParameter(final SQLStatementContext sqlStatementContext) {
        return containsInsertValues(sqlStatementContext);
    }
    
    private GroupedParameterBuilder buildGroupedParameterBuilder(final SQLStatementContext sqlStatementContext) {
        List<List<Object>> groupedParams = new ArrayList<>();
        List<Object> beforeGenericParams = new ArrayList<>();
        List<Object> afterGenericParams = new ArrayList<>();
        if (sqlStatementContext instanceof InsertStatementContext) {
            groupedParams.addAll(((InsertStatementContext) sqlStatementContext).getGroupedParameters());
            // TODO check insert statement whether has beforeGenericParams
            afterGenericParams.addAll(((InsertStatementContext) sqlStatementContext).getOnDuplicateKeyUpdateParameters());
        }
        return new GroupedParameterBuilder(groupedParams, beforeGenericParams, afterGenericParams);
    }
    
    private boolean containsInsertValues(final SQLStatementContext sqlStatementContext) {
        if (!(sqlStatementContext instanceof InsertStatementContext)) {
            return false;
        }
        return null == ((InsertStatementContext) sqlStatementContext).getInsertSelectContext();
    }
    
    /**
     * Add SQL token generators.
     *
     * @param sqlTokenGenerators SQL token generators
     */
    public void addSQLTokenGenerators(final Collection<SQLTokenGenerator> sqlTokenGenerators) {
        this.sqlTokenGenerators.addAll(sqlTokenGenerators);
    }
    
    /**
     * Generate SQL tokens.
     */
    public void generateSQLTokens() {
        sqlTokens.addAll(sqlTokenGenerators.generateSQLTokens(database, sqlStatementContext, parameters, connectionContext));
    }
}
