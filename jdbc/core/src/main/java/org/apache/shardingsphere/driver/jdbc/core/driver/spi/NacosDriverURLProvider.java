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

package org.apache.shardingsphere.driver.jdbc.core.driver.spi;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.google.common.base.Preconditions;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.driver.jdbc.core.driver.ShardingSphereDriverURLProvider;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class NacosDriverURLProvider implements ShardingSphereDriverURLProvider {

    private static final String PARAM_PREFIX = "?";
    private static final String NACOS_TYPE = "nacos:";
    private static final String SHRDING_URL_PREFIX = "jdbc:shardingsphere:";

    @Override
    public boolean accept(String url) {
        return StringUtils.isNotBlank(url) && url.contains(NACOS_TYPE);
    }

    @Override
    @SneakyThrows(NacosException.class)
    public byte[] getContent(String url) {
        Properties props = getNacosConfigByUrl(url);
        ConfigService configService = NacosFactory.createConfigService(props);
        String dataId = props.getProperty(Constants.DATAID);
        String config = configService.getConfig(dataId, props.getProperty(Constants.GROUP, Constants.DEFAULT_GROUP), 500);
        Preconditions.checkArgument(config != null, "Nacos config [" + dataId + "] is Empty.");
        return config.getBytes(StandardCharsets.UTF_8);
    }

    private Properties getNacosConfigByUrl(String url) {
        String nacosConfig = StringUtils.removeStart(url, SHRDING_URL_PREFIX + NACOS_TYPE);
        Preconditions.checkArgument(nacosConfig.indexOf(PARAM_PREFIX) > 0, "Nacos param is required in ShardingSphere driver URL.");
        String dataId = StringUtils.substringBefore(nacosConfig, PARAM_PREFIX);
        //'withKeyValueSeparator(java.lang.String)' is marked unstable with @Beta
        //Map<String, String> confMap = Splitter.on("&").withKeyValueSeparator("=").split(nacosConfig);
        Map<String, String> confMap = getUrlParameters(StringUtils.substringAfter(nacosConfig, PARAM_PREFIX));
        Properties properties = new Properties();
        properties.putAll(confMap);
        properties.put(Constants.DATAID, dataId);
        return properties;
    }

    public static Map<String, String> getUrlParameters(String urlParam) {
        Map<String, String> mapRequest = new HashMap<>();
        String[] arrSplit;
        if (urlParam == null) {
            return mapRequest;
        }
        arrSplit = urlParam.split("[&]");
        for (String strSplit : arrSplit) {
            String[] arrSplitEqual;
            arrSplitEqual = strSplit.split("[=]");
            if (arrSplitEqual.length > 1) {
                mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);
            } else {
                if (!"".equals(arrSplitEqual[0])) {
                    mapRequest.put(arrSplitEqual[0], "");
                }
            }
        }
        return mapRequest;
    }

}
