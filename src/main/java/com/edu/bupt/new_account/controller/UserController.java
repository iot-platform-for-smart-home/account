package com.edu.bupt.new_account.controller;

import com.edu.bupt.new_account.model.Result;
import com.edu.bupt.new_account.model.Tenant;
import com.edu.bupt.new_account.model.User;
import com.edu.bupt.new_account.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.List;

@RestController
@RequestMapping("/api/v1/account")
public class UserController {

    @Autowired
    private UserService userService;


    //（tenant）登陆
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

    /**
     * @Description 用户注册
     * @param info
     * @return
     */
    @RequestMapping(value = "/createUser", method = RequestMethod.POST)
    @ResponseBody
    public Result createUser(@RequestBody String info) {
        JsonObject jsonInfo = new JsonParser().parse(info).getAsJsonObject();
        Result result = new Result();
        try {
            String openid = jsonInfo.get("openid").getAsString();
            String email = jsonInfo.get("email").getAsString();
            String phone = jsonInfo.get("phone").getAsString();
            String address = jsonInfo.get("address").getAsString();
            User u = userService.findUserByOpenid(openid);
            if(u!=null){
                result.setStatus("error");
                result.setResultMsg("user has already exist!");
                return result;
            }
            User user = new User();
            user.setOpenid(openid);
            user.setEmail(email);
            user.setPhone(phone);
            user.setAddress(address);

            userService.saveUser(user);
            result.setResultMsg("create success");
        } catch (Exception e) {
            System.out.println(e);
            result.setStatus("error");

        }finally {
            return result;
        }
    }

    //判断openid是否在表内
    @RequestMapping(value = "/userLogin", method = RequestMethod.POST)
    @ResponseBody
    public Result userLogin(@RequestBody String loginInfo) {
        JsonObject login = new JsonParser().parse(loginInfo).getAsJsonObject();
        Result result = new Result();
        try {
            String openid = login.get("openid").getAsString();
            User user = userService.findUserByOpenid(openid);
            if(user == null){
                result.setStatus("error");
                result.setResultMsg("用户不存在");
                return result;
            }
            result.setResultMsg("用户存在");

        } catch (Exception e) {
            System.out.println(e);
            result.setStatus("error");
            result.setResultMsg("用户不存在");
        }finally {
            return result;
        }
    }

    //用户（user）修改
    @RequestMapping(value = "/userModify", method = RequestMethod.POST)
    @ResponseBody
    public Result userModify(@RequestBody String loginInfo) {
        JsonObject jsonInfo = new JsonParser().parse(loginInfo).getAsJsonObject();
        Result result = new Result();
        try {
            String openid = jsonInfo.get("openid").getAsString();
            String email = jsonInfo.get("email").getAsString();
            String phone = jsonInfo.get("phone").getAsString();
            String address = jsonInfo.get("address").getAsString();

            User user = new User();
            user.setOpenid(openid);
            user.setPhone(phone);
            user.setEmail(email);
            user.setAddress(address);

            userService.updateUserInfo(user);

        } catch (Exception e) {
            System.out.println(e);
            result.setStatus("error");
            result.setResultMsg("用户不存在");
        }finally {
            return result;
        }
    }
    // 搜索所有用户
    @RequestMapping(value = "/searchAllUser", method = RequestMethod.POST)
    @ResponseBody
    public Result searchAllUser(@RequestBody String info) {
        Result result = new Result();
        try {
            List<User> list = userService.findAllUser();
            result.setData(list);
        } catch (Exception e) {
            System.out.println(e);
            result.setStatus("error");
        }finally {
            return result;
        }
    }
}
