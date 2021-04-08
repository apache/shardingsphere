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

package org.apache.shardingsphere.authority.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * Privilege Type.
 */
@RequiredArgsConstructor
@Getter
public enum PrivilegeType {
    
    SELECT,
    INSERT,
    UPDATE,
    DELETE,
    CREATE,
    ALTER,
    DROP,
    GRANT,
    INDEX,
    REFERENCES,
    LOCK_TABLES,
    CREATE_VIEW,
    SHOW_VIEW,
    EXECUTE,
    EVENT,
    TRIGGER,
    SUPER,
    SHOW_DB,
    RELOAD,
    SHUTDOWN,
    PROCESS,
    FILE,
    CREATE_TMP,
    REPL_SLAVE,
    REPL_CLIENT,
    CREATE_PROC,
    ALTER_PROC,
    CREATE_USER,
    CREATE_TABLESPACE,
    CREATE_ROLE,
    DROP_ROLE,
    TRUNCATE,
    USAGE,
    CONNECT,
    TEMPORARY,
    CREATE_DATABASE,
    INHERIT,
    CAN_LOGIN,
    CREATE_SEQUENCE,
    CREATE_TYPE,
    CREATE_SESSION,
    ALTER_SESSION,
    CREATE_SYNONYM,
    CREATE_TABLE
}
