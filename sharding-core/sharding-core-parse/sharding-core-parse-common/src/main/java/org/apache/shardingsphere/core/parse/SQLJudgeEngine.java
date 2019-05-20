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

package org.apache.shardingsphere.core.parse;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;

/**
 * SQL judge engine.
 *
 * @author zhangliang
 * @author panjuan
 */
@RequiredArgsConstructor
public final class SQLJudgeEngine {
    
    private final DatabaseType databaseType;
    
    private final String sql;
    
    /**
     * Judge SQL type only.
     *
     * @return SQL statement
     */
    public SQLStatement judge() {
        return new SQLParseEngine(databaseType, sql, null, null).parse();
//        LexerEngine lexerEngine = LexerEngineFactory.newInstance(DatabaseType.MySQL, sql);
//        lexerEngine.nextToken();
//        while (true) {
//            TokenType tokenType = lexerEngine.getCurrentToken().getType();
//            if (tokenType instanceof Keyword) {
//                if (DQLStatement.isDQL(tokenType)) {
//                    return getDQLStatement();
//                }
//                if (DMLStatement.isDML(tokenType)) {
//                    return getDMLStatement(tokenType);
//                }
//                if (TCLStatement.isTCL(tokenType)) {
//                    return getTCLStatement();
//                }
//                if (DALStatement.isDAL(tokenType)) {
//                    return getDALStatement(tokenType, lexerEngine);
//                }
//                lexerEngine.nextToken();
//                TokenType secondaryTokenType = lexerEngine.getCurrentToken().getType();
//                if (DDLStatement.isDDL(tokenType, secondaryTokenType)) {
//                    return getDDLStatement();
//                }
//                if (DCLStatement.isDCL(tokenType, secondaryTokenType)) {
//                    return getDCLStatement();
//                }
//                if (TCLStatement.isTCLUnsafe(DatabaseType.MySQL, tokenType, lexerEngine)) {
//                    return getTCLStatement();
//                }
//                if (DefaultKeyword.SET.equals(tokenType)) {
//                    return new SetStatement();
//                }
//            } else {
//                lexerEngine.nextToken();
//            }
//            if (sql.toUpperCase().startsWith("CALL")) {
//                return getDQLStatement();
//            }
//            if (tokenType instanceof Assist && Assist.END == tokenType) {
//                throw new SQLParsingException("Unsupported SQL statement: [%s]", sql);
//            }
//        }
    }
}
