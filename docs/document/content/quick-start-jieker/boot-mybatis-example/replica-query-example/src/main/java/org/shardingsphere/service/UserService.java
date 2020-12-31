package org.shardingsphere.service;


import org.shardingsphere.domain.User;

import java.util.List;

public interface UserService {

    List<User> list();

    String saveUser();

    String deleteUser(Long id);
}