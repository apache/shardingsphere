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

package io.shardingsphere.core.parsing.antler.parser.factory;

import org.antlr.v4.runtime.ParserRuleContext;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antler.VisitorRegistry;
import io.shardingsphere.core.parsing.antler.statement.visitor.StatementVisitor;
import io.shardingsphere.core.parsing.lexer.token.TokenType;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.rule.ShardingRule;

public class StatementFactory {
    
    /**Parse input to SQLStatement.
     * @param dbType database type
     * @param tokenType token type
     * @param shardingRule databases and tables sharding rule
     * @param sql input sql text
     * @param shardingTableMetaData table meta data
     * @return SQLStatement instance
     */
    public static SQLStatement getStatement(final DatabaseType dbType, final TokenType tokenType,
            final ShardingRule shardingRule, final String sql, final ShardingTableMetaData shardingTableMetaData) {
        DatabaseType execDbType = dbType;
        if(DatabaseType.H2 == execDbType) {
            execDbType = DatabaseType.MySQL;
        }
        
        ParserRuleContext rootNode = ParseTreeFactory.getTableDDLParser(execDbType, tokenType, shardingRule, sql);
        if (rootNode != null) {
            String commandName = getCommandName(rootNode);
            StatementVisitor visitor = VisitorRegistry.getInstance().getVisitor(execDbType, commandName);
            if (null != visitor) {
                return visitor.visit(rootNode, shardingTableMetaData);
            }
        }

        return null;
    }

    private static String getCommandName(final ParserRuleContext node) {
        String name = node.getClass().getSimpleName();
        int pos = name.indexOf("Context");
        if (pos > 0) {
            return name.substring(0, pos);
        }

        return name;
    }
}
