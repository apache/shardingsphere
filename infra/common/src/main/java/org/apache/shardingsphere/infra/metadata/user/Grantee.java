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

package org.apache.shardingsphere.infra.metadata.user;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;

/**
 * Grantee.
 */
public final class Grantee {
    
    private static final String DELIMITER = "@";
    
    private static final String HOST_WILDCARD = "%";
    
    @Getter
    private final String username;
    
    @Getter
    private final String hostname;
    
    private final boolean isUnlimitedHost;
    
    private final int hashCode;
    
    private final String toString;
    
    public Grantee(final String username, final String hostname) {
        this.username = username;
        this.hostname = Strings.isNullOrEmpty(hostname) ? HOST_WILDCARD : hostname;
        isUnlimitedHost = HOST_WILDCARD.equals(this.hostname);
        hashCode = Objects.hashCode(username.toUpperCase(), this.hostname.toUpperCase());
        toString = username + DELIMITER + this.hostname;
    }
    
    /**
     * Check if the grantee is acceptable.
     *
     * @param grantee grantee
     * @return if the grantee is acceptable
     */
    @HighFrequencyInvocation
    public boolean accept(final Grantee grantee) {
        return grantee.username.equalsIgnoreCase(username) && isPermittedHost(grantee);
    }
    
    private boolean isPermittedHost(final Grantee grantee) {
        return isUnlimitedHost || grantee.hostname.equalsIgnoreCase(hostname);
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Grantee) {
            Grantee grantee = (Grantee) obj;
            return grantee.username.equalsIgnoreCase(username) && grantee.hostname.equalsIgnoreCase(hostname);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return hashCode;
    }
    
    @Override
    public String toString() {
        return toString;
    }
}
