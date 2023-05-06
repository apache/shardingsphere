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

package org.apache.shardingsphere.data.pipeline.api.metadata;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Identifier name.
 * <p>It might be schema name or table name, etc.</p>
 * <p>It's case-insensitive.</p>
 */
@Getter
// TODO table name case-sensitive for some database
@EqualsAndHashCode(of = "lowercase")
public class IdentifierName {
    
    private final String original;
    
    private final String lowercase;
    
    public IdentifierName(final String identifierName) {
        this.original = identifierName;
        this.lowercase = null != identifierName ? identifierName.toLowerCase() : null;
    }
    
    @Override
    public String toString() {
        return original;
    }
}
