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

package org.apache.shardingsphere.test.integration.engine.junit;

import lombok.Getter;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.parameterized.TestWithParameters;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

/**
 * A {@link BlockJUnit4ClassRunner} with parameters support. Parameters can be
 * injected via constructor or into annotated fields.
 */
public final class ITBlockJUnit4ClassRunnerWithParameters extends BlockJUnit4ClassRunner {
    
    private final String name;
    
    @Getter
    private final Object[] parameters;
    
    public ITBlockJUnit4ClassRunnerWithParameters(final TestWithParameters test) throws InitializationError {
        super(test.getTestClass().getJavaClass());
        name = test.getName();
        parameters = test.getParameters().toArray(new Object[0]);
    }
    
    @Override
    public Object createTest() throws Exception {
        if (fieldsAreAnnotated()) {
            return createTestUsingFieldInjection();
        } else {
            return createTestUsingConstructorInjection();
        }
    }
    
    private Object createTestUsingConstructorInjection() throws ReflectiveOperationException {
        return getTestClass().getOnlyConstructor().newInstance(parameters);
    }
    
    private Object createTestUsingFieldInjection() throws Exception {
        List<FrameworkField> annotatedFieldsByParameter = getAnnotatedFieldsByParameter();
        if (annotatedFieldsByParameter.size() != parameters.length) {
            throw new Exception(
                    "Wrong number of parameters and @Parameter fields."
                            + " @Parameter fields counted: "
                            + annotatedFieldsByParameter.size()
                            + ", available parameters: " + parameters.length
                            + ".");
        }
        Object testClassInstance = getTestClass().getJavaClass().newInstance();
        for (FrameworkField each : annotatedFieldsByParameter) {
            Field field = each.getField();
            Parameter annotation = field.getAnnotation(Parameter.class);
            int index = annotation.value();
            try {
                field.set(testClassInstance, parameters[index]);
            } catch (IllegalArgumentException iare) {
                throw new Exception(getTestClass().getName()
                        + ": Trying to set " + field.getName()
                        + " with the value " + parameters[index]
                        + " that is not the right type ("
                        + parameters[index].getClass().getSimpleName()
                        + " instead of " + field.getType().getSimpleName()
                        + ").", iare);
            }
        }
        return testClassInstance;
    }
    
    @Override
    protected String getName() {
        return name;
    }
    
    @Override
    protected String testName(final FrameworkMethod method) {
        return method.getName() + name;
    }
    
    @Override
    protected void validateConstructor(final List<Throwable> errors) {
        validateOnlyOneConstructor(errors);
        if (fieldsAreAnnotated()) {
            validateZeroArgConstructor(errors);
        }
    }
    
    @Override
    protected void validateFields(final List<Throwable> errors) {
        super.validateFields(errors);
        if (fieldsAreAnnotated()) {
            List<FrameworkField> annotatedFieldsByParameter = getAnnotatedFieldsByParameter();
            int[] usedIndices = new int[annotatedFieldsByParameter.size()];
            for (FrameworkField each : annotatedFieldsByParameter) {
                int index = each.getField().getAnnotation(Parameter.class)
                        .value();
                if (index < 0 || index > annotatedFieldsByParameter.size() - 1) {
                    errors.add(new Exception("Invalid @Parameter value: "
                            + index + ". @Parameter fields counted: "
                            + annotatedFieldsByParameter.size()
                            + ". Please use an index between 0 and "
                            + (annotatedFieldsByParameter.size() - 1) + "."));
                } else {
                    usedIndices[index]++;
                }
            }
            for (int index = 0; index < usedIndices.length; index++) {
                int numberOfUse = usedIndices[index];
                if (numberOfUse == 0) {
                    errors.add(new Exception("@Parameter(" + index + ") is never used."));
                } else if (numberOfUse > 1) {
                    errors.add(new Exception("@Parameter(" + index + ") is used more than once (" + numberOfUse + ")."));
                }
            }
        }
    }
    
    @Override
    protected Statement classBlock(final RunNotifier notifier) {
        return childrenInvoker(notifier);
    }
    
    @Override
    protected Annotation[] getRunnerAnnotations() {
        return new Annotation[0];
    }
    
    private List<FrameworkField> getAnnotatedFieldsByParameter() {
        return getTestClass().getAnnotatedFields(Parameter.class);
    }
    
    private boolean fieldsAreAnnotated() {
        return !getAnnotatedFieldsByParameter().isEmpty();
    }
}
