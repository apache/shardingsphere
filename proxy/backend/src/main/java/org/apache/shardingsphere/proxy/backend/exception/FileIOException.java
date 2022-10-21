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

package org.apache.shardingsphere.proxy.backend.exception;

import org.apache.shardingsphere.infra.util.exception.external.sql.sqlstate.XOpenSQLState;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.workerid.exception.ClusterSQLException;

import java.io.IOException;

/**
 * File IO exception.
 */
public final class FileIOException extends ClusterSQLException {
    
    private static final long serialVersionUID = 1104839422339487793L;
    
    public FileIOException(final IOException cause) {
        super(XOpenSQLState.GENERAL_ERROR, 2, "File access failed, reason is: %s", cause.getMessage());
    }
}
