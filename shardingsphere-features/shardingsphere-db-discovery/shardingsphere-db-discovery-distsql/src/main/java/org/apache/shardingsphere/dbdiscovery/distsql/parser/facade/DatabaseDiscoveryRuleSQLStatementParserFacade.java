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

package org.apache.shardingsphere.dbdiscovery.distsql.parser.facade;

import org.apache.shardingsphere.dbdiscovery.distsql.parser.core.DatabaseDiscoveryRuleLexer;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.core.DatabaseDiscoveryRuleParser;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.core.DatabaseDiscoveryRuleSQLStatementVisitor;
import org.apache.shardingsphere.distsql.parser.spi.RuleSQLStatementParserFacade;
import org.apache.shardingsphere.sql.parser.api.parser.SQLLexer;
import org.apache.shardingsphere.sql.parser.api.parser.SQLParser;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;

/**
 * SQL parser facade for database discovery rule SQL statement.
 */
public final class DatabaseDiscoveryRuleSQLStatementParserFacade implements RuleSQLStatementParserFacade {
    
    @Override
    public Class<? extends SQLLexer> getLexerClass() {
        return DatabaseDiscoveryRuleLexer.class;
    }
    
    @Override
    public Class<? extends SQLParser> getParserClass() {
        return DatabaseDiscoveryRuleParser.class;
    }
    
    @Override
    public Class<? extends SQLVisitor> getVisitorClass() {
        return DatabaseDiscoveryRuleSQLStatementVisitor.class;
    }
    
    @Override
    public String getRuleType() {
        return "database-discovery";
    }
}
