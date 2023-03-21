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

package org.apache.shardingsphere.test.e2e.engine.arg;

import org.apache.shardingsphere.test.e2e.cases.SQLCommandType;
import org.apache.shardingsphere.test.e2e.framework.E2EExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * E2E test case settings.
 */
@ExtendWith(E2EExtension.class)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface E2ETestCaseSettings {
    
    /**
     * Get SQL command type.
     *
     * @return SQL command type
     */
    SQLCommandType value();
    
    /**
     * Is batch execute.
     * 
     * @return batch execute or not
     */
    boolean batch() default false;
}
