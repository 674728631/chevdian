package com.cvd.chevdian.controller;

import com.cvd.chevdian.bean.distribute.UserDistributor;
import com.cvd.chevdian.bean.system.City;
import com.cvd.chevdian.bean.system.Role;
import com.cvd.chevdian.bean.system.RoleMenu;
import com.cvd.chevdian.bean.vo.SearchInfo;
import com.cvd.chevdian.common.annotation.ApiAuth;
import com.cvd.chevdian.common.annotation.LogAnnotation;
import com.cvd.chevdian.common.constant.DBDict;
import com.cvd.chevdian.common.constant.GlobalConstant;
import com.cvd.chevdian.common.enums.LogTypeEnum;
import com.cvd.chevdian.common.enums.RoleAuth;
import com.cvd.chevdian.common.wrapper.WrapMapper;
import com.cvd.chevdian.common.wrapper.Wrapper;
import com.cvd.chevdian.service.UserDistributorService;
import com.cvd.chevdian.service.system.CityService;
import com.cvd.chevdian.service.system.RoleMenuService;
import com.cvd.chevdian.service.system.RoleService;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;


@Api(tags = "我的API")
@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class UserDistributorController {

    @Autowired
    RoleService roleService;
    @Autowired
    RoleMenuService roleMenuService;
    @Autowired
    CityService cityService;
    @Autowired
    UserDistributorService userDistributorService;


    @ApiOperation("用户详情")
    @GetMapping("/loginUser")
    public Wrapper<UserDistributor> getLoginUserInfo(HttpServletRequest request) {
        UserDistributor userDistributor = (UserDistributor) request.getSession().getAttribute(GlobalConstant.LOGIN_USER);
        return WrapMapper.ok(userDistributor);
    }

    @ApiAuth(role = {RoleAuth.ADMIN})
    @ApiOperation("角色类型列表")
    @GetMapping("/role/type")
    public Wrapper<List<Role>> listRole() throws Exception {

        return WrapMapper.ok(roleService.listRole());
    }

    @LogAnnotation(logType = LogTypeEnum.OPERATION_LOG, isSaveRequestData = true)
    @ApiAuth(role = {RoleAuth.ADMIN})
    @ApiOperation("创建角色")
    @PostMapping("/role/save")
    public Wrapper saveRoleMenu(@RequestBody RoleMenu roleMenu) {
        roleMenuService.save(roleMenu);
        return WrapMapper.ok();
    }

    @LogAnnotation(logType = LogTypeEnum.OPERATION_LOG, isSaveRequestData = true)
    @ApiAuth(role = {RoleAuth.ADMIN})
    @ApiOperation("编辑角色")
    @PostMapping("/role/edit")
    public Wrapper updateRoleMenu(@RequestBody RoleMenu roleMenu) {
        roleMenuService.update(roleMenu);
        return WrapMapper.ok();
    }

    @LogAnnotation(logType = LogTypeEnum.OPERATION_LOG, isSaveRequestData = true)
    @ApiAuth(role = {RoleAuth.ADMIN})
    @ApiOperation("删除角色")
    @DeleteMapping("/role/delete/{id}")
    public Wrapper saveRoleMenu(@PathVariable("id") Integer id) {
        roleMenuService.delete(id);
        return WrapMapper.ok();
    }

    @ApiAuth(role = {RoleAuth.ADMIN})
    @ApiOperation("查询角色使用情况,>0在使用")
    @GetMapping("/role/user/{id}")
    public Wrapper<Integer> isUsed(@PathVariable("id") Integer id) {
        return WrapMapper.ok(userDistributorService.selectByRoleId(id).size());
    }

    @ApiAuth(role = {RoleAuth.ADMIN})
    @ApiOperation("角色列表")
    @GetMapping("/role/list")
    public Wrapper<PageInfo<RoleMenu>> listRoleMenu(@RequestParam(name = "pageNo", required = false, defaultValue = "1") Integer pageNo,
                                                    @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        return WrapMapper.ok(roleMenuService.ListRoleMenu(pageNo, pageSize));
    }

    @ApiAuth(role = {RoleAuth.ADMIN})
    @ApiOperation("角色详情")
    @GetMapping("/role/detail/{id}")
    public Wrapper<RoleMenu> roleMenuDetail(@PathVariable("id") Integer roleMenuId) {
        return WrapMapper.ok(roleMenuService.getRoleMenu(roleMenuId));
    }

    @ApiOperation("地区列表")
    @GetMapping("/city/list")
    public Wrapper<List<City>> listCity() {
        return WrapMapper.ok(cityService.listCity());
    }

    @ApiOperation("搜索")
    @PostMapping("/search")
    public Wrapper<PageInfo<UserDistributor>> search(HttpServletRequest request, @RequestBody SearchInfo searchInfo) {
        UserDistributor loginUser = (UserDistributor) request.getSession().getAttribute(GlobalConstant.LOGIN_USER);
        return WrapMapper.ok(userDistributorService.searchByTypeAndInfo(loginUser, searchInfo));
    }
}
