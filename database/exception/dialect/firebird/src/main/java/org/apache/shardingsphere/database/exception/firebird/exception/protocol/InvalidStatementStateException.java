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

package org.apache.shardingsphere.database.exception.firebird.exception.protocol;

import lombok.Getter;
import org.apache.shardingsphere.database.exception.core.exception.SQLDialectException;

/**
 * Invalid statement state exception for Firebird.
 *
 * <p>The statement handle does not refer to an allocated statement, or the allocated statement is not prepared.</p>
 */
@Getter
public final class InvalidStatementStateException extends SQLDialectException {
    
    private static final long serialVersionUID = -2954106328813297165L;
    
    private final int statementHandle;
    
    private final boolean allocated;
    
    public InvalidStatementStateException(final int statementHandle, final boolean allocated) {
        super(allocated ? String.format("Statement %d is not prepared", statementHandle) : String.format("Statement handle %d is not allocated", statementHandle));
        this.statementHandle = statementHandle;
        this.allocated = allocated;
    }
}
