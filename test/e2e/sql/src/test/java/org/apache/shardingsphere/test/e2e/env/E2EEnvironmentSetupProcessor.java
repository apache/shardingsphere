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

package org.apache.shardingsphere.test.e2e.env;

import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.engine.arg.E2ETestCaseSettings;
import org.apache.shardingsphere.test.e2e.env.container.atomic.enums.AdapterMode;
import org.apache.shardingsphere.test.e2e.env.container.atomic.enums.AdapterType;
import org.apache.shardingsphere.test.e2e.env.runtime.E2ETestEnvironment;
import org.apache.shardingsphere.test.e2e.framework.param.array.E2ETestParameterFactory;
import org.apache.shardingsphere.test.e2e.framework.param.model.E2ETestParameter;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.lang.reflect.Method;

public class E2EEnvironmentSetupProcessor implements BeforeAllCallback, InvocationInterceptor {
    
    @Override
    public void beforeAll(final ExtensionContext context) {
        if (!E2ETestParameterFactory.containsTestParameter()) {
            return;
        }
        E2ETestEnvironment testEnvironment = E2ETestEnvironment.getInstance();
        E2ETestCaseSettings settings = context.getRequiredTestClass().getAnnotation(E2ETestCaseSettings.class);
        for (String scenario : testEnvironment.getScenarios()) {
            for (DatabaseType databaseType : testEnvironment.getClusterEnvironment().getDatabaseTypes()) {
                for (String adapterMode : testEnvironment.getRunModes()) {
                    for (String adapterType : testEnvironment.getClusterEnvironment().getAdapters()) {
                        // todo Remove the key originating from the historical code.
                        String key = String.join("-", settings.value().toString(), scenario, adapterType, databaseType.getType());
                        new E2EEnvironmentEngine(key, scenario, databaseType, AdapterMode.valueOf(adapterMode.toUpperCase()), AdapterType.valueOf(adapterType.toUpperCase()));
                    }
                }
            }
        }
    }
    
    @Override
    public void interceptTestTemplateMethod(final Invocation<Void> invocation, final ReflectiveInvocationContext<Method> invocationContext, final ExtensionContext extensionContext) throws Throwable {
        interceptTestMethod(invocation, invocationContext, extensionContext);
    }
    
    @Override
    public void interceptTestMethod(final Invocation<Void> invocation, final ReflectiveInvocationContext<Method> invocationContext, final ExtensionContext extensionContext) throws Throwable {
        if (!(extensionContext.getRequiredTestInstance() instanceof E2EEnvironmentAware)) {
            throw new UnsupportedOperationException("E2EEnvironmentAware is required.");
        }
        for (Object each : invocationContext.getArguments()) {
            if (each instanceof E2ETestParameter) {
                E2ETestParameter testParameter = (E2ETestParameter) each;
                // TODO make sure test case can not be null
                if (null == testParameter.getTestCaseContext()) {
                    break;
                }
                E2EEnvironmentEngine e2EEnvironmentEngine = new E2EEnvironmentEngine(testParameter.getKey(), testParameter.getScenario(), testParameter.getDatabaseType(),
                        AdapterMode.valueOf(testParameter.getMode().toUpperCase()), AdapterType.valueOf(testParameter.getAdapter().toUpperCase()));
                ((E2EEnvironmentAware) extensionContext.getRequiredTestInstance()).setEnvironmentEngine(e2EEnvironmentEngine);
            }
        }
        invocation.proceed();
    }
}
