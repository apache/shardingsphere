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

package org.apache.shardingsphere.sqlfederation.provider.none;

import org.apache.shardingsphere.infra.exception.external.server.ShardingSphereServerException;

/**
 * SQL federation execution is unsupported by the NONE provider.
 */
public final class SQLFederationProviderUnsupportedException extends ShardingSphereServerException {
    
    private static final long serialVersionUID = 1937348220116813622L;
    
    public SQLFederationProviderUnsupportedException() {
        super("SQL_FEDERATION", 3, "SQL Federation provider 'NONE' does not support federation execution.");
    }
}
