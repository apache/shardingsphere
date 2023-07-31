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

package org.apache.shardingsphere.infra.expr.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.expr.spi.InlineExpressionParser;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

/**
 * Inline expression parser factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InlineExpressionParserFactory {
    
    // workaround for https://junit.org/junit5/docs/current/api/org.junit.jupiter.api/org/junit/jupiter/api/condition/EnabledInNativeImage.html
    private static final boolean IS_SUBSTRATE_VM = "runtime".equals(System.getProperty("org.graalvm.nativeimage.imagecode"));
    
    /**
     * Create new instance of inline expression parser.
     * 
     * @return created instance
     */
    public static InlineExpressionParser newInstance() {
        return TypedSPILoader.getService(InlineExpressionParser.class, IS_SUBSTRATE_VM ? "ESPRESSO" : "HOTSPOT");
    }
}
