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
 * Invalid algorithm configuration exception.
 */
public final class InvalidAlgorithmConfigurationException extends RuleDefinitionViolationException {
    
    private static final long serialVersionUID = 9076740384552385180L;
    
    public InvalidAlgorithmConfigurationException(final String algorithmType, final Collection<String> algorithms) {
        super(1114, String.format("Invalid %s algorithms %s.", algorithmType, algorithms));
    }
    
    public InvalidAlgorithmConfigurationException(final String algorithmType, final String algorithm) {
        super(1114, String.format("Invalid %s algorithm %s.", algorithmType, algorithm));
    }
    
    public InvalidAlgorithmConfigurationException(final String algorithmType) {
        super(1114, String.format("Invalid %s algorithms configuration.", algorithmType));
    }
}
