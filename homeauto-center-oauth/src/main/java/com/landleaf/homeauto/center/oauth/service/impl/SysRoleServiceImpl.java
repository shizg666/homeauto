package com.landleaf.homeauto.center.oauth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.landleaf.homeauto.center.oauth.mapper.SysRoleMapper;
import com.landleaf.homeauto.center.oauth.service.ISysRolePermissionScopService;
import com.landleaf.homeauto.center.oauth.service.ISysRolePermissionService;
import com.landleaf.homeauto.center.oauth.service.ISysRoleService;
import com.landleaf.homeauto.common.domain.dto.oauth.sysrole.*;
import com.landleaf.homeauto.common.domain.po.oauth.SysRole;
import com.landleaf.homeauto.common.domain.po.oauth.SysRolePermission;
import com.landleaf.homeauto.common.domain.po.oauth.SysRolePermissionScop;
import com.landleaf.homeauto.common.domain.vo.BasePageVO;
import com.landleaf.homeauto.common.domain.vo.oauth.SysRoleSelectVO;
import com.landleaf.homeauto.common.enums.DelFlagEnum;
import com.landleaf.homeauto.common.enums.StatusEnum;
import com.landleaf.homeauto.common.enums.oauth.RoleTypeEnum;
import com.landleaf.homeauto.common.exception.BusinessException;
import com.landleaf.homeauto.common.redis.RedisUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.landleaf.homeauto.common.constance.ErrorCodeEnumConst.*;

/**
 * <p>
 * 后台账号角色表 服务实现类
 * </p>
 *
 * @author wyl
 * @since 2019-08-12
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private ISysRolePermissionService sysRolePermissionService;
    @Autowired
    private ISysRolePermissionScopService sysRolePermissionScopService;

    @Override
    public List<SysRole> queryRolesByIds(List<String> roleIds) {
        List<SysRole> result = Lists.newArrayList();
        Collection<SysRole> sysRoles = listByIds(roleIds);
        if (!CollectionUtils.isEmpty(sysRoles)) {
            result.addAll(sysRoles);
        }
        return result;
    }

    @Override
    public SysRoleDTO getSysRoleInfo(String roleId) {
        SysRoleDTO result = new SysRoleDTO();
        SysRole sysRole = getById(roleId);
        if (sysRole != null) {
            BeanUtils.copyProperties(sysRole, result);
            result.setRoleTypeName(RoleTypeEnum.getEnumByType(result.getRoleType()).getName());
            result.setStatusName(StatusEnum.getStatusByType(result.getStatus()).getName());
        }
        return result;
    }

    @Override
    public BasePageVO<SysRoleDTO> pageListSysRoles(SysRolePageReqDTO requestBody) {
        BasePageVO<SysRoleDTO> result = new BasePageVO<SysRoleDTO>();
        List<SysRoleDTO> data = Lists.newArrayList();
        PageHelper.startPage(requestBody.getPageNum(), requestBody.getPageSize(), true);
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        if (requestBody.getRoleType() != null) {
            queryWrapper.eq(SysRole::getRoleType, requestBody.getRoleType());
        }
        if (requestBody.getStatus() != null) {
            queryWrapper.eq(SysRole::getStatus, requestBody.getStatus());
        }
        if (!StringUtils.isEmpty(requestBody.getRoleName())) {
            queryWrapper.and(wapper -> wapper.like(SysRole::getRoleName, requestBody.getRoleName()));
        }
        if (!StringUtils.isEmpty(requestBody.getRemark())) {
            queryWrapper.and(wapper -> wapper.like(SysRole::getRemark, requestBody.getRemark()));
        }
        queryWrapper.orderByDesc(SysRole::getCreateTime);
        Page<SysRole> page = new Page<>();
        BeanUtils.copyProperties(requestBody, page);
        List<SysRole> sysRoles = list(queryWrapper);
        PageInfo pageInfo = new PageInfo(sysRoles);
        if (!CollectionUtils.isEmpty(sysRoles)) {
            data.addAll(sysRoles.stream().map(i -> {
                SysRoleDTO tmp = new SysRoleDTO();
                BeanUtils.copyProperties(i, tmp);
                tmp.setRoleTypeName(RoleTypeEnum.getEnumByType(tmp.getRoleType()).getName());
                tmp.setStatusName(StatusEnum.getStatusByType(tmp.getStatus()).getName());
                return tmp;
            }).collect(Collectors.toList()));
        }
        pageInfo.setList(data);
        BeanUtils.copyProperties(pageInfo, result);
        return result;
    }

    @Override
    @Transactional
    public boolean updateSysRole(SysRoleUpdateComplexReqDTO requestBody) {
        SysRoleUpdateReqDTO sysRole = requestBody.getSysRole();
        SysRoleDTO params = new SysRoleDTO();
        BeanUtils.copyProperties(sysRole, params);
        if (!saveOrUpdateValidParams(params, true)) {
            throw new BusinessException(CHECK_PARAM_ERROR);
        }
        SysRole updateRole = new SysRole();
        BeanUtils.copyProperties(sysRole, updateRole);
        SysRole existRole = getById(sysRole.getId());
        if (existRole == null) {
            throw new BusinessException(ROLE_NOT_EXIST_ERROE);
        }
        boolean updateRoleFlag = updateById(updateRole);
        List<String> sysPermissions = requestBody.getSysPermissionIds();
        sysRolePermissionService.updateRolePermissions(existRole.getId(), sysPermissions);
        List<String> scopPaths = requestBody.getScopPaths();
        sysRolePermissionScopService.updateRolePermissionScop(existRole.getId(), scopPaths);
        return updateById(updateRole);
    }

    private boolean saveOrUpdateValidParams(SysRoleDTO params, boolean update) {
        if (params == null) {
            return false;
        }
        String id = params.getId();

        if (StringUtils.isEmpty(id) && update) {
            return false;
        }
        if (StringUtils.isEmpty(params.getRoleName()) ||
                StringUtils.isEmpty(params.getRoleType())) {
            return false;
        }
        //校验角色名称是否存在
        QueryWrapper<SysRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_name", params.getRoleName())
                .eq("del_flag", DelFlagEnum.UNDELETE.getType());
        if (update) {
            List<String> ids = Lists.newArrayList();
            ids.add(params.getId());
            queryWrapper.notIn("id", ids);
        }
        int count = count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ROLE_EXIST_ERROE);
        }
        return true;
    }

    @Override
    public boolean addSysRole(SysRoleAddComplexReqDTO requestBody) {
        SysRoleAddReqDTO sysRole = requestBody.getSysRole();
        SysRoleDTO params = new SysRoleDTO();
        BeanUtils.copyProperties(sysRole, params);
        if (!saveOrUpdateValidParams(params, false)) {
            throw new BusinessException(CHECK_PARAM_ERROR);
        }
        SysRole saveRole = new SysRole();
        BeanUtils.copyProperties(sysRole, saveRole);
        save(saveRole);
        List<String> sysPermissions = requestBody.getSysPermissionIds();
        if (!CollectionUtils.isEmpty(sysPermissions)) {
            sysRolePermissionService.updateRolePermissions(saveRole.getId(), sysPermissions);
        }
        List<String> scopPaths = requestBody.getScopPaths();
        if (!CollectionUtils.isEmpty(scopPaths)) {
            sysRolePermissionScopService.updateRolePermissionScop(saveRole.getId(), scopPaths);
        }
        return true;
    }

    @Override
    public boolean deleteSysRoles(List<String> ids) {
        return removeByIds(ids);
    }

    @Override
    public List<SysRole> queryAllRole() {
        QueryWrapper<SysRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("del_flag", DelFlagEnum.UNDELETE.getType());
        return list(queryWrapper);
    }

    @Override
    public List<SysRoleSelectVO> listSysRolesByStatus(Integer status) {
        List<SysRoleSelectVO> result = Lists.newArrayList();
        QueryWrapper<SysRole> queryWrapper = new QueryWrapper<>();
        if (status != null) {
            queryWrapper.eq("status", StatusEnum.ACTIVE.getType());
        }
        List<SysRole> list = list(queryWrapper);
        if (!CollectionUtils.isEmpty(list)) {
            result.addAll(list.stream().map(i -> {
                SysRoleSelectVO tmp = new SysRoleSelectVO();
                BeanUtils.copyProperties(i, tmp);
                return tmp;
            }).collect(Collectors.toList()));
        }
        return result;
    }

    @Override
    public void updateStatus(SysRoleUpdateStatusReqDTO requestBody) {
        Integer status = requestBody.getStatus();
        String roleId = requestBody.getRoleId();
        SysRole existRole = getById(roleId);
        if (existRole == null) {
            throw new BusinessException(ROLE_NOT_EXIST_ERROE);
        }
        SysRole updateRole = new SysRole();
        updateRole.setId(roleId);
        updateRole.setStatus(status);
        updateById(updateRole);
    }

    @Override
    public SysRoleAddComplexReqDTO getSysRoleComplexInfo(String roleId) {
        SysRoleAddComplexReqDTO result = new SysRoleAddComplexReqDTO();
        SysRoleAddReqDTO sysRole = new SysRoleAddReqDTO();
        List<String> sysPermissionIds = Lists.newArrayList();
        List<String> scopPaths = Lists.newArrayList();
        //获取角色基本信息
        SysRole role = getById(roleId);
        if (role == null) {
            return result;
        }

        BeanUtils.copyProperties(role, sysRole);
        //根据角色获取权限信息
        List<SysRolePermission> rolePermissions = sysRolePermissionService.getRolePermissionByRole(roleId);
        if (!CollectionUtils.isEmpty(rolePermissions)) {
            sysPermissionIds.addAll(rolePermissions.stream().map(i -> i.getPermissionId()).collect(Collectors.toList()));
        }
        List<SysRolePermissionScop> permissionScops = sysRolePermissionScopService.getPermissionScopByRoleId(roleId);
        if (!CollectionUtils.isEmpty(permissionScops)) {
            scopPaths.addAll(permissionScops.stream().map(i -> i.getPath()).collect(Collectors.toList()));
        }
        result.setSysRole(sysRole);
        result.setSysPermissionIds(sysPermissionIds);
        result.setScopPaths(scopPaths);
        return result;
    }
}
