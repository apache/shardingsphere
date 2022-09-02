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

package org.apache.shardingsphere.infra.util.exception.internal.fixture;

import org.apache.shardingsphere.infra.util.exception.internal.ShardingSphereInternalException;

public final class ShardingSphereInternalExceptionFixture extends ShardingSphereInternalException {
    
    private static final long serialVersionUID = 3759417279204541147L;
    
    public ShardingSphereInternalExceptionFixture(final String message) {
        super("Fixture error message: %s", message);
    }
    
    public ShardingSphereInternalExceptionFixture(final String message, final Exception cause) {
        super(message, cause);
    }
    
    public ShardingSphereInternalExceptionFixture(final Exception cause) {
        super(cause);
    }
}
