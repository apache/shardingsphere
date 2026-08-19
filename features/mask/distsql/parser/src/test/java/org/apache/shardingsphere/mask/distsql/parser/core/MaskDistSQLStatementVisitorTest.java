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

package org.apache.shardingsphere.mask.distsql.parser.core;

import org.apache.shardingsphere.mask.distsql.parser.facade.MaskDistSQLParserFacade;
import org.apache.shardingsphere.mask.distsql.segment.MaskColumnSegment;
import org.apache.shardingsphere.mask.distsql.segment.MaskRuleSegment;
import org.apache.shardingsphere.mask.distsql.statement.CreateMaskRuleStatement;
import org.apache.shardingsphere.mask.distsql.statement.DropMaskRuleStatement;
import org.apache.shardingsphere.mask.distsql.statement.ShowMaskRulesStatement;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.engine.core.ParseASTNode;
import org.apache.shardingsphere.sql.parser.engine.core.SQLParserFactory;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MaskDistSQLStatementVisitorTest {
    
    @Test
    void assertCreateMaskRuleWithRuleNameOfRemovedAlgorithmType() {
        CreateMaskRuleStatement actual = (CreateMaskRuleStatement) parse("CREATE MASK RULE address_random_replace (COLUMNS((NAME=phone, TYPE(NAME=MD5))))");
        assertThat(actual.getRules().iterator().next().getTableName(), is("address_random_replace"));
    }
    
    @Test
    void assertShowMaskRulesFromDatabaseNamedAsRemovedAlgorithmType() {
        ShowMaskRulesStatement actual = (ShowMaskRulesStatement) parse("SHOW MASK RULES FROM address_random_replace");
        assertThat(actual.getFromDatabase().getDatabase().getIdentifier().getValue(), is("address_random_replace"));
    }
    
    @Test
    void assertDropMaskRuleWithRuleNameOfRemovedAlgorithmType() {
        DropMaskRuleStatement actual = (DropMaskRuleStatement) parse("DROP MASK RULE address_random_replace");
        assertThat(actual.getTables(), is(Collections.singletonList("address_random_replace")));
    }
    
    @Test
    void assertCreateMaskRuleWithBuildInAlgorithmType() {
        CreateMaskRuleStatement actual = (CreateMaskRuleStatement) parse("CREATE MASK RULE t_mask (COLUMNS((NAME=phone, TYPE(NAME=GENERIC_TABLE_RANDOM_REPLACE))))");
        MaskRuleSegment actualRule = actual.getRules().iterator().next();
        assertThat(actualRule.getTableName(), is("t_mask"));
        MaskColumnSegment actualColumn = actualRule.getColumns().iterator().next();
        assertThat(actualColumn.getName(), is("phone"));
        assertThat(actualColumn.getAlgorithm().getName(), is("GENERIC_TABLE_RANDOM_REPLACE"));
    }
    
    private ASTNode parse(final String sql) {
        MaskDistSQLParserFacade facade = new MaskDistSQLParserFacade();
        ParseASTNode parseASTNode = (ParseASTNode) SQLParserFactory.newInstance(sql, facade.getLexerClass(), facade.getParserClass()).parse();
        return new MaskDistSQLStatementVisitor().visit(parseASTNode.getRootNode());
    }
}
