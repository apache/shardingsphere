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

package org.apache.shardingsphere.infra.exception.external.server;

import org.apache.shardingsphere.infra.exception.external.ShardingSphereExternalException;

/**
 * ShardingSphere server exception.
 */
public abstract class ShardingSphereServerException extends ShardingSphereExternalException {
    
    private static final long serialVersionUID = 1547233217081261239L;
    
    protected ShardingSphereServerException(final String errorCategory, final int errorCode, final String message) {
        super(String.format("%s-%05d: %s", errorCategory, errorCode, message));
    }
    
    protected ShardingSphereServerException(final String errorCategory, final int errorCode, final String message, final Exception cause) {
        super(String.format("%s-%05d: %s", errorCategory, errorCode, message), cause);
    }
}
