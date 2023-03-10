+++
title = "ElasticJob UI now supports Auth 2.0, OIDC and SAML single sign-on thanks to Casdoor"
weight = 80
chapter = true 
+++

![img](https://shardingsphere.apache.org/blog/img/2022_11_22_ElasticJob_UI_now_supports_Auth_2.0,_OIDC_and_SAML_single_sign-on_thanks_to_Casdoor1.png)

> If you're looking to add SSO to the administration console when using ElasticJob UI, this article will help you tackle this user management problem using ElasticJob UI's built-in Casdoor.

# Background

[ElasticJob UI](https://github.com/apache/shardingsphere-elasticjob-ui) is the visual admin console of [ElasticJob](https://github.com/apache/shardingsphere-elasticjob), whose target users are developers and DevOps teams rather than users. Generally, it is deployed only in the internal environment and thus its R&D focus more on its features.

Any open source project will inevitably become an object of interest for security researchers. Previously, security researchers submitted a lot of security issues on ElasticJob UI to the [Apache ShardingSphere](https://shardingsphere.apache.org/) community without considering its actual usage scenario.

After careful considerations regarding dealing with those security vulnerability reports, the community decided to stop maintaining ElasticJob UI.

The [Casdoor](https://casdoor.org/) community members noticed our discussion about stopping the maintenance of ElasticJob UI and thought it would be a loss to deactivate ElasticJob UI because of unrealistic security reports.

If ElasticJob UI would be connected to Casdoor, it would be improved in terms of authentication security and features. In this context, the Casdoor and Apache ShardingSphere community reached a consensus on collaboration.

# How to connect ElasticJob UI to Casdoor?

## Step 1: Deploy Casdoor

The source code of Casdoor is on [GitHub](https://github.com/casdoor/casdoor), and its boot mode includes development mode and production mode. The development mode is taken as an example here. Please refer to this [link](https://casdoor.org/docs/basic/server-installation) for more details.

**Backend boot mode**

```bash
go run main.go
```

**Front-end boot mode**

```bash
cd web
yarn install
yarn start
```

## Step 2: Configure Casdoor and obtain the required data

![img](https://shardingsphere.apache.org/blog/img/2022_11_22_ElasticJob_UI_now_supports_Auth_2.0,_OIDC_and_SAML_single_sign-on_thanks_to_Casdoor2.png)

The red arrows indicate what the backend configuration requires, with "Redirect URLs" referring to the address where you perform a callback.

We also need to find the corresponding cert we selected in the cert option, such as `cert-build-in` here. A certificate is also needed.

![img](https://shardingsphere.apache.org/blog/img/2022_11_22_ElasticJob_UI_now_supports_Auth_2.0,_OIDC_and_SAML_single_sign-on_thanks_to_Casdoor3.png)

For additional Casdoor documentation, please refer to this [link](https://casdoor.org/docs/overview).

## Step 3: Configuration in ElasticJob UI

Find `application.properties` in [shardingsphere-elasticjob-ui](https://github.com/apache/shardingsphere-elasticjob-ui) and configure it.

![img](https://shardingsphere.apache.org/blog/img/2022_11_22_ElasticJob_UI_now_supports_Auth_2.0,_OIDC_and_SAML_single_sign-on_thanks_to_Casdoor4.png)

Paste the data we obtained from Casdoor into the corresponding position as follows:

![img](https://shardingsphere.apache.org/blog/img/2022_11_22_ElasticJob_UI_now_supports_Auth_2.0,_OIDC_and_SAML_single_sign-on_thanks_to_Casdoor5.png)

Now, we can use Casdoor in ElasticJob UI.

Once the ElasticJob's admin console connects to Casdoor, it will support UI-first centralized identity access/single sign-on based on [OAuth 2.0](https://oauth.net/2/), [OIDC](https://openid.net/connect/) and [SAML](https://auth0.com/blog/how-saml-authentication-works/).

Thanks to developers from the Casdoor and Apache ShardingSphere community, our collaboration has been going on in a smooth and friendly way. At first, [jakiuncle](https://github.com/jakiuncle) from Casdoor proposed an issue and committed a PR, and then our Committer [TeslaCN](https://github.com/TeslaCN) and PMC [tristaZero](https://github.com/tristaZero) reviewed the PR. This cross-community collaboration stands as a testament to the Beaty of open source.

# About ElasticJob UI

[ElasticJob](https://github.com/apache/shardingsphere-elasticjob) is a distributed scheduling solution oriented towards Internet applications and massive tasks.

It provides elastic scheduling, resource management, and job governance combined with open architecture design, building a diversified job ecosystem. It uses a unified job API for each project.

[ElasticJob-UI](https://github.com/apache/shardingsphere-elasticjob-ui) is the visual admin console of ElasticJob, supporting dynamic configuration, job management and control, job history retrieval and other features.

🔗 [**GitHub**](https://github.com/apache/shardingsphere-elasticjob-ui)

# About Casdoor

[Casdoor](https://casdoor.org/) is a UI-first identity access management (IAM) / single-sign-on (SSO) platform based on OAuth 2.0 / OIDC. Casdoor can help you solve user management problems. There's no need to develop a series of authentication features such as user login and registration. It can manage the user module entirely in a few simple steps in conjunction with the host application. It's convenient, easy-to-use and powerful.

🔗 [**GitHub**](https://github.com/casdoor/casdoor)