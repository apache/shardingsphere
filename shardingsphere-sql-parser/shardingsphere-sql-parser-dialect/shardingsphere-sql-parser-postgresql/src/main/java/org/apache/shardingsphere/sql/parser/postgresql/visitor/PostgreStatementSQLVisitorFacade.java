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

package org.apache.shardingsphere.sql.parser.postgresql.visitor;

import org.apache.shardingsphere.sql.parser.api.visitor.statement.StatementSQLVisitorFacade;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.impl.DALVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.impl.DCLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.impl.DDLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.impl.DMLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.impl.RLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.impl.TCLVisitor;
import org.apache.shardingsphere.sql.parser.postgresql.visitor.impl.PostgreSQLDALVisitor;
import org.apache.shardingsphere.sql.parser.postgresql.visitor.impl.PostgreSQLDCLVisitor;
import org.apache.shardingsphere.sql.parser.postgresql.visitor.impl.PostgreSQLDDLVisitor;
import org.apache.shardingsphere.sql.parser.postgresql.visitor.impl.PostgreSQLDMLVisitor;
import org.apache.shardingsphere.sql.parser.postgresql.visitor.impl.PostgreSQLTCLVisitor;

/**
 * Visitor facade for PostgreSQL.
 */
public final class PostgreStatementSQLVisitorFacade implements StatementSQLVisitorFacade {
    
    @Override
    public Class<? extends DMLVisitor> getDMLVisitorClass() {
        return PostgreSQLDMLVisitor.class;
    }
    
    @Override
    public Class<? extends DDLVisitor> getDDLVisitorClass() {
        return PostgreSQLDDLVisitor.class;
    }
    
    @Override
    public Class<? extends TCLVisitor> getTCLVisitorClass() {
        return PostgreSQLTCLVisitor.class;
    }
    
    @Override
    public Class<? extends DCLVisitor> getDCLVisitorClass() {
        return PostgreSQLDCLVisitor.class;
    }
    
    @Override
    public Class<? extends DALVisitor> getDALVisitorClass() {
        return PostgreSQLDALVisitor.class;
    }
    
    @Override
    public Class<? extends RLVisitor> getRLVisitorClass() {
        return null;
    }
}
