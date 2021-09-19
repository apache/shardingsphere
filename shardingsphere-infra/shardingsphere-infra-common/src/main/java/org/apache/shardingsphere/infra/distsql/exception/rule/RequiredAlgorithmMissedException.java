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

package org.apache.shardingsphere.infra.distsql.exception.rule;

import java.util.Collection;

/**
 * Required algorithm missed exception.
 */
public final class RequiredAlgorithmMissedException extends RuleDefinitionViolationException {
    
    private static final long serialVersionUID = -1952698375135777585L;
    
    public RequiredAlgorithmMissedException(final String schemaName) {
        super(1115, String.format("Sharding algorithm does not exist in schema `%s`.", schemaName));
    }
    
    public RequiredAlgorithmMissedException(final String schemaName, final Collection<String> algorithmNames) {
        super(1115, String.format("Sharding algorithms `%s` do not exist in schema `%s`.", algorithmNames, schemaName));
    }
    
    public RequiredAlgorithmMissedException(final String type, final String schemaName, final Collection<String> algorithmNames) {
        super(1115, String.format("%s algorithms `%s` do not exist in schema `%s`.", type, algorithmNames, schemaName));
    }
}
