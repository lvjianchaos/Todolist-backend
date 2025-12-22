package com.chaos.smattodo.task.service;

import com.chaos.smattodo.task.dto.req.ListGroupSaveReqDTO;
import com.chaos.smattodo.task.dto.req.ListGroupUpdateNameReqDTO;
import com.chaos.smattodo.task.dto.req.ListGroupUpdateSortOrderReqDTO;
import com.chaos.smattodo.task.dto.resp.ListGroupRespDTO;

import java.util.List;

public interface ListGroupService {

    /**
     * 根据 userId 获取
     * @param userId
     * @return 清单分组 DTO 列表
     */
    List<ListGroupRespDTO> listListGroupByUserId(Long userId);

    /**
     * 保存清单分组
     * @param userId
     * @param dto
     */
    void saveListGroup(Long userId, ListGroupSaveReqDTO dto);

    /**
     * 删除清单分组（包含其下的所有 清单 > 任务分组 > 任务）
     * @param listGroupId
     */
    void removeListGroup(Long listGroupId);

    /**
     * 更新清单分组名称
     * @param dto
     */
    void updateListGroupName(ListGroupUpdateNameReqDTO dto);

    /**
     * 更新清单分组排序
     * @param dto
     */
    void updateListGroupSortOrder(ListGroupUpdateSortOrderReqDTO dto);

}
