package org.shardingsphere.controller;


import org.shardingsphere.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 * Test API
 * http://localhost:8080/save-user: add a line of data
 * http://localhost:8080/list-user: show all of data
 * http://localhost:8080/delete-user: delete a line of data about user
 */
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("save-user")
    public String saveUser() {
        return userService.saveUser();
    }

    @DeleteMapping("delete-user")
    public String deleteUser(Long id) {
        return userService.deleteUser(id);
    }

    @GetMapping("list-user")
    public Object listUser() {
        return userService.list();
    }

}
