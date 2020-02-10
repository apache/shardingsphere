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

import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.command.HintAddDatabaseShardingValueCommand;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.command.HintAddTableShardingValueCommand;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.command.HintClearCommand;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.command.HintErrorParameterCommand;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.command.HintSetDatabaseShardingValueCommand;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.command.HintSetMasterOnlyCommand;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.command.HintShowStatusCommand;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.command.HintShowTableStatusCommand;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ShardingCTLHintParserTest {
    
    @Test
    public void assertValidSetMasterOnlySql() {
        String masterOnlySql = "sctl:hint set master_only=true ";
        ShardingCTLHintStatement masterOnlyStatement = new ShardingCTLHintParser(masterOnlySql).doParse().get();
        assertTrue(((HintSetMasterOnlyCommand) masterOnlyStatement.getHintCommand()).isMasterOnly());
    }
    
    @Test
    public void assertInValidSetMasterOnlySql() {
        String masterOnlySql = "sctl:hint set master_only1=true ";
        assertThat(new ShardingCTLHintParser(masterOnlySql).doParse().get().getHintCommand(), instanceOf(HintErrorParameterCommand.class));
    }
    
    @Test
    public void assertValidSetDatabaseShardingValueSql() {
        String sql = " sctl:HINT set DatabaseShardingValue=100  ";
        ShardingCTLHintStatement statement = new ShardingCTLHintParser(sql).doParse().get();
        assertEquals(((HintSetDatabaseShardingValueCommand) statement.getHintCommand()).getValue(), "100");
    }
    
    @Test
    public void assertInValidSetDatabaseShardingValueSql() {
        String sql = " sctl:HINT set DatabaseShardingValue1=100  ";
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
    public void assertValidShowStatusSql() {
        String sql = " sctl:HINT show status ";
        ShardingCTLHintStatement statement = new ShardingCTLHintParser(sql).doParse().get();
        assertThat(statement.getHintCommand(), instanceOf(HintShowStatusCommand.class));
    }
    
    @Test
    public void assertInValidShowStatusSql() {
        String sql = " sctl:HINT show status1";
        ShardingCTLHintStatement statement = new ShardingCTLHintParser(sql).doParse().get();
        assertThat(statement.getHintCommand(), instanceOf(HintErrorParameterCommand.class));
    }
    
    @Test
    public void assertValidShowTableStatusSql() {
        String sql = " sctl:HINT show table status ";
        ShardingCTLHintStatement statement = new ShardingCTLHintParser(sql).doParse().get();
        assertThat(statement.getHintCommand(), instanceOf(HintShowTableStatusCommand.class));
    }
    
    @Test
    public void assertInValidShowTableStatusSql() {
        String sql = " sctl:HINT show table status1";
        ShardingCTLHintStatement statement = new ShardingCTLHintParser(sql).doParse().get();
        assertThat(statement.getHintCommand(), instanceOf(HintErrorParameterCommand.class));
    }
    
    @Test
    public void assertNotHintSql() {
        String sql = "sctl:hint1 abcd efg hijk lmn";
        assertFalse(new ShardingCTLHintParser(sql).doParse().isPresent());
    }
}
