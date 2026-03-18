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

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Storage units validate exception.
 */
public final class StorageUnitsValidateException extends ResourceDefinitionException {
    
    private static final long serialVersionUID = 1824912697040264268L;
    
    public StorageUnitsValidateException(final Map<String, Exception> causes) {
        super(XOpenSQLState.CONNECTION_EXCEPTION, 10, "Storage units validate error, messages are: %s.", causes.entrySet().stream().map(entry -> String.format(
                "Storage unit name: '%s', error message is: %s", entry.getKey(), entry.getValue().getMessage())).collect(Collectors.joining(System.lineSeparator())));
    }
}
