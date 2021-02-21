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

package org.apache.shardingsphere.infra.metadata.auth.model.user;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Grantee.
 */
@RequiredArgsConstructor
@Getter
public final class Grantee {
    
    private final String username;
    
    private final String hostname;
    
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Grantee) {
            Grantee grantee = (Grantee) obj;
            return grantee.getUsername().equalsIgnoreCase(username) && (grantee.getHostname().equalsIgnoreCase(hostname) || Strings.isNullOrEmpty(hostname));
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return Strings.isNullOrEmpty(hostname)
                ? Objects.hashCode(username.toUpperCase()) : Objects.hashCode(username.toUpperCase(), hostname.toUpperCase());
    }
}
