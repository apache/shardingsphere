+++
title = "2FA"
weight = 10
chapter = true
+++

## 双因素认证(2FA)

双因子验证（2FA）是指结合密码以及实物（信用卡、SMS手机、令牌或指纹等生物标志）两种条件对用户进行认证的方法。
为保证提交者账户的安全，我们需要您在GitHub上启用2FA来验证登录用户、并贡献代码。

具体内容可参考[2FA](https://help.github.com/articles/requiring-two-factor-authentication-in-your-organization/)。

> 注意：若您不启用2FA，您将会从本项目中除名，并且无法访问我们的仓库以及来自我们私有仓库的fork仓库。

## 在GitHub上启用2FA

具体操作，请参考[使用TOTP启用双因素认证](https://help.github.com/articles/configuring-two-factor-authentication-via-a-totp-mobile-app/)。

当您开启2FA验证后，您需要使用用户名/密码 + 手机认证码的方式来登录GitHub。

提示：若无法通过页面链接下载对应APP，可在手机的应用市场或是APP Store里搜索并下载Google Authenticator。

## 如何提交代码

当启用2FA认证后，您需要生成私有访问Token来进行git提交等操作。此时，您将使用用户名 + 私有访问Token 来代替 用户名 + 密码的方式
进行代码的提交。

具体操作，请参考[创建私有Token](https://help.github.com/articles/creating-a-personal-access-token-for-the-command-line/)。
