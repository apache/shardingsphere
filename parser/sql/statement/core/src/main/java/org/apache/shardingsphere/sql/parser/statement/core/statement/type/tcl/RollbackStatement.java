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

package org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;

import java.util.Optional;

/**
 * Rollback statement.
 */
public final class RollbackStatement extends TCLStatement {
    
    private final String savepointName;
    
    public RollbackStatement(final DatabaseType databaseType) {
        this(databaseType, null);
    }
    
    public RollbackStatement(final DatabaseType databaseType, final String savepointName) {
        super(databaseType);
        this.savepointName = savepointName;
    }
    
    /**
     * Get save point name.
     *
     * @return save point name
     */
    public Optional<String> getSavepointName() {
        return Optional.ofNullable(savepointName);
    }
}
