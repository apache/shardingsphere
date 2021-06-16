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

package org.apache.shardingsphere.readwritesplitting.distsql.parser.core;

import org.apache.shardingsphere.distsql.parser.api.DistSQLStatementParserEngine;
import org.apache.shardingsphere.distsql.parser.segment.rdl.ReadwriteSplittingRuleSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.impl.AlterReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.impl.ShowReadwriteSplittingRulesStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

// TODO use Parameterized + XML instead of static test
public final class ReadwriteSplittingRuleStatementParserEngineTest {
    
    private static final String CREATE_STATIC_READWRITE_SPLITTING_RULE = "CREATE READWRITE_SPLITTING RULE ms_group_0 ("
            + "WRITE_RESOURCE=primary_ds,"
            + "READ_RESOURCES(replica_ds_0,replica_ds_1),"
            + "TYPE(NAME=random)"
            + ")";
    
    private static final String CREATE_DYNAMIC_READWRITE_SPLITTING_RULE = "CREATE READWRITE_SPLITTING RULE ms_group_1 ("
            + "AUTO_AWARE_RESOURCE=group_0,"
            + "TYPE(NAME=random,PROPERTIES(read_weight='2:1'))"
            + ")";
    
    private static final String ALTER_READWRITE_SPLITTING_RULE = "ALTER READWRITE_SPLITTING RULE ms_group_0 ("
            + "AUTO_AWARE_RESOURCE=group_0,"
            + "TYPE(NAME=random,PROPERTIES(read_weight='2:1'))),"
            + "ms_group_1 ("
            + "WRITE_RESOURCE=primary_ds,"
            + "READ_RESOURCES(replica_ds_0,replica_ds_1),"
            + "TYPE(NAME=random)"
            + ")";
    
    private static final String DROP_READWRITE_SPLITTING_RULE = "DROP READWRITE_SPLITTING RULE ms_group_0,ms_group_1";
    
    private static final String SHOW_READWRITE_SPLITTING_RULES = "SHOW READWRITE_SPLITTING RULES FROM readwrite_splitting_db";
    
    private final DistSQLStatementParserEngine engine = new DistSQLStatementParserEngine();
    
    @Test
    public void assertParseStaticReadwriteSplittingRule() {
        SQLStatement sqlStatement = engine.parse(CREATE_STATIC_READWRITE_SPLITTING_RULE);
        assertTrue(sqlStatement instanceof CreateReadwriteSplittingRuleStatement);
        CreateReadwriteSplittingRuleStatement statement = (CreateReadwriteSplittingRuleStatement) sqlStatement;
        assertThat(statement.getRules().size(), is(1));
        List<ReadwriteSplittingRuleSegment> readwriteSplittingRuleSegments
                = new ArrayList<>(((CreateReadwriteSplittingRuleStatement) sqlStatement).getRules());
        assertThat(readwriteSplittingRuleSegments.get(0).getName(), is("ms_group_0"));
        assertThat(readwriteSplittingRuleSegments.get(0).getWriteDataSource(), is("primary_ds"));
        assertThat(readwriteSplittingRuleSegments.get(0).getReadDataSources(), is(Arrays.asList("replica_ds_0", "replica_ds_1")));
        assertThat(readwriteSplittingRuleSegments.get(0).getLoadBalancer(), is("random"));
        assertThat(readwriteSplittingRuleSegments.get(0).getProps().size(), is(0));
    }
    
    @Test
    public void assertParseDynamicReadwriteSplittingRule() {
        SQLStatement sqlStatement = engine.parse(CREATE_DYNAMIC_READWRITE_SPLITTING_RULE);
        assertTrue(sqlStatement instanceof CreateReadwriteSplittingRuleStatement);
        CreateReadwriteSplittingRuleStatement statement = (CreateReadwriteSplittingRuleStatement) sqlStatement;
        assertThat(statement.getRules().size(), is(1));
        List<ReadwriteSplittingRuleSegment> readwriteSplittingRuleSegments
                = new ArrayList<>(((CreateReadwriteSplittingRuleStatement) sqlStatement).getRules());
        assertThat(readwriteSplittingRuleSegments.get(0).getName(), is("ms_group_1"));
        assertThat(readwriteSplittingRuleSegments.get(0).getAutoAwareResource(), is("group_0"));
        assertNull(readwriteSplittingRuleSegments.get(0).getWriteDataSource());
        assertNull(readwriteSplittingRuleSegments.get(0).getReadDataSources());
        assertThat(readwriteSplittingRuleSegments.get(0).getLoadBalancer(), is("random"));
        assertThat(readwriteSplittingRuleSegments.get(0).getProps().size(), is(1));
        assertThat(readwriteSplittingRuleSegments.get(0).getProps().getProperty("read_weight"), is("'2:1'"));
    }
    
    @Test
    public void assertParseAlterReadwriteSplittingRule() {
        SQLStatement sqlStatement = engine.parse(ALTER_READWRITE_SPLITTING_RULE);
        assertTrue(sqlStatement instanceof AlterReadwriteSplittingRuleStatement);
        AlterReadwriteSplittingRuleStatement statement = (AlterReadwriteSplittingRuleStatement) sqlStatement;
        assertThat(statement.getRules().size(), is(2));
        List<ReadwriteSplittingRuleSegment> readwriteSplittingRuleSegments = new ArrayList<>(((AlterReadwriteSplittingRuleStatement) sqlStatement).getRules());
        assertThat(readwriteSplittingRuleSegments.get(0).getName(), is("ms_group_0"));
        assertThat(readwriteSplittingRuleSegments.get(0).getAutoAwareResource(), is("group_0"));
        assertNull(readwriteSplittingRuleSegments.get(0).getWriteDataSource());
        assertNull(readwriteSplittingRuleSegments.get(0).getReadDataSources());
        assertThat(readwriteSplittingRuleSegments.get(0).getLoadBalancer(), is("random"));
        assertThat(readwriteSplittingRuleSegments.get(0).getProps().size(), is(1));
        assertThat(readwriteSplittingRuleSegments.get(0).getProps().getProperty("read_weight"), is("'2:1'"));
        assertThat(readwriteSplittingRuleSegments.get(1).getName(), is("ms_group_1"));
        assertThat(readwriteSplittingRuleSegments.get(1).getWriteDataSource(), is("primary_ds"));
        assertThat(readwriteSplittingRuleSegments.get(1).getReadDataSources(), is(Arrays.asList("replica_ds_0", "replica_ds_1")));
        assertThat(readwriteSplittingRuleSegments.get(1).getLoadBalancer(), is("random"));
        assertThat(readwriteSplittingRuleSegments.get(1).getProps().size(), is(0));
    }
    
    @Test
    public void assertParseDropReadwriteSplittingRule() {
        SQLStatement sqlStatement = engine.parse(DROP_READWRITE_SPLITTING_RULE);
        assertTrue(sqlStatement instanceof DropReadwriteSplittingRuleStatement);
        assertThat(((DropReadwriteSplittingRuleStatement) sqlStatement).getRuleNames(), is(Arrays.asList("ms_group_0", "ms_group_1")));
    }
    
    @Test
    public void assertParseShowReadwriteSplittingRules() {
        SQLStatement sqlStatement = engine.parse(SHOW_READWRITE_SPLITTING_RULES);
        assertTrue(sqlStatement instanceof ShowReadwriteSplittingRulesStatement);
        assertThat(((ShowReadwriteSplittingRulesStatement) sqlStatement).getSchema().get().getIdentifier().getValue(), is("readwrite_splitting_db"));
    }
}
