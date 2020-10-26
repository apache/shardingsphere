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

package org.apache.shardingsphere.sql.parser.api.visitor.operation.format;

import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitorFacade;
import org.apache.shardingsphere.sql.parser.api.visitor.type.impl.DALSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.type.impl.DCLSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.type.impl.DDLSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.type.impl.DMLSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.type.impl.RLSQLVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.type.impl.TCLSQLVisitor;

/**
 * SQL format visitor facade.
 */
public interface SQLFormatVisitorFacade extends SQLVisitorFacade {
    
    /**
     * Get DML visitor class.
     *
     * @return DML visitor class
     */
    Class<? extends DMLSQLVisitor> getDMLVisitorClass();
    
    /**
     * Get DDL visitor class.
     *
     * @return DDL visitor class
     */
    Class<? extends DDLSQLVisitor> getDDLVisitorClass();
    
    /**
     * Get TCL visitor class.
     *
     * @return TCL visitor class
     */
    Class<? extends TCLSQLVisitor> getTCLVisitorClass();
    
    /**
     * Get DCL visitor class.
     *
     * @return DCL visitor class
     */
    Class<? extends DCLSQLVisitor> getDCLVisitorClass();
    
    /**
     * Get DAL visitor class.
     *
     * @return DAL visitor class
     */
    Class<? extends DALSQLVisitor> getDALVisitorClass();
    
    /**
     * Get RL visitor class.
     * 
     * @return RL visitor class
     */
    Class<? extends RLSQLVisitor> getRLVisitorClass();
}
