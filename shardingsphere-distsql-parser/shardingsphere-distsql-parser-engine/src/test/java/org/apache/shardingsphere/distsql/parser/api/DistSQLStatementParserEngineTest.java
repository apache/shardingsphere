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

import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.AddResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.DropResourceStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

// TODO use Parameterized + XML instead of static test
public final class DistSQLStatementParserEngineTest {
    
    private static final String ADD_RESOURCE_SINGLE_WITHOUT_PASSWORD = "ADD RESOURCE ds_0(URL=\"jdbc:mysql://127.0.0.1:3306/test0\",USER=ROOT);";
    
    private static final String ADD_RESOURCE_SINGLE_WITH_PASSWORD = "ADD RESOURCE ds_0(URL=\"jdbc:mysql://127.0.0.1:3306/test0\",USER=ROOT,PASSWORD=123456);";
    
    private static final String ADD_RESOURCE_MULTIPLE = "ADD RESOURCE ds_0(URL=\"jdbc:mysql://127.0.0.1:3306/test0\",USER=ROOT,PASSWORD=123456),"
            + "ds_1(URL=\"jdbc:mysql://127.0.0.1:3306/test1\",USER=ROOT,PASSWORD=123456);";
    
    private static final String ADD_RESOURCE_SINGLE_WITH_EMPTY_PROPERTIES = "ADD RESOURCE ds_0(URL=\"jdbc:mysql://127.0.0.1:3306/test0\",USER=ROOT,PROPERTIES());";
    
    private static final String ADD_RESOURCE_SINGLE_WITH_PROPERTIES = "ADD RESOURCE ds_0(URL=\"jdbc:mysql://127.0.0.1:3306/test0\",USER=ROOT,PASSWORD=123456,PROPERTIES(" 
            + "\"maxPoolSize\"=50));";
    
    private static final String DROP_RESOURCE = "DROP RESOURCE ds_0,ds_1";
    
    private final DistSQLStatementParserEngine engine = new DistSQLStatementParserEngine();
    
    @Test
    public void assertParseAddSingleResourceWithoutPassword() {
        SQLStatement sqlStatement = engine.parse(ADD_RESOURCE_SINGLE_WITHOUT_PASSWORD);
        assertTrue(sqlStatement instanceof AddResourceStatement);
        assertThat(((AddResourceStatement) sqlStatement).getDataSources().size(), is(1));
        DataSourceSegment dataSourceSegment = ((AddResourceStatement) sqlStatement).getDataSources().iterator().next();
        assertThat(dataSourceSegment.getName(), is("ds_0"));
        assertThat(dataSourceSegment.getUrl(), is("jdbc:mysql://127.0.0.1:3306/test0"));
        assertThat(dataSourceSegment.getUser(), is("ROOT"));
    }
    
    @Test
    public void assertParseAddSingleResourceWithPassword() {
        SQLStatement sqlStatement = engine.parse(ADD_RESOURCE_SINGLE_WITH_PASSWORD);
        assertTrue(sqlStatement instanceof AddResourceStatement);
        assertThat(((AddResourceStatement) sqlStatement).getDataSources().size(), is(1));
        DataSourceSegment dataSourceSegment = ((AddResourceStatement) sqlStatement).getDataSources().iterator().next();
        assertThat(dataSourceSegment.getName(), is("ds_0"));
        assertThat(dataSourceSegment.getUrl(), is("jdbc:mysql://127.0.0.1:3306/test0"));
        assertThat(dataSourceSegment.getUser(), is("ROOT"));
        assertThat(dataSourceSegment.getPassword(), is("123456"));
    }
    
    @Test
    public void assertParseAddMultipleResources() {
        SQLStatement sqlStatement = engine.parse(ADD_RESOURCE_MULTIPLE);
        assertTrue(sqlStatement instanceof AddResourceStatement);
        assertThat(((AddResourceStatement) sqlStatement).getDataSources().size(), is(2));
        List<DataSourceSegment> dataSourceSegments = new ArrayList<>(((AddResourceStatement) sqlStatement).getDataSources());
        DataSourceSegment dataSourceSegment = dataSourceSegments.get(0);
        assertThat(dataSourceSegment.getName(), is("ds_0"));
        assertThat(dataSourceSegment.getUrl(), is("jdbc:mysql://127.0.0.1:3306/test0"));
        assertThat(dataSourceSegment.getUser(), is("ROOT"));
        assertThat(dataSourceSegment.getPassword(), is("123456"));
        dataSourceSegment = dataSourceSegments.get(1);
        assertThat(dataSourceSegment.getName(), is("ds_1"));
        assertThat(dataSourceSegment.getUrl(), is("jdbc:mysql://127.0.0.1:3306/test1"));
        assertThat(dataSourceSegment.getUser(), is("ROOT"));
        assertThat(dataSourceSegment.getPassword(), is("123456"));
    }
    
    @Test
    public void assertParseDropResource() {
        SQLStatement sqlStatement = engine.parse(DROP_RESOURCE);
        assertTrue(sqlStatement instanceof DropResourceStatement);
        assertThat(((DropResourceStatement) sqlStatement).getNames().size(), is(2));
        assertTrue(((DropResourceStatement) sqlStatement).getNames().containsAll(Arrays.asList("ds_0", "ds_1")));
    }
    
    @Test
    public void assertParseAddSingleResourceWithEmptyProperties() {
        SQLStatement sqlStatement = engine.parse(ADD_RESOURCE_SINGLE_WITH_EMPTY_PROPERTIES);
        assertTrue(sqlStatement instanceof AddResourceStatement);
        assertThat(((AddResourceStatement) sqlStatement).getDataSources().size(), is(1));
        DataSourceSegment dataSourceSegment = ((AddResourceStatement) sqlStatement).getDataSources().iterator().next();
        assertThat(dataSourceSegment.getName(), is("ds_0"));
        assertThat(dataSourceSegment.getUrl(), is("jdbc:mysql://127.0.0.1:3306/test0"));
        assertThat(dataSourceSegment.getUser(), is("ROOT"));
        assertThat(dataSourceSegment.getProperties().size(), is(0));
    }
    
    @Test
    public void assertParseAddSingleResourceWithProperties() {
        SQLStatement sqlStatement = engine.parse(ADD_RESOURCE_SINGLE_WITH_PROPERTIES);
        assertTrue(sqlStatement instanceof AddResourceStatement);
        assertThat(((AddResourceStatement) sqlStatement).getDataSources().size(), is(1));
        DataSourceSegment dataSourceSegment = ((AddResourceStatement) sqlStatement).getDataSources().iterator().next();
        assertThat(dataSourceSegment.getName(), is("ds_0"));
        assertThat(dataSourceSegment.getUrl(), is("jdbc:mysql://127.0.0.1:3306/test0"));
        assertThat(dataSourceSegment.getUser(), is("ROOT"));
        assertThat(dataSourceSegment.getPassword(), is("123456"));
        assertThat(dataSourceSegment.getProperties().size(), is(1));
        assertThat(dataSourceSegment.getProperties().getProperty("maxPoolSize"), is("50"));
    }
}
