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

package org.apache.shardingsphere.test.e2e.fixture;

import org.apache.shardingsphere.infra.algorithm.keygen.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.infra.algorithm.core.context.AlgorithmSQLContext;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class E2EAutoIncrementKeyGenerateAlgorithmFixture implements KeyGenerateAlgorithm {
    
    private final AtomicLong idGenerator = new AtomicLong(1L);
    
    @Override
    public Collection<Comparable<?>> generateKeys(final AlgorithmSQLContext context, final int keyGenerateCount) {
        return IntStream.range(0, keyGenerateCount).mapToObj(each -> idGenerator.getAndIncrement()).collect(Collectors.toList());
    }
    
    @Override
    public boolean isSupportAutoIncrement() {
        idGenerator.set(1L);
        return true;
    }
    
    @Override
    public String getType() {
        return "E2E.AUTO_INCREMENT.FIXTURE";
    }
}
