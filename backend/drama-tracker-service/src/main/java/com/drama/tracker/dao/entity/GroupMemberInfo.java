package com.drama.tracker.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("group_member_info")
public class GroupMemberInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long groupId;
    private Long userId;
    private Integer role; // 0普通 1群主
    private LocalDateTime lastReadTime;
    private LocalDateTime createTime;
}
