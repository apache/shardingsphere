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

package org.apache.shardingsphere.sql.parser.engine.postgresql.visitor.statement;

import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DALStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DDLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DMLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.LCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.TCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.engine.postgresql.visitor.statement.type.PostgreSQLDALStatementVisitor;
import org.apache.shardingsphere.sql.parser.engine.postgresql.visitor.statement.type.PostgreSQLDCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.engine.postgresql.visitor.statement.type.PostgreSQLDDLStatementVisitor;
import org.apache.shardingsphere.sql.parser.engine.postgresql.visitor.statement.type.PostgreSQLDMLStatementVisitor;
import org.apache.shardingsphere.sql.parser.engine.postgresql.visitor.statement.type.PostgreSQLLCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.engine.postgresql.visitor.statement.type.PostgreSQLTCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.spi.SQLStatementVisitorFacade;

/**
 * Statement visitor facade for PostgreSQL.
 */
public final class PostgreSQLStatementVisitorFacade implements SQLStatementVisitorFacade {
    
    @Override
    public Class<? extends DMLStatementVisitor> getDMLVisitorClass() {
        return PostgreSQLDMLStatementVisitor.class;
    }
    
    @Override
    public Class<? extends DDLStatementVisitor> getDDLVisitorClass() {
        return PostgreSQLDDLStatementVisitor.class;
    }
    
    @Override
    public Class<? extends TCLStatementVisitor> getTCLVisitorClass() {
        return PostgreSQLTCLStatementVisitor.class;
    }
    
    @Override
    public Class<? extends LCLStatementVisitor> getLCLVisitorClass() {
        return PostgreSQLLCLStatementVisitor.class;
    }
    
    @Override
    public Class<? extends DCLStatementVisitor> getDCLVisitorClass() {
        return PostgreSQLDCLStatementVisitor.class;
    }
    
    @Override
    public Class<? extends DALStatementVisitor> getDALVisitorClass() {
        return PostgreSQLDALStatementVisitor.class;
    }
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
}
