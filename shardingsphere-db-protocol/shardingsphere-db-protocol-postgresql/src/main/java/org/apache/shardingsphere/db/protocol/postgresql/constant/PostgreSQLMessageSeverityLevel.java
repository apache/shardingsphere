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

package org.apache.shardingsphere.db.protocol.postgresql.constant;

/**
 * PostgreSQL message severity level.
 *
 * @see <a href="https://www.postgresql.org/docs/12/runtime-config-logging.html#RUNTIME-CONFIG-SEVERITY-LEVELS">Table 19.2. Message Severity Levels</a>
 */
public enum PostgreSQLMessageSeverityLevel {
    
    DEBUG1,
    
    DEBUG2,
    
    DEBUG3,
    
    DEBUG4,
    
    DEBUG5,
    
    INFO,
    
    NOTICE,
    
    WARNING,
    
    ERROR,
    
    LOG,
    
    FATAL,
    
    PANIC
}
