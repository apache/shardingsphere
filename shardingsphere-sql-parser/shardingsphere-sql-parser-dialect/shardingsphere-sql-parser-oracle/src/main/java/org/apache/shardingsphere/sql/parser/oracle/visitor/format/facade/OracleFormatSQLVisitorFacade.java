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

package org.apache.shardingsphere.sql.parser.oracle.visitor.format.facade;

import org.apache.shardingsphere.sql.parser.spi.SQLVisitorFacade;
import org.apache.shardingsphere.sql.parser.engine.visitor.type.DALSQLVisitor;
import org.apache.shardingsphere.sql.parser.engine.visitor.type.DCLSQLVisitor;
import org.apache.shardingsphere.sql.parser.engine.visitor.type.DDLSQLVisitor;
import org.apache.shardingsphere.sql.parser.engine.visitor.type.DMLSQLVisitor;
import org.apache.shardingsphere.sql.parser.engine.visitor.type.RLSQLVisitor;
import org.apache.shardingsphere.sql.parser.engine.visitor.type.TCLSQLVisitor;

/**
 * Format SQL Visitor facade for Oracle.
 */
public final class OracleFormatSQLVisitorFacade implements SQLVisitorFacade {
    
    @Override
    public Class<? extends DMLSQLVisitor> getDMLVisitorClass() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Class<? extends DDLSQLVisitor> getDDLVisitorClass() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Class<? extends TCLSQLVisitor> getTCLVisitorClass() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Class<? extends DCLSQLVisitor> getDCLVisitorClass() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Class<? extends DALSQLVisitor> getDALVisitorClass() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Class<? extends RLSQLVisitor> getRLVisitorClass() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String getDatabaseType() {
        return "Oracle";
    }
    
    @Override
    public String getVisitorType() {
        return "FORMAT";
    }
}
