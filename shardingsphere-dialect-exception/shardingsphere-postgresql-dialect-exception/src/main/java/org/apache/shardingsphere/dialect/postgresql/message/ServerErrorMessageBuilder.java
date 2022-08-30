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

package org.apache.shardingsphere.dialect.postgresql.message;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.util.exception.sql.vendor.VendorError;
import org.postgresql.util.ServerErrorMessage;

/**
 * Server error message builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ServerErrorMessageBuilder {
    
    /**
     * Build server error message.
     *
     * @param severity severity
     * @param vendorError vendor error
     * @param reasonArguments reason arguments
     * @return server error message
     */
    public static ServerErrorMessage build(final String severity, final VendorError vendorError, final Object... reasonArguments) {
        return new ServerErrorMessage(
                String.join("\0", buildSeverity(severity), buildNewSeverity(severity), buildSQLState(vendorError), buildReason(String.format(vendorError.getReason(), reasonArguments))));
    }
    
    private static String buildSeverity(final String severity) {
        return 'S' + severity;
    }
    
    private static String buildNewSeverity(final String severity) {
        return 'V' + severity;
    }
    
    private static String buildSQLState(final VendorError vendorError) {
        return 'C' + vendorError.getSqlState().getValue();
    }
    
    private static String buildReason(final String reason) {
        return 'M' + reason;
    }
}
