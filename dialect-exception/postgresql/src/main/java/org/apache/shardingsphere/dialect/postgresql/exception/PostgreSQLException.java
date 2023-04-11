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

import lombok.Getter;
import org.apache.shardingsphere.infra.util.exception.external.sql.vendor.VendorError;

import java.sql.SQLException;

@Getter
public class PostgreSQLException extends SQLException {
    
    private String severity;
    private String SQLState;
    private String reason;
    
    public PostgreSQLException(String msg, String state) {
        super(msg, state);
    }
    
    public PostgreSQLException(final String severity, final VendorError vendorError, final Object... reasonArgs) {
        this.severity = severity;
        this.SQLState = vendorError.getSqlState().getValue();
        this.reason = String.format(vendorError.getReason(), reasonArgs);
    }
    
}
