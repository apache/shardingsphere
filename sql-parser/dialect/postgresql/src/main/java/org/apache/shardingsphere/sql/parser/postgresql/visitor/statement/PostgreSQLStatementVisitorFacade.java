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

package org.apache.shardingsphere.sql.parser.postgresql.visitor.statement;

import org.apache.shardingsphere.sql.parser.spi.SQLStatementVisitorFacade;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DALSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DCLSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DDLSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DMLSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.RLSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.TCLSQLVisitor;
import org.apache.shardingsphere.sql.parser.postgresql.visitor.statement.type.PostgreSQLDALStatementVisitor;
import org.apache.shardingsphere.sql.parser.postgresql.visitor.statement.type.PostgreSQLDCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.postgresql.visitor.statement.type.PostgreSQLDDLStatementVisitor;
import org.apache.shardingsphere.sql.parser.postgresql.visitor.statement.type.PostgreSQLDMLStatementVisitor;
import org.apache.shardingsphere.sql.parser.postgresql.visitor.statement.type.PostgreSQLTCLStatementVisitor;

/**
 * Statement visitor facade for PostgreSQL.
 */
public final class PostgreSQLStatementVisitorFacade implements SQLStatementVisitorFacade {
    
    @Override
    public Class<? extends DMLSQLVisitor> getDMLVisitorClass() {
        return PostgreSQLDMLStatementVisitor.class;
    }
    
    @Override
    public Class<? extends DDLSQLVisitor> getDDLVisitorClass() {
        return PostgreSQLDDLStatementVisitor.class;
    }
    
    @Override
    public Class<? extends TCLSQLVisitor> getTCLVisitorClass() {
        return PostgreSQLTCLStatementVisitor.class;
    }
    
    @Override
    public Class<? extends DCLSQLVisitor> getDCLVisitorClass() {
        return PostgreSQLDCLStatementVisitor.class;
    }
    
    @Override
    public Class<? extends DALSQLVisitor> getDALVisitorClass() {
        return PostgreSQLDALStatementVisitor.class;
    }
    
    @Override
    public Class<? extends RLSQLVisitor> getRLVisitorClass() {
        return null;
    }
    
    @Override
    public String getType() {
        return "PostgreSQL";
    }
}
