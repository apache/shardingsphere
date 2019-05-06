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

package io.shardingsphere.core.parsing.parser.clause;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.lexer.dialect.mysql.MySQLKeyword;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.core.parsing.lexer.token.Keyword;
import io.shardingsphere.core.parsing.lexer.token.Symbol;
import io.shardingsphere.core.parsing.parser.clause.expression.AliasExpressionParser;
import io.shardingsphere.core.parsing.parser.clause.expression.BasicExpressionParser;
import io.shardingsphere.core.parsing.parser.context.table.Table;
import io.shardingsphere.core.parsing.parser.dialect.ExpressionParserFactory;
import io.shardingsphere.core.parsing.parser.exception.SQLParsingUnsupportedException;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.token.IndexToken;
import io.shardingsphere.core.parsing.parser.token.TableToken;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.core.util.SQLUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Table references clause parser.
 *
 * @author zhangliang
 * @author maxiaoguang
 */
public class TableReferencesClauseParser implements SQLClauseParser {
    
    private final ShardingRule shardingRule;
    
    @Getter
    private final LexerEngine lexerEngine;
    
    private final AliasExpressionParser aliasExpressionParser;
    
    private final BasicExpressionParser basicExpressionParser;
    
    public TableReferencesClauseParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        this.shardingRule = shardingRule;
        this.lexerEngine = lexerEngine;
        aliasExpressionParser = ExpressionParserFactory.createAliasExpressionParser(lexerEngine);
        basicExpressionParser = ExpressionParserFactory.createBasicExpressionParser(lexerEngine);
    }
    
    /**
     * Parse table references.
     *
     * @param sqlStatement SQL statement
     * @param isSingleTableOnly is parse single table only
     */
    public final void parse(final SQLStatement sqlStatement, final boolean isSingleTableOnly) {
        do {
            parseTableReference(sqlStatement, isSingleTableOnly);
        } while (lexerEngine.skipIfEqual(Symbol.COMMA));
    }

    /**
     * Parse table references.
     *
     * @param sqlStatement SQL statement
     * @param isSingleTableOnly is parse single table only
     */
    protected void parseTableReference(final SQLStatement sqlStatement, final boolean isSingleTableOnly) {
        parseTableFactor(sqlStatement, isSingleTableOnly);
    }
    
    protected final void parseTableFactor(final SQLStatement sqlStatement, final boolean isSingleTableOnly) {
        final int beginPosition = lexerEngine.getCurrentToken().getEndPosition() - lexerEngine.getCurrentToken().getLiterals().length();
        String literals = lexerEngine.getCurrentToken().getLiterals();
        int skippedSchemaNameLength = 0;
        lexerEngine.nextToken();
        if (lexerEngine.skipIfEqual(Symbol.DOT)) {
            skippedSchemaNameLength = literals.length() + Symbol.DOT.getLiterals().length();
            literals = lexerEngine.getCurrentToken().getLiterals();
            lexerEngine.nextToken();
        }
        String tableName = SQLUtil.getExactlyValue(literals);
        if (Strings.isNullOrEmpty(tableName)) {
            return;
        }
        if (isSingleTableOnly || shardingRule.findTableRuleByLogicTable(tableName).isPresent()
                || shardingRule.isBroadcastTable(tableName) || shardingRule.findBindingTableRule(tableName).isPresent()
                || shardingRule.getShardingDataSourceNames().getDataSourceNames().contains(shardingRule.getShardingDataSourceNames().getDefaultDataSourceName())) {
            sqlStatement.addSQLToken(new TableToken(beginPosition, skippedSchemaNameLength, literals));
            sqlStatement.getTables().add(new Table(tableName, aliasExpressionParser.parseTableAlias(sqlStatement, true, tableName)));
        } else {
            aliasExpressionParser.parseTableAlias();
        }
        parseForceIndex(tableName, sqlStatement);
        parseJoinTable(sqlStatement);
        if (isSingleTableOnly && !sqlStatement.getTables().isSingleTable()) {
            throw new SQLParsingUnsupportedException("Cannot support Multiple-Table.");
        }
    }
    
    private void parseForceIndex(final String tableName, final SQLStatement sqlStatement) {
        boolean skipIfForce = lexerEngine.skipIfEqual(MySQLKeyword.FORCE) && this.lexerEngine.skipIfEqual(DefaultKeyword.INDEX);
        if (skipIfForce) {
            lexerEngine.accept(Symbol.LEFT_PAREN);
            do {
                lexerEngine.skipIfEqual(Symbol.COMMA);
                String literals = lexerEngine.getCurrentToken().getLiterals();
                Preconditions.checkState(!Symbol.RIGHT_PAREN.getLiterals().equals(literals), "There is an error in the vicinity of the force index syntax.");
                if (shardingRule.isLogicIndex(literals, tableName)) {
                    int beginPosition = lexerEngine.getCurrentToken().getEndPosition() - literals.length();
                    sqlStatement.addSQLToken(new IndexToken(beginPosition, literals, tableName));
                }
                lexerEngine.nextToken();
            } while (lexerEngine.skipIfEqual(Symbol.COMMA));
            lexerEngine.accept(Symbol.RIGHT_PAREN);
        }
    }
    
    private void parseJoinTable(final SQLStatement sqlStatement) {
        while (parseJoinType()) {
            if (lexerEngine.equalAny(Symbol.LEFT_PAREN)) {
                throw new SQLParsingUnsupportedException("Cannot support sub query for join table.");
            }
            parseTableFactor(sqlStatement, false);
            parseJoinCondition(sqlStatement);
        }
    }
    
    private boolean parseJoinType() {
        List<Keyword> joinTypeKeywords = new LinkedList<>();
        joinTypeKeywords.addAll(Arrays.asList(
                DefaultKeyword.INNER, DefaultKeyword.OUTER, DefaultKeyword.LEFT, DefaultKeyword.RIGHT, DefaultKeyword.FULL, DefaultKeyword.CROSS, DefaultKeyword.NATURAL, DefaultKeyword.JOIN));
        joinTypeKeywords.addAll(Arrays.asList(getKeywordsForJoinType()));
        Keyword[] joinTypeKeywordArrays = joinTypeKeywords.toArray(new Keyword[joinTypeKeywords.size()]);
        if (!lexerEngine.equalAny(joinTypeKeywordArrays)) {
            return false;
        }
        lexerEngine.skipAll(joinTypeKeywordArrays);
        return true;
    }

    /**
     * Get keywords for join type.
     *
     * @return new Keyword object array
     */
    protected Keyword[] getKeywordsForJoinType() {
        return new Keyword[0];
    }
    
    private void parseJoinCondition(final SQLStatement sqlStatement) {
        if (lexerEngine.skipIfEqual(DefaultKeyword.ON)) {
            do {
                basicExpressionParser.parse(sqlStatement);
                lexerEngine.accept(Symbol.EQ);
                basicExpressionParser.parse(sqlStatement);
            } while (lexerEngine.skipIfEqual(DefaultKeyword.AND));
        } else if (lexerEngine.skipIfEqual(DefaultKeyword.USING)) {
            lexerEngine.skipParentheses(sqlStatement);
        }
    }
    
    /**
     * Parse single table without alias.
     *
     * @param sqlStatement SQL statement
     */
    public final void parseSingleTableWithoutAlias(final SQLStatement sqlStatement) {
        int beginPosition = lexerEngine.getCurrentToken().getEndPosition() - lexerEngine.getCurrentToken().getLiterals().length();
        String literals = lexerEngine.getCurrentToken().getLiterals();
        int skippedSchemaNameLength = 0;
        lexerEngine.nextToken();
        if (lexerEngine.skipIfEqual(Symbol.DOT)) {
            skippedSchemaNameLength = literals.length() + Symbol.DOT.getLiterals().length();
            literals = lexerEngine.getCurrentToken().getLiterals();
            lexerEngine.nextToken();
        }
        sqlStatement.addSQLToken(new TableToken(beginPosition, skippedSchemaNameLength, literals));
        sqlStatement.getTables().add(new Table(SQLUtil.getExactlyValue(literals), Optional.<String>absent()));
    }
}
