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

import io.shardingjdbc.console.domain.AccountInfo;
import io.shardingjdbc.console.domain.AccountResponseResult;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * SqlService.
 *
 * @author zhangyonglun
 */
@Service
public class AccountService {

    public AccountResponseResult login(final AccountInfo accountInfo, final HttpSession httpSession) {

        String driver = accountInfo.getDriver();
        String url = accountInfo.getUrl();
        String username = accountInfo.getUsername();
        String password = accountInfo.getPassword();
        AccountResponseResult accountResponseResult = new AccountResponseResult();

        if (null == driver || null == url || null == username || null == password) {
            accountResponseResult.setErrMsg("param error");
            return accountResponseResult;
        }
        if (url.equals("") || driver.equals("")) {
            accountResponseResult.setErrMsg("param empty");
            return accountResponseResult;
        }

        Connection connection = null;
        try {
            Class.forName(driver);
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException sqe) {
            accountResponseResult.setErrMsg(sqe.getMessage());
            return accountResponseResult;
        } catch (ClassNotFoundException cne) {
            accountResponseResult.setErrMsg(cne.getMessage());
            return accountResponseResult;
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException cse) { }
        }

        httpSession.setAttribute("accountInfo", accountInfo);
        accountResponseResult.setStatusCode(0);
        return accountResponseResult;
    }
}
