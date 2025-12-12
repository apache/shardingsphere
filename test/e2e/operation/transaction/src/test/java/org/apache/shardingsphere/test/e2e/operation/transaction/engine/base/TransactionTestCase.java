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

package org.apache.shardingsphere.test.e2e.operation.transaction.engine.base;

import org.apache.shardingsphere.transaction.api.TransactionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * It represents a class to be tested for transaction integration.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TransactionTestCase {
    
    /**
     * Specifies which databases do the case run on.
     *
     * @return database types
     */
    String[] dbTypes() default {"MySQL", "PostgreSQL", "OpenGauss"};
    
    /**
     * Specifies which adapters do the case run on.
     *
     * @return run adapters
     */
    String[] adapters() default {"jdbc", "proxy"};
    
    /**
     * Specifies which transaction types do the case run on.
     *
     * @return transaction types
     */
    TransactionType[] transactionTypes() default {TransactionType.LOCAL, TransactionType.XA, TransactionType.BASE};
    
    /**
     * Specifies which scenario the test case belongs to.
     *
     * @return test group
     */
    String scenario() default "default";
}
