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

package org.apache.shardingsphere.database.exception.postgresql.exception;

import lombok.Getter;
import org.apache.shardingsphere.infra.exception.external.sql.vendor.VendorError;

import java.io.Serializable;
import java.sql.SQLException;
import java.text.MessageFormat;

/**
 * Replacement for {@link org.postgresql.util.PSQLException}.
 */
@Getter
public final class PostgreSQLException extends SQLException {
    
    private static final long serialVersionUID = -593592349806424431L;
    
    private final ServerErrorMessage serverErrorMessage;
    
    public PostgreSQLException(final String msg, final String state) {
        super(msg, state);
        serverErrorMessage = null;
    }
    
    public PostgreSQLException(final ServerErrorMessage serverErrorMessage) {
        super(serverErrorMessage.toString(), serverErrorMessage.getSqlState());
        this.serverErrorMessage = serverErrorMessage;
    }
    
    @Getter
    public static class ServerErrorMessage implements Serializable {
        
        private static final long serialVersionUID = -2823942573556507523L;
        
        private final String severity;
        
        private final String sqlState;
        
        private final String message;
        
        public ServerErrorMessage(final String severity, final VendorError vendorError, final Object... reasonArgs) {
            this.severity = severity;
            sqlState = vendorError.getSqlState().getValue();
            message = String.format(vendorError.getReason(), reasonArgs);
        }
        
        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            if (null != severity) {
                result.append(severity).append(": ");
            }
            if (null != message) {
                result.append(message);
            }
            if (null != sqlState) {
                result.append("\n  ").append(MessageFormat.format("Server SQLState: {0}", sqlState));
            }
            return result.toString();
        }
    }
}
