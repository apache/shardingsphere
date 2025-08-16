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

package org.apache.shardingsphere.infra.algorithm.cryptographic.spi;

import org.apache.shardingsphere.infra.algorithm.core.ShardingSphereAlgorithm;

/**
 * Cryptographic algorithm.
 */
public interface CryptographicAlgorithm extends ShardingSphereAlgorithm {
    
    /**
     * Encrypt.
     *
     * @param plainValue plain value
     * @return cipher value
     */
    Object encrypt(Object plainValue);
    
    /**
     * Decrypt.
     *
     * @param cipherValue cipher value
     * @return plain value
     */
    Object decrypt(Object cipherValue);
}
