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

package org.apache.shardingsphere.test.e2e.driver.fixture.keygen;

import lombok.Getter;
import org.apache.shardingsphere.infra.algorithm.core.context.AlgorithmSQLContext;
import org.apache.shardingsphere.infra.algorithm.keygen.spi.KeyGenerateAlgorithm;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class ResetIncrementKeyGenerateAlgorithmFixture implements KeyGenerateAlgorithm {
    
    @Getter
    private static final AtomicInteger COUNT = new AtomicInteger();
    
    @Override
    public Collection<Comparable<?>> generateKeys(final AlgorithmSQLContext context, final int keyGenerateCount) {
        return IntStream.range(0, keyGenerateCount).mapToObj(each -> COUNT.incrementAndGet()).collect(Collectors.toList());
    }
    
    @Override
    public String getType() {
        return "JDBC.RESET_INCREMENT.FIXTURE";
    }
    
    @Override
    public boolean isSupportAutoIncrement() {
        return true;
    }
}
