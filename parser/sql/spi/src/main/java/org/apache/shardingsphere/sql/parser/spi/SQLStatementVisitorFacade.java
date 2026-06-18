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

package org.apache.shardingsphere.sql.parser.spi;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPI;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DALStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DDLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DMLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.LCLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.TCLStatementVisitor;

/**
 * SQL statement visitor facade.
 */
@SingletonSPI
public interface SQLStatementVisitorFacade extends DatabaseTypedSPI {
    
    /**
     * Get DML visitor class.
     *
     * @return DML visitor class
     */
    Class<? extends DMLStatementVisitor> getDMLVisitorClass();
    
    /**
     * Get DDL visitor class.
     *
     * @return DDL visitor class
     */
    Class<? extends DDLStatementVisitor> getDDLVisitorClass();
    
    /**
     * Get TCL visitor class.
     *
     * @return TCL visitor class
     */
    Class<? extends TCLStatementVisitor> getTCLVisitorClass();
    
    /**
     * Get LCL visitor class.
     *
     * @return LCL visitor class
     */
    Class<? extends LCLStatementVisitor> getLCLVisitorClass();
    
    /**
     * Get DCL visitor class.
     *
     * @return DCL visitor class
     */
    Class<? extends DCLStatementVisitor> getDCLVisitorClass();
    
    /**
     * Get DAL visitor class.
     *
     * @return DAL visitor class
     */
    Class<? extends DALStatementVisitor> getDALVisitorClass();
}
