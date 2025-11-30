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

package org.apache.shardingsphere.sql.parser.engine.core.database.parser;

import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.api.parser.SQLParser;
import org.apache.shardingsphere.sql.parser.engine.core.ParseASTNode;
import org.apache.shardingsphere.sql.parser.engine.core.SQLParserFactory;
import org.apache.shardingsphere.sql.parser.engine.exception.SQLParsingException;
import org.apache.shardingsphere.sql.parser.spi.DialectSQLParserFacade;

/**
 * SQL parser executor.
 */
@RequiredArgsConstructor
public final class SQLParserExecutor {
    
    private final DatabaseType databaseType;
    
    /**
     * Parse SQL.
     *
     * @param sql SQL to be parsed
     * @return parse AST node
     * @throws SQLParsingException SQL parsing exception
     */
    public ParseASTNode parse(final String sql) {
        ParseASTNode result = twoPhaseParse(sql);
        if (result.getRootNode() instanceof ErrorNode) {
            throw new SQLParsingException(sql);
        }
        return result;
    }
    
    private ParseASTNode twoPhaseParse(final String sql) {
        DialectSQLParserFacade sqlParserFacade = DatabaseTypedSPILoader.getService(DialectSQLParserFacade.class, databaseType);
        SQLParser sqlParser = SQLParserFactory.newInstance(sql, sqlParserFacade.getLexerClass(), sqlParserFacade.getParserClass());
        try {
            ((Parser) sqlParser).getInterpreter().setPredictionMode(PredictionMode.SLL);
            return (ParseASTNode) sqlParser.parse();
        } catch (final ParseCancellationException ex) {
            ((Parser) sqlParser).reset();
            ((Parser) sqlParser).getInterpreter().setPredictionMode(PredictionMode.LL);
            ((Parser) sqlParser).removeErrorListeners();
            ((Parser) sqlParser).addErrorListener(SQLParserErrorListener.getInstance());
            try {
                return (ParseASTNode) sqlParser.parse();
            } catch (final ParseCancellationException exception) {
                throw new SQLParsingException(sql + ", " + exception.getMessage());
            }
        }
    }
}
