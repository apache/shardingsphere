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

package org.apache.shardingsphere.encrypt.spi;

import org.apache.shardingsphere.infra.algorithm.core.ShardingSphereAlgorithm;
import org.apache.shardingsphere.infra.algorithm.core.context.AlgorithmSQLContext;

/**
 * Encrypt algorithm.
 */
public interface EncryptAlgorithm extends ShardingSphereAlgorithm {
    
    /**
     * Encrypt.
     *
     * @param plainValue plain value
     * @param algorithmSQLContext algorithm SQL context
     * @return cipher value
     */
    Object encrypt(Object plainValue, AlgorithmSQLContext algorithmSQLContext);
    
    /**
     * Decrypt.
     *
     * @param cipherValue cipher value
     * @param algorithmSQLContext algorithm SQL context
     * @return plain value
     */
    Object decrypt(Object cipherValue, AlgorithmSQLContext algorithmSQLContext);
    
    /**
     * Get encrypt algorithm meta data.
     *
     * @return encrypt algorithm meta data
     */
    EncryptAlgorithmMetaData getMetaData();
}
