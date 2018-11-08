/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.antlr.extractor;

import lombok.RequiredArgsConstructor;

/**
 * SQL statement type.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public enum SQLStatementType {
    
    CREATE_TABLE("CreateTable"),
    
    ALTER_TABLE("AlterTable"),
    
    DROP_TABLE("DropTable"),
    
    TRUNCATE_TABLE("TruncateTable"),
    
    CREATE_INDEX("CreateIndex"),
    
    ALTER_INDEX("AlterIndex"),
    
    DROP_INDEX("DropIndex"),
    
    SET_TRANSACTION("SetTransaction"),
    
    COMMIT("Commit"),
    
    ROLLBACK("Rollback"),
    
    SAVEPOINT("Savepoint"),
    
    BEGIN_WORK("BeginWork"),
    
    SET_VARIABLE("SetVariable");
    
    private final String name;
    
    /**
     * Get SQL statement type via name.
     * 
     * @param name name of SQL statement type
     * @return SQL statement type
     */
    public static SQLStatementType nameOf(final String name) {
        for (SQLStatementType each : SQLStatementType.values()) {
            if ((each.name + "Context").equals(name)) {
                return each;
            }
        }
        throw new UnsupportedOperationException(String.format("Unsupported SQL statement of `%s`", name));
    }
}
