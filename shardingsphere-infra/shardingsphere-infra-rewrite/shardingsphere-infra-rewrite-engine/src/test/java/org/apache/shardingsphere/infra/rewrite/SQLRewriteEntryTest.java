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

package org.apache.shardingsphere.infra.rewrite;

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.rewrite.engine.result.GenericSQLRewriteResult;
import org.apache.shardingsphere.infra.rewrite.engine.result.RouteSQLRewriteResult;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public final class SQLRewriteEntryTest {
    
    @Mock
    private SchemaMetaData metaData;
    
    @Mock
    private ConfigurationProperties props;
    
    @Test
    public void assertRewriteForGenericSQLRewriteResult() {
        SQLRewriteEntry sqlRewriteEntry = new SQLRewriteEntry(metaData, props, Collections.emptyList());
        RouteContext routeContext = new RouteContext();
        GenericSQLRewriteResult sqlRewriteResult = (GenericSQLRewriteResult) sqlRewriteEntry.rewrite("SELECT ?", Collections.singletonList(1), mock(SQLStatementContext.class), routeContext);
        assertThat(sqlRewriteResult.getSqlRewriteUnit().getSql(), is("SELECT ?"));
        assertThat(sqlRewriteResult.getSqlRewriteUnit().getParameters(), is(Collections.singletonList(1)));
    }
    
    @Test
    public void assertRewriteForRouteSQLRewriteResult() {
        SQLRewriteEntry sqlRewriteEntry = new SQLRewriteEntry(metaData, props, Collections.emptyList());
        RouteContext routeContext = new RouteContext();
        routeContext.getRouteResult().getRouteUnits().addAll(Arrays.asList(mock(RouteUnit.class), mock(RouteUnit.class)));
        RouteSQLRewriteResult sqlRewriteResult = (RouteSQLRewriteResult) sqlRewriteEntry.rewrite("SELECT ?", Collections.singletonList(1), mock(SQLStatementContext.class), routeContext);
        assertThat(sqlRewriteResult.getSqlRewriteUnits().size(), is(2));
    }
}
