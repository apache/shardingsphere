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

package org.apache.shardingsphere.sql.parser.mysql.visitor;

import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitorFacade;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.DALVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.DCLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.DDLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.DMLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.RLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.TCLVisitor;
import org.apache.shardingsphere.sql.parser.mysql.visitor.impl.MySQLDALVisitor;
import org.apache.shardingsphere.sql.parser.mysql.visitor.impl.MySQLDCLVisitor;
import org.apache.shardingsphere.sql.parser.mysql.visitor.impl.MySQLDDLVisitor;
import org.apache.shardingsphere.sql.parser.mysql.visitor.impl.MySQLDMLVisitor;
import org.apache.shardingsphere.sql.parser.mysql.visitor.impl.MySQLRLVisitor;
import org.apache.shardingsphere.sql.parser.mysql.visitor.impl.MySQLTCLVisitor;

/**
 * Visitor facade for MySQL.
 */
public final class MySQLVisitorFacade implements SQLVisitorFacade {
    
    @Override
    public Class<? extends DMLVisitor> getDMLVisitorClass() {
        return MySQLDMLVisitor.class;
    }
    
    @Override
    public Class<? extends DDLVisitor> getDDLVisitorClass() {
        return MySQLDDLVisitor.class;
    }
    
    @Override
    public Class<? extends TCLVisitor> getTCLVisitorClass() {
        return MySQLTCLVisitor.class;
    }
    
    @Override
    public Class<? extends DCLVisitor> getDCLVisitorClass() {
        return MySQLDCLVisitor.class;
    }
    
    @Override
    public Class<? extends DALVisitor> getDALVisitorClass() {
        return MySQLDALVisitor.class;
    }
    
    @Override
    public Class<? extends RLVisitor> getRLVisitorClass() {
        return MySQLRLVisitor.class;
    }
}
