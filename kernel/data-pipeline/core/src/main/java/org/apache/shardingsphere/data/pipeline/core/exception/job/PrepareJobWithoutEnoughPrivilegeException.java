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

package org.apache.shardingsphere.data.pipeline.core.exception.job;

import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;

import java.util.Collection;

/**
 * Prepare job without enough privilege exception.
 */
public final class PrepareJobWithoutEnoughPrivilegeException extends PipelineJobException {
    
    private static final long serialVersionUID = -8462039913248251254L;
    
    public PrepareJobWithoutEnoughPrivilegeException(final Collection<String> privileges) {
        super(XOpenSQLState.PRIVILEGE_NOT_GRANTED, 6, String.format("Source data source lacks '%s' privilege(s).", privileges));
    }
}
