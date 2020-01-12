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

package org.apache.shardingsphere.masterslave.route.engine;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.apache.shardingsphere.core.rule.MasterSlaveRule;
import org.apache.shardingsphere.core.strategy.masterslave.RandomMasterSlaveLoadBalanceAlgorithm;
import org.apache.shardingsphere.masterslave.route.engine.impl.MasterVisitedManager;
import org.apache.shardingsphere.sql.parser.SQLParseEngine;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.LockSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.underlying.route.context.RouteContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MasterSlaveRouterTest {
    
    private static final String INSERT_SQL = "INSERT INTO table (id) VALUES (1)";
    
    private static final String QUERY_SQL = "SELECT * FROM table";
    
    private static final String QUERY_SQL_LOCK = "SELECT * FROM table FOR UPDATE";
    
    private static final String MASTER_DATASOURCE = "master";
    
    private static final String SLAVE_DATASOURCE = "query";
    
    @Mock
    private SQLParseEngine sqlParseEngine;
    
    @Mock
    private InsertStatement insertStatement;
    
    @Mock
    private MasterSlaveRule masterSlaveRule;
    
    @Mock
    private SelectStatement selectStatement;
    
    private MasterSlaveRouter masterSlaveRouter;
    
    @Before
    public void setUp() {
        masterSlaveRouter = new MasterSlaveRouter(masterSlaveRule, sqlParseEngine, true);
        when(sqlParseEngine.parse(QUERY_SQL, false)).thenReturn(selectStatement);
        when(sqlParseEngine.parse(INSERT_SQL, false)).thenReturn(insertStatement);
        when(selectStatement.getLock()).thenReturn(Optional.<LockSegment>absent());
        when(masterSlaveRule.getMasterDataSourceName()).thenReturn(MASTER_DATASOURCE);
        when(masterSlaveRule.getLoadBalanceAlgorithm()).thenReturn(new RandomMasterSlaveLoadBalanceAlgorithm());
        when(masterSlaveRule.getSlaveDataSourceNames()).thenReturn(Lists.newArrayList(SLAVE_DATASOURCE));
    }
    
    @After
    public void tearDown() {
        MasterVisitedManager.clear();
    }
    
    @Test
    public void assertRouteToMaster() {
        RouteContext actual = masterSlaveRouter.route(INSERT_SQL, Collections.emptyList(), false);
        assertThat(actual.getRouteResult().getRouteUnits().size(), is(1));
        assertThat(actual.getRouteResult().getRouteUnits().iterator().next().getActualDataSourceName(), is(MASTER_DATASOURCE));
    }
    
    @Test
    public void assertRouteToSlave() {
        RouteContext actual = masterSlaveRouter.route(QUERY_SQL, Collections.emptyList(), false);
        assertThat(actual.getRouteResult().getRouteUnits().size(), is(1));
        assertThat(actual.getRouteResult().getRouteUnits().iterator().next().getActualDataSourceName(), is(SLAVE_DATASOURCE));
    }
    
    @Test
    public void assertLockRouteToMaster() {
        RouteContext actual = masterSlaveRouter.route(QUERY_SQL_LOCK, Collections.emptyList(), false);
        assertThat(actual.getRouteResult().getRouteUnits().size(), is(1));
        assertThat(actual.getRouteResult().getRouteUnits().iterator().next().getActualDataSourceName(), is(MASTER_DATASOURCE));
    }
}
