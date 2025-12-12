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

package org.apache.shardingsphere.infra.algorithm.keygen.spi;

import org.apache.shardingsphere.infra.algorithm.core.ShardingSphereAlgorithm;
import org.apache.shardingsphere.infra.algorithm.core.context.AlgorithmSQLContext;

import java.util.Collection;

/**
 * Key generate algorithm.
 */
public interface KeyGenerateAlgorithm extends ShardingSphereAlgorithm {
    
    /**
     * Generate keys.
     *
     * @param context algorithm SQL context 
     * @param keyGenerateCount key generate count
     * @return generated keys
     */
    Collection<? extends Comparable<?>> generateKeys(AlgorithmSQLContext context, int keyGenerateCount);
    
    /**
     * Judge whether support auto increment or not.
     *
     * @return whether support auto increment or not
     */
    default boolean isSupportAutoIncrement() {
        return false;
    }
}
