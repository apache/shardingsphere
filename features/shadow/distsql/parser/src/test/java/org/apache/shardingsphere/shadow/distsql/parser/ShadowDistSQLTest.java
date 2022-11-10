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

package org.apache.shardingsphere.shadow.distsql.parser;

import lombok.SneakyThrows;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.apache.shardingsphere.distsql.parser.core.featured.FeaturedDistSQLStatementParserFacadeFactory;
import org.apache.shardingsphere.distsql.parser.statement.DistSQLStatement;
import org.apache.shardingsphere.shadow.distsql.parser.facade.ShadowDistSQLStatementParserFacade;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowRuleSegment;
import org.apache.shardingsphere.shadow.distsql.parser.statement.AlterShadowRuleStatement;
import org.apache.shardingsphere.shadow.distsql.parser.statement.CreateShadowRuleStatement;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.core.ParseASTNode;
import org.apache.shardingsphere.sql.parser.core.SQLParserFactory;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class ShadowDistSQLTest {
    
    @Test
    public void assertCreateShadowRule() {
        String sql = "CREATE SHADOW RULE `shadow_rule`(SOURCE=demo_ds,SHADOW=demo_ds_shadow,t_order(TYPE(NAME='SIMPLE_HINT',PROPERTIES('shadow'='true','foo'='bar'))))";
        CreateShadowRuleStatement shadowDistSQLStatement = (CreateShadowRuleStatement) getShadowDistSQLStatement(sql);
        assertThat(shadowDistSQLStatement.getRules().size(), is(1));
        assertShadowRuleSegment(shadowDistSQLStatement.getRules().iterator().next());
    }
    
    @Test
    public void assertAlterShadowRule() {
        String sql = "ALTER SHADOW RULE `shadow_rule`(SOURCE=demo_ds,SHADOW=demo_ds_shadow,t_order(TYPE(NAME='SIMPLE_HINT',PROPERTIES('shadow'='true','foo'='bar'))))";
        AlterShadowRuleStatement shadowDistSQLStatement = (AlterShadowRuleStatement) getShadowDistSQLStatement(sql);
        assertThat(shadowDistSQLStatement.getRules().size(), is(1));
        assertShadowRuleSegment(shadowDistSQLStatement.getRules().iterator().next());
    }
    
    private void assertShadowRuleSegment(final ShadowRuleSegment shadowRuleSegment) {
        assertThat(shadowRuleSegment.getRuleName(), is("shadow_rule"));
        assertThat(shadowRuleSegment.getSource(), is("demo_ds"));
        assertThat(shadowRuleSegment.getShadow(), is("demo_ds_shadow"));
        assertThat(shadowRuleSegment.getShadowTableRules().size(), is(1));
        assertThat(shadowRuleSegment.getShadowTableRules().containsKey("t_order"), is(true));
        ShadowAlgorithmSegment shadowAlgorithmSegment = shadowRuleSegment.getShadowTableRules().get("t_order").iterator().next();
        assertThat(shadowAlgorithmSegment.getAlgorithmName(), is("shadow_rule_t_order_simple_hint_0"));
        assertThat(shadowAlgorithmSegment.getAlgorithmSegment().getName(), is("SIMPLE_HINT"));
        Properties properties = new Properties();
        properties.setProperty("shadow", "true");
        properties.setProperty("foo", "bar");
        assertThat(shadowAlgorithmSegment.getAlgorithmSegment().getProps(), is(properties));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    @SuppressWarnings("rawtypes")
    private DistSQLStatement getShadowDistSQLStatement(final String sql) {
        ShadowDistSQLStatementParserFacade facade = new ShadowDistSQLStatementParserFacade();
        ParseASTNode parseASTNode = (ParseASTNode) SQLParserFactory.newInstance(sql, facade.getLexerClass(), facade.getParserClass()).parse();
        SQLVisitor visitor = FeaturedDistSQLStatementParserFacadeFactory.getInstance(facade.getType()).getVisitorClass().getDeclaredConstructor().newInstance();
        return (DistSQLStatement) ((ParseTreeVisitor) visitor).visit(parseASTNode.getRootNode());
    }
}
