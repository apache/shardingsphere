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
 *
 */

package org.apache.shardingsphere.agent.core.plugin;

import com.google.common.collect.Lists;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.shardingsphere.agent.core.plugin.point.ClassStaticMethodPoint;
import org.apache.shardingsphere.agent.core.plugin.point.ConstructorPoint;
import org.apache.shardingsphere.agent.core.plugin.point.InstanceMethodPoint;

import java.util.List;

/**
 * The advice of plugin configurer.
 *
 * <code>
 * PluginAdviceDefine.intercept("Target.class")
 * .onConstructor(ElementMatchers.any()).implement("Advice.class").build()
 * .method(ElementMatchers.named("greet").implement("Advice.class").build()
 * .staticMethod(ElementMatchers.named("of").implement("OfAdvice.class").build()
 * .install();
 * </code>
 */
public final class PluginAdviceDefine {

    private final List<ConstructorPoint> constructorPoints;

    private final List<InstanceMethodPoint> instanceMethodPoints;

    private final List<ClassStaticMethodPoint> classStaticMethodPoints;

    private PluginAdviceDefine(final List<ConstructorPoint> constructorPoints,
                               final List<InstanceMethodPoint> instanceMethodPoints,
                               final List<ClassStaticMethodPoint> classStaticMethodPoints) {
        this.constructorPoints = constructorPoints;
        this.instanceMethodPoints = instanceMethodPoints;
        this.classStaticMethodPoints = classStaticMethodPoints;
    }

    /**
     * Intercept target class.
     * @param classNameOfTarget a class name of wanted advice target.
     * @return Configurer.
     */
    public static Builder intercept(final String classNameOfTarget) {
        return new Builder(classNameOfTarget);
    }

    /**
     * To get static method point configurations.
     *
     * @return a series of static method point configuration.
     */
    public List<ClassStaticMethodPoint> getClassStaticMethodPoints() {
        return classStaticMethodPoints;
    }

    /**
     * To get constructor point configurations.
     *
     * @return a series of constructor point configuration.
     */
    public List<ConstructorPoint> getConstructorPoints() {
        return constructorPoints;
    }

    /**
     * To get instance point configurations.
     *
     * @return a series of instance method point configuration.
     */
    public List<InstanceMethodPoint> getInstanceMethodPoints() {
        return instanceMethodPoints;
    }

    /**
     * Plugin advice configuration builder.
     */
    public static final class Builder {
        private final List<ConstructorPoint> constructorPoints = Lists.newArrayList();
        
        private final List<InstanceMethodPoint> instanceMethodPoints = Lists.newArrayList();
        
        private final List<ClassStaticMethodPoint> classStaticMethodPoints = Lists.newArrayList();
        
        private final String classNameOfTarget;

        private Builder(final String classNameOfTarget) {
            this.classNameOfTarget = classNameOfTarget;
        }

        /**
         * Intercept the new target.
         * @param classNameOfTarget the class name of target.
         * @return Configuration builder.
         */
        public Builder intercept(final String classNameOfTarget) {
            // TODO not-implemented yet
            return this;
        }

        /**
         * to configure the intercepting point on constructor.
         * @param matcher constraints
         * @return Configuration builder
         */
        public ConstructorPointBuilder onConstructor(final ElementMatcher<? super MethodDescription> matcher) {
            return new ConstructorPointBuilder(this, matcher);
        }

        /**
         * to configure the intercepting point around instance method.
         * @param matcher constraints
         * @return Configuration builder
         */
        public InstanceMethodPointBuilder method(final ElementMatcher<? super MethodDescription> matcher) {
            return new InstanceMethodPointBuilder(this, matcher);
        }

        /**
         * to configure the intercepting point around instance method.
         * @param matcher constraints
         * @return Configuration builder
         */
        public StaticMethodPointBuilder staticMethod(final ElementMatcher<? super MethodDescription> matcher) {
            return new StaticMethodPointBuilder(this, matcher);
        }


        /**
         * build configuration.
         * @return Plugin advice definition.
         */
        public PluginAdviceDefine install() {
            return new PluginAdviceDefine(constructorPoints, instanceMethodPoints, classStaticMethodPoints);
        }

        /**
         * Instance method intercepting point configuration builder.
         */
        public static final class InstanceMethodPointBuilder {
            private final Builder builder;

            private String classNameOfAdvice;
            
            private boolean overrideArgs;
            
            private ElementMatcher<? super MethodDescription> matcher;

            private InstanceMethodPointBuilder(final Builder builder, final ElementMatcher<? super MethodDescription> matcher) {
                this.builder = builder;
                this.matcher = matcher;
            }

            /**
             * to configure implementation for intercepting point.
             * @param classNameOfAdvice the class name of advice
             * @return Instance method point configurer.
             */
            public InstanceMethodPointBuilder implement(final String classNameOfAdvice) {
                this.classNameOfAdvice = classNameOfAdvice;
                return this;
            }

            /**
             * to configure whether or not override the origin method arguments.
             * @param overrideArgs whether to override origin method arguments.
             * @return Instance method point configurer.
             */
            public InstanceMethodPointBuilder overrideArgs(final boolean overrideArgs) {
                this.overrideArgs = overrideArgs;
                return this;
            }

            /**
             * to build instance methods configuration.
             * @return Plugin advice builder.
             */
            public Builder build() {
                builder.instanceMethodPoints.add(new InstanceMethodPoint(matcher, classNameOfAdvice, overrideArgs));
                return builder;
            }
        }

        /**
         * Static method intercepting point configuration builder.
         */
        public static final class StaticMethodPointBuilder {
            private final Builder builder;
            
            private String classNameOfAdvice;
            
            private boolean overrideArgs;
            
            private ElementMatcher<? super MethodDescription> matcher;

            private StaticMethodPointBuilder(final Builder builder, final ElementMatcher<? super MethodDescription> matcher) {
                this.builder = builder;
                this.matcher = ElementMatchers.isStatic().and(matcher);
            }

            /**
             * to configure implementation for intercepting point.
             * @param classNameOfAdvice the class name of advice
             * @return Static method point configurer.
             */
            public StaticMethodPointBuilder implement(final String classNameOfAdvice) {
                this.classNameOfAdvice = classNameOfAdvice;
                return this;
            }

            /**
             * to configure whether or not override the origin method arguments.
             * @param overrideArgs whether to override origin method arguments.
             * @return Static method point configurer.
             */
            public StaticMethodPointBuilder overrideArgs(final boolean overrideArgs) {
                this.overrideArgs = overrideArgs;
                return this;
            }

            /**
             * to build static methods configuration.
             * @return Plugin advice builder.
             */
            public Builder build() {
                builder.classStaticMethodPoints.add(new ClassStaticMethodPoint(matcher, classNameOfAdvice, overrideArgs));
                return builder;
            }

        }

        /**
         * Instance constructor intercepting point configuration builder.
         */
        public static final class ConstructorPointBuilder {
            private final Builder builder;

            private String classNameOfAdvice;
            
            private ElementMatcher<? super MethodDescription> matcher;

            private ConstructorPointBuilder(final Builder builder, final ElementMatcher<? super MethodDescription> matcher) {
                this.builder = builder;
                this.matcher = ElementMatchers.isConstructor().and(matcher);
            }

            /**
             * to configure implementation for intercepting point.
             * @param classNameOfAdvice the class name of advice
             * @return Constructor point configurer.
             */
            public ConstructorPointBuilder implement(final String classNameOfAdvice) {
                this.classNameOfAdvice = classNameOfAdvice;
                return this;
            }

            /**
             * to build constructor point configuration.
             * @return Plugin advice builder.
             */
            public Builder build() {
                builder.constructorPoints.add(new ConstructorPoint(matcher, classNameOfAdvice));
                return builder;
            }
        }
    }
}
