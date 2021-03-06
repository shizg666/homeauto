package com.landleaf.homeauto.common.domain.dto.oauth.sysuser;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * 系统账号修改请求DTO
 *
 * @author wenyilu*/
@Data
@ApiModel(value = "SysUserUpdateReqDTO", description = "系统账号修改请求DTO")
public class SysUserUpdateReqDTO {

    @ApiModelProperty(value = "主键",required = true)
    private String id;

    @ApiModelProperty(value = "邮箱（用户名）",required = true)
    private String email;

    @ApiModelProperty(value = "密码",required = true)
    private String password;

    @ApiModelProperty(value = "姓名",required = true)
    private String name;

    @ApiModelProperty(value = "手机号",required = true)
    private String mobile;

    @ApiModelProperty(value = "所选角色ID",required = true)
    private String roleId;

}
