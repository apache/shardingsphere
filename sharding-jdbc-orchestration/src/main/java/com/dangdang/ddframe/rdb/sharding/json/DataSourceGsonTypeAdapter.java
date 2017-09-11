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

package com.dangdang.ddframe.rdb.sharding.json;

import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.NamedDataSource;
import com.google.common.collect.Sets;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 作业配置的Json转换适配器.
 *
 * @author zhangliang
 */
public final class DataSourceGsonTypeAdapter extends TypeAdapter<NamedDataSource> {
    
    @Override
    public NamedDataSource read(final JsonReader in) throws IOException {
        return null;
//        String jobName = "";
//        String cron = "";
//        int shardingTotalCount = 0;
//        String shardingItemParameters = "";
//        String jobParameter = "";
//        boolean failover = false;
//        boolean misfire = failover;
//        String description = "";
//        JobProperties jobProperties = new JobProperties();
//        JobType jobType = null;
//        String jobClass = "";
//        boolean streamingProcess = false;
//        String scriptCommandLine = "";
//        Map<String, Object> customizedValueMap = new HashMap<>(32, 1);
//        in.beginObject();
//        while (in.hasNext()) {
//            String jsonName = in.nextName();
//            switch (jsonName) {
//                case "jobName":
//                    jobName = in.nextString();
//                    break;
//                case "cron":
//                    cron = in.nextString();
//                    break;
//                case "shardingTotalCount":
//                    shardingTotalCount = in.nextInt();
//                    break;
//                case "shardingItemParameters":
//                    shardingItemParameters = in.nextString();
//                    break;
//                case "jobParameter":
//                    jobParameter = in.nextString();
//                    break;
//                case "failover":
//                    failover = in.nextBoolean();
//                    break;
//                case "misfire":
//                    misfire = in.nextBoolean();
//                    break;
//                case "description":
//                    description = in.nextString();
//                    break;
//                case "jobProperties":
//                    jobProperties = getJobProperties(in);
//                    break;
//                case "jobType":
//                    jobType = JobType.valueOf(in.nextString());
//                    break;
//                case "jobClass":
//                    jobClass = in.nextString();
//                    break;
//                case "streamingProcess":
//                    streamingProcess = in.nextBoolean();
//                    break;
//                case "scriptCommandLine":
//                    scriptCommandLine = in.nextString();
//                    break;
//                default:
//                    addToCustomizedValueMap(jsonName, in, customizedValueMap);
//                    break;
//            }
//        }
//        in.endObject();
//        JobCoreConfiguration coreConfig = getJobCoreConfiguration(jobName, cron, shardingTotalCount, shardingItemParameters,
//                jobParameter, failover, misfire, description, jobProperties);
//        JobTypeConfiguration typeConfig = getJobTypeConfiguration(coreConfig, jobType, jobClass, streamingProcess, scriptCommandLine);
//        return getJobRootConfiguration(typeConfig, customizedValueMap);
//    }
//    
//    private JobProperties getJobProperties(final JsonReader in) throws IOException {
//        JobProperties result = new JobProperties();
//        in.beginObject();
//        while (in.hasNext()) {
//            switch (in.nextName()) {
//                case "job_exception_handler":
//                    result.put(JobProperties.JobPropertiesEnum.JOB_EXCEPTION_HANDLER.getKey(), in.nextString());
//                    break;
//                case "executor_service_handler":
//                    result.put(JobProperties.JobPropertiesEnum.EXECUTOR_SERVICE_HANDLER.getKey(), in.nextString());
//                    break;
//                default:
//                    break;
//            }
//        }
//        in.endObject();
//        return result;
    }
    
    @Override
    public void write(final JsonWriter out, final NamedDataSource value) throws IOException {
        out.beginObject();
        out.name("name").value(value.getName());
        out.name("class").value(value.getDataSource().getClass().getName());
        Method[] methods = value.getDataSource().getClass().getDeclaredMethods();
        Map<String, Method> getterMethods = new HashMap<>(methods.length, 1);
        Map<String, Method> setterMethods = new HashMap<>(methods.length, 1);
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
        return Sets.<Class<?>>newHashSet(boolean.class, Boolean.class, int.class, Integer.class, long.class, Long.class, String.class).contains(clazz);
    }
    
    private boolean isVoid(final Class<?> clazz) {
        return void.class == clazz || Void.class == clazz;
    }
    
    private String getPropertyName(final Method each) {
        return String.valueOf(each.getName().charAt(3)).toLowerCase() + each.getName().substring(4, each.getName().length());
    }
    
    private Map<String, Method> getPairedGetterMethods(final Map<String, Method> getterMethods, final Map<String, Method> setterMethods) {
        Map<String, Method> result = new HashMap<>(getterMethods.size());
        for (Entry<String, Method> entry : getterMethods.entrySet()) {
            if (setterMethods.containsKey(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
    
    private Map<String, Method> getPairedSetterMethods(final Map<String, Method> getterMethods, final Map<String, Method> setterMethods) {
        Map<String, Method> result = new HashMap<>(setterMethods.size());
        for (Entry<String, Method> entry : setterMethods.entrySet()) {
            if (getterMethods.containsKey(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
}
