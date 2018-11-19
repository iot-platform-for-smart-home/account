package com.edu.bupt.new_account.controller;

import com.edu.bupt.new_account.model.Relation;
import com.edu.bupt.new_account.model.Result;
import com.edu.bupt.new_account.model.Tenant;
import com.edu.bupt.new_account.model.User;
import com.edu.bupt.new_account.service.UserService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
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
            Tenant tenant = userService.findTenantByNameAndPasswd(tenantName, passwd);
            if (tenant == null)
                result.setStatus("error");
        } catch (Exception e) {
            System.out.println(e);
            result.setStatus("error");
        } finally {
            return result;
        }
    }

    /**
     * @param info
     * @return
     * @Description 用户注册
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
            if (u != null) {
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
            result.setData("user");
        } catch (Exception e) {
            System.out.println(e);
            result.setStatus("error");

        } finally {
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
            if (user == null) {
                result.setStatus("error");
                result.setResultMsg("用户不存在");
                return result;
            }
            result.setResultMsg("用户存在");
            result.setData(user);

        } catch (Exception e) {
            System.out.println(e);
            result.setStatus("error");
            result.setResultMsg("用户不存在");
        } finally {
            return result;
        }
    }

    //绑定所属主用户及其网关
    @RequestMapping(value = "/bindGate", method = RequestMethod.POST)
    @ResponseBody
    public Result bindGate(@RequestBody String Info) {
        JsonObject info = new JsonParser().parse(Info).getAsJsonObject();
        Result result = new Result();
        try {
            String binder = info.get("customerid").getAsString();
            String phone = info.get("phone").getAsString();
            String gateids = info.get("gateids").getAsString();
            User user = userService.findUserByphone(phone);
            if (user == null) {
                result.setStatus("error");
                result.setResultMsg("被绑定者不存在");
                return result;
            }
            Relation relation = new Relation();
            relation.setBinder(Integer.parseInt(binder));
            relation.setBinded(user.getId());
            relation.setGateid(gateids);
            userService.saveRelation(relation);

        } catch (Exception e) {
            System.out.println(e);
            result.setStatus("error");
            result.setResultMsg("插入失败");
        } finally {
            result.setResultMsg("绑定成功");
            return result;
        }
    }

    //解绑所属主用户及其网关
    @RequestMapping(value = "/unBindGate", method = RequestMethod.POST)
    @ResponseBody
    public Result unBindGate(@RequestBody String Info) {
        JsonObject info = new JsonParser().parse(Info).getAsJsonObject();
        Result result = new Result();
        try {
            String binder = info.get("customerid").getAsString();
            String phone = info.get("phone").getAsString();
            String gateids = info.get("gateids").getAsString();
            User user = userService.findUserByphone(phone);
            int binderId = Integer.parseInt(binder);
            int bindedId = user.getId();
            Relation relation = userService.findRelationByBinderAndBinded(binderId, bindedId);
            if (relation == null) {
                result.setStatus("error");
                result.setResultMsg("不存在该绑定关系");
                return result;
            }
            //删除绑定关系
            userService.unbind(relation.getId());
            String[] neededUnbindGates = gateids.split(",");
            String[] originGates = relation.getGateid().split(",");
            String newGates = "";
            int judge = 0;
            for (String i : originGates) {
                if (!Arrays.asList(neededUnbindGates).contains(i)) {
                    newGates += i + ',';
                    judge = 1;
                }
            }
            if(judge==1){
                newGates = newGates.substring(0,newGates.length()-1);
            }
            if(newGates!=""){
                relation.setBinder(binderId);
                relation.setBinded(bindedId);
                relation.setGateid(newGates);
                userService.saveRelation(relation);
            }
        } catch (Exception e) {
            System.out.println(e);
            result.setStatus("error");
            result.setResultMsg("解除失败");
        } finally {
            result.setResultMsg("解除成功");
            return result;
        }
    }

    //查询用户所对应的网关
    @RequestMapping(value = "/getGates", method = RequestMethod.POST)
    @ResponseBody
    public Result getGates(@RequestBody String loginInfo) {
        JsonObject info = new JsonParser().parse(loginInfo).getAsJsonObject();
        Result result = new Result();
        try {
            String binded = info.get("customerid").getAsString();
            int bindedId = Integer.parseInt(binded);
            List<Relation> relations = userService.getBindedRelations(bindedId);
            if (relations == null) {
                result.setStatus("error");
                result.setResultMsg("未找到绑定网关");
                return result;
            }
            String ret = "";
            for (Relation relation : relations) {
                ret += relation.getGateid() + ',';
            }
            result.setData(ret);
        } catch (Exception e) {
            System.out.println(e);
            result.setStatus("error");
        } finally {
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
        } finally {
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
        } finally {
            return result;
        }
    }
}
