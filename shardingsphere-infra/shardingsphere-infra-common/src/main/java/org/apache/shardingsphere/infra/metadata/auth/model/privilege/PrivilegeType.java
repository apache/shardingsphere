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
    
    SELECT("SELECT"),
    INSERT("INSERT"),
    UPDATE("UPDATE"),
    DELETE("DELETE"),
    USAGE("USAGE"),
    CREATE("CREATE"),
    DROP("DROP"),
    RELOAD("RELOAD"),
    SHUTDOWN("SHUTDOWN"),
    PROCESS("PROCESS"),
    FILE("FILE"),
    GRANT("GRANT"),
    REFERENCES("REFERENCES"),
    INDEX("INDEX"),
    ALTER("ALTER"),
    SHOW_DB("SHOW_DB"),
    SUPER("SUPER"),
    CREATE_TMP("CREATE_TMP"),
    LOCK_TABLES("LOCK_TABLES"),
    EXECUTE("EXECUTE"),
    REPL_SLAVE("REPL_SLAVE"),
    REPL_CLIENT("REPL_CLIENT"),
    CREATE_VIEW("CREATE_VIEW"),
    SHOW_VIEW("SHOW_VIEW"),
    CREATE_PROC("CREATE_PROC"),
    ALTER_PROC("ALTER_PROC"),
    CREATE_USER("CREATE_USER"),
    EVENT("EVENT"),
    TRIGGER("TRIGGER"),
    CREATE_TABLESPACE("CREATE_TABLESPACE"),
    CREATE_ROLE("CREATE_ROLE"),
    DROP_ROLE("DROP_ROLE");
    
    private final String name;
}
