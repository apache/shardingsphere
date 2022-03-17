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

package org.apache.shardingsphere.test.integration.cases;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.ral.RALStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.RDLStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.RQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.TCLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * SQL command type.
 */
@RequiredArgsConstructor
@Getter
public enum SQLCommandType {
    
    /**
     * Data Query Language.
     * 
     * <p>Such as {@code SELECT}.</p>
     */
    DQL(SelectStatement.class, "dql-integration-test-cases", false, Arrays.asList("jdbc", "proxy")),
    
    /**
     * Data Manipulation Language.
     *
     * <p>Such as {@code INSERT}, {@code UPDATE}, {@code DELETE}.</p>
     */
    DML(DMLStatement.class, "dml-integration-test-cases", false, Arrays.asList("jdbc", "proxy")),
    
    /**
     * Data Definition Language.
     *
     * <p>Such as {@code CREATE}, {@code ALTER}, {@code DROP}, {@code TRUNCATE}.</p>
     */
    DDL(DDLStatement.class, "ddl-integration-test-cases", false, Arrays.asList("jdbc", "proxy")),
    
    /**
     * Transaction Control Language.
     *
     * <p>Such as {@code SET}, {@code COMMIT}, {@code ROLLBACK}, {@code SAVEPOIINT}, {@code BEGIN}.</p>
     */
    TCL(TCLStatement.class, "tcl-integration-test-cases", true, Arrays.asList("jdbc", "proxy")),
    
    /**
     * Database administrator Language.
     */
    DAL(DALStatement.class, "dal-integration-test-cases", true, Collections.singletonList("proxy")),
    
    /**
     * Database control Language.
     */
    DCL(DCLStatement.class, "dcl-integration-test-cases", false, Arrays.asList("jdbc", "proxy")),
    
    /**
     * Resource & Rule Administration Language.
     */
    RAL(RALStatement.class, "ral-integration-test-cases", true, Collections.singletonList("proxy")),
    
    /**
     * Resource & Rule Definition Language.
     */
    RDL(RDLStatement.class, "rdl-integration-test-cases", true, Collections.singletonList("proxy")),
    
    /**
     * Resource & Rule Query Language.
     */
    RQL(RQLStatement.class, "rql-integration-test-cases", true, Collections.singletonList("proxy"));
    
    private final Class<? extends SQLStatement> sqlStatementClass;
    
    private final String filePrefix;
    
    private final boolean literalOnly;
    
    private final Collection<String> runningAdaptors;
}
