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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * DistSQL script constants.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DistSQLScriptConstants {
    
    public static final String RESOURCE_DB = "resource_db";
    
    public static final String SHARDING_DB = "sharding_db";
    
    public static final String READWRITE_SPLITTING_DB = "readwrite_splitting_db";
    
    public static final String DATABASE_DISCOVERY_DB = "database_discovery_db";
    
    public static final String ENCRYPT_DB = "encrypt_db";
    
    public static final String SHADOW_DB = "shadow_db";
    
    public static final String STANDARD = "standard";
    
    public static final String COMPLEX = "complex";
    
    public static final String HINT = "hint";
    
    public static final String COMMA = ",";
    
    public static final String SEMI = ";";
    
    public static final String CREATE_DATABASE = "CREATE DATABASE %s;";
    
    public static final String USE_DATABASE = "USE %s;";
    
    public static final String ADD_RESOURCE = "REGISTER STORAGE UNIT";
    
    public static final String KEY_URL = "url";
    
    public static final String KEY_USERNAME = "username";
    
    public static final String KEY_PASSWORD = "password";
    
    public static final String RESOURCE_DEFINITION = " %s ("
            + System.lineSeparator()
            + "URL=\"%s\","
            + System.lineSeparator()
            + "USER=\"%s\","
            + System.lineSeparator()
            + "PASSWORD=\"%s\","
            + System.lineSeparator()
            + "PROPERTIES(%s)"
            + System.lineSeparator()
            + ")";
    
    public static final String RESOURCE_DEFINITION_WITHOUT_PASSWORD = " %s ("
            + System.lineSeparator()
            + "URL=\"%s\","
            + System.lineSeparator()
            + "USER=\"%s\","
            + System.lineSeparator()
            + "PROPERTIES(%s)"
            + ")";
    
    public static final String PROPERTY = "\"%s\"=\"%s\"";
    
    public static final String CREATE_SHARDING_ALGORITHM = "CREATE SHARDING ALGORITHM";
    
    public static final String SHARDING_ALGORITHM = " %s ("
            + System.lineSeparator()
            + "TYPE(NAME=\"%s\", PROPERTIES(%s))"
            + System.lineSeparator()
            + ")";
    
    public static final String CREATE_SHARDING_TABLE = "CREATE SHARDING TABLE RULE";
    
    public static final String SHARDING_TABLE = " %s ("
            + System.lineSeparator()
            + "DATANODES(\"%s\"),"
            + System.lineSeparator()
            + "%s"
            + System.lineSeparator()
            + ")";
    
    public static final String DATABASE_STRATEGY = "DATABASE_STRATEGY";
    
    public static final String TABLE_STRATEGY = "TABLE_STRATEGY";
    
    public static final String SHARDING_STRATEGY_STANDARD = "%s(TYPE=\"%s\", SHARDING_COLUMN=%s, SHARDING_ALGORITHM=%s),"
            + System.lineSeparator();
    
    public static final String SHARDING_STRATEGY_COMPLEX = "%s(TYPE=\"%s\", SHARDING_COLUMNS=%s, SHARDING_ALGORITHM=%s),"
            + System.lineSeparator();
    
    public static final String SHARDING_STRATEGY_HINT = "%s(TYPE=\"%s\", SHARDING_ALGORITHM=%s),"
            + System.lineSeparator();
    
    public static final String KEY_GENERATOR_STRATEGY = "KEY_GENERATE_STRATEGY(COLUMN=%s, KEY_GENERATOR=%s),"
            + System.lineSeparator();
    
    public static final String CREATE_KEY_GENERATOR = "CREATE SHARDING KEY GENERATOR";
    
    public static final String KEY_GENERATOR = " %s ("
            + System.lineSeparator()
            + "TYPE(NAME=\"%s\")"
            + System.lineSeparator()
            + ")";
    
    public static final String SHARDING_BINDING_TABLE_RULES = "CREATE SHARDING BINDING TABLE RULES %s";
    
    public static final String BRACKET = "(%s)";
    
    public static final String BROADCAST_TABLE_RULE = "CREATE BROADCAST TABLE RULE %s";
    
    public static final String CREATE_READWRITE_SPLITTING_RULE = "CREATE READWRITE_SPLITTING RULE";
    
    public static final String READWRITE_SPLITTING = " %s ("
            + System.lineSeparator()
            + "WRITE_RESOURCE=%s,"
            + System.lineSeparator()
            + "READ_RESOURCES(%s),"
            + System.lineSeparator()
            + "%s"
            + System.lineSeparator()
            + ")";
    
    public static final String READWRITE_SPLITTING_TYPE = "TYPE(NAME=\"%s\")";
    
    public static final String READWRITE_SPLITTING_TYPE_PROPERTIES = "TYPE(NAME=\"%s\", PROPERTIES(%s))";
    
    public static final String READ_RESOURCE = "%s";
    
    public static final String CREATE_DB_DISCOVERY = "CREATE DB_DISCOVERY RULE";
    
    public static final String DB_DISCOVERY = " %s ("
            + System.lineSeparator()
            + "STORAGE_UNITS(%s),"
            + System.lineSeparator()
            + "%s,"
            + System.lineSeparator()
            + "HEARTBEAT(PROPERTIES(%s))"
            + System.lineSeparator()
            + ")";
    
    public static final String CREATE_ENCRYPT = "CREATE ENCRYPT RULE";
    
    public static final String ENCRYPT = " %s ("
            + System.lineSeparator()
            + "COLUMNS("
            + System.lineSeparator()
            + "%s"
            + System.lineSeparator()
            + "),"
            + "QUERY_WITH_CIPHER_COLUMN=%s)";
    
    public static final String ENCRYPT_COLUMN = "(NAME=%s,%s,%s)";
    
    public static final String PLAIN = "PLAIN=%s";
    
    public static final String CIPHER = "CIPHER=%s";
    
    public static final String TYPE = "TYPE(NAME=\'%s\', PROPERTIES(%s))";
    
    public static final String ENCRYPT_TYPE_WITHOUT_PROPERTIES = "TYPE(NAME=\'%s\')";
    
    public static final String CREATE_SHADOW = "CREATE SHADOW RULE";
    
    public static final String SHADOW = " %s("
            + System.lineSeparator()
            + "SOURCE=%s,"
            + System.lineSeparator()
            + "SHADOW=%s,"
            + System.lineSeparator()
            + "%s"
            + System.lineSeparator()
            + ")";
    
    public static final String SHADOW_TABLE = "%s(%s)";
    
    public static final String SHADOW_TABLE_TYPE = "TYPE(NAME=\"%s\", PROPERTIES(%s))";
}
