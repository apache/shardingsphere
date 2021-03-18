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

package org.apache.shardingsphere.infra.metadata.auth.model.privilege;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * Privilege Type.
 */
@RequiredArgsConstructor
@Getter
public enum PrivilegeType {
    
    SELECT("SELECT", false),
    INSERT("INSERT", false),
    UPDATE("UPDATE", false),
    DELETE("DELETE", false),
    CREATE("CREATE", false),
    ALTER("ALTER", false),
    DROP("DROP", false),
    GRANT("GRANT", false),
    INDEX("INDEX", false),
    REFERENCES("REFERENCES", false),
    LOCK_TABLES("LOCK_TABLES", false),
    CREATE_VIEW("CREATE_VIEW", false),
    SHOW_VIEW("SHOW_VIEW", false),
    EXECUTE("EXECUTE", false),
    EVENT("EVENT", false),
    TRIGGER("TRIGGER", false),
    SUPER("SUPER", true),
    SHOW_DB("SHOW_DB", true),
    RELOAD("RELOAD", true),
    SHUTDOWN("SHUTDOWN", true),
    PROCESS("PROCESS", true),
    FILE("FILE", true),
    CREATE_TMP("CREATE_TMP", true),
    REPL_SLAVE("REPL_SLAVE", true),
    REPL_CLIENT("REPL_CLIENT", true),
    CREATE_PROC("CREATE_PROC", true),
    ALTER_PROC("ALTER_PROC", true),
    CREATE_USER("CREATE_USER", true),
    CREATE_TABLESPACE("CREATE_TABLESPACE", true),
    CREATE_ROLE("CREATE_ROLE", true),
    DROP_ROLE("DROP_ROLE", true);
    
    private final String name;
    
    private final boolean isAdministrativeOnly;
}
