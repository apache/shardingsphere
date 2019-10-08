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

package org.apache.shardingsphere.core.rewrite;

import lombok.Getter;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.optimize.statement.SQLStatementContext;
import org.apache.shardingsphere.core.optimize.statement.impl.InsertSQLStatementContext;
import org.apache.shardingsphere.core.rewrite.parameter.builder.ParameterBuilder;
import org.apache.shardingsphere.core.rewrite.parameter.builder.impl.GroupedParameterBuilder;
import org.apache.shardingsphere.core.rewrite.parameter.builder.impl.StandardParameterBuilder;
import org.apache.shardingsphere.core.rewrite.sql.SQLBuilder;
import org.apache.shardingsphere.core.rewrite.sql.token.SQLTokenGenerators;
import org.apache.shardingsphere.core.rewrite.sql.token.builder.BaseTokenGeneratorBuilder;
import org.apache.shardingsphere.core.rewrite.sql.token.generator.SQLTokenGenerator;

import java.util.Collection;
import java.util.List;

/**
 * SQL rewrite builder.
 * 
 * @author zhangliang
 */
public final class SQLRewriteBuilder {
    
    private final SQLStatementContext sqlStatementContext;
    
    private final String sql;
    
    private final List<Object> parameters;
    
    private final TableMetas tableMetas;
    
    private final SQLTokenGenerators sqlTokenGenerators = new SQLTokenGenerators();
    
    @Getter
    private final ParameterBuilder parameterBuilder;
    
    public SQLRewriteBuilder(final TableMetas tableMetas, final SQLStatementContext sqlStatementContext, final String sql, final List<Object> parameters) {
        this.tableMetas = tableMetas;
        this.sqlStatementContext = sqlStatementContext;
        this.sql = sql;
        this.parameters = parameters;
        addSQLTokenGenerators(new BaseTokenGeneratorBuilder().getSQLTokenGenerators());
        parameterBuilder = sqlStatementContext instanceof InsertSQLStatementContext
                ? new GroupedParameterBuilder(((InsertSQLStatementContext) sqlStatementContext).getGroupedParameters()) : new StandardParameterBuilder(parameters);
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
     * Get SQL builder.
     * 
     * @return SQL builder
     */
    public SQLBuilder getSQLBuilder() {
        SQLBuilder result = new SQLBuilder(sql);
        result.getSqlTokens().addAll(sqlTokenGenerators.generateSQLTokens(sqlStatementContext, parameters, tableMetas));
        return result;
    }
}
