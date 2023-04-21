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

package org.apache.shardingsphere.sql.parser.oracle.visitor.statement;

import org.apache.shardingsphere.sql.parser.oracle.visitor.statement.type.OracleTCLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.spi.SQLStatementVisitorFacade;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.DALSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.DCLSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.DDLSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.DMLSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.RLSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.TCLSQLVisitor;
import org.apache.shardingsphere.sql.parser.oracle.visitor.statement.type.OracleDALStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.oracle.visitor.statement.type.OracleDCLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.oracle.visitor.statement.type.OracleDDLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.oracle.visitor.statement.type.OracleDMLStatementSQLVisitor;

/**
 * SQL statement visitor facade for Oracle.
 */
public final class OracleSQLStatementVisitorFacade implements SQLStatementVisitorFacade {
    
    @Override
    public Class<? extends DMLSQLVisitor> getDMLVisitorClass() {
        return OracleDMLStatementSQLVisitor.class;
    }
    
    @Override
    public Class<? extends DDLSQLVisitor> getDDLVisitorClass() {
        return OracleDDLStatementSQLVisitor.class;
    }
    
    @Override
    public Class<? extends TCLSQLVisitor> getTCLVisitorClass() {
        return OracleTCLStatementSQLVisitor.class;
    }
    
    @Override
    public Class<? extends DCLSQLVisitor> getDCLVisitorClass() {
        return OracleDCLStatementSQLVisitor.class;
    }
    
    @Override
    public Class<? extends DALSQLVisitor> getDALVisitorClass() {
        return OracleDALStatementSQLVisitor.class;
    }
    
    @Override
    public Class<? extends RLSQLVisitor> getRLVisitorClass() {
        return null;
    }
    
    @Override
    public String getType() {
        return "Oracle";
    }
}
