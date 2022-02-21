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

import org.apache.shardingsphere.encrypt.spi.context.EncryptContext;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithm;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmPostProcessor;

/**
 * Encrypt algorithm for SPI.
 * 
 * @param <I> type of plain value
 * @param <O> type of cipher value
 */
public interface EncryptAlgorithm<I, O> extends ShardingSphereAlgorithm, ShardingSphereAlgorithmPostProcessor {
    
    /**
     * Encode.
     *
     * @param plainValue plain value
     * @param encryptContext encrypt context
     * @return cipher value
     */
    O encrypt(I plainValue, EncryptContext encryptContext);
    
    /**
     * Decode.
     *
     * @param cipherValue cipher value
     * @param encryptContext encrypt context
     * @return plain value
     */
    I decrypt(O cipherValue, EncryptContext encryptContext);
}
