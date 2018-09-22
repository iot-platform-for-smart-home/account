package com.edu.bupt.new_account.controller;

import com.edu.bupt.new_account.model.Result;
import com.edu.bupt.new_account.model.Tenant;
import com.edu.bupt.new_account.model.User;
import com.edu.bupt.new_account.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@RestController
@RequestMapping("/api/v1/account")
public class UserController {

    @Autowired
    private UserService userService;


    //登陆
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public Result login(@RequestBody String loginInfo) {
        JsonObject login = new JsonParser().parse(loginInfo).getAsJsonObject();
        Result result = new Result();
        try {
            String tenantName = login.get("tenantName").getAsString();
            String passwd = login.get("password").getAsString();
            Tenant tenant = userService.findTenantByNameAndPasswd(tenantName,passwd);
            if(tenant==null)
                result.setStatus("error");
        } catch (Exception e) {
            System.out.println(e);
            result.setStatus("error");
        }finally {
            return result;
        }
    }

    @RequestMapping(value = "/createUser", method = RequestMethod.POST)
    @ResponseBody
    public Result createUser(@RequestBody String info) {
        JsonObject jsonInfo = new JsonParser().parse(info).getAsJsonObject();
        Result result = new Result();
        try {
            String openid = jsonInfo.get("openid").getAsString();
            User u = userService.findUserByOpenid(openid);
            if(u!=null){
                result.setStatus("error");
                result.setResultMsg("user has already exist!");
                return result;
            }
            User user = new User();
            user.setOpenid(openid);
            userService.saveUser(user);
            result.setResultMsg("create success");
        } catch (Exception e) {
            System.out.println(e);
            result.setStatus("error");

        }finally {
            return result;
        }
    }

}
