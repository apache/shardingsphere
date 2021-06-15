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

package org.apache.shardingsphere.dbdiscovery.distsql.parser.core;

import org.apache.shardingsphere.distsql.parser.api.DistSQLStatementParserEngine;
import org.apache.shardingsphere.distsql.parser.segment.rdl.DatabaseDiscoveryRuleSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.impl.AlterDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.impl.ShowDatabaseDiscoveryRulesStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

// TODO use Parameterized + XML instead of static test
public final class DatabaseDiscoveryRuleStatementParserEngineTest {
    
    private static final String CREATE_DATABASE_DISCOVERY_RULE = "CREATE DB_DISCOVERY RULE ha_group_0 ("
            + "RESOURCES(resource0,resource1),"
            + "TYPE(NAME=mgr,PROPERTIES(groupName='92504d5b-6dec',keepAliveCron=''))),"
            + "ha_group_1 ("
            + "RESOURCES(resource2,resource3),"
            + "TYPE(NAME=mgr2,PROPERTIES(groupName='92504d5b-6dec-2',keepAliveCron=''))"
            + ")";
    
    private static final String ALTER_DATABASE_DISCOVERY_RULE = "ALTER DB_DISCOVERY RULE ha_group_0 ("
            + "RESOURCES(resource0,resource1),"
            + "TYPE(NAME=mgr,PROPERTIES(groupName='92504d5b-6dec',keepAliveCron=''))),"
            + "ha_group_1 ("
            + "RESOURCES(resource2,resource3),"
            + "TYPE(NAME=mgr2,PROPERTIES(groupName='92504d5b-6dec-2',keepAliveCron=''))"
            + ")";
    
    private static final String DROP_DATABASE_DISCOVERY_RULE = "DROP DB_DISCOVERY RULE ha_group_0,ha_group_1";
    
    private static final String SHOW_DB_DISCOVERY_RULES = "SHOW DB_DISCOVERY RULES FROM db_discovery_db";
    
    private final DistSQLStatementParserEngine engine = new DistSQLStatementParserEngine();
    
    @Test
    public void assertParseCreateDatabaseDiscoveryRule() {
        SQLStatement sqlStatement = engine.parse(CREATE_DATABASE_DISCOVERY_RULE);
        assertTrue(sqlStatement instanceof CreateDatabaseDiscoveryRuleStatement);
        CreateDatabaseDiscoveryRuleStatement statement = (CreateDatabaseDiscoveryRuleStatement) sqlStatement;
        assertThat(statement.getRules().size(), is(2));
        List<DatabaseDiscoveryRuleSegment> databaseDiscoveryRuleSegments
                = new ArrayList<>(((CreateDatabaseDiscoveryRuleStatement) sqlStatement).getRules());
        assertThat(databaseDiscoveryRuleSegments.get(0).getName(), is("ha_group_0"));
        assertThat(databaseDiscoveryRuleSegments.get(0).getDiscoveryTypeName(), is("mgr"));
        assertThat(databaseDiscoveryRuleSegments.get(0).getDataSources(), is(Arrays.asList("resource0", "resource1")));
        assertThat(databaseDiscoveryRuleSegments.get(0).getProps().get("groupName"), is("92504d5b-6dec"));
        assertThat(databaseDiscoveryRuleSegments.get(1).getName(), is("ha_group_1"));
        assertThat(databaseDiscoveryRuleSegments.get(1).getDiscoveryTypeName(), is("mgr2"));
        assertThat(databaseDiscoveryRuleSegments.get(1).getDataSources(), is(Arrays.asList("resource2", "resource3")));
        assertThat(databaseDiscoveryRuleSegments.get(1).getProps().get("groupName"), is("92504d5b-6dec-2"));
    }
    
    @Test
    public void assertParseAlterDatabaseDiscoveryRule() {
        SQLStatement sqlStatement = engine.parse(ALTER_DATABASE_DISCOVERY_RULE);
        assertTrue(sqlStatement instanceof AlterDatabaseDiscoveryRuleStatement);
        AlterDatabaseDiscoveryRuleStatement statement = (AlterDatabaseDiscoveryRuleStatement) sqlStatement;
        assertThat(statement.getRules().size(), is(2));
        List<DatabaseDiscoveryRuleSegment> databaseDiscoveryRuleSegments = new ArrayList<>(((AlterDatabaseDiscoveryRuleStatement) sqlStatement).getRules());
        assertThat(databaseDiscoveryRuleSegments.get(0).getName(), is("ha_group_0"));
        assertThat(databaseDiscoveryRuleSegments.get(0).getDiscoveryTypeName(), is("mgr"));
        assertThat(databaseDiscoveryRuleSegments.get(0).getDataSources(), is(Arrays.asList("resource0", "resource1")));
        assertThat(databaseDiscoveryRuleSegments.get(0).getProps().get("groupName"), is("92504d5b-6dec"));
        assertThat(databaseDiscoveryRuleSegments.get(1).getName(), is("ha_group_1"));
        assertThat(databaseDiscoveryRuleSegments.get(1).getDiscoveryTypeName(), is("mgr2"));
        assertThat(databaseDiscoveryRuleSegments.get(1).getDataSources(), is(Arrays.asList("resource2", "resource3")));
        assertThat(databaseDiscoveryRuleSegments.get(1).getProps().get("groupName"), is("92504d5b-6dec-2"));
    }
    
    @Test
    public void assertParseDropDatabaseDiscoveryRule() {
        SQLStatement sqlStatement = engine.parse(DROP_DATABASE_DISCOVERY_RULE);
        assertTrue(sqlStatement instanceof DropDatabaseDiscoveryRuleStatement);
        assertThat(((DropDatabaseDiscoveryRuleStatement) sqlStatement).getRuleNames(), is(Arrays.asList("ha_group_0", "ha_group_1")));
    }
    
    @Test
    public void assertParseShowDatabaseDiscoveryRules() {
        SQLStatement sqlStatement = engine.parse(SHOW_DB_DISCOVERY_RULES);
        assertTrue(sqlStatement instanceof ShowDatabaseDiscoveryRulesStatement);
        assertThat(((ShowDatabaseDiscoveryRulesStatement) sqlStatement).getSchema().get().getIdentifier().getValue(), is("db_discovery_db"));
    }
}
