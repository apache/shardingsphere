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

package org.apache.shardingsphere.distsql.handler.validate;

import org.apache.shardingsphere.database.connector.core.checker.PrivilegeCheckType;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.datasource.pool.props.validator.DataSourcePoolPropertiesValidator;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.StorageUnitsValidateException;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * DistSQL data source pool properties validator.
 */
public final class DistSQLDataSourcePoolPropertiesValidator {
    
    /**
     * Validate data source properties map.
     *
     * @param propsMap data source pool properties map
     */
    public void validate(final Map<String, DataSourcePoolProperties> propsMap) {
        validate(propsMap, Collections.emptySet());
    }
    
    /**
     * Validate data source properties map.
     *
     * @param propsMap data source pool properties map
     * @param expectedPrivileges expected privileges
     */
    public void validate(final Map<String, DataSourcePoolProperties> propsMap, final Collection<PrivilegeCheckType> expectedPrivileges) {
        Map<String, Exception> exceptions = DataSourcePoolPropertiesValidator.validate(propsMap, expectedPrivileges);
        ShardingSpherePreconditions.checkMustEmpty(exceptions, () -> new StorageUnitsValidateException(exceptions));
    }
}
