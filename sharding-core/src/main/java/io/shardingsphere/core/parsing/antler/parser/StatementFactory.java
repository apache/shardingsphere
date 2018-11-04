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

package io.shardingsphere.core.parsing.antler.parser;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antler.VisitorRegistry;
import io.shardingsphere.core.parsing.antler.ast.ParseTreeBuilderFactory;
import io.shardingsphere.core.parsing.antler.statement.visitor.StatementVisitor;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.rule.ShardingRule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Use AST generate SQLStatement.
 *
 * @author duhongjun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StatementFactory {

    /**
     * Parse SQL.
     *
     * @param dbType database type
     * @param sql SQL
     * @param shardingRule sharding rule
     * @param shardingTableMetaData table meta data
     * @return SQL statement
     */
    public static SQLStatement parse(final DatabaseType dbType, final String sql, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
        ParserRuleContext parserRuleContext = ParseTreeBuilderFactory.getParserTree(dbType, sql, shardingRule);
        if (null == parserRuleContext) {
            return null;
        }
        StatementVisitor visitor = VisitorRegistry.getInstance().getVisitor(dbType, getCommandName(parserRuleContext));
        if (null == visitor) {
            return null;
        }
        return visitor.visit(parserRuleContext, shardingTableMetaData);
    }
    
    private static String getCommandName(final ParserRuleContext parserRuleContext) {
        String name = parserRuleContext.getClass().getSimpleName();
        int pos = name.indexOf("Context");
        return pos > 0 ? name.substring(0, pos) : name;
    }
}
