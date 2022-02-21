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

package org.apache.shardingsphere.infra.distsql.exception.resource;

import java.util.Collection;

/**
 * Required resource missed exception.
 */
public final class RequiredResourceMissedException extends ResourceDefinitionViolationException {
    
    private static final long serialVersionUID = 1704331180489268L;
    
    public RequiredResourceMissedException(final String schemaName, final Collection<String> resourceNames) {
        super(1102, String.format("Resources %s do not exist in schema %s.", resourceNames, schemaName));
    }
    
    public RequiredResourceMissedException(final String schemaName) {
        super(1102, String.format("There are no resources in the schema %s.", schemaName));
    }
}
