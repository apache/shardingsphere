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

package org.apache.shardingsphere.ui.config;

import org.apache.shardingsphere.ui.security.AuthenticationFilter;
import org.apache.shardingsphere.ui.security.UserAuthenticationService;
import org.apache.shardingsphere.ui.web.filter.CORSFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Web filter configuration.
 */
@Configuration
public class FilterConfiguration {
    
    @Autowired
    private UserAuthenticationService userAuthenticationService;
    
    /**
     * Register the CORS filter.
     *
     * @return filter registration bean
     */
    @Bean
    public FilterRegistrationBean corsFilter() {
        CORSFilter corsFilter = new CORSFilter();
        FilterRegistrationBean filterRegBean = new FilterRegistrationBean();
        filterRegBean.setFilter(corsFilter);
        List<String> urlPatterns = new ArrayList<>();
        urlPatterns.add("/api/*");
        filterRegBean.setUrlPatterns(urlPatterns);
        return filterRegBean;
    }
    
    /**
     * Register the authentication filter.
     *
     * @return filter registration bean
     */
    @Bean
    public FilterRegistrationBean authenticationFilter() {
        AuthenticationFilter authenticationFilter = new AuthenticationFilter();
        authenticationFilter.setUserAuthenticationService(userAuthenticationService);
        FilterRegistrationBean filterRegBean = new FilterRegistrationBean();
        filterRegBean.setFilter(authenticationFilter);
        List<String> urlPatterns = new ArrayList<>();
        urlPatterns.add("/api/*");
        filterRegBean.setUrlPatterns(urlPatterns);
        return filterRegBean;
    }
    
}
