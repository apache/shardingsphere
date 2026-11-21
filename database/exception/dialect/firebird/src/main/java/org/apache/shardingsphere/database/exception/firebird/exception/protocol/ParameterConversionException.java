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
 * Parameter conversion exception for Firebird.
 *
 * <p>Firebird moves a value of the client message to the statement parameter by {@code MOVD_move} and reports an error of the move
 * after the Dynamic SQL error with SQL error code -303.</p>
 */
@Getter
public final class ParameterConversionException extends SQLDialectException {
    
    private static final long serialVersionUID = 5724397585632840137L;
    
    private final SQLDialectException conversionError;
    
    public ParameterConversionException(final SQLDialectException conversionError) {
        super(conversionError.getMessage());
        this.conversionError = conversionError;
    }
}
