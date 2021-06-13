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

package org.apache.shardingsphere.distsql.parser.api;

import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowDatabaseDiscoveryRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowEncryptRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowReadwriteSplittingRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowShardingTableRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowShardingBindingTableRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowShardingBroadcastTableRulesStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

// TODO use Parameterized + XML instead of static test
public final class DistSQLStatementParserEngineTest {

    private static final String RQL_SHOW_SHARDING_BINDING_TABLE_RULES = "SHOW SHARDING BINDING TABLE RULES FROM sharding_db";

    private static final String RQL_SHOW_SHARDING_BROADCAST_TABLE_RULES = "SHOW SHARDING BROADCAST TABLE RULES FROM sharding_db";

    private static final String RQL_SHOW_SHARDING_TABLE_RULES = "SHOW SHARDING TABLE RULES FROM schemaName";

    private static final String RQL_SHOW_SHARDING_TABLE_RULE = "SHOW SHARDING TABLE RULE t_order";

    private static final String RQL_SHOW_SHARDING_TABLE_RULE_FROM = "SHOW SHARDING TABLE RULE t_order FROM schemaName";

    private static final String RQL_SHOW_READWRITE_SPLITTING_RULES = "SHOW READWRITE_SPLITTING RULES FROM readwrite_splitting_db";

    private static final String RQL_SHOW_DB_DISCOVERY_RULES = "SHOW DB_DISCOVERY RULES FROM db_discovery_db";

    private static final String RQL_SHOW_ENCRYPT_RULES = "SHOW ENCRYPT RULES FROM encrypt_db";

    private static final String RQL_SHOW_ENCRYPT_TABLE_RULE = "SHOW ENCRYPT TABLE RULE t_encrypt FROM encrypt_db";

    private final DistSQLStatementParserEngine engine = new DistSQLStatementParserEngine();

    @Test
    public void assertParseShowShardingTableRules() {
        SQLStatement sqlStatement = engine.parse(RQL_SHOW_SHARDING_TABLE_RULES);
        assertTrue(sqlStatement instanceof ShowShardingTableRulesStatement);
        assertThat(((ShowShardingTableRulesStatement) sqlStatement).getSchema().get().getIdentifier().getValue(), is("schemaName"));
    }

    @Test
    public void assertParseShowShardingTableRule() {
        SQLStatement sqlStatement = engine.parse(RQL_SHOW_SHARDING_TABLE_RULE);
        assertTrue(sqlStatement instanceof ShowShardingTableRulesStatement);
        assertThat(((ShowShardingTableRulesStatement) sqlStatement).getTableName(), is("t_order"));
    }

    @Test
    public void assertParseShowShardingTableRuleFrom() {
        SQLStatement sqlStatement = engine.parse(RQL_SHOW_SHARDING_TABLE_RULE_FROM);
        assertTrue(sqlStatement instanceof ShowShardingTableRulesStatement);
        assertThat(((ShowShardingTableRulesStatement) sqlStatement).getTableName(), is("t_order"));
        assertThat(((ShowShardingTableRulesStatement) sqlStatement).getSchema().get().getIdentifier().getValue(), is("schemaName"));
    }

    @Test
    public void assertParseShowShardingBindingTableRules() {
        SQLStatement sqlStatement = engine.parse(RQL_SHOW_SHARDING_BINDING_TABLE_RULES);
        assertTrue(sqlStatement instanceof ShowShardingBindingTableRulesStatement);
        assertThat(((ShowShardingBindingTableRulesStatement) sqlStatement).getSchema().get().getIdentifier().getValue(), is("sharding_db"));
    }

    @Test
    public void assertParseShowShardingBroadcastTableRules() {
        SQLStatement sqlStatement = engine.parse(RQL_SHOW_SHARDING_BROADCAST_TABLE_RULES);
        assertTrue(sqlStatement instanceof ShowShardingBroadcastTableRulesStatement);
        assertThat(((ShowShardingBroadcastTableRulesStatement) sqlStatement).getSchema().get().getIdentifier().getValue(), is("sharding_db"));
    }

    @Test
    public void assertParseShowReadwriteSplittingRules() {
        SQLStatement sqlStatement = engine.parse(RQL_SHOW_READWRITE_SPLITTING_RULES);
        assertTrue(sqlStatement instanceof ShowReadwriteSplittingRulesStatement);
        assertThat(((ShowReadwriteSplittingRulesStatement) sqlStatement).getSchema().get().getIdentifier().getValue(), is("readwrite_splitting_db"));
    }

    @Test
    public void assertParseShowDatabaseDiscoveryRules() {
        SQLStatement sqlStatement = engine.parse(RQL_SHOW_DB_DISCOVERY_RULES);
        assertTrue(sqlStatement instanceof ShowDatabaseDiscoveryRulesStatement);
        assertThat(((ShowDatabaseDiscoveryRulesStatement) sqlStatement).getSchema().get().getIdentifier().getValue(), is("db_discovery_db"));
    }

    @Test
    public void assertParseShowEncryptRules() {
        SQLStatement sqlStatement = engine.parse(RQL_SHOW_ENCRYPT_RULES);
        assertTrue(sqlStatement instanceof ShowEncryptRulesStatement);
        assertNull(((ShowEncryptRulesStatement) sqlStatement).getTableName());
        assertThat(((ShowEncryptRulesStatement) sqlStatement).getSchema().get().getIdentifier().getValue(), is("encrypt_db"));
    }

    @Test
    public void assertParseShowEncryptTableRule() {
        SQLStatement sqlStatement = engine.parse(RQL_SHOW_ENCRYPT_TABLE_RULE);
        assertTrue(sqlStatement instanceof ShowEncryptRulesStatement);
        assertThat(((ShowEncryptRulesStatement) sqlStatement).getSchema().get().getIdentifier().getValue(), is("encrypt_db"));
        assertThat(((ShowEncryptRulesStatement) sqlStatement).getTableName(), is("t_encrypt"));
    }
}
