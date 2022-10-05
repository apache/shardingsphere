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

package org.apache.shardingsphere.transaction.distsql.parser.facade;

import org.apache.shardingsphere.distsql.parser.engine.spi.FeaturedDistSQLStatementParserFacade;
import org.apache.shardingsphere.sql.parser.api.parser.SQLLexer;
import org.apache.shardingsphere.sql.parser.api.parser.SQLParser;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.transaction.distsql.parser.core.TransactionDistSQLLexer;
import org.apache.shardingsphere.transaction.distsql.parser.core.TransactionDistSQLParser;
import org.apache.shardingsphere.transaction.distsql.parser.core.TransactionDistSQLStatementVisitor;

/**
 * SQL parser facade for transaction dist SQL statement.
 */
public final class TransactionDistSQLStatementParserFacade implements FeaturedDistSQLStatementParserFacade {
    
    @Override
    public Class<? extends SQLLexer> getLexerClass() {
        return TransactionDistSQLLexer.class;
    }
    
    @Override
    public Class<? extends SQLParser> getParserClass() {
        return TransactionDistSQLParser.class;
    }
    
    @Override
    public Class<? extends SQLVisitor> getVisitorClass() {
        return TransactionDistSQLStatementVisitor.class;
    }
    
    @Override
    public String getType() {
        return "transaction";
    }
}
