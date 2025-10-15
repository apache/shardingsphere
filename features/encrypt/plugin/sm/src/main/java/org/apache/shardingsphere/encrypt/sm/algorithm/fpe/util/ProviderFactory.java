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

package org.apache.shardingsphere.encrypt.sm.algorithm.fpe.util;

import java.security.Provider;

/**
 * Provider对象生产工厂类
 *
 * <pre>
 * 1. 调用{@link #createBouncyCastleProvider()} 用于新建一个org.bouncycastle.jce.provider.BouncyCastleProvider对象
 * </pre>
 *
 * @author looly
 * @since 4.2.1
 */
public class ProviderFactory {
    
    /**
     * 创建Bouncy Castle 提供者<br>
     * 如果用户未引入bouncycastle库，则此方法抛出{@link NoClassDefFoundError} 异常
     *
     * @return {@link Provider}
     */
    public static Provider createBouncyCastleProvider() {
        return new org.bouncycastle.jce.provider.BouncyCastleProvider();
    }
}
