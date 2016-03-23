/**
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.config.common.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import groovy.lang.GString;
import groovy.lang.GroovyShell;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 配置文件工具类.
 * 
 * @author gaohongtao
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigUtil {
    
    public static List<String> transformCommaStringToList(final String strWithComma) {
        final GroovyShell shell = new GroovyShell();
        return flattenList(Lists.transform(splitComma(strWithComma), new Function<String, Object>() {
            @Override
            public Object apply(final String input) {
                String compactInput = input.trim();
                StringBuilder expression = new StringBuilder(compactInput);
                if (!compactInput.startsWith("\"")) {
                    expression.insert(0, "\"");
                }
                if (!compactInput.endsWith("\"")) {
                    expression.append("\"");
                }
                return shell.evaluate(expression.toString());
            }
        }));
    }
    
    private static List<String> flattenList(final List list) {
        List<String> result = new ArrayList<>();
        for (Object each : list) {
            if (each instanceof GString) {
                result.addAll(transformGStringToList((GString) each));
            } else {
                result.add(each.toString());
            }
        }
        return result;
    }
    
    private static List<String> splitComma(final String strWithComma) {
        List<String> result = new ArrayList<>();
        int offset = -1;
        StringBuilder token = new StringBuilder();
        int closureDepth = 0;
        boolean isStartMatch = false;
        while (++offset < strWithComma.length()) {
            char c = strWithComma.charAt(offset);
            switch (c) {
                case ',':
                    if (closureDepth > 0) {
                        token.append(c);
                    } else {
                        result.add(token.toString());
                        token.setLength(0);
                    }
                    break;
                case '$':
                    isStartMatch = true;
                    token.append(c);
                    break;
                case '{':
                    closureDepth = isStartMatch ? closureDepth + 1 : closureDepth;
                    isStartMatch = false;
                    token.append(c);
                    break;
                case '}':
                    closureDepth--;
                    isStartMatch = false;
                    token.append(c);
                    break;
                default:
                    token.append(c);
                    break;
            }
        }
        if (token.length() > 0) {
            result.add(token.toString());
        }
        return result;
    }
    
    private static List<String> transformGStringToList(final GString gString) {
        String[] strings = gString.getStrings();
        Set<List<String>> valueScenario = getValueScenario(gString);
        List<String> result = new ArrayList<>(valueScenario.size());
        for (List<String> each : valueScenario) {
            StringBuilder stringItemBuilder = new StringBuilder();
            int numberOfValues = each.size();
            for (int i = 0, size = strings.length; i < size; i++) {
                stringItemBuilder.append(strings[i]);
                if (i < numberOfValues) {
                    stringItemBuilder.append(each.get(i));
                }
            }
            result.add(stringItemBuilder.toString());
        }
        return result;
    }
    
    @SuppressWarnings(value = "unchecked")
    private static Set<List<String>> getValueScenario(final GString gString) {
        List<Set<String>> dimValue = new ArrayList<>(gString.getValues().length);
        for (Object each : gString.getValues()) {
            if (null == each) {
                continue;
            }
            if (each instanceof Collection) {
                dimValue.add(Sets.newHashSet(Collections2.transform((Collection<Object>) each, new Function<Object, String>() {
                    @Override
                    public String apply(final Object input) {
                        return input.toString();
                    }
                })));
            } else {
                dimValue.add(Sets.newHashSet(each.toString()));
            }
        }
        return Sets.cartesianProduct(dimValue);
    }
}
