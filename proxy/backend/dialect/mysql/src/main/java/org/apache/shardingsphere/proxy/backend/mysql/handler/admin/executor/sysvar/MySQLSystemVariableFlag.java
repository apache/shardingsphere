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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.sysvar;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * System variable flag for MySQL.
 *
 * @see <a href="https://github.com/mysql/mysql-server/blob/mysql-8.0.32/sql/set_var.h#L125-L151">sql/set_var.h</a>
 */
@SuppressWarnings("unused")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLSystemVariableFlag {
    
    public static final int GLOBAL = 0x0001;
    
    public static final int SESSION = 0x0002;
    
    public static final int ONLY_SESSION = 0x0004;
    
    public static final int SCOPE_MASK = 0x03FF;
    
    public static final int READONLY = 0x0400;
    
    public static final int ALLOCATED = 0x0800;
    
    public static final int INVISIBLE = 0x1000;
    
    public static final int TRI_LEVEL = 0x2000;
    
    public static final int NOTPERSIST = 0x4000;
    
    public static final int HINT_UPDATEABLE = 0x8000;
    
    public static final int PERSIST_AS_READ_ONLY = 0x10000;
    
    public static final int SENSITIVE = 0x20000;
}
