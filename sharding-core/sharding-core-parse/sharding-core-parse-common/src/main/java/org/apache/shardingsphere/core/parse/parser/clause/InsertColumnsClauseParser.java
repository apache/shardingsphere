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

package org.apache.shardingsphere.core.parse.parser.clause;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.lexer.LexerEngine;
import org.apache.shardingsphere.core.parse.lexer.token.Assist;
import org.apache.shardingsphere.core.parse.lexer.token.Symbol;
import org.apache.shardingsphere.core.parse.parser.clause.expression.BasicExpressionParser;
import org.apache.shardingsphere.core.parse.parser.context.condition.Column;
import org.apache.shardingsphere.core.parse.parser.dialect.ExpressionParserFactory;
import org.apache.shardingsphere.core.parse.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parse.parser.expression.SQLIdentifierExpression;
import org.apache.shardingsphere.core.parse.parser.expression.SQLIgnoreExpression;
import org.apache.shardingsphere.core.parse.parser.expression.SQLPropertyExpression;
import org.apache.shardingsphere.core.parse.parser.sql.dml.insert.InsertStatement;
import org.apache.shardingsphere.core.parse.util.SQLUtil;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Insert columns clause parser.
 *
 * @author zhangliang
 * @author maxiaoguang
 * @author panjuan
 */
public final class InsertColumnsClauseParser implements SQLClauseParser {
    
    private final LexerEngine lexerEngine;
    
    private final BasicExpressionParser basicExpressionParser;
    
    public InsertColumnsClauseParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        this.lexerEngine = lexerEngine;
        basicExpressionParser = ExpressionParserFactory.createBasicExpressionParser(lexerEngine);
    }
    
    /**
     * Parse insert columns.
     *
     * @param insertStatement insert statement
     * @param shardingTableMetaData sharding table meta data
     */
    public void parse(final InsertStatement insertStatement, final ShardingTableMetaData shardingTableMetaData) {
        String tableName = insertStatement.getTables().getSingleTableName();
        insertStatement.getColumns().addAll(lexerEngine.equalAny(Symbol.LEFT_PAREN)
                ? parseWithColumn(insertStatement, tableName) : parseWithoutColumn(shardingTableMetaData, tableName));
    }
    
    private Collection<Column> parseWithColumn(final InsertStatement insertStatement, final String tableName) {
        Collection<Column> result = new LinkedList<>();
        do {
            lexerEngine.nextToken();
            SQLExpression sqlExpression = basicExpressionParser.parse(insertStatement);
            String columnName = null;
            if (sqlExpression instanceof SQLPropertyExpression) {
                columnName = SQLUtil.getExactlyValue(((SQLPropertyExpression) sqlExpression).getName());
            }
            if (sqlExpression instanceof SQLIdentifierExpression) {
                columnName = SQLUtil.getExactlyValue(((SQLIdentifierExpression) sqlExpression).getName());
            }
            if (sqlExpression instanceof SQLIgnoreExpression) {
                columnName = SQLUtil.getExactlyValue(((SQLIgnoreExpression) sqlExpression).getExpression());
            }
            Preconditions.checkNotNull(columnName);
            result.add(new Column(columnName, tableName));
        } while (!lexerEngine.equalAny(Symbol.RIGHT_PAREN) && !lexerEngine.equalAny(Assist.END));
        lexerEngine.nextToken();
        return result;
    }
    
    private Collection<Column> parseWithoutColumn(final ShardingTableMetaData shardingTableMetaData, final String tableName) {
        Collection<Column> result = new LinkedList<>();
        if (shardingTableMetaData.containsTable(tableName)) {
            for (String each : shardingTableMetaData.getAllColumnNames(tableName)) {
                result.add(new Column(each, tableName));
            }
        }
        return result;
    }
}
