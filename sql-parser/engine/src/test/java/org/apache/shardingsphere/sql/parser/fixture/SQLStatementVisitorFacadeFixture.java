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

package org.apache.shardingsphere.sql.parser.fixture;

import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.DALSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.DCLSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.DDLSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.DMLSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.RLSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.TCLSQLVisitor;
import org.apache.shardingsphere.sql.parser.spi.SQLStatementVisitorFacade;

public final class SQLStatementVisitorFacadeFixture implements SQLStatementVisitorFacade {
    
    @Override
    public Class<? extends SQLVisitor> getDMLVisitorClass() {
        return DMLSQLVisitor.class;
    }
    
    @Override
    public Class<? extends SQLVisitor> getDDLVisitorClass() {
        return DDLSQLVisitor.class;
    }
    
    @Override
    public Class<? extends SQLVisitor> getTCLVisitorClass() {
        return TCLSQLVisitor.class;
    }
    
    @Override
    public Class<? extends SQLVisitor> getDCLVisitorClass() {
        return DCLSQLVisitor.class;
    }
    
    @Override
    public Class<? extends SQLVisitor> getDALVisitorClass() {
        return DALSQLVisitor.class;
    }
    
    @Override
    public Class<? extends SQLVisitor> getRLVisitorClass() {
        return RLSQLVisitor.class;
    }
    
    @Override
    public String getType() {
        return "FIXTURE";
    }
}
