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

package org.apache.shardingsphere.driver.jdbc.core.driver;

import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.driver.jdbc.exception.syntax.DriverURLProviderNotFoundException;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * ShardingSphere driver URL manager.
 */
public final class ShardingSphereDriverURLManager {
    
    /**
     * Get config content from url.
     * 
     * @param url the driver url
     * @return the config content
     */
    public static byte[] getContent(final String url) {
        for (ShardingSphereDriverURLProvider each : ShardingSphereServiceLoader.getServiceInstances(ShardingSphereDriverURLProvider.class)) {
            if (each.accept(url)) {
                return each.getContent(url);
            }
        }
        throw new DriverURLProviderNotFoundException(url);
    }
    
    /**
     * parse the url and get the parameters of the url.
     * @param url the driver url
     * @return the parameters of the url
     */
    public static Properties parseURL(final String url) {
        Properties result = new Properties();
        int index = url.indexOf("?");
        if (-1 == index) {
            return result;
        }
        String paramString = url.substring(index + 1);
        StringTokenizer queryParams = new StringTokenizer(paramString, "&");
        while (queryParams.hasMoreTokens()) {
            String parameterValuePair = queryParams.nextToken();
            int indexOfEquals = StringUtils.indexOfIgnoreCase(parameterValuePair, "=", 0);
            String parameter = null;
            String value = null;
            if (indexOfEquals == -1) {
                continue;
            }
            parameter = parameterValuePair.substring(0, indexOfEquals);
            if (indexOfEquals + 1 < parameterValuePair.length()) {
                value = parameterValuePair.substring(indexOfEquals + 1);
            }
            if (value != null && value.length() > 0 && parameter.length() > 0) {
                try {
                    result.put(parameter, URLDecoder.decode(value, "UTF-8"));
                } catch (UnsupportedEncodingException | NoSuchMethodError e) {
                    result.put(parameter, URLDecoder.decode(value));
                }
            }
        }
        return result;
    }
}
