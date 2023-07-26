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

package org.apache.shardingsphere.infra.rewrite.sql.token.generator;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.ConnectionContextAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.ParametersAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.PreviousSQLTokensAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.aware.SchemaMetaDataAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * SQL token generators.
 */
public final class SQLTokenGenerators {
    
    private final Map<Class<?>, SQLTokenGenerator> generators = new LinkedHashMap<>();
    
    /**
     * Add all SQL token generators.
     * 
     * @param sqlTokenGenerators SQL token generators
     */
    public void addAll(final Collection<SQLTokenGenerator> sqlTokenGenerators) {
        for (SQLTokenGenerator each : sqlTokenGenerators) {
            generators.putIfAbsent(each.getClass(), each);
        }
    }
    
    /**
     * Generate SQL tokens.
     *
     * @param databaseName database name
     * @param sqlStatementContext SQL statement context
     * @param params SQL parameters
     * @param schemas schema map
     * @param connectionContext connection context
     * @return SQL tokens
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public List<SQLToken> generateSQLTokens(final String databaseName, final Map<String, ShardingSphereSchema> schemas,
                                            final SQLStatementContext sqlStatementContext, final List<Object> params, final ConnectionContext connectionContext) {
        List<SQLToken> result = new LinkedList<>();
        for (SQLTokenGenerator each : generators.values()) {
            setUpSQLTokenGenerator(each, params, databaseName, schemas, result, connectionContext);
            if (each instanceof OptionalSQLTokenGenerator) {
                SQLToken sqlToken = ((OptionalSQLTokenGenerator) each).generateSQLToken(sqlStatementContext);
                if (!result.contains(sqlToken)) {
                    result.add(sqlToken);
                }
            } else if (each instanceof CollectionSQLTokenGenerator) {
                result.addAll(((CollectionSQLTokenGenerator) each).generateSQLTokens(sqlStatementContext));
            }
        }
        return result;
    }
    
    private void setUpSQLTokenGenerator(final SQLTokenGenerator sqlTokenGenerator, final List<Object> params,
                                        final String databaseName, final Map<String, ShardingSphereSchema> schemas, final List<SQLToken> previousSQLTokens, final ConnectionContext connectionContext) {
        if (sqlTokenGenerator instanceof ParametersAware) {
            ((ParametersAware) sqlTokenGenerator).setParameters(params);
        }
        if (sqlTokenGenerator instanceof SchemaMetaDataAware) {
            ((SchemaMetaDataAware) sqlTokenGenerator).setSchemas(schemas);
            ((SchemaMetaDataAware) sqlTokenGenerator).setDatabaseName(databaseName);
        }
        if (sqlTokenGenerator instanceof PreviousSQLTokensAware) {
            ((PreviousSQLTokensAware) sqlTokenGenerator).setPreviousSQLTokens(previousSQLTokens);
        }
        if (sqlTokenGenerator instanceof ConnectionContextAware) {
            ((ConnectionContextAware) sqlTokenGenerator).setConnectionContext(connectionContext);
        }
    }
}
