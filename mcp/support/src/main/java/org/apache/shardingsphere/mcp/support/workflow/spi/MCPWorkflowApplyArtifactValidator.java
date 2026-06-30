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

package org.apache.shardingsphere.mcp.support.workflow.spi;

import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowArtifactBundle.ExecutableWorkflowArtifact;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Workflow apply artifact validator.
 */
@FunctionalInterface
public interface MCPWorkflowApplyArtifactValidator {
    
    MCPWorkflowApplyArtifactValidator NO_OP = (snapshot, artifacts) -> List.of();
    
    /**
     * Validate executable workflow artifacts before preview or apply.
     *
     * @param snapshot workflow snapshot
     * @param artifacts executable workflow artifacts
     * @return validation issues
     */
    List<Map<String, Object>> validate(WorkflowContextSnapshot snapshot, Collection<ExecutableWorkflowArtifact> artifacts);
}
