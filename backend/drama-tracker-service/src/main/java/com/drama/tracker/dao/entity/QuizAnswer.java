package com.drama.tracker.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("quiz_answer")
public class QuizAnswer {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long activityId;
    private Long quizId;
    private Long userId;
    private String answerText;
    private String answerImages;
    private String answerFiles;
    private Integer status; // 0待批改 1通过 2不通过
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
