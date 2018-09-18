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

package io.shardingsphere.core.parsing.parser.sql;

import com.google.common.collect.Range;
import io.shardingsphere.core.api.algorithm.sharding.ListShardingValue;
import io.shardingsphere.core.api.algorithm.sharding.RangeShardingValue;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.ShardingOperator;
import io.shardingsphere.core.parsing.SQLParsingEngine;
import io.shardingsphere.core.parsing.parser.context.condition.Column;
import io.shardingsphere.core.parsing.parser.context.condition.Condition;
import io.shardingsphere.core.parsing.parser.sql.dml.DMLStatement;
import io.shardingsphere.core.rule.ShardingRule;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class UpdateStatementParserTest extends AbstractStatementParserTest {
    
    @Test
    public void parseWithoutParameter() {
        ShardingRule shardingRule = createShardingRule();
        SQLParsingEngine statementParser = new SQLParsingEngine(DatabaseType.MySQL, "UPDATE TABLE_XXX xxx SET TABLE_XXX.field1=field1+1,xxx.field2=2 WHERE TABLE_XXX.field4<10 AND"
                + " TABLE_XXX.field1=1 AND xxx.field5>10 AND TABLE_XXX.field2 IN (1,3) AND xxx.field6<=10 AND TABLE_XXX.field3 BETWEEN 5 AND 20 AND xxx.field7>=10", shardingRule, null);
        DMLStatement updateStatement = (DMLStatement) statementParser.parse(false);
        assertUpdateStatementWithoutParameter(updateStatement);
    }
    
    private void assertUpdateStatementWithoutParameter(final DMLStatement updateStatement) {
        assertThat(updateStatement.getTables().find("TABLE_XXX").get().getName(), is("TABLE_XXX"));
        assertThat(updateStatement.getTables().find("TABLE_XXX").get().getAlias().get(), is("xxx"));
        Condition condition1 = updateStatement.getConditions().find(new Column("field1", "TABLE_XXX")).get();
        assertThat(condition1.getOperator(), CoreMatchers.is(ShardingOperator.EQUAL));
        assertThat(((ListShardingValue<? extends Comparable>) condition1.getShardingValue(Collections.emptyList())).getValues().iterator().next(), is((Comparable) 1));
        Condition condition2 = updateStatement.getConditions().find(new Column("field2", "TABLE_XXX")).get();
        assertThat(condition2.getOperator(), is(ShardingOperator.IN));
        Iterator<?> shardingValues2 = ((ListShardingValue) condition2.getShardingValue(Collections.emptyList())).getValues().iterator();
        assertThat(shardingValues2.next(), is((Object) 1));
        assertThat(shardingValues2.next(), is((Object) 3));
        assertFalse(shardingValues2.hasNext());
        Condition condition3 = updateStatement.getConditions().find(new Column("field3", "TABLE_XXX")).get();
        Range shardingValues3 = ((RangeShardingValue) condition3.getShardingValue(Collections.emptyList())).getValueRange();
        assertThat(condition3.getOperator(), is(ShardingOperator.BETWEEN));
        assertThat(shardingValues3.lowerEndpoint(), is((Comparable) 5));
        assertThat(shardingValues3.upperEndpoint(), is((Comparable) 20));
    }
    
    @Test
    public void parseWithOr() {
        ShardingRule shardingRule = createShardingRule();
        DMLStatement updateStatement = (DMLStatement) new SQLParsingEngine(
                DatabaseType.Oracle, "UPDATE TABLE_XXX AS xxx SET field1=1 WHERE field1<1 AND (field1 >2 OR xxx.field2 =1)", shardingRule, null).parse(false);
        assertUpdateStatementWitOr(updateStatement);
    }
    
    private void assertUpdateStatementWitOr(final DMLStatement updateStatement) {
        assertThat(updateStatement.getTables().find("TABLE_XXX").get().getName(), is("TABLE_XXX"));
        assertThat(updateStatement.getTables().find("TABLE_XXX").get().getAlias().get(), is("xxx"));
        assertTrue(updateStatement.getConditions().getOrCondition().getAndConditions().isEmpty());

    }
    
    @Test
    public void parseWithSpecialSyntax() {
        parseWithSpecialSyntax(DatabaseType.Oracle, "UPDATE /*+ index(field1) */ ONLY TABLE_XXX SET field1=1 WHERE field1=1 RETURNING *");
        parseWithSpecialSyntax(DatabaseType.Oracle, "UPDATE /*+ index(field1) */ ONLY TABLE_XXX SET field1=1 WHERE field1=1 LOG ERRORS INTO TABLE_LOG");
    }
    
    private void parseWithSpecialSyntax(final DatabaseType dbType, final String actualSQL) {
        ShardingRule shardingRule = createShardingRule();
        DMLStatement updateStatement = (DMLStatement) new SQLParsingEngine(dbType, actualSQL, shardingRule, null).parse(false);
        assertThat(updateStatement.getTables().find("TABLE_XXX").get().getName(), is("TABLE_XXX"));
        assertFalse(updateStatement.getTables().find("TABLE_XXX").get().getAlias().isPresent());
        Condition condition = updateStatement.getConditions().find(new Column("field1", "TABLE_XXX")).get();
        assertThat(condition.getOperator(), is(ShardingOperator.EQUAL));
        assertThat(((ListShardingValue<? extends Comparable>) condition.getShardingValue(Collections.emptyList())).getValues().iterator().next(), is((Object) 1));
    }
}
