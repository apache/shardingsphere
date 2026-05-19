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

package org.apache.shardingsphere.infra.metadata.identifier;

import com.cedarsoftware.util.CaseInsensitiveMap.CaseInsensitiveString;
import lombok.Getter;

import java.util.Objects;

/**
 * ShardingSphere identifier.
 */
public final class ShardingSphereIdentifier {
    
    private final CaseInsensitiveString value;
    
    @Getter
    private final String standardizeValue;
    
    private final boolean caseSensitive;
    
    public ShardingSphereIdentifier(final String value) {
        this.value = null == value ? null : CaseInsensitiveString.of(value);
        standardizeValue = value;
        caseSensitive = false;
    }
    
    /**
     * Get identifier value.
     *
     * @return identifier value
     */
    public String getValue() {
        return null == value ? null : value.toString();
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof ShardingSphereIdentifier)) {
            return false;
        }
        ShardingSphereIdentifier other = (ShardingSphereIdentifier) obj;
        if (null == getValue() && null == other.getValue()) {
            return true;
        }
        if (null == standardizeValue || null == other.getStandardizeValue()) {
            return false;
        }
        return caseSensitive ? standardizeValue.equals(other.getStandardizeValue()) : Objects.equals(value, other.value);
    }
    
    @Override
    public int hashCode() {
        if (null == standardizeValue) {
            return 0;
        }
        if (caseSensitive) {
            return standardizeValue.hashCode();
        }
        return null == value ? 0 : value.hashCode();
    }
    
    @Override
    public String toString() {
        return getValue();
    }
}
