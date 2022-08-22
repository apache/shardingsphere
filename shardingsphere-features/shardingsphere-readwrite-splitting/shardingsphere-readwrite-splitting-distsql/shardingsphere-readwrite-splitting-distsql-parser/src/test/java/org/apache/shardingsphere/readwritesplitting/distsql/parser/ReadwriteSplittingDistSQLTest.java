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

package org.apache.shardingsphere.readwritesplitting.distsql.parser;

import lombok.SneakyThrows;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.apache.shardingsphere.distsql.parser.core.featured.FeaturedDistSQLStatementParserFacadeFactory;
import org.apache.shardingsphere.distsql.parser.statement.DistSQLStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.facade.ReadwriteSplittingDistSQLStatementParserFacade;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.segment.ReadwriteSplittingRuleSegment;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.CreateReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.core.ParseASTNode;
import org.apache.shardingsphere.sql.parser.core.SQLParserFactory;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ReadwriteSplittingDistSQLTest {
    
    @Test
    public void assertCreateReadwriteSplitting() {
        String sql = "CREATE READWRITE_SPLITTING RULE ms_group_0 (WRITE_RESOURCE=primary_ds, READ_RESOURCES(replica_ds_0,replica_ds_1), TYPE(NAME=random)))";
        CreateReadwriteSplittingRuleStatement distSQLStatement = (CreateReadwriteSplittingRuleStatement) getShadowDistSQLStatement(sql);
        assertThat(distSQLStatement.getRules().size(), is(1));
        ReadwriteSplittingRuleSegment readwriteSplittingRule = distSQLStatement.getRules().iterator().next();
        assertThat(readwriteSplittingRule.getName(), is("ms_group_0"));
        assertThat(readwriteSplittingRule.getWriteDataSource(), is("primary_ds"));
        assertThat(readwriteSplittingRule.getLoadBalancer(), is("random"));
        assertThat(readwriteSplittingRule.getReadDataSources(), is(Arrays.asList("replica_ds_0", "replica_ds_1")));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    @SuppressWarnings("rawtypes")
    private DistSQLStatement getShadowDistSQLStatement(final String sql) {
        ReadwriteSplittingDistSQLStatementParserFacade facade = new ReadwriteSplittingDistSQLStatementParserFacade();
        ParseASTNode parseASTNode = (ParseASTNode) SQLParserFactory.newInstance(sql, facade.getLexerClass(), facade.getParserClass()).parse();
        SQLVisitor visitor = FeaturedDistSQLStatementParserFacadeFactory.getInstance(facade.getType()).getVisitorClass().getDeclaredConstructor().newInstance();
        return (DistSQLStatement) ((ParseTreeVisitor) visitor).visit(parseASTNode.getRootNode());
    }
}
