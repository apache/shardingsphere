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

package org.apache.shardingsphere.keygen.uuid.algorithm;

import org.apache.shardingsphere.keygen.core.algorithm.KeyGenerateAlgorithm;
import org.apache.shardingsphere.keygen.core.context.KeyGenerateContext;

import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * UUID key generate algorithm.
 */
public final class UUIDKeyGenerateAlgorithm implements KeyGenerateAlgorithm {
    
    @Override
    public Collection<Comparable<?>> generateKeys(final KeyGenerateContext keyGenerateContext, final int keyGenerateCount) {
        Collection<Comparable<?>> result = new LinkedList<>();
        ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
        for (int index = 0; index < keyGenerateCount; index++) {
            result.add(generateKey(threadLocalRandom));
        }
        return result;
    }
    
    private String generateKey(final ThreadLocalRandom threadLocalRandom) {
        return new UUID(threadLocalRandom.nextLong(), threadLocalRandom.nextLong()).toString().replace("-", "");
    }
    
    @Override
    public String getType() {
        return "UUID";
    }
}
