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

package org.apache.shardingsphere.core.route.router.masterslave;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collection;

import org.apache.shardingsphere.core.parse.SQLParseEngine;
import org.apache.shardingsphere.core.parse.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.rule.MasterSlaveRule;
import org.apache.shardingsphere.core.strategy.masterslave.RandomMasterSlaveLoadBalanceAlgorithm;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class MasterSlaveRouterTest {
    
    private static final String QUERY_SQL_WITH_TABLE = "select * from table";

    private static final String QUERY_SQL_WITHOUT_TABLE = "select now()";
    
    private static final String INSERT_SQL = "insert into table (id) values (1)";

    private static final String MASTER_DATASOURCE = "master";
    
    private static final String SLAVE_DATASOURCE = "query";
    
    @Mock
    private SQLParseEngine sqlParseEngine;
    
    @Mock
    private MasterSlaveRule masterSlaveRule;

    @Mock
    private SelectStatement selectStatementWithTable;

    @Mock
    private SelectStatement selectStatementWithoutTable;
    
    @Mock
    private InsertStatement insertStatement;

    @Mock
    private TableSegment tableSegment;
    
    private MasterSlaveRouter masterSlaveRouter;
    
    @Before
    public void setUp() throws Exception {
        masterSlaveRouter = new MasterSlaveRouter(masterSlaveRule, sqlParseEngine, true);
        when(sqlParseEngine.parse(QUERY_SQL_WITH_TABLE, false)).thenReturn(selectStatementWithTable);
        when(sqlParseEngine.parse(QUERY_SQL_WITHOUT_TABLE, false)).thenReturn(selectStatementWithoutTable);
        when(sqlParseEngine.parse(INSERT_SQL, false)).thenReturn(insertStatement);
        when(masterSlaveRule.getMasterDataSourceName()).thenReturn(MASTER_DATASOURCE);
        when(masterSlaveRule.getLoadBalanceAlgorithm()).thenReturn(new RandomMasterSlaveLoadBalanceAlgorithm());
        when(masterSlaveRule.getSlaveDataSourceNames()).thenReturn(Lists.newArrayList(SLAVE_DATASOURCE));
        when(selectStatementWithTable.getTables()).thenReturn(Lists.newArrayList(tableSegment));
    }
    
    @After
    public void tearDown() {
        MasterVisitedManager.clear();
    }
    
    @Test
    public void assertRouteToMaster() {
        Collection<String> actual = masterSlaveRouter.route(INSERT_SQL, false);
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(MASTER_DATASOURCE));
    }
    
    @Test
    public void assertRouteToSlave() {
        Collection<String> actual = masterSlaveRouter.route(QUERY_SQL_WITH_TABLE, false);
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(SLAVE_DATASOURCE));
    }

    @Test
    public void assertRouteToMasterWithoutTable() {
        Collection<String> actual = masterSlaveRouter.route(QUERY_SQL_WITHOUT_TABLE, false);
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(MASTER_DATASOURCE));
    }
}
