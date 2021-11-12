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

package org.apache.shardingsphere.shadow.algorithm.shadow.validator.column;

import org.apache.shardingsphere.shadow.algorithm.shadow.ShadowAlgorithmException;
import org.apache.shardingsphere.shadow.algorithm.shadow.validator.ShadowValueValidator;

import java.util.Date;

/**
 * Shadow value validator of date type.
 */
public final class ShadowDateValueValidator implements ShadowValueValidator {
    
    @Override
    public void preValidate(final String table, final String column, final Comparable<?> shadowValue) {
        if (shadowValue instanceof Date) {
            throw new ShadowAlgorithmException("Shadow column `%s` data of shadow table `%s` matching does not support type: `%s`.", column, table, Date.class);
        }
    }
}
