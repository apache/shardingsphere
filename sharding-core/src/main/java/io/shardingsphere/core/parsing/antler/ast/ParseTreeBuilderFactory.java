/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.antler.ast;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.rule.ShardingRule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Parser tree builder factory.
 * 
 * <p>For SQL parse and AST generate.</p>
 * 
 * @author duhongjun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ParseTreeBuilderFactory {
    
    /** 
     * Get SQL statement AST.
     * 
     * @param dbType database type
     * @param sql SQL
     * @param shardingRule sharding rule
     * @return parse tree
     */
    public static ParserRuleContext getParserTree(final DatabaseType dbType, final String sql, final ShardingRule shardingRule) {
        switch (dbType) {
            case H2:
            case MySQL:
                return new MySQLStatementParseTreeBuilder().parse(sql);
            case Oracle:
                return new OracleStatementParseTreeBuilder().parse(sql);
            case SQLServer:
                return new SQLServerStatementParseTreeBuilder().parse(sql);
            case PostgreSQL:
                return new PostgreStatementParseTreeBuilder().parse(sql);
            default:
                throw new UnsupportedOperationException(String.format("Can not support database type [%s].", dbType.name()));
        }
    }
}
