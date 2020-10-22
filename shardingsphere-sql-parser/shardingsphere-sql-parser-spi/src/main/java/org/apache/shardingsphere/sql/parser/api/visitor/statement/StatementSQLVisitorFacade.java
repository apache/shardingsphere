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

package org.apache.shardingsphere.sql.parser.api.visitor.statement;

import org.apache.shardingsphere.sql.parser.api.visitor.statement.impl.DALStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.impl.DCLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.impl.DDLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.impl.DMLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.impl.RLStatementSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.impl.TCLStatementSQLVisitor;

/**
 * Statement SQL visitor facade.
 */
public interface StatementSQLVisitorFacade {
    
    /**
     * Get DML visitor class.
     *
     * @return DML visitor class
     */
    Class<? extends DMLStatementSQLVisitor> getDMLVisitorClass();
    
    /**
     * Get DDL visitor class.
     *
     * @return DDL visitor class
     */
    Class<? extends DDLStatementSQLVisitor> getDDLVisitorClass();
    
    /**
     * Get TCL visitor class.
     *
     * @return TCL visitor class
     */
    Class<? extends TCLStatementSQLVisitor> getTCLVisitorClass();
    
    /**
     * Get DCL visitor class.
     *
     * @return DCL visitor class
     */
    Class<? extends DCLStatementSQLVisitor> getDCLVisitorClass();
    
    /**
     * Get DAL visitor class.
     *
     * @return DAL visitor class
     */
    Class<? extends DALStatementSQLVisitor> getDALVisitorClass();
    
    /**
     * Get RL visitor class.
     * 
     * @return RL visitor class
     */
    Class<? extends RLStatementSQLVisitor> getRLVisitorClass();
}
