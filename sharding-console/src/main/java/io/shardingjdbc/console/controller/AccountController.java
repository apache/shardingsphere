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

package io.shardingjdbc.console.controller;

import io.shardingjdbc.console.domain.AccountInfo;
import io.shardingjdbc.console.domain.AccountResponseResult;
import io.shardingjdbc.console.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


/**
 * Account controller.
 * 
 * @author zhangyonglun
 */
@RestController
public class AccountController {
    
    @Autowired
    private AccountService accountService;
    
    /**
     * Login.
     * 
     * @param accountInfo account information
     * @param servletRequest servlet request
     * @return account response result
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public AccountResponseResult logIn(@RequestBody final AccountInfo accountInfo, final ServletRequest servletRequest) {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpSession httpSession = httpRequest.getSession();
        return accountService.login(accountInfo, httpSession);
    }
    
    /**
     * Logout.
     * 
     * @param servletRequest servlet request
     * @return account response result
     */
    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public AccountResponseResult logOut(final ServletRequest servletRequest) {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpSession httpSession = httpRequest.getSession();
        httpSession.invalidate();
        AccountResponseResult accountResponseResult = new AccountResponseResult();
        accountResponseResult.setStatusCode(0);
        return accountResponseResult;
    }
}
