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

package org.apache.shardingsphere.sql.parser;

import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.spi.NewInstanceServiceLoader;
import org.apache.shardingsphere.sql.parser.core.parser.SQLParserFactory;
import org.apache.shardingsphere.sql.parser.core.visitor.SQLVisitorEngine;
import org.apache.shardingsphere.sql.parser.spi.SQLParserEntry;
import org.apache.shardingsphere.sql.parser.sql.ASTNode;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.UseStatement;
import org.apache.shardingsphere.test.sql.SQLCaseType;
import org.apache.shardingsphere.test.sql.loader.SQLCasesLoader;
import org.apache.shardingsphere.test.sql.loader.sharding.ShardingSQLCasesRegistry;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class MySQLVisitorTest {
    
    private static SQLCasesLoader sqlCasesLoader = ShardingSQLCasesRegistry.getInstance().getSqlCasesLoader();
    
    private final String databaseTypeName = "MySQL";
    
    @Before
    public void setUp() {
        NewInstanceServiceLoader.register(SQLParserEntry.class);
    }
    
    private ASTNode getParseTree(final String sqlCase) {
        String sql = sqlCasesLoader.getSQL(sqlCase, SQLCaseType.Literal, Collections.emptyList());
        ParseTree parseTree = SQLParserFactory.newInstance(databaseTypeName, sql).execute().getChild(0);
        return new SQLVisitorEngine(databaseTypeName, parseTree).parse();
    }
    
    @Test
    public void assertUseStatement() {
        UseStatement useStatement = (UseStatement) getParseTree("use_db");
        assertThat(useStatement.getSchema(), is("sharding_db"));
    }
}
