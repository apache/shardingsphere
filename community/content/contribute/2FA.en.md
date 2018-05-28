+++
title = "2FA"
weight = 3
chapter = true
+++

## Two-factor authentication(2FA)

Two-factor authentication(2FA) is the safe method of authenticating users with password and physical objects (such as credit CARDS, SMS phones, tokens or fingerprints).

To ensure the security of the committer's account, we need you to enable 2FA on GitHub to verify account and contribute code.

More details, please refer to [2FA](https://help.github.com/articles/requiring-two-factor-authentication-in-your-organization/).

> Note: If you do not enable 2FA, you will be removed from our organization and lose access to our repositories. You will also lose access to your forks of our private repositories.

## Enable 2FA on GitHub

Please refer to [Configuring two-factor authentication via a TOTP mobile app](https://help.github.com/articles/configuring-two-factor-authentication-via-a-totp-mobile-app/) for operations.

When you enable the 2FA authentication, you need to login GitHub using the username/password + mobile phone authentication code.

Tip: If you can't download the APP via the page link, you can search and download Google Authenticator in APP Store.

## How to contribute code

When 2FA authentication is enabled, you need to generate a private access Token to do git operations. Now, you will use username + private access Token but not username + password
to commit code.

Please refer to [Creating a personal access token for the command line](https://help.github.com/articles/creating-a-personal-access-token-for-the-command-line/) for operations.
