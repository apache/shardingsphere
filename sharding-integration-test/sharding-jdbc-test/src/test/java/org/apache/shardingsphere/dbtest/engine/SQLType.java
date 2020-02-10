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

package org.apache.shardingsphere.dbtest.engine;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.tcl.TCLStatement;

/**
 * SQL Type.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public enum SQLType {
    
    /**
     * Data Query Language.
     * 
     * <p>Such as {@code SELECT}.</p>
     */
    DQL(SelectStatement.class),
    
    /**
     * Data Manipulation Language.
     *
     * <p>Such as {@code INSERT}, {@code UPDATE}, {@code DELETE}.</p>
     */
    DML(DMLStatement.class),
    
    /**
     * Data Definition Language.
     *
     * <p>Such as {@code CREATE}, {@code ALTER}, {@code DROP}, {@code TRUNCATE}.</p>
     */
    DDL(DDLStatement.class),
    
    /**
     * Transaction Control Language.
     *
     * <p>Such as {@code SET}, {@code COMMIT}, {@code ROLLBACK}, {@code SAVEPOIINT}, {@code BEGIN}.</p>
     */
    TCL(TCLStatement.class),
    
    /**
     * Database administrator Language.
     */
    DAL(DALStatement.class),
    
    /**
     * Database control Language.
     */
    DCL(DCLStatement.class);
    
    private final Class<? extends SQLStatement> sqlStatementClass;
}
