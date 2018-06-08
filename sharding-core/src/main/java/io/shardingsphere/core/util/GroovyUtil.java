/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.util;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Groovy util.
 * 
 * @author Pramy
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GroovyUtil {
    
    private static final Map<String, Script> SCRIPTS = new ConcurrentHashMap<>();
    
    private static final GroovyShell SHELL = new GroovyShell();
    
    /**
     * evaluate expression.
     * 
     * @param expression expression
     * @return result of expression execution
     */
    public static Object evaluate(final String expression) {
        Script script;
        if (SCRIPTS.containsKey(expression)) {
            script = SCRIPTS.get(expression);
        } else {
            script = SHELL.parse(expression);
            SCRIPTS.put(expression, script);
        }
        return script.run();
    }
}
