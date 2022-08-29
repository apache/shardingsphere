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

package org.apache.shardingsphere.dialect.postgresql.vendor;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.util.exception.sql.vendor.VendorError;
import org.postgresql.util.ServerErrorMessage;

/**
 * PostgreSQL server error message.
 */
@RequiredArgsConstructor
public final class PostgreSQLServerErrorMessage {
    
    private final String severity;
    
    private final VendorError vendorError;
    
    private final String reason;
    
    /**
     * To server error message.
     * 
     * @return server error message
     */
    public ServerErrorMessage toServerErrorMessage() {
        return new ServerErrorMessage("S" + severity + "\0" + "V" + severity + "\0" + "C" + vendorError.getSqlState().getValue() + "\0" + "M" + reason);
    }
}
