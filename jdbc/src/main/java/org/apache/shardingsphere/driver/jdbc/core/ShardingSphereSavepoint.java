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

package org.apache.shardingsphere.driver.jdbc.core;

import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;

import java.rmi.server.UID;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Savepoint;

/**
 * ShardingSphere savepoint.
 */
public final class ShardingSphereSavepoint implements Savepoint {
    
    private final String savepointName;
    
    public ShardingSphereSavepoint() {
        savepointName = getUniqueId();
    }
    
    public ShardingSphereSavepoint(final String name) throws SQLException {
        ShardingSpherePreconditions.checkState(null != name && 0 != name.length(), () -> new SQLFeatureNotSupportedException("Savepoint name can not be NULL or empty"));
        savepointName = name;
    }
    
    @Override
    public int getSavepointId() throws SQLException {
        throw new SQLFeatureNotSupportedException("Only named savepoint are supported.");
    }
    
    @Override
    public String getSavepointName() {
        return savepointName;
    }
    
    private String getUniqueId() {
        String uidStr = new UID().toString();
        int uidLength = uidStr.length();
        StringBuilder safeString = new StringBuilder(uidLength + 1);
        safeString.append('_');
        for (int i = 0; i < uidLength; i++) {
            char c = uidStr.charAt(i);
            if (Character.isLetter(c) || Character.isDigit(c)) {
                safeString.append(c);
            } else {
                safeString.append('_');
            }
        }
        return safeString.toString();
    }
}
