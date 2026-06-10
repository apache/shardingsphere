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

package org.apache.shardingsphere.database.exception.firebird.sqlstate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.SQLState;

/**
 * Firebird SQL state.
 *
 * <p>Holds SQL states that Jaybird derives from a GDSCODE but that are not defined in {@code XOpenSQLState}.</p>
 */
@RequiredArgsConstructor
@Getter
public enum FirebirdState implements SQLState {
    
    UNAVAILABLE_DATABASE("08001"),
    
    INVALID_BATCH_HANDLE("08003"),
    
    BATCH_TOO_BIG("54000"),
    
    CHARSET_NOT_FOUND("2C000");
    
    private final String value;
}
