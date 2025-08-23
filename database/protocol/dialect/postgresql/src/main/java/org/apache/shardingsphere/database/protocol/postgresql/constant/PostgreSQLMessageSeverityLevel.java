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

package org.apache.shardingsphere.database.protocol.postgresql.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * PostgreSQL message severity level.
 *
 * @see <a href="https://www.postgresql.org/docs/12/runtime-config-logging.html#RUNTIME-CONFIG-SEVERITY-LEVELS">Table 19.2. Message Severity Levels</a>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLMessageSeverityLevel {
    
    public static final String DEBUG1 = "DEBUG1";
    
    public static final String DEBUG2 = "DEBUG2";
    
    public static final String DEBUG3 = "DEBUG3";
    
    public static final String DEBUG4 = "DEBUG4";
    
    public static final String DEBUG5 = "DEBUG5";
    
    public static final String INFO = "INFO";
    
    public static final String NOTICE = "NOTICE";
    
    public static final String WARNING = "WARNING";
    
    public static final String ERROR = "ERROR";
    
    public static final String LOG = "LOG";
    
    public static final String FATAL = "FATAL";
    
    public static final String PANIC = "PANIC";
}
