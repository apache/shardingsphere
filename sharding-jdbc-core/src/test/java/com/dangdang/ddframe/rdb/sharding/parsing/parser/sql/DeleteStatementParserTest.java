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

package com.dangdang.ddframe.rdb.sharding.parsing.parser.sql;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.constant.ShardingOperator;
import com.dangdang.ddframe.rdb.sharding.parsing.SQLParsingEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Column;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Condition;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.dml.DMLStatement;
import com.google.common.collect.Range;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public final class DeleteStatementParserTest extends AbstractStatementParserTest {
    
    @Test
    public void parseWithoutCondition() throws SQLException {
        ShardingRule shardingRule = createShardingRule();
        SQLParsingEngine statementParser = new SQLParsingEngine(DatabaseType.MySQL, "DELETE FROM TABLE_XXX", shardingRule);
        DMLStatement deleteStatement = (DMLStatement) statementParser.parse();
        assertThat(deleteStatement.getTables().find("TABLE_XXX").get().getName(), is("TABLE_XXX"));
    }
    
    @Test
    public void parseWithoutParameter() throws SQLException {
        ShardingRule shardingRule = createShardingRule();
        SQLParsingEngine statementParser = new SQLParsingEngine(DatabaseType.MySQL, 
                "DELETE FROM TABLE_XXX xxx WHERE field4<10 AND TABLE_XXX.field1=1 AND field5>10 AND xxx.field2 IN (1,3) AND field6<=10 AND field3 BETWEEN 5 AND 20 AND field7>=10", shardingRule);
        DMLStatement deleteStatement = (DMLStatement) statementParser.parse();
        assertDeleteStatementWithoutParameter(deleteStatement);
    }
    
    private void assertDeleteStatementWithoutParameter(final DMLStatement deleteStatement) {
        assertThat(deleteStatement.getTables().find("TABLE_XXX").get().getName(), is("TABLE_XXX"));
        assertThat(deleteStatement.getTables().find("xxx").get().getAlias().get(), is("xxx"));
        Condition condition1 = deleteStatement.getConditions().find(new Column("field1", "TABLE_XXX")).get();
        assertThat(condition1.getOperator(), is(ShardingOperator.EQUAL));
        assertThat(condition1.getShardingValue(Collections.emptyList()).getValue(), is((Object) 1));
        Condition condition2 = deleteStatement.getConditions().find(new Column("field2", "TABLE_XXX")).get();
        assertThat(condition2.getOperator(), is(ShardingOperator.IN));
        Iterator<?> shardingValues2 = condition2.getShardingValue(Collections.emptyList()).getValues().iterator();
        assertThat(shardingValues2.next(), is((Object) 1));
        assertThat(shardingValues2.next(), is((Object) 3));
        assertFalse(shardingValues2.hasNext());
        Condition condition3 = deleteStatement.getConditions().find(new Column("field3", "TABLE_XXX")).get();
        assertThat(condition3.getOperator(), is(ShardingOperator.BETWEEN));
        Range shardingValues3 = condition3.getShardingValue(Collections.emptyList()).getValueRange();
        assertThat(shardingValues3.lowerEndpoint(), is((Comparable) 5));
        assertThat(shardingValues3.upperEndpoint(), is((Comparable) 20));
    }
    
    @Test
    public void parseWithParameter() throws SQLException {
        ShardingRule shardingRule = createShardingRule();
        SQLParsingEngine statementParser = new SQLParsingEngine(DatabaseType.MySQL, 
                "DELETE FROM TABLE_XXX xxx WHERE field4<? AND field1=? AND field5>? AND field2 IN (?,?) AND field6<=? AND field3 BETWEEN ? AND ? AND field7>=?", shardingRule);
        DMLStatement deleteStatement = (DMLStatement) statementParser.parse();
        assertDeleteStatementWithParameter(deleteStatement);
    }
    
    private void assertDeleteStatementWithParameter(final DMLStatement deleteStatement) {
        assertThat(deleteStatement.getTables().find("TABLE_XXX").get().getName(), is("TABLE_XXX"));
        assertThat(deleteStatement.getTables().find("xxx").get().getAlias().get(), is("xxx"));
        List<Object> actualParameters = Arrays.<Object>asList(0, 10, 20, 30, 40, 50, 60, 70, 80);
        Condition condition1 = deleteStatement.getConditions().find(new Column("field1", "TABLE_XXX")).get();
        assertThat(condition1.getOperator(), is(ShardingOperator.EQUAL));
        assertThat(condition1.getShardingValue(actualParameters).getValue(), is((Object) 10));
        Condition condition2 = deleteStatement.getConditions().find(new Column("field2", "TABLE_XXX")).get();
        assertThat(condition2.getOperator(), is(ShardingOperator.IN));
        Iterator<?> shardingValues2 = condition2.getShardingValue(actualParameters).getValues().iterator();
        assertThat(shardingValues2.next(), is((Object) 30));
        assertThat(shardingValues2.next(), is((Object) 40));
        Condition condition3 = deleteStatement.getConditions().find(new Column("field3", "TABLE_XXX")).get();
        assertThat(condition3.getOperator(), is(ShardingOperator.BETWEEN));
        Range shardingValues3 = condition3.getShardingValue(actualParameters).getValueRange();
        assertThat(shardingValues3.lowerEndpoint(), is((Comparable) 60));
        assertThat(shardingValues3.upperEndpoint(), is((Comparable) 70));
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void parseStatementWithDeleteMultipleTable() {
        ShardingRule shardingRule = createShardingRule();
        new SQLParsingEngine(DatabaseType.MySQL, "DELETE TABLE_XXX1, TABLE_xxx2 FROM TABLE_XXX1 JOIN TABLE_XXX2", shardingRule).parse();
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void parseStatementWithDeleteMultipleTableWithUsing() {
        ShardingRule shardingRule = createShardingRule();
        new SQLParsingEngine(DatabaseType.MySQL, "DELETE FROM TABLE_XXX1, TABLE_xxx2 USING TABLE_XXX1 JOIN TABLE_XXX2", shardingRule).parse();
    }
    
    @Test
    public void parseWithSpecialSyntax() throws SQLException {
//        parseWithSpecialSyntax(DatabaseType.MySQL, "DELETE `TABLE_XXX` WHERE `field1`=1");
        parseWithSpecialSyntax(DatabaseType.Oracle, "DELETE /*+ index(field1) */ ONLY (TABLE_XXX) WHERE field1=1 RETURN * LOG ERRORS INTO TABLE_LOG");
        parseWithSpecialSyntax(DatabaseType.Oracle, "DELETE /*+ index(field1) */ ONLY (TABLE_XXX) WHERE field1=1 RETURNING *");
    }
    
    private void parseWithSpecialSyntax(final DatabaseType dbType, final String actualSQL) {
        ShardingRule shardingRule = createShardingRule();
        DMLStatement deleteStatement = (DMLStatement) new SQLParsingEngine(dbType, actualSQL, shardingRule).parse();
        assertThat(deleteStatement.getTables().find("TABLE_XXX").get().getName(), is("TABLE_XXX"));
        assertFalse(deleteStatement.getTables().find("TABLE_XXX").get().getAlias().isPresent());
        Condition condition = deleteStatement.getConditions().find(new Column("field1", "TABLE_XXX")).get();
        assertThat(condition.getOperator(), is(ShardingOperator.EQUAL));
        assertThat(condition.getShardingValue(Collections.emptyList()).getValue(), is((Comparable) 1));
    }
}
