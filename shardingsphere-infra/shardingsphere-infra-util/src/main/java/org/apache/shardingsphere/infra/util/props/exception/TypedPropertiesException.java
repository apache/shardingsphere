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

package org.apache.shardingsphere.infra.util.props.exception;

import java.util.Collection;

/**
 * Typed properties exception.
 */
public final class TypedPropertiesException extends RuntimeException {
    
    private static final long serialVersionUID = -8301410307117564844L;
    
    private static final int ERROR_CODE = 1;
    
    public TypedPropertiesException(final Collection<String> errorMessages) {
        super(createErrorMessage(errorMessages));
    }
    
    private static String createErrorMessage(final Collection<String> errorMessages) {
        return String.format("PROPS-%05d: %s", ERROR_CODE, String.join(System.lineSeparator(), errorMessages));
    }
}
