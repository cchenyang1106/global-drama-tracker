package com.drama.tracker.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("activity_quiz")
public class ActivityQuiz {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long activityId;
    private String questionText;
    private String questionImages;
    private String questionFiles;
    private Integer sortOrder;
    private LocalDateTime createTime;
}
