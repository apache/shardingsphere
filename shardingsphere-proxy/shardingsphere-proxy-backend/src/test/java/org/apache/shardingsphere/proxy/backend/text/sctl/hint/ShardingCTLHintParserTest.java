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

package org.apache.shardingsphere.proxy.backend.text.sctl.hint;

import org.apache.shardingsphere.proxy.backend.text.sctl.hint.internal.command.HintAddDatabaseShardingValueCommand;
import org.apache.shardingsphere.proxy.backend.text.sctl.hint.internal.command.HintAddTableShardingValueCommand;
import org.apache.shardingsphere.proxy.backend.text.sctl.hint.internal.command.HintClearCommand;
import org.apache.shardingsphere.proxy.backend.text.sctl.hint.internal.command.HintErrorParameterCommand;
import org.apache.shardingsphere.proxy.backend.text.sctl.hint.internal.command.HintSetDatabaseShardingValueCommand;
import org.apache.shardingsphere.proxy.backend.text.sctl.hint.internal.command.HintSetPrimaryOnlyCommand;
import org.apache.shardingsphere.proxy.backend.text.sctl.hint.internal.command.HintShowStatusCommand;
import org.apache.shardingsphere.proxy.backend.text.sctl.hint.internal.command.HintShowTableStatusCommand;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ShardingCTLHintParserTest {
    
    @Test
    public void assertValidSetMasterOnlySQL() {
        String sql = "sctl:hint set primary_only=true ";
        Optional<ShardingCTLHintStatement> actual = new ShardingCTLHintParser(sql).doParse();
        assertTrue(actual.isPresent());
        assertTrue(((HintSetPrimaryOnlyCommand) actual.get().getHintCommand()).isPrimaryOnly());
    }
    
    @Test
    public void assertInValidSetMasterOnlySQL() {
        String sql = "sctl:hint set primary_only1=true ";
        Optional<ShardingCTLHintStatement> actual = new ShardingCTLHintParser(sql).doParse();
        assertTrue(actual.isPresent());
        assertThat(actual.get().getHintCommand(), instanceOf(HintErrorParameterCommand.class));
    }
    
    @Test
    public void assertValidSetDatabaseShardingValueSQL() {
        String sql = " sctl:HINT set DatabaseShardingValue=100  ";
        Optional<ShardingCTLHintStatement> actual = new ShardingCTLHintParser(sql).doParse();
        assertTrue(actual.isPresent());
        assertThat(((HintSetDatabaseShardingValueCommand) actual.get().getHintCommand()).getValue(), is("100"));
    }
    
    @Test
    public void assertInValidSetDatabaseShardingValueSQL() {
        String sql = " sctl:HINT set DatabaseShardingValue1=100  ";
        Optional<ShardingCTLHintStatement> actual = new ShardingCTLHintParser(sql).doParse();
        assertTrue(actual.isPresent());
        assertThat(actual.get().getHintCommand(), instanceOf(HintErrorParameterCommand.class));
    }
    
    @Test
    public void assertValidAddDatabaseShardingValueSQL() {
        String sql = " sctl:HINT addDatabaseShardingValue user=100 ";
        Optional<ShardingCTLHintStatement> actual = new ShardingCTLHintParser(sql).doParse();
        assertTrue(actual.isPresent());
        HintAddDatabaseShardingValueCommand databaseCommand = (HintAddDatabaseShardingValueCommand) actual.get().getHintCommand();
        assertThat(databaseCommand.getLogicTable(), is("user"));
        assertThat(databaseCommand.getValue(), is("100"));
    }
    
    @Test
    public void assertInValidAddDatabaseShardingValueSQL() {
        String sql = " sctl:HINT addDatabaseShardingValue1 user=100 ";
        Optional<ShardingCTLHintStatement> actual = new ShardingCTLHintParser(sql).doParse();
        assertTrue(actual.isPresent());
        assertThat(actual.get().getHintCommand(), instanceOf(HintErrorParameterCommand.class));
    }
    
    @Test
    public void assertValidAddTableShardingValueSQL() {
        String sql = " sctl:HINT addTableShardingValue order=some ";
        Optional<ShardingCTLHintStatement> actual = new ShardingCTLHintParser(sql).doParse();
        assertTrue(actual.isPresent());
        HintAddTableShardingValueCommand tableCommand = (HintAddTableShardingValueCommand) actual.get().getHintCommand();
        assertThat(tableCommand.getLogicTable(), is("order"));
        assertThat(tableCommand.getValue(), is("some"));
    }
    
    @Test
    public void assertInValidAddTableShardingValueSQL() {
        String sql = " sctl:HINT addTableShardingValue 100 ";
        Optional<ShardingCTLHintStatement> actual = new ShardingCTLHintParser(sql).doParse();
        assertTrue(actual.isPresent());
        assertThat(actual.get().getHintCommand(), instanceOf(HintErrorParameterCommand.class));
    }
    
    @Test
    public void assertInValidAddShardingValueSQL() {
        String databaseSQL = " sctl:HINT addShardingValue1 value_Type=database user=100 ";
        Optional<ShardingCTLHintStatement> databaseSQLActual = new ShardingCTLHintParser(databaseSQL).doParse();
        assertTrue(databaseSQLActual.isPresent());
        assertThat(databaseSQLActual.get().getHintCommand(), instanceOf(HintErrorParameterCommand.class));
        String tableSQL = " sctl:HINT addShardingValue Value_Type=Table1 order=some ";
        Optional<ShardingCTLHintStatement> tableSQLActual = new ShardingCTLHintParser(tableSQL).doParse();
        assertTrue(tableSQLActual.isPresent());
        assertThat(tableSQLActual.get().getHintCommand(), instanceOf(HintErrorParameterCommand.class));
    }
    
    @Test
    public void assertValidClearSQL() {
        String sql = " sctl:HINT clear ";
        Optional<ShardingCTLHintStatement> actual = new ShardingCTLHintParser(sql).doParse();
        assertTrue(actual.isPresent());
        assertThat(actual.get().getHintCommand(), instanceOf(HintClearCommand.class));
    }
    
    @Test
    public void assertInValidClearSQL() {
        String sql = " sctl:HINT clear xxx";
        Optional<ShardingCTLHintStatement> actual = new ShardingCTLHintParser(sql).doParse();
        assertTrue(actual.isPresent());
        assertThat(actual.get().getHintCommand(), instanceOf(HintErrorParameterCommand.class));
    }
    
    @Test
    public void assertValidShowStatusSQL() {
        String sql = " sctl:HINT show status ";
        Optional<ShardingCTLHintStatement> actual = new ShardingCTLHintParser(sql).doParse();
        assertTrue(actual.isPresent());
        assertThat(actual.get().getHintCommand(), instanceOf(HintShowStatusCommand.class));
    }
    
    @Test
    public void assertInValidShowStatusSQL() {
        String sql = " sctl:HINT show status1";
        Optional<ShardingCTLHintStatement> actual = new ShardingCTLHintParser(sql).doParse();
        assertTrue(actual.isPresent());
        assertThat(actual.get().getHintCommand(), instanceOf(HintErrorParameterCommand.class));
    }
    
    @Test
    public void assertValidShowTableStatusSQL() {
        String sql = " sctl:HINT show table status ";
        Optional<ShardingCTLHintStatement> actual = new ShardingCTLHintParser(sql).doParse();
        assertTrue(actual.isPresent());
        assertThat(actual.get().getHintCommand(), instanceOf(HintShowTableStatusCommand.class));
    }
    
    @Test
    public void assertInValidShowTableStatusSQL() {
        String sql = " sctl:HINT show table status1";
        Optional<ShardingCTLHintStatement> actual = new ShardingCTLHintParser(sql).doParse();
        assertTrue(actual.isPresent());
        assertThat(actual.get().getHintCommand(), instanceOf(HintErrorParameterCommand.class));
    }
    
    @Test
    public void assertNotHintSQL() {
        String sql = "sctl:hint1 abcd efg hijk lmn";
        assertFalse(new ShardingCTLHintParser(sql).doParse().isPresent());
    }
}
