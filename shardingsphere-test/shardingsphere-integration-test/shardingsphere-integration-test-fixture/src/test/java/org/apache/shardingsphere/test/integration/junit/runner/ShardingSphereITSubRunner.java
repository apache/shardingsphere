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

package org.apache.shardingsphere.test.integration.junit.runner;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.integration.common.SQLExecuteType;
import org.apache.shardingsphere.test.integration.junit.annotation.ShardingSphereITInject;
import org.apache.shardingsphere.test.integration.junit.resolver.ConditionResolver;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.lang.reflect.Field;

@Slf4j
public class ShardingSphereITSubRunner extends BlockJUnit4ClassRunner {
    
    private final TestCaseBeanContext context;
    
    @NonNull
    private final ConditionResolver resolver;
    
    public ShardingSphereITSubRunner(final Class<?> testClass, final TestCaseBeanContext context, final ConditionResolver resolver) throws InitializationError {
        super(testClass);
        this.context = context;
        this.resolver = resolver;
    }
    
    @Override
    protected Object createTest() throws Exception {
        final Object testInstance = super.createTest();
        getTestClass().getAnnotatedFields(ShardingSphereITInject.class)
                .forEach(e -> {
                    try {
                        Field field = e.getField();
                        field.setAccessible(true);
                        if (field.getType() == String.class) {
                            field.set(testInstance, context.getBeanByName(field.getName()));
                        } else {
                            field.set(testInstance, context.getBean(e.getType()));
                        }
                    } catch (IllegalAccessException ex) {
                        throw new RuntimeException(ex.getMessage(), ex);
                    }
                });
        return testInstance;
    }

    @Override
    protected Statement withBeforeClasses(final Statement statement) {
        // skip @BeforeClass
        return statement;
    }
    
    @Override
    protected Statement withAfterClasses(final Statement statement) {
        // skip @AfterClass
        return statement;
    }
    
    @Override
    protected String getName() {
        // {2}: {3} -> {4} -> {5} -> {6}
        TestCaseDescription description = context.getBean(TestCaseDescription.class);
        return String.format("[%s: %s -> %s -> %s -> %s]",
                description.getAdapter(),
                description.getScenario(),
                description.getDatabase(),
                context.getBean(SQLExecuteType.class),
                context.getBeanByName("statement")
        );
    }
    
}
