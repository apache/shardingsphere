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

package org.apache.shardingsphere.distsql.parser.core.featured;

import lombok.SneakyThrows;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.apache.shardingsphere.distsql.parser.engine.spi.DistSQLParserFacade;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.engine.core.ParseASTNode;
import org.apache.shardingsphere.sql.parser.engine.core.SQLParserFactory;
import org.apache.shardingsphere.sql.parser.engine.exception.SQLParsingException;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;

/**
 * DistSQL parser engine.
 */
public final class DistSQLParserEngine {
    
    /**
     * Parse SQL.
     *
     * @param sql SQL to be parsed
     * @return SQL statement
     * @throws SQLParsingException SQL parsing exception
     */
    public SQLStatement parse(final String sql) {
        for (DistSQLParserFacade each : ShardingSphereServiceLoader.getServiceInstances(DistSQLParserFacade.class)) {
            try {
                ParseASTNode astNode = (ParseASTNode) SQLParserFactory.newInstance(sql, each.getLexerClass(), each.getParserClass()).parse();
                return getSQLStatement(sql, each, astNode);
            } catch (final ParseCancellationException | SQLParsingException ignored) {
            }
        }
        throw new SQLParsingException(sql);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    @SuppressWarnings("rawtypes")
    private SQLStatement getSQLStatement(final String sql, final DistSQLParserFacade facade, final ParseASTNode parseASTNode) {
        if (parseASTNode.getRootNode() instanceof ErrorNode) {
            throw new SQLParsingException(sql);
        }
        SQLVisitor visitor = facade.getVisitorClass().getDeclaredConstructor().newInstance();
        SQLStatement result = (SQLStatement) visitor.visit(parseASTNode.getRootNode());
        result.buildAttributes();
        return result;
    }
}
