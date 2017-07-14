/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.parsing.parser.statement;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.parsing.SQLParsingEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.select.SelectStatement;
import com.dangdang.ddframe.rdb.sharding.rewrite.SQLRewriteEngine;

import java.sql.SQLException;

public final class SelectStatementParserTest extends AbstractStatementParserTest {

    @Test
    public void parseOrCondition() throws SQLException {
        ShardingRule shardingRule = createShardingRule();
        String sql = "SELECT * FROM `TABLE_XXX` where `field1` is not null and (`field1` < ? or `field1` >= ?)";
        SQLParsingEngine statementParser = new SQLParsingEngine(DatabaseType.MySQL, sql, shardingRule);
        SelectStatement selectStatement = (SelectStatement) statementParser.parse();
        assertThat(selectStatement.getTables().find("TABLE_XXX").get().getName(), is("TABLE_XXX"));
        assertThat(new SQLRewriteEngine(shardingRule, sql, selectStatement).rewrite(true).toString(),
            is("SELECT * FROM [Token(TABLE_XXX)] where `field1` is not null and (`field1` < ? or `field1` >= ?)"));
    }

    @Test
    public void parseOrCondition2() throws SQLException {
        ShardingRule shardingRule = createShardingRule();
        String sql = "Select * from (SELECT t.* FROM `TABLE_XXX` t where t.`field1` is not null and (t.`field1` < ? or t.`field1` >= ?)) d where d=1";
        SQLParsingEngine statementParser = new SQLParsingEngine(DatabaseType.MySQL, sql, shardingRule);
        SelectStatement selectStatement = (SelectStatement) statementParser.parse();
        assertThat(selectStatement.getTables().find("TABLE_XXX").get().getName(), is("TABLE_XXX"));
        assertThat(new SQLRewriteEngine(shardingRule, sql, selectStatement).rewrite(true).toString(),
            is("Select * from (SELECT [Token(t)].* FROM [Token(TABLE_XXX)] t where t.`field1` is not null and (t.`field1` < ? or t.`field1` >= ?)) d where d=1"));
    }

    @Test
    public void parseParentheses() throws SQLException {
        ShardingRule shardingRule = createShardingRule();
        String sql = " select * from (SELECT"
            + "        t.*"
            + "        FROM `TABLE_XXX` t, `ACCOUNT` acc"
            + "        where t.`field1` is not null and ((t.type in ('1') and t.to_id = acc.id))) d "
            + "        where d.id=1";
        SQLParsingEngine statementParser = new SQLParsingEngine(DatabaseType.MySQL, sql, shardingRule);
        SelectStatement selectStatement = (SelectStatement) statementParser.parse();
        assertThat(selectStatement.getTables().find("TABLE_XXX").get().getName(), is("TABLE_XXX"));
        assertThat(new SQLRewriteEngine(shardingRule, sql, selectStatement).rewrite(true).toString(),
            is(" select * from (SELECT"
                + "        [Token(t)].*"
                + "        FROM [Token(TABLE_XXX)] t, [Token(ACCOUNT)] acc"
                + "        where t.`field1` is not null and ((t.type in ('1') and t.to_id = acc.id))) d "
                + "        where d.id=1"));


    }

    @Test
    public void parseJoinCondition() throws SQLException {
        ShardingRule shardingRule = createShardingRule();
        String sql = " select * from (SELECT"
            + "        TX.*"
            + "        FROM `TABLE_XXX` TX  "
            + "      LEFT OUTER JOIN ACCOUNT FROM_ACC ON (FROM_ACC.ID = TX.FROM_ID or FROM_ACC.ID=TX.TO_ID)"
            + "      LEFT OUTER JOIN ACCOUNT TO_ACC ON (TO_ACC.ID = TX.TO_ID)"
            + "        where (TX.`field1` is not null or TX.`filed1`>'1')) d "
            + "        where d.id=1";
        SQLParsingEngine statementParser = new SQLParsingEngine(DatabaseType.MySQL, sql, shardingRule);
        SelectStatement selectStatement = (SelectStatement) statementParser.parse();
        assertThat(selectStatement.getTables().find("TABLE_XXX").get().getName(), is("TABLE_XXX"));
        assertThat(new SQLRewriteEngine(shardingRule, sql, selectStatement).rewrite(true).toString(),
            is(" select * from (SELECT"
                + "        [Token(TX)].*"
                + "        FROM [Token(TABLE_XXX)] TX  "
                + "      LEFT OUTER JOIN [Token(ACCOUNT)] FROM_ACC ON (FROM_ACC.ID = TX.FROM_ID or FROM_ACC.ID=TX.TO_ID)"
                + "      LEFT OUTER JOIN [Token(ACCOUNT)] TO_ACC ON (TO_ACC.ID = TX.TO_ID)"
                + "        where (TX.`field1` is not null or TX.`filed1`>'1')) d "
                + "        where d.id=1"));
    }

    

}
