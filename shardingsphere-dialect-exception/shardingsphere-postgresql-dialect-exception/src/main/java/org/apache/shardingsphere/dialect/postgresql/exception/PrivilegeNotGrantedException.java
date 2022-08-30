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

package org.apache.shardingsphere.dialect.postgresql.exception;

import org.apache.shardingsphere.dialect.exception.SQLDialectException;

/**
 * Privilege not granted exception.
 */
public final class PrivilegeNotGrantedException extends SQLDialectException {
    
    private static final long serialVersionUID = 8410672833723209253L;
    
    public PrivilegeNotGrantedException(final String username, final String databaseName) {
        super("Access denied for user '%s' to database '%s'", username, databaseName);
    }
}
