package com.douyuehan.doubao.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.douyuehan.doubao.common.api.ApiResult;
import com.douyuehan.doubao.enumSetting.SettingType;
import com.douyuehan.doubao.mapper.UmsUserMapper;
import com.douyuehan.doubao.model.dto.LoginDTO;
import com.douyuehan.doubao.model.dto.RegisterDTO;
import com.douyuehan.doubao.model.entity.BmsPost;
import com.douyuehan.doubao.model.entity.UmsUser;
import com.douyuehan.doubao.service.IBmsPostService;
import com.douyuehan.doubao.service.IUmsUserService;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

import static com.douyuehan.doubao.jwt.JwtUtil.USER_NAME;


@RestController
@RequestMapping("/ums/user")
public class UmsUserController extends BaseController {
    @Resource
    private IUmsUserService iUmsUserService;
    @Resource
    private IBmsPostService iBmsPostService;
    
    @Autowired
    private UmsUserMapper umsUserMapper;
    
	public static String info;

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ApiResult<Map<String, Object>> register(@Valid @RequestBody RegisterDTO dto) {
        UmsUser user = iUmsUserService.executeRegister(dto);
        if (ObjectUtils.isEmpty(user)) {
            return ApiResult.failed("账号注册失败");
        }
        Map<String, Object> map = new HashMap<>(16);
        map.put("user", user);
        return ApiResult.success(map);
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ApiResult<Map<String, String>> login(@Valid @RequestBody LoginDTO dto) {
        String token = iUmsUserService.executeLogin(dto);
        if (ObjectUtils.isEmpty(token)) {
            return ApiResult.failed("账号密码错误");
        }
        Map<String, String> map = new HashMap<>(16);
        map.put("token", token);
        return ApiResult.success(map, "登录成功");
    }

    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public ApiResult<UmsUser> getUser(@RequestHeader(value = USER_NAME) String userName) {
        UmsUser user = iUmsUserService.getUserByUsername(userName);
        return ApiResult.success(user);
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public ApiResult<Object> logOut() {
        return ApiResult.success(null, "注销成功");
    }

    @GetMapping("/{username}")
    public ApiResult<Map<String, Object>> getUserByName(@PathVariable("username") String username,
                                                        @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
                                                        @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Map<String, Object> map = new HashMap<>(16);
        UmsUser user = iUmsUserService.getUserByUsername(username);
        Assert.notNull(user, "用户不存在");
        Page<BmsPost> page = iBmsPostService.page(new Page<>(pageNo, size),
                new LambdaQueryWrapper<BmsPost>().eq(BmsPost::getUserId, user.getId()));
        map.put("user", user);
        map.put("topics", page);
        return ApiResult.success(map);
    }
    @PostMapping("/update")
    public ApiResult<UmsUser> updateUser(@RequestBody Map<String,UmsUser> settingMap) {
    	String key = settingMap.keySet().iterator().next();
    	UmsUser umsUser = settingMap.get(key);
    	UmsUser userDb = iUmsUserService.getUserByUsername(umsUser.getUsername());
		LambdaUpdateWrapper<UmsUser> updateWrapper = Wrappers.lambdaUpdate();
    	if(modifyCheck(userDb,umsUser,key,updateWrapper)) {
//            iUmsUserService.updateById(umsUser);
    		umsUserMapper.update(null, updateWrapper);
            return ApiResult.success(umsUser);
    	}else {
            return ApiResult.success(umsUser,info+"は変更されていません、変更して提出してください。");
    	}
    }
    public boolean modifyCheck(UmsUser userDb,UmsUser umsUser,String key,
    		LambdaUpdateWrapper<UmsUser> updateWrapper) {
    	boolean modify = true;
    	if(key.equals(SettingType.BaseInfo.getSettingForm())) {
        	if(userDb.getAlias().equals(umsUser.getAlias()) &&
        			userDb.getBio().equals(umsUser.getBio())) {
        		modify = false;
        		info = SettingType.BaseInfo.getSettingName();
        	}else {
                updateWrapper.eq(UmsUser::getId, umsUser.getId())
                             .set(UmsUser::getBio, umsUser.getBio())
                             .set(UmsUser::getAlias, umsUser.getAlias());
        	}
    	}else if(key.equals(SettingType.EmailInfo.getSettingForm())) {
        	if(userDb.getEmail().equals(umsUser.getEmail())) {
        		modify = false;
        		info = SettingType.EmailInfo.getSettingName();
        	}else {
                updateWrapper.eq(UmsUser::getId, umsUser.getId())
                             .set(UmsUser::getEmail, umsUser.getEmail());
        	}
    	}else if(key.equals(SettingType.phoneInfo.getSettingForm())) {
        	if(userDb.getMobile().equals(umsUser.getMobile())) {
        		modify = false;
        		info = SettingType.phoneInfo.getSettingName();
        	}else {
                updateWrapper.eq(UmsUser::getId, umsUser.getId())
                .set(UmsUser::getMobile, umsUser.getMobile());
        	}
    	}
    	return modify;
    }
}
