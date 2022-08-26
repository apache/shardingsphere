+++
title = "2FA"
weight = 4
chapter = true
+++

## 双因素认证（2FA）

双因子验证（2FA）是指结合密码以及实物（信用卡、SMS 手机、令牌或指纹等生物标志）两种条件对用户进行认证的方法。
为保证提交者账户的安全，我们需要您在 GitHub 上启用 2FA 来验证登录用户、并贡献代码。

具体内容可参考 [2FA](https://help.github.com/articles/requiring-two-factor-authentication-in-your-organization/)。

> 注意：若您不启用 2FA，您将会从本项目中除名，并且无法访问我们的仓库以及来自我们私有仓库的 fork 仓库。

## 在 GitHub 上启用 2FA

具体操作，请参考[使用 TOTP 启用双因素认证](https://help.github.com/articles/configuring-two-factor-authentication-via-a-totp-mobile-app/)。

当您开启 2FA 验证后，您需要使用用户名/密码 + 手机认证码的方式来登录 GitHub。

提示：若无法通过页面链接下载对应 APP，可在手机的应用市场或是 APP Store 里搜索并下载 Google Authenticator。

## 如何提交代码

当启用 2FA 认证后，您需要生成私有访问 Token 来进行 git 提交等操作。此时，您将使用用户名 + 私有访问 Token 来代替 用户名 + 密码的方式
进行代码的提交。

具体操作，请参考[创建私有 Token](https://help.github.com/articles/creating-a-personal-access-token-for-the-command-line/)。
