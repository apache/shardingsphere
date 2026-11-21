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

package org.apache.shardingsphere.database.exception.firebird.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * Firebird exception.
 *
 * <p>Carries the status vector that Firebird reports for the error, because the error code and message of a SQL exception cannot hold several error codes and numeric arguments.</p>
 */
@Getter
public final class FirebirdException extends SQLException {
    
    private static final long serialVersionUID = 4473318734621905436L;
    
    private final Collection<StatusVectorEntry> statusVector;
    
    public FirebirdException(final String reason, final String sqlState, final int vendorCode, final Collection<StatusVectorEntry> statusVector) {
        super(reason, sqlState, vendorCode);
        this.statusVector = statusVector;
    }
    
    /**
     * Entry of Firebird status vector: an error code with its arguments, each of which is an {@link Integer} number or a {@link String}.
     */
    @RequiredArgsConstructor
    @Getter
    public static final class StatusVectorEntry implements Serializable {
        
        private static final long serialVersionUID = -6131581945120183917L;
        
        private final int gdsCode;
        
        private final List<Object> arguments;
    }
}
