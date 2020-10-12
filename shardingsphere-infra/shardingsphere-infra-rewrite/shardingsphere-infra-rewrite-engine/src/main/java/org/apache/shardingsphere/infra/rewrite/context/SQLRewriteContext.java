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
import org.apache.shardingsphere.infra.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.GroupedParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.StandardParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.SQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.SQLTokenGenerators;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.builder.DefaultTokenGeneratorBuilder;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.metadata.schema.model.schema.SchemaMetaData;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * SQL rewrite context.
 */
@Getter
public final class SQLRewriteContext {
    
    private final SchemaMetaData schemaMetaData;
    
    private final SQLStatementContext<?> sqlStatementContext;
    
    private final String sql;
    
    private final List<Object> parameters;
    
    private final ParameterBuilder parameterBuilder;
    
    private final List<SQLToken> sqlTokens = new LinkedList<>();
    
    @Getter(AccessLevel.NONE)
    private final SQLTokenGenerators sqlTokenGenerators = new SQLTokenGenerators();
    
    public SQLRewriteContext(final SchemaMetaData schemaMetaData, final SQLStatementContext<?> sqlStatementContext, final String sql, final List<Object> parameters) {
        this.schemaMetaData = schemaMetaData;
        this.sqlStatementContext = sqlStatementContext;
        this.sql = sql;
        this.parameters = parameters;
        addSQLTokenGenerators(new DefaultTokenGeneratorBuilder().getSQLTokenGenerators());
        parameterBuilder = sqlStatementContext instanceof InsertStatementContext
                ? new GroupedParameterBuilder(
                        ((InsertStatementContext) sqlStatementContext).getGroupedParameters(), ((InsertStatementContext) sqlStatementContext).getOnDuplicateKeyUpdateParameters())
                : new StandardParameterBuilder(parameters);
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
        sqlTokens.addAll(sqlTokenGenerators.generateSQLTokens(sqlStatementContext, parameters, schemaMetaData));
    }
}
