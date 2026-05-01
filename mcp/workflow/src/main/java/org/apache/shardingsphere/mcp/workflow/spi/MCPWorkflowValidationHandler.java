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

package org.apache.shardingsphere.mcp.workflow.spi;

import org.apache.shardingsphere.mcp.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.workflow.WorkflowSessionContext;

import java.util.Map;

/**
 * Workflow validation handler.
 */
@FunctionalInterface
public interface MCPWorkflowValidationHandler {
    
    /**
     * Validate one workflow plan.
     *
     * @param workflowSessionContext workflow session context
     * @param metadataQueryFacade metadata query facade
     * @param queryFacade direct query facade
     * @param executionFacade execution facade
     * @param sessionId session id
     * @param planId plan id
     * @return validation payload
     */
    Map<String, Object> validate(WorkflowSessionContext workflowSessionContext, MCPMetadataQueryFacade metadataQueryFacade,
                                 MCPFeatureQueryFacade queryFacade, MCPFeatureExecutionFacade executionFacade, String sessionId, String planId);
}
