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

package org.apache.shardingsphere.agent.metrics.api.advice;

import org.apache.shardingsphere.agent.api.result.MethodInvocationResult;
import org.apache.shardingsphere.agent.metrics.api.MetricsPool;
import org.apache.shardingsphere.agent.metrics.api.constant.MetricIds;
import org.apache.shardingsphere.agent.metrics.api.fixture.FixtureWrapper;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.AddResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowResourcesStatement;
import org.apache.shardingsphere.scaling.distsql.statement.ShowScalingJobListStatement;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.SchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dcl.MySQLCreateUserStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLDeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLUpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.tcl.MySQLCommitStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class SQLParserEngineAdviceTest extends MetricsAdviceBaseTest {
    
    private final SQLParserEngineAdvice sqlParseEngineAdvice = new SQLParserEngineAdvice();
    
    @Mock
    private Method parse;
    
    @Test
    @SuppressWarnings({"unchecked", "OptionalGetWithoutIsPresent"})
    public void assertParse() {
        MockAdviceTargetObject targetObject = new MockAdviceTargetObject();
        MethodInvocationResult result = new MethodInvocationResult();
        result.rebase(new MySQLInsertStatement());
        sqlParseEngineAdvice.afterMethod(targetObject, parse, new Object[]{}, result);
        FixtureWrapper wrapper = (FixtureWrapper) MetricsPool.get(MetricIds.PARSE_SQL_INSERT).get();
        assertNotNull(wrapper);
        assertThat(wrapper.getFixtureValue(), org.hamcrest.Matchers.is(1.0));
        result.rebase(new MySQLDeleteStatement());
        sqlParseEngineAdvice.afterMethod(targetObject, parse, new Object[]{}, result);
        wrapper = (FixtureWrapper) MetricsPool.get(MetricIds.PARSE_SQL_DELETE).get();
        assertNotNull(wrapper);
        assertThat(wrapper.getFixtureValue(), org.hamcrest.Matchers.is(1.0));
        result.rebase(new MySQLUpdateStatement());
        sqlParseEngineAdvice.afterMethod(targetObject, parse, new Object[]{}, result);
        wrapper = (FixtureWrapper) MetricsPool.get(MetricIds.PARSE_SQL_UPDATE).get();
        assertNotNull(wrapper);
        assertThat(wrapper.getFixtureValue(), org.hamcrest.Matchers.is(1.0));
        result.rebase(new MySQLSelectStatement());
        sqlParseEngineAdvice.afterMethod(targetObject, parse, new Object[]{}, result);
        wrapper = (FixtureWrapper) MetricsPool.get(MetricIds.PARSE_SQL_SELECT).get();
        assertNotNull(wrapper);
        assertThat(wrapper.getFixtureValue(), org.hamcrest.Matchers.is(1.0));
        result.rebase(new MySQLCreateDatabaseStatement());
        sqlParseEngineAdvice.afterMethod(targetObject, parse, new Object[]{}, result);
        wrapper = (FixtureWrapper) MetricsPool.get(MetricIds.PARSE_SQL_DDL).get();
        assertNotNull(wrapper);
        assertThat(wrapper.getFixtureValue(), org.hamcrest.Matchers.is(1.0));
        result.rebase(new MySQLCreateUserStatement());
        sqlParseEngineAdvice.afterMethod(targetObject, parse, new Object[]{}, result);
        wrapper = (FixtureWrapper) MetricsPool.get(MetricIds.PARSE_SQL_DCL).get();
        assertNotNull(wrapper);
        assertThat(wrapper.getFixtureValue(), org.hamcrest.Matchers.is(1.0));
        result.rebase(new MySQLShowDatabasesStatement());
        sqlParseEngineAdvice.afterMethod(targetObject, parse, new Object[]{}, result);
        wrapper = (FixtureWrapper) MetricsPool.get(MetricIds.PARSE_SQL_DAL).get();
        assertNotNull(wrapper);
        assertThat(wrapper.getFixtureValue(), org.hamcrest.Matchers.is(1.0));
        result.rebase(new MySQLCommitStatement());
        sqlParseEngineAdvice.afterMethod(targetObject, parse, new Object[]{}, result);
        wrapper = (FixtureWrapper) MetricsPool.get(MetricIds.PARSE_SQL_TCL).get();
        assertNotNull(wrapper);
        assertThat(wrapper.getFixtureValue(), org.hamcrest.Matchers.is(1.0));
        result.rebase(new ShowResourcesStatement(new SchemaSegment(0, 0, null)));
        sqlParseEngineAdvice.afterMethod(targetObject, parse, new Object[]{}, result);
        wrapper = (FixtureWrapper) MetricsPool.get(MetricIds.PARSE_DIST_SQL_RQL).get();
        assertNotNull(wrapper);
        assertThat(wrapper.getFixtureValue(), org.hamcrest.Matchers.is(1.0));
        result.rebase(new AddResourceStatement(Collections.EMPTY_LIST));
        sqlParseEngineAdvice.afterMethod(targetObject, parse, new Object[]{}, result);
        wrapper = (FixtureWrapper) MetricsPool.get(MetricIds.PARSE_DIST_SQL_RDL).get();
        assertNotNull(wrapper);
        assertThat(wrapper.getFixtureValue(), org.hamcrest.Matchers.is(1.0));
        result.rebase(new ShowScalingJobListStatement());
        sqlParseEngineAdvice.afterMethod(targetObject, parse, new Object[]{}, result);
        wrapper = (FixtureWrapper) MetricsPool.get(MetricIds.PARSE_DIST_SQL_RAL).get();
        assertNotNull(wrapper);
        assertThat(wrapper.getFixtureValue(), org.hamcrest.Matchers.is(1.0));
    }
}
