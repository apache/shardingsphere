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

package org.apache.shardingsphere.sql.parser.mysql.visitor.statement;

import org.apache.shardingsphere.sql.parser.mysql.visitor.statement.type.MySQLRLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.spi.SQLStatementVisitorFacade;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DALSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DCLSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DDLSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DMLSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.RLSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.TCLSQLVisitor;
import org.apache.shardingsphere.sql.parser.mysql.visitor.statement.type.MySQLDALStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.mysql.visitor.statement.type.MySQLDCLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.mysql.visitor.statement.type.MySQLDDLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.mysql.visitor.statement.type.MySQLDMLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.mysql.visitor.statement.type.MySQLTCLStatementSQLVisitor;

/**
 * SQL statement visitor facade for MySQL.
 */
public final class MySQLSQLStatementVisitorFacade implements SQLStatementVisitorFacade {
    
    @Override
    public Class<? extends DMLSQLVisitor> getDMLVisitorClass() {
        return MySQLDMLStatementSQLVisitor.class;
    }
    
    @Override
    public Class<? extends DDLSQLVisitor> getDDLVisitorClass() {
        return MySQLDDLStatementSQLVisitor.class;
    }
    
    @Override
    public Class<? extends TCLSQLVisitor> getTCLVisitorClass() {
        return MySQLTCLStatementSQLVisitor.class;
    }
    
    @Override
    public Class<? extends DCLSQLVisitor> getDCLVisitorClass() {
        return MySQLDCLStatementSQLVisitor.class;
    }
    
    @Override
    public Class<? extends DALSQLVisitor> getDALVisitorClass() {
        return MySQLDALStatementSQLVisitor.class;
    }
    
    @Override
    public Class<? extends RLSQLVisitor> getRLVisitorClass() {
        return MySQLRLStatementSQLVisitor.class;
    }
    
    @Override
    public String getType() {
        return "MySQL";
    }
}
