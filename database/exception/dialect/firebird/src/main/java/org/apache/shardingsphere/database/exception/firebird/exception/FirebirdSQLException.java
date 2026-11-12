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
import org.apache.shardingsphere.infra.exception.external.sql.vendor.VendorError;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * Firebird SQL exception keeping the typed message arguments of the vendor error.
 */
@Getter
public final class FirebirdSQLException extends SQLException {
    
    private static final long serialVersionUID = 4329371620785372165L;
    
    private final List<Object> messageArguments;
    
    public FirebirdSQLException(final VendorError vendorError, final Object... messageArguments) {
        super(String.format(vendorError.getReason(), messageArguments), vendorError.getSqlState().getValue(), vendorError.getVendorCode());
        this.messageArguments = Arrays.asList(messageArguments);
    }
}
