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

package org.apache.shardingsphere.sql.parser.engine.doris.visitor.statement;

import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DALStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DDLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DMLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.LCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.TCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.engine.doris.visitor.statement.type.DorisDALStatementVisitor;
import org.apache.shardingsphere.sql.parser.engine.doris.visitor.statement.type.DorisDCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.engine.doris.visitor.statement.type.DorisDDLStatementVisitor;
import org.apache.shardingsphere.sql.parser.engine.doris.visitor.statement.type.DorisDMLStatementVisitor;
import org.apache.shardingsphere.sql.parser.engine.doris.visitor.statement.type.DorisLCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.engine.doris.visitor.statement.type.DorisTCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.spi.SQLStatementVisitorFacade;

/**
 * Statement visitor facade for Doris.
 */
public final class DorisStatementVisitorFacade implements SQLStatementVisitorFacade {
    
    @Override
    public Class<? extends DMLStatementVisitor> getDMLVisitorClass() {
        return DorisDMLStatementVisitor.class;
    }
    
    @Override
    public Class<? extends DDLStatementVisitor> getDDLVisitorClass() {
        return DorisDDLStatementVisitor.class;
    }
    
    @Override
    public Class<? extends TCLStatementVisitor> getTCLVisitorClass() {
        return DorisTCLStatementVisitor.class;
    }
    
    @Override
    public Class<? extends LCLStatementVisitor> getLCLVisitorClass() {
        return DorisLCLStatementVisitor.class;
    }
    
    @Override
    public Class<? extends DCLStatementVisitor> getDCLVisitorClass() {
        return DorisDCLStatementVisitor.class;
    }
    
    @Override
    public Class<? extends DALStatementVisitor> getDALVisitorClass() {
        return DorisDALStatementVisitor.class;
    }
    
    @Override
    public String getDatabaseType() {
        return "Doris";
    }
}
