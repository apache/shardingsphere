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

package org.apache.shardingsphere.transaction.core;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.transaction.exception.ResourceNameLengthExceededException;

import javax.sql.DataSource;

/**
 * Unique resource data source.
 */
@Getter
public final class ResourceDataSource {
    
    private static final int MAX_RESOURCE_NAME_LENGTH = 45;
    
    private final String originalName;
    
    private final String uniqueResourceName;
    
    private final DataSource dataSource;
    
    public ResourceDataSource(final String originalName, final DataSource dataSource) {
        String[] databaseAndDataSourceName = originalName.split("\\.");
        Preconditions.checkState(2 == databaseAndDataSourceName.length, "Database and data source name must be provided together by `%s`.", originalName);
        this.originalName = originalName;
        this.dataSource = dataSource;
        uniqueResourceName = ResourceIdGenerator.getInstance().nextId() + databaseAndDataSourceName[1];
        ShardingSpherePreconditions.checkState(uniqueResourceName.getBytes().length <= MAX_RESOURCE_NAME_LENGTH, () -> new ResourceNameLengthExceededException(uniqueResourceName));
    }
}
