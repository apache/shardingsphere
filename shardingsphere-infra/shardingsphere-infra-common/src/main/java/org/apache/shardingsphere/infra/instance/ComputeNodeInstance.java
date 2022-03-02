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

package org.apache.shardingsphere.infra.instance;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.infra.instance.definition.InstanceDefinition;

import java.util.Collection;

/**
 * Instance of compute node.
 */
@RequiredArgsConstructor
@Getter
@Setter
public final class ComputeNodeInstance {
    
    private final InstanceDefinition instanceDefinition;
    
    private Collection<String> labels;
    
    private Collection<String> status;
    
    private Long workerId;
    
    private String xaRecoveryId;
}
