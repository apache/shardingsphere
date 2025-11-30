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

package org.apache.shardingsphere.test.it.sql.parser.external;

import org.apache.shardingsphere.test.it.sql.parser.external.loader.template.ExternalTestParameterLoadTemplate;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * External SQL case test settings.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface ExternalCaseSettings {
    
    /**
     * Get to be tested database types.
     *
     * @return to be tested database types
     */
    String value();
    
    /**
     * Get test case URL.
     *
     * @return test case URL
     */
    String caseURL();
    
    /**
     * Get test case result URL.
     *
     * @return test case result URL
     */
    String resultURL();
    
    /**
     * Report type.
     *
     * @return get report type
     */
    String reportType() default "CSV";
    
    /**
     * Get test parameter load template.
     *
     * @return test parameter load template
     */
    Class<? extends ExternalTestParameterLoadTemplate> template();
}
