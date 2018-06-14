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

import com.google.common.collect.Lists;
import io.shardingsphere.core.api.algorithm.fixture.TestComplexKeysShardingAlgorithm;
import io.shardingsphere.core.api.algorithm.sharding.ListShardingValue;
import io.shardingsphere.core.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.api.config.TableRuleConfiguration;
import io.shardingsphere.core.api.config.strategy.ComplexShardingStrategyConfiguration;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.ShardingOperator;
import io.shardingsphere.core.keygen.fixture.IncrementKeyGenerator;
import io.shardingsphere.core.metadata.ShardingMetaData;
import io.shardingsphere.core.parsing.SQLParsingEngine;
import io.shardingsphere.core.parsing.parser.context.condition.Column;
import io.shardingsphere.core.parsing.parser.context.condition.Condition;
import io.shardingsphere.core.parsing.parser.exception.SQLParsingException;
import io.shardingsphere.core.parsing.parser.exception.SQLParsingUnsupportedException;
import io.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import io.shardingsphere.core.rule.ShardingRule;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class InsertStatementParserTest extends AbstractStatementParserTest {
    
    @Test
    public void assertParseWithoutParameter() {
        ShardingRule shardingRule = createShardingRule();
        SQLParsingEngine statementParser = new SQLParsingEngine(DatabaseType.MySQL, "INSERT INTO `TABLE_XXX` (`field1`, `field2`) VALUES (10, 1)", shardingRule, null);
        InsertStatement insertStatement = (InsertStatement) statementParser.parse(false);
        assertInsertStatementWithoutParameter(insertStatement);
    }
    
    @Test
    public void assertParseWithParameter() {
        ShardingRule shardingRule = createShardingRule();
        SQLParsingEngine statementParser = new SQLParsingEngine(DatabaseType.MySQL, "INSERT INTO TABLE_XXX (field1, field2) VALUES (?, ?)", shardingRule, null);
        InsertStatement insertStatement = (InsertStatement) statementParser.parse(false);
        assertInsertStatementWithParameter(insertStatement);
    }
    
    @Test
    public void assertParseWithGenerateKeyColumnsWithoutParameter() {
        ShardingRule shardingRule = createShardingRuleWithGenerateKeyColumns();
        SQLParsingEngine statementParser = new SQLParsingEngine(DatabaseType.MySQL, "INSERT INTO `TABLE_XXX` (`field1`) VALUES (10)", shardingRule, null);
        InsertStatement insertStatement = (InsertStatement) statementParser.parse(false);
        assertInsertStatementWithoutParameter(insertStatement);
    }
    
    @SuppressWarnings("unchecked")
    private void assertInsertStatementWithoutParameter(final InsertStatement insertStatement) {
        assertThat(insertStatement.getTables().find("TABLE_XXX").get().getName(), is("TABLE_XXX"));
        Condition condition = insertStatement.getConditions().find(new Column("field1", "TABLE_XXX")).get();
        assertThat(condition.getOperator(), CoreMatchers.is(ShardingOperator.EQUAL));
        assertThat(((ListShardingValue<? extends Comparable>) condition.getShardingValue(Collections.emptyList())).getValues().iterator().next(), is((Comparable) 10));
    }
    
    @Test
    public void assertParseWithGenerateKeyColumnsWithParameter() {
        ShardingRule shardingRule = createShardingRuleWithGenerateKeyColumns();
        SQLParsingEngine statementParser = new SQLParsingEngine(DatabaseType.MySQL, "INSERT INTO `TABLE_XXX` (`field1`) VALUES (?)", shardingRule, null);
        InsertStatement insertStatement = (InsertStatement) statementParser.parse(false);
        assertInsertStatementWithParameter(insertStatement);
    }
    
    @Test
    public void assertParseWithoutColumnsWithGenerateKeyColumnsWithoutParameter() {
        ShardingRule shardingRule = createShardingRuleWithGenerateKeyColumns();
        ShardingMetaData shardingMetaData = createShardingMetaData();
        SQLParsingEngine statementParser = new SQLParsingEngine(DatabaseType.MySQL, "INSERT INTO `TABLE_XXX` VALUES (10)", shardingRule, shardingMetaData);
        InsertStatement insertStatement = (InsertStatement) statementParser.parse(false);
        assertInsertStatementWithoutParameter(insertStatement);
    }
    
    @Test
    public void assertParseWithoutColumnsWithGenerateKeyColumnsWithParameter() {
        ShardingRule shardingRule = createShardingRuleWithGenerateKeyColumns();
        ShardingMetaData shardingMetaData = createShardingMetaData();
        SQLParsingEngine statementParser = new SQLParsingEngine(DatabaseType.MySQL, "INSERT INTO `TABLE_XXX` VALUES (?)", shardingRule, shardingMetaData);
        InsertStatement insertStatement = (InsertStatement) statementParser.parse(false);
        assertInsertStatementWithParameter(insertStatement);
    }
    
    @Test
    public void assertParseWithoutColumnsWithoutParameter() {
        ShardingRule shardingRule = createShardingRule();
        ShardingMetaData shardingMetaData = createShardingMetaData();
        SQLParsingEngine statementParser = new SQLParsingEngine(DatabaseType.MySQL, "INSERT INTO `TABLE_XXX` VALUES (10,20)", shardingRule, shardingMetaData);
        InsertStatement insertStatement = (InsertStatement) statementParser.parse(false);
        assertInsertStatementWithoutParameter(insertStatement);
    }
    
    @Test
    public void assertParseWithoutColumnsWithParameter() {
        ShardingRule shardingRule = createShardingRule();
        ShardingMetaData shardingMetaData = createShardingMetaData();
        SQLParsingEngine statementParser = new SQLParsingEngine(DatabaseType.MySQL, "INSERT INTO `TABLE_XXX` VALUES (?, ?)", shardingRule, shardingMetaData);
        InsertStatement insertStatement = (InsertStatement) statementParser.parse(false);
        assertInsertStatementWithParameter(insertStatement);
    }
    
    private void assertInsertStatementWithParameter(final InsertStatement insertStatement) {
        assertThat(insertStatement.getTables().find("TABLE_XXX").get().getName(), is("TABLE_XXX"));
        Condition condition = insertStatement.getConditions().find(new Column("field1", "TABLE_XXX")).get();
        assertThat(condition.getOperator(), is(ShardingOperator.EQUAL));
        assertThat(((ListShardingValue<? extends Comparable>) condition.getShardingValue(Collections.<Object>singletonList(0))).getValues().iterator().next(), is((Comparable) 0));
    }
    
    private ShardingRule createShardingRuleWithGenerateKeyColumns() {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        try {
            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.getMetaData()).thenReturn(databaseMetaData);
            when(databaseMetaData.getDatabaseProductName()).thenReturn("H2");
        } catch (final SQLException ex) {
            throw new RuntimeException(ex);
        }
        final ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("TABLE_XXX");
        tableRuleConfig.setActualDataNodes("ds.table_${0..2}");
        tableRuleConfig.setTableShardingStrategyConfig(new ComplexShardingStrategyConfiguration("field1", new TestComplexKeysShardingAlgorithm()));
        tableRuleConfig.setKeyGeneratorColumnName("field2");
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.setDefaultKeyGenerator(new IncrementKeyGenerator());
        return new ShardingRule(shardingRuleConfig, Lists.newArrayList("ds"));
    }
    
    @Test
    public void parseWithSpecialSyntax() {
//        parseWithSpecialSyntax(DatabaseType.MySQL, "INSERT LOW_PRIORITY IGNORE INTO `TABLE_XXX` PARTITION (partition1,partition2) (`field1`) VALUE (1)");
        parseWithSpecialSyntax(DatabaseType.MySQL, "INSERT INTO TABLE_XXX SET field1=1");
        // TODO
//         parseWithSpecialSyntax(DatabaseType.MySQL, "INSERT INTO TABLE_XXX (field1) SELECT field1 FROM TABLE_XXX2 ON DUPLICATE KEY UPDATE field1=field1+1");
        parseWithSpecialSyntax(DatabaseType.MySQL, "INSERT /*+ index(field1) */ INTO TABLE_XXX (`field1`) VALUES (1) RETURNING field1*2 LOG ERRORS INTO TABLE_LOG");
    }
    
    @SuppressWarnings("unchecked")
    private void parseWithSpecialSyntax(final DatabaseType dbType, final String actualSQL) {
        ShardingRule shardingRule = createShardingRule();
        ShardingMetaData shardingMetaData = createShardingMetaData();
        InsertStatement insertStatement = (InsertStatement) new SQLParsingEngine(dbType, actualSQL, shardingRule, shardingMetaData).parse(false);
        assertThat(insertStatement.getTables().find("TABLE_XXX").get().getName(), is("TABLE_XXX"));
        assertFalse(insertStatement.getTables().find("TABLE_XXX").get().getAlias().isPresent());
        Condition condition = insertStatement.getConditions().find(new Column("field1", "TABLE_XXX")).get();
        assertThat(condition.getOperator(), is(ShardingOperator.EQUAL));
        assertThat(((ListShardingValue<? extends Comparable>) condition.getShardingValue(Collections.emptyList())).getValues().iterator().next(), is((Comparable) 1));
    }
    
    @Test
    public void parseInsertOnDuplicateKeyUpdateWithNoShardingColumn() {
        ShardingRule shardingRule = createShardingRule();
        new SQLParsingEngine(DatabaseType.MySQL, "INSERT ALL INTO TABLE_XXX (field8) VALUES (field8) ON DUPLICATE KEY UPDATE field8 = VALUES(field8)", shardingRule, null).parse(false);
    }
    
    @Test(expected = SQLParsingException.class)
    public void parseInsertOnDuplicateKeyUpdateWithShardingColumn() {
        ShardingRule shardingRule = createShardingRule();
        new SQLParsingEngine(DatabaseType.MySQL, "INSERT ALL INTO TABLE_XXX (field1) VALUES (field1) ON DUPLICATE KEY UPDATE field1 = VALUES(field1)", shardingRule, null).parse(false);
    }
    
    @Test
    // TODO assert
    public void parseMultipleInsertForMySQL() {
        ShardingRule shardingRule = createShardingRule();
        new SQLParsingEngine(DatabaseType.MySQL, "INSERT INTO TABLE_XXX (`field1`, `field2`) VALUES (1, 'value_char'), (2, 'value_char')", shardingRule, null).parse(false);
    }
    
    @Test(expected = SQLParsingUnsupportedException.class)
    public void parseInsertAllForOracle() {
        ShardingRule shardingRule = createShardingRule();
        new SQLParsingEngine(DatabaseType.Oracle, "INSERT ALL INTO TABLE_XXX (field1) VALUES (field1) SELECT field1 FROM TABLE_XXX2", shardingRule, null).parse(false);
    }
    
    @Test(expected = SQLParsingUnsupportedException.class)
    public void parseInsertFirstForOracle() {
        ShardingRule shardingRule = createShardingRule();
        new SQLParsingEngine(DatabaseType.Oracle, "INSERT FIRST INTO TABLE_XXX (field1) VALUES (field1) SELECT field1 FROM TABLE_XXX2", shardingRule, null).parse(false);
    }
}
