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

package org.apache.shardingsphere.driver.jdbc.core.savepoint;

import lombok.Getter;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Savepoint;
import java.util.UUID;

/**
 * ShardingSphere savepoint.
 */
@Getter
public final class ShardingSphereSavepoint implements Savepoint {
    
    private final String savepointName;
    
    public ShardingSphereSavepoint() {
        savepointName = UUID.randomUUID().toString().replaceAll("-", "_");
    }
    
    public ShardingSphereSavepoint(final String savepointName) throws SQLException {
        ShardingSpherePreconditions.checkNotEmpty(savepointName, () -> new SQLFeatureNotSupportedException("Savepoint name can not be NULL or empty"));
        this.savepointName = savepointName;
    }
    
    @Override
    public int getSavepointId() throws SQLException {
        throw new SQLFeatureNotSupportedException("Only named savepoint are supported.");
    }
}
