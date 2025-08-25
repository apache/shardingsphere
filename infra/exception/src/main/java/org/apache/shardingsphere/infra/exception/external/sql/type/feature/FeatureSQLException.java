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

package org.apache.shardingsphere.infra.exception.external.sql.type.feature;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.exception.external.sql.ShardingSphereSQLException;
import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.SQLState;

/**
 * Feature SQL exception.
 */
public abstract class FeatureSQLException extends ShardingSphereSQLException {
    
    private static final long serialVersionUID = -3748977692432149265L;
    
    private static final int TYPE_OFFSET = 2;
    
    protected FeatureSQLException(final SQLState sqlState, final int featureCode, final int errorCode, final String reason, final Object... messageArgs) {
        super(sqlState, TYPE_OFFSET, getErrorCode(featureCode, errorCode), reason, messageArgs);
    }
    
    protected FeatureSQLException(final SQLState sqlState, final int featureCode, final int errorCode, final Exception cause, final String reason, final Object... messageArgs) {
        super(sqlState, TYPE_OFFSET, getErrorCode(featureCode, errorCode), cause, reason, messageArgs);
    }
    
    private static int getErrorCode(final int featureCode, final int errorCode) {
        Preconditions.checkArgument(featureCode >= 0 && featureCode < 100, "The value range of feature code should be [0, 100).");
        Preconditions.checkArgument(errorCode >= 0 && errorCode < 100, "The value range of error code should be [0, 100).");
        return featureCode * 100 + errorCode;
    }
}
