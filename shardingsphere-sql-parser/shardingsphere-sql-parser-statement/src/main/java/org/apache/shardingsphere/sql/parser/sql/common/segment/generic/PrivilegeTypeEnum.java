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

package org.apache.shardingsphere.sql.parser.sql.common.segment.generic;

/**
 * Privilege type enum.
 */
public enum PrivilegeTypeEnum {
    
    SELECT_ACL,
    
    INSERT_ACL,
    
    UPDATE_ACL,
    
    DELETE_ACL,
    
    USAGE_ACL,
    
    CREATE_ACL,
    
    DROP_ACL,
    
    RELOAD_ACL,
    
    SHUTDOWN_ACL,
    
    PROCESS_ACL,
    
    FILE_ACL,
    
    GRANT_ACL,
    
    REFERENCES_ACL,
    
    INDEX_ACL,
    
    ALTER_ACL,
    
    SHOW_DB_ACL,
    
    SUPER_ACL,
    
    CREATE_TMP_ACL,
    
    LOCK_TABLES_ACL,
    
    EXECUTE_ACL,
    
    REPL_SLAVE_ACL,
    
    REPL_CLIENT_ACL,
    
    CREATE_VIEW_ACL,
    
    SHOW_VIEW_ACL,
    
    CREATE_PROC_ACL,
    
    ALTER_PROC_ACL,
    
    CREATE_USER_ACL,
    
    EVENT_ACL,
    
    TRIGGER_ACL,
    
    CREATE_TABLESPACE_ACL,
    
    CREATE_ROLE_ACL,
    
    DROP_ROLE_ACL
}
