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

package org.apache.shardingsphere.sql.parser.engine.firebird.visitor.statement;

import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DALStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DDLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DMLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.LCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.TCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.engine.firebird.visitor.statement.type.FirebirdDALStatementVisitor;
import org.apache.shardingsphere.sql.parser.engine.firebird.visitor.statement.type.FirebirdDCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.engine.firebird.visitor.statement.type.FirebirdDDLStatementVisitor;
import org.apache.shardingsphere.sql.parser.engine.firebird.visitor.statement.type.FirebirdDMLStatementVisitor;
import org.apache.shardingsphere.sql.parser.engine.firebird.visitor.statement.type.FirebirdTCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.spi.SQLStatementVisitorFacade;

/**
 * Statement visitor facade for Firebird.
 */
public final class FirebirdStatementVisitorFacade implements SQLStatementVisitorFacade {
    
    @Override
    public Class<? extends DMLStatementVisitor> getDMLVisitorClass() {
        return FirebirdDMLStatementVisitor.class;
    }
    
    @Override
    public Class<? extends DDLStatementVisitor> getDDLVisitorClass() {
        return FirebirdDDLStatementVisitor.class;
    }
    
    @Override
    public Class<? extends TCLStatementVisitor> getTCLVisitorClass() {
        return FirebirdTCLStatementVisitor.class;
    }
    
    @Override
    public Class<? extends LCLStatementVisitor> getLCLVisitorClass() {
        throw new UnsupportedOperationException("");
    }
    
    @Override
    public Class<? extends DCLStatementVisitor> getDCLVisitorClass() {
        return FirebirdDCLStatementVisitor.class;
    }
    
    @Override
    public Class<? extends DALStatementVisitor> getDALVisitorClass() {
        return FirebirdDALStatementVisitor.class;
    }
    
    @Override
    public String getDatabaseType() {
        return "Firebird";
    }
}
