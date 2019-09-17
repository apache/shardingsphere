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

package org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint;

import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.HintType;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.command.HintAddDatabaseShardingValueCommand;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.command.HintAddTableShardingValueCommand;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.command.HintClearCommand;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.command.HintErrorParameterCommand;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.command.HintSetCommand;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.command.HintSetDatabaseShardingValueCommand;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

/**
 * Test for ShardingCTLHintParser.
 *
 * @author liya
 */
public final class ShardingCTLHintParserTest {
    
    @Test
    public void assertValidSetSql() {
        String databaseTablesSql = "sctl:hint set hint_type=DATAbase_TAbles ";
        String databaseOnlySql = " sctl:HINT SET hint_TYPE=database_only ";
        String masterOnlySql = " sctl:hint set hint_type=master_only ";
        ShardingCTLHintStatement databaseTablesStatement = new ShardingCTLHintParser(databaseTablesSql).doParse().get();
        ShardingCTLHintStatement databaseOnlyStatement = new ShardingCTLHintParser(databaseOnlySql).doParse().get();
        ShardingCTLHintStatement masterOnlyStatement = new ShardingCTLHintParser(masterOnlySql).doParse().get();
        assertEquals(((HintSetCommand) databaseTablesStatement.getHintCommand()).getHintType(), HintType.DATABASE_TABLES);
        assertEquals(((HintSetCommand) databaseOnlyStatement.getHintCommand()).getHintType(), HintType.DATABASE_ONLY);
        assertEquals(((HintSetCommand) masterOnlyStatement.getHintCommand()).getHintType(), HintType.MASTER_ONLY);
    }
    
    @Test
    public void assertInValidSetSql() {
        String databaseTablesSql = "sctl:hint set1 hint_type=DATAbase_TAbles ";
        String databaseOnlySql = " sctl:HINT SET hint_TYPE1=database_only ";
        String masterOnlySql = " sctl:hint set hint_type=master_only1 ";
        assertThat(new ShardingCTLHintParser(databaseTablesSql).doParse().get().getHintCommand(), instanceOf(HintErrorParameterCommand.class));
        assertThat(new ShardingCTLHintParser(databaseOnlySql).doParse().get().getHintCommand(), instanceOf(HintErrorParameterCommand.class));
        assertThat(new ShardingCTLHintParser(masterOnlySql).doParse().get().getHintCommand(), instanceOf(HintErrorParameterCommand.class));
    }
    
    @Test
    public void assertValidSetDatabaseShardingValueSql() {
        String sql = " sctl:HINT setDatabaseShardingValue 100  ";
        ShardingCTLHintStatement statement = new ShardingCTLHintParser(sql).doParse().get();
        assertEquals(((HintSetDatabaseShardingValueCommand) statement.getHintCommand()).getValue(), "100");
    }
    
    @Test
    public void assertInValidSetDatabaseShardingValueSql() {
        String sql = " sctl:HINT setDatabaseShardingValue1 100  ";
        assertThat(new ShardingCTLHintParser(sql).doParse().get().getHintCommand(), instanceOf(HintErrorParameterCommand.class));
    }
    
    @Test
    public void assertValidAddDatabaseShardingValueSql() {
        String databaseSql = " sctl:HINT addDatabaseShardingValue user=100 ";
        ShardingCTLHintStatement databaseStatement = new ShardingCTLHintParser(databaseSql).doParse().get();
        HintAddDatabaseShardingValueCommand databaseCommand = (HintAddDatabaseShardingValueCommand) databaseStatement.getHintCommand();
        assertEquals(databaseCommand.getLogicTable(), "user");
        assertEquals(databaseCommand.getValue(), "100");
    }
    
    @Test
    public void assertInValidAddDatabaseShardingValueSql() {
        String databaseSql = " sctl:HINT addDatabaseShardingValue1 user=100 ";
        assertThat(new ShardingCTLHintParser(databaseSql).doParse().get().getHintCommand(), instanceOf(HintErrorParameterCommand.class));
    }
    
    @Test
    public void assertValidAddTableShardingValueSql() {
        String tableSql = " sctl:HINT addTableShardingValue order=some ";
        ShardingCTLHintStatement tableStatement = new ShardingCTLHintParser(tableSql).doParse().get();
        HintAddTableShardingValueCommand tableCommand = (HintAddTableShardingValueCommand) tableStatement.getHintCommand();
        assertEquals(tableCommand.getLogicTable(), "order");
        assertEquals(tableCommand.getValue(), "some");
    }
    
    @Test
    public void assertInValidAddTableShardingValueSql() {
        String databaseSql = " sctl:HINT addTableShardingValue 100 ";
        assertThat(new ShardingCTLHintParser(databaseSql).doParse().get().getHintCommand(), instanceOf(HintErrorParameterCommand.class));
    }
    
    @Test
    public void assertInValidAddShardingValueSql() {
        String databaseSql = " sctl:HINT addShardingValue1 value_Type=database user=100 ";
        String tableSql = " sctl:HINT addShardingValue Value_Type=Table1 order=some ";
        assertThat(new ShardingCTLHintParser(databaseSql).doParse().get().getHintCommand(), instanceOf(HintErrorParameterCommand.class));
        assertThat(new ShardingCTLHintParser(tableSql).doParse().get().getHintCommand(), instanceOf(HintErrorParameterCommand.class));
    }
    
    @Test
    public void assertValidClearSql() {
        String sql = " sctl:HINT clear ";
        ShardingCTLHintStatement statement = new ShardingCTLHintParser(sql).doParse().get();
        assertThat(statement.getHintCommand(), instanceOf(HintClearCommand.class));
    }
    
    @Test
    public void assertInValidClearSql() {
        String sql = " sctl:HINT clear xxx";
        ShardingCTLHintStatement statement = new ShardingCTLHintParser(sql).doParse().get();
        assertThat(statement.getHintCommand(), instanceOf(HintErrorParameterCommand.class));
    }
    
    @Test
    public void assertNotHintSql() {
        String sql = "sctl:hint1 abcd efg hijk lmn";
        assertFalse(new ShardingCTLHintParser(sql).doParse().isPresent());
    }
}
