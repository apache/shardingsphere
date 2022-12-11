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

package org.apache.shardingsphere.agent.api.interceptor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Plugin interceptor point.
 *
 * {@code
 * PluginInterceptorPoint.intercept("Target.class")
 * .onConstructor(ElementMatchers.any()).implement("Advice.class").build()
 * .method(ElementMatchers.named("greet").implement("Advice.class").build()
 * .staticMethod(ElementMatchers.named("of").implement("OfAdvice.class").build()
 * .install();
 * }
 */
@RequiredArgsConstructor
@Getter
public final class PluginInterceptorPoint {
    
    private final String targetClassName;
    
    private final List<ConstructorInterceptorPoint> constructorInterceptorPoints;
    
    private final List<InstanceMethodInterceptorPoint> instanceMethodInterceptorPoints;
    
    private final List<StaticMethodInterceptorPoint> staticMethodInterceptorPoints;
    
    /**
     * Create plugin interceptor point.
     *
     * @return plugin interceptor point
     */
    public static PluginInterceptorPoint createDefault() {
        return new PluginInterceptorPoint("", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }
    
    /**
     * Intercept target class.
     *
     * @param targetClassName target class name
     * @return builder
     */
    public static Builder intercept(final String targetClassName) {
        return new Builder(targetClassName);
    }
    
    /**
     * Plugin advice definition configuration builder.
     */
    @RequiredArgsConstructor
    public static final class Builder {
        
        private final String targetClassName;
        
        private final List<ConstructorInterceptorPoint> constructorInterceptorPoints = new ArrayList<>();
        
        private final List<InstanceMethodInterceptorPoint> instanceMethodInterceptorPoints = new ArrayList<>();
        
        private final List<StaticMethodInterceptorPoint> staticMethodInterceptorPoints = new ArrayList<>();
        
        /**
         * Configure the intercepting point on constructor.
         *
         * @param matcher constraints
         * @return configuration point builder
         */
        public ConstructorPointBuilder onConstructor(final ElementMatcher<? super MethodDescription> matcher) {
            return new ConstructorPointBuilder(this, matcher);
        }
        
        /**
         * Configure the intercepting point around instance method.
         *
         * @param matcher constraints
         * @return instance method point builder
         */
        public InstanceMethodPointBuilder aroundInstanceMethod(final ElementMatcher<? super MethodDescription> matcher) {
            return new InstanceMethodPointBuilder(this, matcher);
        }
        
        /**
         * Configure the intercepting point around instance method.
         *
         * @param matcher constraints
         * @return static method point builder
         */
        public StaticMethodPointBuilder aroundStaticMethod(final ElementMatcher<? super MethodDescription> matcher) {
            return new StaticMethodPointBuilder(this, matcher);
        }
        
        /**
         * Build plugin advice definition.
         *
         * @return plugin advice definition
         */
        public PluginInterceptorPoint install() {
            return new PluginInterceptorPoint(targetClassName, constructorInterceptorPoints, instanceMethodInterceptorPoints, staticMethodInterceptorPoints);
        }
        
        /**
         * Instance method intercepting point configuration builder.
         */
        @RequiredArgsConstructor
        public static final class InstanceMethodPointBuilder {
            
            private final Builder builder;
            
            private final ElementMatcher<? super MethodDescription> matcher;
            
            private String adviceClassName;
            
            private boolean overrideArgs;
            
            /**
             * Configure implementation for interceptor point.
             *
             * @param adviceClassName advice class name
             * @return instance method point builder
             */
            public InstanceMethodPointBuilder implement(final String adviceClassName) {
                this.adviceClassName = adviceClassName;
                return this;
            }
            
            /**
             * Configure whether or not override the origin method arguments.
             *
             * @param overrideArgs whether to override origin method arguments
             * @return instance method point configurer
             */
            public InstanceMethodPointBuilder overrideArgs(final boolean overrideArgs) {
                this.overrideArgs = overrideArgs;
                return this;
            }
            
            /**
             * Build instance methods configuration.
             *
             * @return plugin advice builder
             */
            public Builder build() {
                builder.instanceMethodInterceptorPoints.add(new InstanceMethodInterceptorPoint(matcher, adviceClassName, overrideArgs));
                return builder;
            }
        }
        
        /**
         * Static method intercepting point configuration builder.
         */
        public static final class StaticMethodPointBuilder {
            
            private final Builder builder;
            
            private final ElementMatcher<? super MethodDescription> matcher;
            
            private String adviceClassName;
            
            private boolean overrideArgs;
            
            private StaticMethodPointBuilder(final Builder builder, final ElementMatcher<? super MethodDescription> matcher) {
                this.builder = builder;
                this.matcher = ElementMatchers.isStatic().and(matcher);
            }
            
            /**
             * Configure implementation for intercepting point.
             *
             * @param adviceClassName advice class name
             * @return static method point configure builder
             */
            public StaticMethodPointBuilder implement(final String adviceClassName) {
                this.adviceClassName = adviceClassName;
                return this;
            }
            
            /**
             * Configure whether or not override the origin method arguments.
             *
             * @param overrideArgs whether to override origin method arguments
             * @return static method point builder
             */
            public StaticMethodPointBuilder overrideArgs(final boolean overrideArgs) {
                this.overrideArgs = overrideArgs;
                return this;
            }
            
            /**
             * Build static methods configuration.
             *
             * @return builder
             */
            public Builder build() {
                builder.staticMethodInterceptorPoints.add(new StaticMethodInterceptorPoint(matcher, adviceClassName, overrideArgs));
                return builder;
            }
        }
        
        /**
         * Instance constructor intercepting point configuration builder.
         */
        public static final class ConstructorPointBuilder {
            
            private final Builder builder;
            
            private final ElementMatcher<? super MethodDescription> matcher;
            
            private String adviceClassName;
            
            private ConstructorPointBuilder(final Builder builder, final ElementMatcher<? super MethodDescription> matcher) {
                this.builder = builder;
                this.matcher = ElementMatchers.isConstructor().and(matcher);
            }
            
            /**
             * Configure implementation for intercepting point.
             *
             * @param adviceClassName advice class name
             * @return constructor point builder
             */
            public ConstructorPointBuilder implement(final String adviceClassName) {
                this.adviceClassName = adviceClassName;
                return this;
            }
            
            /**
             * Build constructor point configuration.
             *
             * @return plugin advice builder
             */
            public Builder build() {
                builder.constructorInterceptorPoints.add(new ConstructorInterceptorPoint(matcher, adviceClassName));
                return builder;
            }
        }
    }
}
