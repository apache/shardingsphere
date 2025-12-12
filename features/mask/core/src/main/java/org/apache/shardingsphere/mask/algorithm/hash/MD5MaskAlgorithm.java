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

package org.apache.shardingsphere.mask.algorithm.hash;

import org.apache.shardingsphere.infra.algorithm.messagedigest.spi.MessageDigestAlgorithm;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;

import java.util.Properties;

/**
 * MD5 mask algorithm.
 */
public final class MD5MaskAlgorithm implements MaskAlgorithm<Object, String> {
    
    private MessageDigestAlgorithm digestAlgorithm;
    
    @Override
    public void init(final Properties props) {
        digestAlgorithm = TypedSPILoader.getService(MessageDigestAlgorithm.class, getType(), props);
    }
    
    @HighFrequencyInvocation
    @Override
    public String mask(final Object plainValue) {
        return digestAlgorithm.digest(plainValue);
    }
    
    @Override
    public String getType() {
        return "MD5";
    }
}
