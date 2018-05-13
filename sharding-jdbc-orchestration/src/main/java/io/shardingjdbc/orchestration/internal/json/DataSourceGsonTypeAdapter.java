/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.orchestration.internal.json;

import com.google.common.collect.Sets;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.shardingjdbc.core.exception.ShardingJdbcException;
import io.shardingjdbc.core.jdbc.core.datasource.NamedDataSource;
import io.shardingjdbc.core.util.DataSourceUtil;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Data source gson type adapter.
 *
 * @author zhangliang
 */
public final class DataSourceGsonTypeAdapter extends TypeAdapter<NamedDataSource> {
    
    private static Collection<Class<?>> generalClassType;
    
    static {
        generalClassType = Sets.<Class<?>>newHashSet(boolean.class, Boolean.class, int.class, Integer.class, long.class, Long.class, String.class);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public NamedDataSource read(final JsonReader in) throws IOException {
        String name = "";
        String clazz = "";
        Map<String, Object> properties = new TreeMap<>();
        in.beginObject();
        while (in.hasNext()) {
            String jsonName = in.nextName();
            if (DataSourceGsonTypeConstants.DATASOURCE_NAME.equals(jsonName)) {
                name = in.nextString();
            } else if (DataSourceGsonTypeConstants.CLAZZ_NAME.equals(jsonName)) {
                clazz = in.nextString();
            } else {
                properties.put(jsonName, in.nextString());
            }
        }
        in.endObject();
        try {
            return new NamedDataSource(name, DataSourceUtil.getDataSource(clazz, properties));
        } catch (final ReflectiveOperationException ex) {
            throw new ShardingJdbcException(ex);
        }
    }
    
    @Override
    public void write(final JsonWriter out, final NamedDataSource value) throws IOException {
        out.beginObject();
        out.name(DataSourceGsonTypeConstants.DATASOURCE_NAME).value(value.getName());
        out.name(DataSourceGsonTypeConstants.CLAZZ_NAME).value(value.getDataSource().getClass().getName());
        Method[] methods = value.getDataSource().getClass().getMethods();
        Map<String, Method> getterMethods = new TreeMap<>();
        Map<String, Method> setterMethods = new TreeMap<>();
        for (Method each : methods) {
            if (isGetterMethod(each)) {
                getterMethods.put(getPropertyName(each), each);
            } else if (isSetterMethod(each)) {
                setterMethods.put(getPropertyName(each), each);
            }
        }
        Map<String, Method> getterPairedGetterMethods = getPairedGetterMethods(getterMethods, setterMethods);
        for (Entry<String, Method> entry : getterPairedGetterMethods.entrySet()) {
            Object getterResult = null;
            try {
                getterResult = entry.getValue().invoke(value.getDataSource());
            // CHECKSTYLE:OFF
            } catch (final Exception ex) {
            }
            // CHECKSTYLE:ON
            if (null != getterResult) {
                out.name(entry.getKey()).value(getterResult.toString());
            }
        }
        out.endObject();
    }
    
    private boolean isGetterMethod(final Method method) {
        return method.getName().startsWith("get") && 0 == method.getParameterTypes().length && isGeneralClassType(method.getReturnType());
    }
    
    private boolean isSetterMethod(final Method method) {
        return method.getName().startsWith("set") && 1 == method.getParameterTypes().length && isGeneralClassType(method.getParameterTypes()[0]) && isVoid(method.getReturnType());
    }
    
    private boolean isGeneralClassType(final Class<?> clazz) {
        return generalClassType.contains(clazz);
    }
    
    private boolean isVoid(final Class<?> clazz) {
        return void.class == clazz || Void.class == clazz;
    }
    
    private String getPropertyName(final Method method) {
        return String.valueOf(method.getName().charAt(3)).toLowerCase() + method.getName().substring(4, method.getName().length());
    }
    
    private Map<String, Method> getPairedGetterMethods(final Map<String, Method> getterMethods, final Map<String, Method> setterMethods) {
        Map<String, Method> result = new TreeMap<>();
        for (Entry<String, Method> entry : getterMethods.entrySet()) {
            if (setterMethods.containsKey(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
}
