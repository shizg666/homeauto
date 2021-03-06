package com.landleaf.homeauto.common.domain.qry;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author Lokiy
 * @date 2019/8/13 15:29
 * @description:
 */
@Data
@ApiModel("消息类型查询")
public class MsgQry extends BaseQry {

    @ApiModelProperty("查询名称")
    private String name;

    @ApiModelProperty("地址类型")
    private String path;

    @ApiModelProperty("多选地址集合")
    private List<String> paths;

    @ApiModelProperty("地址code")
    private String code;

    @ApiModelProperty(value = "发布标识 0-未发布 1-发布", required = true)
    private Integer releaseFlag;

    @ApiModelProperty("发布人")
    private String releaseUser;


    @ApiModelProperty(value = "场景/联动类型 **在场景/联动中必传,1-场景 2-联动**")
    private Integer rcmdSlType;
}
