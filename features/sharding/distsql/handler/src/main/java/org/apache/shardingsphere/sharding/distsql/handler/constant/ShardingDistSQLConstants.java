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

package org.apache.shardingsphere.sharding.distsql.handler.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Sharding DistSQL constants.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingDistSQLConstants {
    
    public static final String STANDARD = "standard";
    
    public static final String COMPLEX = "complex";
    
    public static final String HINT = "hint";
    
    public static final String NONE = "";
    
    public static final String COMMA = ",";
    
    public static final String SEMI = ";";
    
    public static final String PROPERTY = "'%s'='%s'";
    
    public static final String CREATE_SHARDING_TABLE = "CREATE SHARDING TABLE RULE";
    
    public static final String SHARDING_TABLE = " %s ("
            + System.lineSeparator()
            + "DATANODES('%s')%s"
            + System.lineSeparator()
            + ")";
    
    public static final String SHARDING_AUTO_TABLE = " %s ("
            + System.lineSeparator()
            + "STORAGE_UNITS(%s),"
            + System.lineSeparator()
            + "%s"
            + System.lineSeparator()
            + ")";
    
    public static final String AUTO_TABLE_STRATEGY = "SHARDING_COLUMN=%s,"
            + System.lineSeparator()
            + "%s";
    
    public static final String DATABASE_STRATEGY = "DATABASE_STRATEGY";
    
    public static final String TABLE_STRATEGY = "TABLE_STRATEGY";
    
    public static final String STRATEGY_STANDARD = "TYPE='%s', SHARDING_COLUMN=%s, SHARDING_ALGORITHM(%s)";
    
    public static final String STRATEGY_COMPLEX = "TYPE='%s', SHARDING_COLUMNS=%s, SHARDING_ALGORITHM(%s)";
    
    public static final String STRATEGY_HINT = "TYPE='%s', SHARDING_ALGORITHM(%s)";
    
    public static final String STRATEGY_NONE = "TYPE='%s'";
    
    public static final String SHARDING_STRATEGY_STANDARD = "%s(" + STRATEGY_STANDARD + ")";
    
    public static final String SHARDING_STRATEGY_COMPLEX = "%s(" + STRATEGY_COMPLEX + ")";
    
    public static final String SHARDING_STRATEGY_HINT = "%s(" + STRATEGY_HINT + ")";
    
    public static final String SHARDING_STRATEGY_NONE = "%s(" + STRATEGY_NONE + ")";
    
    public static final String KEY_GENERATOR_STRATEGY = "KEY_GENERATE_STRATEGY(COLUMN=%s, %s)";
    
    public static final String AUDIT_STRATEGY = "AUDIT_STRATEGY(%s, ALLOW_HINT_DISABLE=%s)";
    
    public static final String SHARDING_BINDING_TABLE_RULES = "CREATE SHARDING TABLE REFERENCE RULE";
    
    public static final String BINDING_TABLES = " %s (%s)";
    
    public static final String DEFAULT_DATABASE_STRATEGY = "CREATE DEFAULT SHARDING DATABASE STRATEGY";
    
    public static final String DEFAULT_TABLE_STRATEGY = "CREATE DEFAULT SHARDING TABLE STRATEGY";
    
    public static final String ALGORITHM_TYPE = "TYPE(NAME='%s', PROPERTIES(%s))";
    
    public static final String ALGORITHM_TYPE_WITHOUT_PROPS = "TYPE(NAME='%s')";
}
