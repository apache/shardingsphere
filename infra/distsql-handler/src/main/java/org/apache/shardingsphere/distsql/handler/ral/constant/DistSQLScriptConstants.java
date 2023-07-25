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

package org.apache.shardingsphere.distsql.handler.ral.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * DistSQL script constants.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DistSQLScriptConstants {
    
    public static final String COMMA = ",";
    
    public static final String SEMI = ";";
    
    public static final String CREATE_DATABASE = "CREATE DATABASE %s;";
    
    public static final String USE_DATABASE = "USE %s;";
    
    public static final String REGISTER_STORAGE_UNIT = "REGISTER STORAGE UNIT";
    
    public static final String KEY_URL = "url";
    
    public static final String KEY_USERNAME = "username";
    
    public static final String KEY_PASSWORD = "password";
    
    public static final String RESOURCE_DEFINITION = " %s ("
            + System.lineSeparator()
            + "URL='%s',"
            + System.lineSeparator()
            + "USER='%s',"
            + System.lineSeparator()
            + "PASSWORD='%s',"
            + System.lineSeparator()
            + "PROPERTIES(%s)"
            + System.lineSeparator()
            + ")";
    
    public static final String RESOURCE_DEFINITION_WITHOUT_PASSWORD = " %s ("
            + System.lineSeparator()
            + "URL='%s',"
            + System.lineSeparator()
            + "USER='%s',"
            + System.lineSeparator()
            + "PROPERTIES(%s)"
            + ")";
    
    public static final String PROPERTY = "'%s'='%s'";
}
