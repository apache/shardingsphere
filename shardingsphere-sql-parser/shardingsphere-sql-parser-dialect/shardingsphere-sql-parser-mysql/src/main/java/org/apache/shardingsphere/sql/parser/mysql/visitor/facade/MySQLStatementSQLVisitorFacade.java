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

package org.apache.shardingsphere.sql.parser.mysql.visitor.facade;

import org.apache.shardingsphere.sql.parser.api.visitor.facade.impl.StatementSQLVisitorFacade;
import org.apache.shardingsphere.sql.parser.api.visitor.impl.statement.impl.DALStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.impl.statement.impl.DCLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.impl.statement.impl.DDLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.impl.statement.impl.DMLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.impl.statement.impl.RLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.impl.statement.impl.TCLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.mysql.visitor.impl.statement.MySQLDALStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.mysql.visitor.impl.statement.MySQLDCLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.mysql.visitor.impl.statement.MySQLDDLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.mysql.visitor.impl.statement.MySQLDMLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.mysql.visitor.impl.statement.MySQLRLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.mysql.visitor.impl.statement.MySQLTCLStatementSQLVisitor;

/**
 * Statement SQL Visitor facade for MySQL.
 */
public final class MySQLStatementSQLVisitorFacade implements StatementSQLVisitorFacade {
    
    @Override
    public Class<? extends DMLStatementSQLVisitor> getDMLVisitorClass() {
        return MySQLDMLStatementSQLVisitor.class;
    }
    
    @Override
    public Class<? extends DDLStatementSQLVisitor> getDDLVisitorClass() {
        return MySQLDDLStatementSQLVisitor.class;
    }
    
    @Override
    public Class<? extends TCLStatementSQLVisitor> getTCLVisitorClass() {
        return MySQLTCLStatementSQLVisitor.class;
    }
    
    @Override
    public Class<? extends DCLStatementSQLVisitor> getDCLVisitorClass() {
        return MySQLDCLStatementSQLVisitor.class;
    }
    
    @Override
    public Class<? extends DALStatementSQLVisitor> getDALVisitorClass() {
        return MySQLDALStatementSQLVisitor.class;
    }
    
    @Override
    public Class<? extends RLStatementSQLVisitor> getRLVisitorClass() {
        return MySQLRLStatementSQLVisitor.class;
    }
}
