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

package org.apache.shardingsphere.sql.parser.engine.sqlserver.visitor.statement;

import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DALStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DDLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DMLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.LCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.TCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.spi.SQLStatementVisitorFacade;
import org.apache.shardingsphere.sql.parser.engine.sqlserver.visitor.statement.type.SQLServerDALStatementVisitor;
import org.apache.shardingsphere.sql.parser.engine.sqlserver.visitor.statement.type.SQLServerDCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.engine.sqlserver.visitor.statement.type.SQLServerDDLStatementVisitor;
import org.apache.shardingsphere.sql.parser.engine.sqlserver.visitor.statement.type.SQLServerDMLStatementVisitor;
import org.apache.shardingsphere.sql.parser.engine.sqlserver.visitor.statement.type.SQLServerTCLStatementVisitor;

/**
 * Statement visitor facade for SQLServer.
 */
public final class SQLServerStatementVisitorFacade implements SQLStatementVisitorFacade {
    
    @Override
    public Class<? extends DMLStatementVisitor> getDMLVisitorClass() {
        return SQLServerDMLStatementVisitor.class;
    }
    
    @Override
    public Class<? extends DDLStatementVisitor> getDDLVisitorClass() {
        return SQLServerDDLStatementVisitor.class;
    }
    
    @Override
    public Class<? extends TCLStatementVisitor> getTCLVisitorClass() {
        return SQLServerTCLStatementVisitor.class;
    }
    
    @Override
    public Class<? extends LCLStatementVisitor> getLCLVisitorClass() {
        throw new UnsupportedOperationException("");
    }
    
    @Override
    public Class<? extends DCLStatementVisitor> getDCLVisitorClass() {
        return SQLServerDCLStatementVisitor.class;
    }
    
    @Override
    public Class<? extends DALStatementVisitor> getDALVisitorClass() {
        return SQLServerDALStatementVisitor.class;
    }
    
    @Override
    public String getDatabaseType() {
        return "SQLServer";
    }
}
