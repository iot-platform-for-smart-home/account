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

import java.util.*;

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
        String openid = jsonInfo.get("openid").getAsString();
        String email = jsonInfo.get("email").getAsString();
        String phone = jsonInfo.get("phone").getAsString();
        String address = jsonInfo.get("address").getAsString();
        try {
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
            int customerid = userService.findUserByOpenid(openid).getId();
            user.setId(customerid);
            result.setResultMsg("create success");
            result.setData(user);
        } catch (Exception e) {
            userService.deleteUserById(userService.findUserByOpenid(openid).getId());
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


    //绑定所属主用户及其网关   A 分享给 B
    @RequestMapping(value = "/bindGate", method = RequestMethod.POST)
    @ResponseBody
    public Result bindGate(@RequestBody String Info) {
        JsonObject info = new JsonParser().parse(Info).getAsJsonObject();
        Result result = new Result();
        try {
            String binded = info.get("customerid").getAsString();
            String phone = info.get("phone").getAsString();
            String gateids = info.get("gateids").getAsString();
            String remark = info.get("remark").getAsString();
            User user = userService.findUserByphone(phone);
            if (user == null) {
                result.setStatus("error");
                result.setResultMsg("该用户不存在");
                return result;
            }

            //检查是否已经有该绑定关系
            Relation re = userService.findRelationByBinderAndBinded(user.getId(), Integer.parseInt(binded));
            if (re != null) {
                if (userService.is_shared(re.getGateid(), gateids)){
                    result.setStatus("error");
                    result.setResultMsg("网关重复分享");
                    return result;
                } else {  // 添加新分享的网关
                    String old_gatewayids = re.getGateid();
                    re.setGateid(old_gatewayids + "," + gateids);
                    userService.updateRelation(re);
                    result.setResultMsg("绑定成功");
                }
            } else {
                Relation relation = new Relation();
                relation.setBinded(Integer.parseInt(binded));
                relation.setBinder(user.getId());
                relation.setGateid(gateids);
                relation.setRemark(remark);
                userService.saveRelation(relation);
                result.setResultMsg("绑定成功");
            }
        } catch (Exception e) {
            result.setStatus("error");
            result.setResultMsg("插入失败");
            e.printStackTrace();
        } finally {
            return result;
        }
    }


    //获取绑定者对应被绑定者电话及其网关  A分享给B， B要获取A信息
    @RequestMapping(value = "/getBinderGates", method = RequestMethod.POST)
    @ResponseBody
    public Result getBinderGates(@RequestBody String Info) {
        JsonObject info = new JsonParser().parse(Info).getAsJsonObject();
        Result result = new Result();
        try {
            String binder = info.get("customerid").getAsString();
            int binderId = Integer.parseInt(binder);
            // 查找是否存在别人分享的网关
            List<Relation> relations = userService.findRelationsByBinderID(binderId);
            if (relations.size() == 0) {
                result.setStatus("error");
                result.setResultMsg("该绑定关系不存在");
                return result;
            }
            List list = new ArrayList();
            for (Relation relation : relations) {
                list.add(relation.getGateid());
            }
            result.setData(list);
        } catch (Exception e) {
            System.out.println(e);
            result.setStatus("error");
            result.setResultMsg("操作失败");
        } finally {
            return result;
        }
    }


    //解绑被绑定者及其网关  A 分享给 B， A 要取消对 B 的分享
    @RequestMapping(value = "/unBindedGate", method = RequestMethod.POST)
    @ResponseBody
    public Result unBindedGate(@RequestBody String Info) {
        JsonObject info = new JsonParser().parse(Info).getAsJsonObject();
        Result result = new Result();
        try {
            String binded = info.get("customerid").getAsString();
            String phone = info.get("phone").getAsString();
            String gateids = info.get("gateids").getAsString();
            User user = userService.findUserByphone(phone);
            int bindedId = Integer.parseInt(binded);
            int binderId = user.getId();
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
            if (judge == 1) {
                newGates = newGates.substring(0, newGates.length() - 1);
            }
            if (newGates != "") {
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
            return result;
        }
    }


    //解绑被绑定者及其网关 只有一个网关id  网关拥有者取消所有分享
    @RequestMapping(value = "/unBindedALLGate", method = RequestMethod.POST)
    @ResponseBody
    public Result unBindedALLGate(@RequestBody String Info) {
        JsonObject info = new JsonParser().parse(Info).getAsJsonObject();
        Result result = new Result();
        try {
            String binded = info.get("customerid").getAsString();
            String gateid = info.get("gateid").getAsString();
            int bindedId = Integer.parseInt(binded);
            List<Relation> relations = userService.getBindedRelations(bindedId);

            if (relations.size() == 0) {
                result.setStatus("error");
                result.setResultMsg("不存在该绑定关系");
                return result;
            }

            for(Relation re:relations){
                //删除绑定关系
                String re_gates = re.getGateid();
                if(re_gates.indexOf(gateid)==-1){
                    continue;
                }

                String[] originGates = re.getGateid().split(",");
                String newGates = "";
                int judge = 0;
                for (String i : originGates) {
                    if (!Arrays.asList(gateid).contains(i)) {
                        newGates += i + ',';
                        judge = 1;
                    }
                }
                if (judge == 1) {
                    newGates = newGates.substring(0, newGates.length() - 1);
                }
                if (newGates == "") {
                    userService.unbind(re.getId());
                }else {
                    re.setGateid(newGates);
                    userService.updateRelation(re);
                }
            }

        } catch (Exception e) {
            System.out.println(e);
            result.setStatus("error");
            result.setResultMsg("解除失败");
        } finally {
            return result;
        }
    }


    //解绑绑定者及其网关
    @RequestMapping(value = "/unBinderGates", method = RequestMethod.POST)
    @ResponseBody
    public Result unBinderGates(@RequestBody String Info) {
        JsonObject info = new JsonParser().parse(Info).getAsJsonObject();
        Result result = new Result();
        try {
            String binder = info.get("customerid").getAsString();
            String gateid = info.get("gateids").getAsString();
            List<Relation> relations = userService.findRelationsByBinderID(Integer.parseInt(binder));
            if (relations.size() == 0) {
                result.setStatus("error");
                result.setResultMsg("不存在该绑定关系");
                return result;
            }

            for (Relation re : relations) {
                String gates = re.getGateid();
                if (gates.indexOf(gateid) != -1) {
                    //删除绑定关系
                    userService.unbind(re.getId());
                    String[] originGates = re.getGateid().split(",");
                    String newGates = "";
                    int judge = 0;
                    for (String i : originGates) {
                        if (!i.equals(gateid)) {
                            newGates += i + ',';
                            judge = 1;
                        }
                    }
                    if (judge == 1) {
                        newGates = newGates.substring(0, newGates.length() - 1);
                    }
                    Relation relation = new Relation();
                    if (newGates != "") {
                        relation.setBinder(re.getBinder());
                        relation.setBinded(re.getBinded());
                        relation.setGateid(newGates);
                        userService.saveRelation(relation);
                    }
                    break;
                }
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


    //查询被绑定用户所对应的网关
    @RequestMapping(value = "/getGates", method = RequestMethod.POST)
    @ResponseBody
    public Result getGates(@RequestBody String loginInfo) {
        JsonObject info = new JsonParser().parse(loginInfo).getAsJsonObject();
        Result result = new Result();
        try {
            String binded = info.get("customerid").getAsString();
            int bindedId = Integer.parseInt(binded);
            List<Relation> relations = userService.getBindedRelations(bindedId);
            if (relations.size() == 0) {
                result.setStatus("error");
                result.setResultMsg("未找到绑定网关");
                return result;
            }
            List list = new ArrayList();

            for (Relation relation : relations) {
                User user = userService.findUserById(relation.getBinder());
                Map map = new HashMap();
                map.put("phone", user.getPhone());
                map.put("gates", relation.getGateid());
                map.put("remark",relation.getRemark());
                list.add(map);
            }
            result.setData(list);

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
