# Sharding UI

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

[![Build Status](https://api.travis-ci.org/sharding-sphere/sharding-ui.png?branch=master)](https://travis-ci.org/sharding-sphere/sharding-ui)

## Overview

Sharding UI is a management background for [ShardingSphere](http://shardingsphere.io/), including: dynamic configuration, Data orchestration, etc.

### Sharding-ui-frontend

Sharding-ui-frontend based on [vue](https://github.com/vuejs/vue) and use the UI Toolkit [element](https://github.com/ElemeFE/element).

* [sharding-ui-frontend/README.md](sharding-ui-frontend/README.md)

### Sharding-ui-backend

Sharding-ui-backend is a standard spring boot project.

## Build

1. `git clone https://github.com/sharding-sphere/sharding-ui.git`
2. `cd shrding-ui/`
3. Run `mvn clean package`
4. Get the package in `/dist`.(sharding-ui.tar.gz)
