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

package org.apache.shardingsphere.encrypt.distsql.parser.core;

import org.apache.shardingsphere.encrypt.distsql.statement.ShowEncryptRulesStatement;
import org.apache.shardingsphere.sql.parser.engine.core.ParseASTNode;
import org.apache.shardingsphere.sql.parser.engine.core.SQLParserFactory;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class EncryptDistSQLStatementVisitorTest {
    
    @Test
    void assertVisitShowEncryptRulesWithEscapedIdentifiers() {
        ShowEncryptRulesStatement actual = parseShowEncryptRules("SHOW ENCRYPT TABLE RULE `订``单` FROM `逻``辑库`");
        assertThat(actual.getTableName(), is("订`单"));
        assertThat(actual.getFromDatabase().getDatabase().getIdentifier().getValue(), is("逻`辑库"));
    }
    
    private ShowEncryptRulesStatement parseShowEncryptRules(final String sql) {
        ParseASTNode parseASTNode = (ParseASTNode) SQLParserFactory.newInstance(sql, EncryptDistSQLLexer.class, EncryptDistSQLParser.class).parse();
        return (ShowEncryptRulesStatement) new EncryptDistSQLStatementVisitor().visit(parseASTNode.getRootNode());
    }
}
