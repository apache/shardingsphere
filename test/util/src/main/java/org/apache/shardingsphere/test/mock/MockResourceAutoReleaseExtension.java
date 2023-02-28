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

package org.apache.shardingsphere.test.mock;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.MockedStatic;

import java.util.Collection;
import java.util.LinkedList;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mockStatic;

/**
 * Mock resource auto release extension.
 */
public final class MockResourceAutoReleaseExtension implements BeforeEachCallback, AfterEachCallback {
    
    private final Collection<MockedStatic<?>> mockedStatics = new LinkedList<>();
    
    @Override
    public void beforeEach(final ExtensionContext context) {
        StaticMockSettings staticMockSettings = context.getRequiredTestClass().getAnnotation(StaticMockSettings.class);
        if (null != staticMockSettings) {
            for (Class<?> each : staticMockSettings.value()) {
                mockedStatics.add(mockStatic(each, RETURNS_DEEP_STUBS));
            }
        }
    }
    
    @Override
    public void afterEach(final ExtensionContext context) {
        for (MockedStatic<?> each : mockedStatics) {
            each.close();
        }
        mockedStatics.clear();
    }
}
