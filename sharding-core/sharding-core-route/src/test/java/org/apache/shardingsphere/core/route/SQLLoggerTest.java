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

package org.apache.shardingsphere.core.route;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.ContextBase;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class SQLLoggerTest {

    private String sql;
    private Collection<String> dataSourceNames;
    private Collection<RouteUnit> routeUnits;

    @Before
    public void setUp() {
        this.sql = "select * from user";
        this.dataSourceNames = Arrays.asList("db1", "db2", "db3");
        this.routeUnits = mockRouteUnits(dataSourceNames,sql);
    }

    @Test
    public void assertlogSQLShard() {
        List<String> actualLogMessages = watchLogMessages(Level.INFO);
        SQLLogger.logSQL(sql, false, null, routeUnits);
        List<String> expectedLogMessages = buildLogSQLShardExpectedMessages();
        assertThat(actualLogMessages, is(expectedLogMessages));
    }

    @Test
    public void assertlogSQLShardSimple() {
        List<String> actualLogMessages = watchLogMessages(Level.INFO);
        SQLLogger.logSQL(sql, true, null, routeUnits);
        List<String> expectedLogMessages = buildLogSQLShardSimpleExpectedMessages();
        assertThat(actualLogMessages, is(expectedLogMessages));
    }

    @Test
    public void assertlogSQLMasterSlave() {
        List<String> actualLogMessages = watchLogMessages(Level.INFO);
        SQLLogger.logSQL(sql, dataSourceNames);
        List<String> expectedLogMessages = buildLogSQLMasterSlaveExpectedMessages();
        assertThat(actualLogMessages, is(expectedLogMessages));
    }

    private List<String> buildLogSQLShardExpectedMessages() {
        List<String> expectedLogMessages = new ArrayList<>();
        expectedLogMessages.add("Rule Type: sharding");
        expectedLogMessages.add("Logic SQL: select * from user");
        expectedLogMessages.add("SQLStatement: null");
        expectedLogMessages.add("Actual SQL: db1 ::: select * from user");
        expectedLogMessages.add("Actual SQL: db2 ::: select * from user");
        expectedLogMessages.add("Actual SQL: db3 ::: select * from user");
        return expectedLogMessages;
    }

    private List<String> buildLogSQLShardSimpleExpectedMessages() {
        Set<String> dataSourceNamesSet = new HashSet<>(routeUnits.size());
        for (RouteUnit each : routeUnits) {
            dataSourceNamesSet.add(each.getDataSourceName());
        }
        List<String> expectedLogMessages = new ArrayList<>();
        expectedLogMessages.add("Rule Type: sharding");
        expectedLogMessages.add("Logic SQL: select * from user");
        expectedLogMessages.add("SQLStatement: null");
        expectedLogMessages.add("Actual SQL(simple): "+dataSourceNamesSet.toString()+" ::: 3");
        return expectedLogMessages;
    }

    private List<String> buildLogSQLMasterSlaveExpectedMessages() {
        List<String> expectedLogMessages = new ArrayList<>();
        expectedLogMessages.add("Rule Type: master-slave");
        expectedLogMessages.add("SQL: select * from user ::: DataSources: db1,db2,db3");
        return expectedLogMessages;
    }

    private List<String> watchLogMessages(Level level) {
        final List<String> loggingEvents = new LinkedList<>();
        ConsoleAppender<ILoggingEvent> appener = new ConsoleAppender<ILoggingEvent>() {
            @Override
            public void doAppend(ILoggingEvent eventObject) {
                loggingEvents.add(eventObject.getFormattedMessage());
            }
        };
        appener.setContext(new ContextBase());
        Logger logger = (Logger) LoggerFactory.getLogger("ShardingSphere-SQL");
        logger.setLevel(level);
        logger.addAppender(appener);
        return loggingEvents;
    }

    private Collection<RouteUnit> mockRouteUnits(Collection<String> dataSourceNames, String sql) {
        List<RouteUnit> results = new LinkedList<>();
        for (String dsName : dataSourceNames) {
            results.addAll(mockOneShard(dsName, 1, sql));
        }
        return results;
    }

    private Collection<RouteUnit> mockOneShard(final String dsName, final int size, String sql) {
        Collection<RouteUnit> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(new RouteUnit(dsName, new SQLUnit(sql, new ArrayList<>())));
        }
        return result;
    }
}
