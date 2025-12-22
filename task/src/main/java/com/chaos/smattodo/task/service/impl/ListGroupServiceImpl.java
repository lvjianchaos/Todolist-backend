package com.chaos.smattodo.task.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chaos.smattodo.task.common.enums.ListGroupErrorCodeEnum;
import com.chaos.smattodo.task.common.exception.ServiceException;
import com.chaos.smattodo.task.entity.ListGroup;
import com.chaos.smattodo.task.dto.req.ListGroupSaveReqDTO;
import com.chaos.smattodo.task.dto.req.ListGroupUpdateNameReqDTO;
import com.chaos.smattodo.task.dto.req.ListGroupUpdateSortOrderReqDTO;
import com.chaos.smattodo.task.dto.resp.ListGroupRespDTO;
import com.chaos.smattodo.task.mapper.ListGroupMapper;
import com.chaos.smattodo.task.service.ListGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListGroupServiceImpl implements ListGroupService {

    private final ListGroupMapper listGroupMapper;

    @Override
    public List<ListGroupRespDTO> listListGroupByUserId(Long userId) {
        LambdaQueryWrapper<ListGroup> queryWrapper = Wrappers.lambdaQuery(ListGroup.class)
                .eq(ListGroup::getUserId, userId)
                .orderByAsc(ListGroup::getSortOrder);
        List<ListGroup> listGroups = listGroupMapper.selectList(queryWrapper);

        // 使用 Stream API 将 Entity 列表转换为 DTO 列表
        return listGroups.stream()
                .map(item -> BeanUtil.copyProperties(item, ListGroupRespDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public void saveListGroup(Long userId, ListGroupSaveReqDTO dto) {
        ListGroup listGroup = BeanUtil.copyProperties(dto, ListGroup.class);
        listGroup.setUserId(userId);
        listGroupMapper.insert(listGroup);
    }

    @Override
    public void removeListGroup(Long listGroupId) {
        listGroupMapper.deleteById(listGroupId);
    }

    @Override
    public void updateListGroupName(ListGroupUpdateNameReqDTO dto) {
        ListGroup listGroup = new ListGroup();
        listGroup.setId(dto.getId());
        listGroup.setName(dto.getName());
        listGroupMapper.updateById(listGroup);
    }

    @Override
    public void updateListGroupSortOrder(ListGroupUpdateSortOrderReqDTO dto) {
        Double prev = dto.getPrevSortOrder();
        Double next = dto.getNextSortOrder();
        Double newSortOrder;

        if (prev == 0 && next == 0) {
            // 没有任何参照（第一个元素）：初始值 1000
            newSortOrder = 1000.0;
        } else if (prev == 0 && next > 0) {
            // 移动到最前面：取后一个的一半
            newSortOrder = next / 2;
        } else if (next == 0 && prev > 0) {
            // 移动到最后面：前一个 + 1000
            newSortOrder = prev + 1000.0;
        } else {
            // 移动到中间：取前后平均值
            // 校验顺序是否正确
            if (prev >= next) {
                throw new ServiceException(ListGroupErrorCodeEnum.LIST_GROUP_SORT_ORDER_ERROR);
            }
            newSortOrder = (prev + next) / 2;
        }

        ListGroup listGroup = new ListGroup();
        listGroup.setId(dto.getId());
        listGroup.setSortOrder(newSortOrder);
        listGroupMapper.updateById(listGroup);
    }
}
