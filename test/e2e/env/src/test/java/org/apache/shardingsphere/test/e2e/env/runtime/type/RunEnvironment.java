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

package org.apache.shardingsphere.test.e2e.env.runtime.type;

import com.google.common.base.Strings;
import lombok.Getter;

import java.util.Properties;

/**
 * Run environment.
 */
@Getter
public final class RunEnvironment {
    
    private final Type type;
    
    private final boolean isRunAdditionalCases;
    
    private final boolean isRunSmokeCases;
    
    public RunEnvironment(final Properties props) {
        type = getType(props);
        isRunAdditionalCases = Boolean.parseBoolean(props.getProperty("e2e.run.additional.cases", Boolean.FALSE.toString()));
        isRunSmokeCases = Boolean.parseBoolean(props.getProperty("e2e.run.smoke.cases", Boolean.FALSE.toString()));
    }
    
    private Type getType(final Properties props) {
        String value = props.getProperty("e2e.run.type");
        return Strings.isNullOrEmpty(value) ? null : Type.valueOf(value.toUpperCase());
    }
    
    /**
     * Run type.
     */
    public enum Type {
        
        DOCKER, NATIVE
    }
}
