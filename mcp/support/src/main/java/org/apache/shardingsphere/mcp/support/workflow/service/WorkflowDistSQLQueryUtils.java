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

package org.apache.shardingsphere.mcp.support.workflow.service;

import org.apache.shardingsphere.mcp.api.protocol.exception.MCPQueryFailedException;

import java.sql.SQLSyntaxErrorException;
import java.util.Locale;
import java.util.Objects;

/**
 * DistSQL query utilities for workflow planning.
 */
public final class WorkflowDistSQLQueryUtils {
    
    private WorkflowDistSQLQueryUtils() {
    }
    
    /**
     * Judge whether a query failed because the current backend cannot parse ShardingSphere DistSQL.
     *
     * @param ex query failure
     * @return whether the backend does not support DistSQL syntax
     */
    public static boolean isUnsupportedDistSQLQueryFailure(final MCPQueryFailedException ex) {
        return hasSyntaxErrorCause(ex) || hasUnsupportedDistSQLMessage(ex);
    }
    
    private static boolean hasSyntaxErrorCause(final Throwable throwable) {
        Throwable current = throwable;
        while (null != current) {
            if (current instanceof SQLSyntaxErrorException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
    
    private static boolean hasUnsupportedDistSQLMessage(final MCPQueryFailedException ex) {
        String message = Objects.toString(ex.getMessage(), "").toLowerCase(Locale.ENGLISH);
        return message.contains("syntax") && (message.contains("distsql") || message.contains(" rule") || message.contains("algorithm plugins"));
    }
}
