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

package io.shardingsphere.orchestration.internal.yaml.representer;

import com.google.common.collect.Sets;
import lombok.SneakyThrows;
import org.yaml.snakeyaml.introspector.Property;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Data source representer.
 *
 * @author panjuan
 */
public final class DataSourceRepresenter extends DefaultRepresenter {
    
    private static final Collection<Class<?>> GENERAL_CLASS_TYPE;
    
    private static final Collection<String> SKIPPED_PROPERTY_NAMES;
    
    private final Collection<String> propertyNames;
    
    static {
        GENERAL_CLASS_TYPE = Sets.<Class<?>>newHashSet(boolean.class, Boolean.class, int.class, Integer.class, long.class, Long.class, String.class);
        SKIPPED_PROPERTY_NAMES = Sets.newHashSet("loginTimeout");
    }
    
    public DataSourceRepresenter(final Class<?> clazz) {
        super();
        propertyNames = getPropertyNames(clazz);
    }
    
    private Set<String> getPropertyNames(final Class<?> clazz) {
        Method[] methods = clazz.getMethods();
        Set<String> getterMethodNames = new LinkedHashSet<>();
        Set<String> setterMethodNames = new LinkedHashSet<>();
        for (Method each : methods) {
            if (isGetterMethod(each)) {
                getterMethodNames.add(getPropertyName(each));
            } else if (isSetterMethod(each)) {
                setterMethodNames.add(getPropertyName(each));
            }
        }
        return getPairedGetterMethodNames(getterMethodNames, setterMethodNames);
    }
    
    private Set<String> getPairedGetterMethodNames(final Set<String> getterMethodNames, final Set<String> setterMethodNames) {
        Set<String> result = new LinkedHashSet<>();
        for (String each : getterMethodNames) {
            if (setterMethodNames.contains(each) && !SKIPPED_PROPERTY_NAMES.contains(each)) {
                result.add(each);
            }
        }
        return result;
    }
    
    private String getPropertyName(final Method method) {
        return String.valueOf(method.getName().charAt(3)).toLowerCase() + method.getName().substring(4, method.getName().length());
    }
    
    private boolean isGetterMethod(final Method method) {
        return method.getName().startsWith("get") && 0 == method.getParameterTypes().length && isGeneralClassType(method.getReturnType());
    }
    
    private boolean isSetterMethod(final Method method) {
        return method.getName().startsWith("set") && 1 == method.getParameterTypes().length && isGeneralClassType(method.getParameterTypes()[0]) && isVoid(method.getReturnType());
    }
    
    private boolean isGeneralClassType(final Class<?> clazz) {
        return GENERAL_CLASS_TYPE.contains(clazz);
    }
    
    private boolean isVoid(final Class<?> clazz) {
        return void.class == clazz || Void.class == clazz;
    }
    
    @SneakyThrows
    @Override
    protected Set<Property> getProperties(final Class<?> type) {
        Set<Property> result = new LinkedHashSet<>(propertyNames.size(), 1);
        for (Property each : super.getProperties(type)) {
            if (propertyNames.contains(each.getName())) {
                result.add(each);
            }
        }
        return result;
    }
}
