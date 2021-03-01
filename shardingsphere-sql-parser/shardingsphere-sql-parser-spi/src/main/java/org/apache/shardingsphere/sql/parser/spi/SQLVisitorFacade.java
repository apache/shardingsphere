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

import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;

/**
 * SQL visitor facade.
 */
public interface SQLVisitorFacade {
    
    /**
     * Get database type.
     *
     * @return database type
     */
    String getDatabaseType();
    
    /**
     * Get visitor type.
     *
     * @return visitor type
     */
    String getVisitorType();
    
    /**
     * Get DML visitor class.
     *
     * @return DML visitor class
     */
    Class<? extends SQLVisitor> getDMLVisitorClass();
    
    /**
     * Get DDL visitor class.
     *
     * @return DDL visitor class
     */
    Class<? extends SQLVisitor> getDDLVisitorClass();
    
    /**
     * Get TCL visitor class.
     *
     * @return TCL visitor class
     */
    Class<? extends SQLVisitor> getTCLVisitorClass();
    
    /**
     * Get DCL visitor class.
     *
     * @return DCL visitor class
     */
    Class<? extends SQLVisitor> getDCLVisitorClass();
    
    /**
     * Get DAL visitor class.
     *
     * @return DAL visitor class
     */
    Class<? extends SQLVisitor> getDALVisitorClass();
    
    /**
     * Get RL visitor class.
     *
     * @return RL visitor class
     */
    Class<? extends SQLVisitor> getRLVisitorClass();
}
