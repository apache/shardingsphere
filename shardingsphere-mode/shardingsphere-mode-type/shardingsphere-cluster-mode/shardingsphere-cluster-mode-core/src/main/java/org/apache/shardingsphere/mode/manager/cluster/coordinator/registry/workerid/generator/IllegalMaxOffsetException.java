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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.workerid.generator;

import org.apache.shardingsphere.infra.util.exception.ShardingSphereException;

/**
 * Illegal max offset exception.
 */
public final class IllegalMaxOffsetException extends ShardingSphereException {
    
    private static final long serialVersionUID = -247360034818257724L;
    
    private static final String ERROR_CATEGORY = "WORKER-ID";
    
    private static final int ERROR_CODE = 1;
    
    public IllegalMaxOffsetException(final String errorMessage, final Object... args) {
        super(ERROR_CATEGORY, ERROR_CODE, String.format(errorMessage, args));
    }
}
