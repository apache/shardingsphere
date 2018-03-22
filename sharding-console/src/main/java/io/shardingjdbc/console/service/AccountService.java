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

package io.shardingjdbc.console.service;

import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.sql.DriverManager;
import java.util.Map;

import io.shardingjdbc.console.constant.LoginInfo;

/**
 * SqlService.
 *
 * @author zhangyonglun
 */
@Service
public class AccountService {

    public String login(final Map<String, String> account, final HttpSession httpSession) {
        String driver = account.get("driver");
        String url = account.get("url");
        String username = account.get("username");
        String password = account.get("password");
        if (null == driver || null == url || null == username || null == password) {
            return "param error";
        }
        if (url.equals("") || driver.equals("")) {
            return "param empty";
        }
        try {
            Class.forName(driver);
            DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            return "login fail";
        }
        httpSession.setAttribute(LoginInfo.DATASOURCE_DRIVER, driver);
        httpSession.setAttribute(LoginInfo.DATASOURCE_URL, url);
        httpSession.setAttribute(LoginInfo.DATASOURCE_USERNAME, username);
        httpSession.setAttribute(LoginInfo.DATASOURCE_PASSWORD, password);

        return "login success";
    }
}
