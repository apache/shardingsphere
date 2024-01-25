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

package org.apache.shardingsphere.data.pipeline.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;

/**
 * Instance type utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InstanceTypeUtils {
    
    /**
     * Encode instance type.
     *
     * @param instanceType instance type
     * @return encoded instance type
     * @throws UnsupportedOperationException if instance type is unknown
     */
    public static char encode(final InstanceType instanceType) {
        switch (instanceType) {
            case PROXY:
                return 'p';
            case JDBC:
                return 'j';
            default:
                throw new UnsupportedOperationException("Unknown instance type: " + instanceType);
        }
    }
    
    /**
     * Decode instance type.
     *
     * @param instanceType instance type
     * @return decoded instance type
     * @throws UnsupportedOperationException if instance type is unknown
     */
    public static InstanceType decode(final char instanceType) {
        switch (instanceType) {
            case 'p':
                return InstanceType.PROXY;
            case 'j':
                return InstanceType.JDBC;
            default:
                throw new UnsupportedOperationException("Unknown instance type: " + instanceType);
        }
    }
}
