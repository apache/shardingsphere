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

package org.apache.shardingsphere.test.e2e.data.pipeline.framework;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import java.lang.reflect.Method;

/**
 * Pipeline E2E extension.
 */
@Slf4j
public final class PipelineE2EExtension implements BeforeEachCallback, TestWatcher {
    
    @Override
    public void beforeEach(final ExtensionContext context) throws Exception {
        log.info("Before case: {}.{}({})", context.getTestClass().map(Class::getSimpleName).orElse(""),
                context.getTestMethod().map(Method::getName).orElse(""), context.getDisplayName());
    }
    
    @Override
    public void testFailed(final ExtensionContext context, final Throwable cause) {
        log.error("Error case: {}.{}({})", context.getTestClass().map(Class::getSimpleName).orElse(""),
                context.getTestMethod().map(Method::getName).orElse(""), context.getDisplayName(), cause);
    }
}
