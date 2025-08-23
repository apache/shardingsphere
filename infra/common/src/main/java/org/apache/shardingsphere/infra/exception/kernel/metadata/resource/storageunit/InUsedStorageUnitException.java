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

package org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit;

import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.ResourceDefinitionException;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * In used storage unit exception.
 */
public final class InUsedStorageUnitException extends ResourceDefinitionException {
    
    private static final long serialVersionUID = -3427324685070457375L;
    
    public InUsedStorageUnitException(final String storageUnitName, final Collection<Class<? extends ShardingSphereRule>> ruleClasses) {
        super(XOpenSQLState.CHECK_OPTION_VIOLATION, 3, "Storage unit '%s' still used by '%s'.", storageUnitName, ruleClasses.stream().map(Class::getSimpleName).collect(Collectors.toList()));
    }
}
