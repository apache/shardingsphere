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

package org.apache.shardingsphere.sql.parser.sql92.visitor;

import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitorFacadeFactory;
import org.apache.shardingsphere.sql.parser.api.visitor.format.facade.FormatSQLVisitorFacade;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.facade.StatementSQLVisitorFacade;
import org.apache.shardingsphere.sql.parser.sql92.visitor.format.facade.SQL92FormatSQLVisitorFacade;
import org.apache.shardingsphere.sql.parser.sql92.visitor.statement.facade.SQL92StatementSQLVisitorFacade;

/**
 * SQL92 SQL visitor facade engine.
 */
public final class SQL92SQLVisitorFacadeFactory implements SQLVisitorFacadeFactory {
    
    @Override
    public Class<? extends StatementSQLVisitorFacade> getStatementSQLVisitorFacadeClass() {
        return SQL92StatementSQLVisitorFacade.class;
    }
    
    @Override
    public Class<? extends FormatSQLVisitorFacade> getFormatSQLVisitorFacadeClass() {
        return SQL92FormatSQLVisitorFacade.class;
    }
}
