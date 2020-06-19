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

package org.apache.shardingsphere.sharding.spring.boot.fixture;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Resource;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.springframework.core.env.Environment;

public final class IncrementKeyGenerateAlgorithmFixture implements KeyGenerateAlgorithm {
    
    private final AtomicInteger count = new AtomicInteger();
    
    @Getter
    @Setter
    private Properties props = new Properties();
    
    @Resource
    @Getter
    private Environment environment;
    
    @Override
    public void init() {
    }
    
    @Override
    public Comparable<?> generateKey() {
        return count.incrementAndGet();
    }
    
    @Override
    public String getType() {
        return "EXTEND_INCREMENT";
    }
}
