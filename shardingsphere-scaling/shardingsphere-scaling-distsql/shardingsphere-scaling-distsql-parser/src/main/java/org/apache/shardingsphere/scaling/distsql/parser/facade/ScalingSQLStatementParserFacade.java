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

package org.apache.shardingsphere.scaling.distsql.parser.facade;

import org.apache.shardingsphere.distsql.parser.spi.RuleSQLStatementParserFacade;
import org.apache.shardingsphere.scaling.distsql.parser.core.ScalingLexer;
import org.apache.shardingsphere.scaling.distsql.parser.core.ScalingParser;
import org.apache.shardingsphere.scaling.distsql.parser.core.ScalingSQLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.parser.SQLLexer;
import org.apache.shardingsphere.sql.parser.api.parser.SQLParser;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;

/**
 * SQL parser facade for scaling SQL statement.
 */
public final class ScalingSQLStatementParserFacade implements RuleSQLStatementParserFacade {
    
    @Override
    public Class<? extends SQLLexer> getLexerClass() {
        return ScalingLexer.class;
    }
    
    @Override
    public Class<? extends SQLParser> getParserClass() {
        return ScalingParser.class;
    }
    
    @Override
    public Class<? extends SQLVisitor> getVisitorClass() {
        return ScalingSQLStatementVisitor.class;
    }
    
    @Override
    public String getRuleType() {
        return "scaling";
    }
}
