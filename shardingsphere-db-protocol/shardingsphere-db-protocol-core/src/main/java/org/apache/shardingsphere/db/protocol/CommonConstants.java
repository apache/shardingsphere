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

package org.apache.shardingsphere.db.protocol;

import io.netty.util.AttributeKey;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Common constants for protocol.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommonConstants {
    
    public static final AttributeKey<Charset> CHARSET_ATTRIBUTE_KEY = AttributeKey.valueOf(Charset.class.getName());
    
    public static final AtomicReference<String> PROXY_VERSION = new AtomicReference<>();
}
