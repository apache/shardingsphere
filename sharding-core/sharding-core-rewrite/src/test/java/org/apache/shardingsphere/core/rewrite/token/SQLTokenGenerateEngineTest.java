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

package org.apache.shardingsphere.core.rewrite.token;

public final class SQLTokenGenerateEngineTest {
    
//    private SQLTokenGenerateEngine shardingTokenGenerateEngine = new ShardingTokenGenerateEngine(mock(ShardingRule.class));
//    
//    private SQLTokenGenerateEngine baseTokenGenerateEngine = new BaseTokenGenerateEngine();
//    
//    private SQLTokenGenerateEngine encryptTokenGenerateEngine = new EncryptTokenGenerateEngine(mock(EncryptRule.class), false);
//    
//    private RewriteStatement rewriteStatement;
//    
//    @Before
//    public void setUp() {
//        SelectStatement selectStatement = new SelectStatement();
//        ProjectionsContext projectionsContext = new ProjectionsContext(1, 20, false, 
//                Collections.<Projection>singletonList(new AggregationDistinctProjection(1, 2, AggregationType.COUNT, "(DISTINCT id)", "c", "id")));
//        SelectSQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(selectStatement,  
//                new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false), 
//                projectionsContext, new PaginationContext(null, null, Collections.emptyList()));
//        rewriteStatement = new RewriteStatement(selectSQLStatementContext, new ShardingConditions(Collections.<ShardingCondition>emptyList()));
//    }
//    
//    @SuppressWarnings("unchecked")
//    @Test
//    public void assertGenerateSQLTokensWithBaseTokenGenerateEngine() {
//        List<SQLToken> actual = baseTokenGenerateEngine.generateSQLTokens(rewriteStatement, null, mock(ShardingRule.class), mock(TableMetas.class), true);
//        assertThat(actual.size(), is(0));
//    }
//    
//    @SuppressWarnings("unchecked")
//    @Test
//    public void assertGetSQLTokenGeneratorsWithShardingTokenGenerateEngineWithoutSingleRoute() {
//        List<SQLToken> actual = shardingTokenGenerateEngine.generateSQLTokens(rewriteStatement, null, mock(ShardingRule.class), mock(TableMetas.class), false);
//        assertThat(actual.size(), is(2));
//        assertThat(actual.get(0), CoreMatchers.<SQLToken>instanceOf(ProjectionPrefixToken.class));
//    }
//    
//    @SuppressWarnings("unchecked")
//    @Test
//    public void assertGetSQLTokenGeneratorsWithShardingTokenGenerateEngineWithSingleRoute() {
//        List<SQLToken> actual = shardingTokenGenerateEngine.generateSQLTokens(rewriteStatement, null, mock(ShardingRule.class), mock(TableMetas.class), true);
//        assertThat(actual.size(), is(0));
//    }
//    
//    @SuppressWarnings("unchecked")
//    @Test
//    public void assertGenerateSQLTokensWithEncryptTokenGenerateEngine() {
//        List<SQLToken> actual = encryptTokenGenerateEngine.generateSQLTokens(rewriteStatement, null, mock(EncryptRule.class), mock(TableMetas.class), true);
//        assertThat(actual.size(), is(0));
//    }
}
