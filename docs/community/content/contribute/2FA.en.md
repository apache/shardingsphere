+++
title = "2FA"
weight = 10
chapter = true
+++

## Two-factor authentication(2FA)

Two factor authentication (2FA) refers to the authentication method that combines both passport, and an object (credit card, SMS phone, token or biomarkers as fingerprint) to identify a user. 
To ensure the security of the committer’s account, we need you to enable 2FA to sign in and contribute codes on GitHub.

More details, please refer to [2FA](https://help.github.com/articles/requiring-two-factor-authentication-in-your-organization/).

> To be noticed: If you do not enable 2FA, you will be removed from the project and unable to access our repositories and the fork from our private repository.

## Enable 2FA on GitHub

For detailed operations, please refer to [Enable Two Factor Authentication with TOTP](https://help.github.com/articles/configuring-two-factor-authentication-via-a-totp-mobile-app/).

After enabling 2FA, you need to sign in GitHub with the way of username/password + mobile phone authentication code.

> Tips: If you cannot download the APP through the page link, you can search and download Google Authenticator in APP Store.

## How to Submit Codes

After enabling 2FA, you need to generate a private access Token to perform operations such as git submit and so on. 
At this time, you will use username + private access Token in replace of username + password to submit codes.

For detailed operations, please refer to [Create a Private Token](https://help.github.com/articles/creating-a-personal-access-token-for-the-command-line/).
