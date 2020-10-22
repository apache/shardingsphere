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

import org.apache.shardingsphere.sql.parser.api.visitor.facade.impl.StatementSQLVisitorFacade;
import org.apache.shardingsphere.sql.parser.api.visitor.impl.statement.impl.DALStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.impl.statement.impl.DCLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.impl.statement.impl.DDLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.impl.statement.impl.DMLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.impl.statement.impl.RLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.impl.statement.impl.TCLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.sql92.visitor.statement.impl.SQL92DALStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.sql92.visitor.statement.impl.SQL92DCLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.sql92.visitor.statement.impl.SQL92DDLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.sql92.visitor.statement.impl.SQL92DMLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.sql92.visitor.statement.impl.SQL92TCLStatementSQLVisitor;

/**
 * Statement SQL Visitor facade for SQL92.
 */
public final class SQL92StatementSQLVisitorFacade implements StatementSQLVisitorFacade {
    
    @Override
    public Class<? extends DMLStatementSQLVisitor> getDMLVisitorClass() {
        return SQL92DMLStatementSQLVisitor.class;
    }
    
    @Override
    public Class<? extends DDLStatementSQLVisitor> getDDLVisitorClass() {
        return SQL92DDLStatementSQLVisitor.class;
    }
    
    @Override
    public Class<? extends TCLStatementSQLVisitor> getTCLVisitorClass() {
        return SQL92TCLStatementSQLVisitor.class;
    }
    
    @Override
    public Class<? extends DCLStatementSQLVisitor> getDCLVisitorClass() {
        return SQL92DCLStatementSQLVisitor.class;
    }
    
    @Override
    public Class<? extends DALStatementSQLVisitor> getDALVisitorClass() {
        return SQL92DALStatementSQLVisitor.class;
    }
    
    @Override
    public Class<? extends RLStatementSQLVisitor> getRLVisitorClass() {
        return null;
    }
}
