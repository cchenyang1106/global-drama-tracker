package com.drama.tracker.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.drama.tracker.common.result.Result;
import com.drama.tracker.common.util.SensitiveWordFilter;
import com.drama.tracker.dao.entity.*;
import com.drama.tracker.dao.mapper.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    @Autowired private ActivityQuizMapper quizMapper;
    @Autowired private QuizAnswerMapper answerMapper;
    @Autowired private ActivityMapper activityMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private UserProfileMapper profileMapper;
    @Autowired private GroupChatMapper groupChatMapper;
    @Autowired private GroupMemberInfoMapper groupMemberMapper;
    @Autowired private MatchRequestMapper matchRequestMapper;

    @Value("${jwt.secret:drama-tracker-jwt-secret}")
    private String jwtSecret;

    // ============= 发布人出题 =============

    /**
     * 保存/更新题目（发布人）。
     */
    @PostMapping("/save")
    public Result<?> saveQuiz(@RequestHeader(value = "Authorization", required = false) String auth,
                              @RequestBody Map<String, Object> body) {
        Long userId = getUserIdFromToken(auth);
        if (userId == null) return Result.fail(401, "请先登录");

        Long activityId = Long.valueOf(body.get("activityId").toString());
        Activity a = activityMapper.selectById(activityId);
        if (a == null || !a.getUserId().equals(userId)) return Result.fail(403, "只有发布者可以出题");

        // 敏感词检查
        String text = (String) body.get("questionText");
        String bad = SensitiveWordFilter.detect(text);
        if (bad != null) return Result.fail("题目包含违规词汇「" + bad + "」");

        ActivityQuiz quiz = new ActivityQuiz();
        if (body.get("id") != null) {
            quiz = quizMapper.selectById(Long.valueOf(body.get("id").toString()));
            if (quiz == null) return Result.fail(404, "题目不存在");
        }
        quiz.setActivityId(activityId);
        quiz.setQuestionText(text);
        quiz.setQuestionImages((String) body.get("questionImages"));
        quiz.setQuestionFiles((String) body.get("questionFiles"));
        quiz.setSortOrder(body.get("sortOrder") != null ? Integer.parseInt(body.get("sortOrder").toString()) : 0);
        quiz.setCreateTime(LocalDateTime.now());

        if (quiz.getId() == null) quizMapper.insert(quiz);
        else quizMapper.updateById(quiz);

        return Result.success(quiz.getId());
    }

    /**
     * 删除题目。
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteQuiz(@RequestHeader(value = "Authorization", required = false) String auth,
                                @PathVariable Long id) {
        Long userId = getUserIdFromToken(auth);
        if (userId == null) return Result.fail(401, "请先登录");
        ActivityQuiz quiz = quizMapper.selectById(id);
        if (quiz == null) return Result.fail(404, "题目不存在");
        Activity a = activityMapper.selectById(quiz.getActivityId());
        if (a == null || !a.getUserId().equals(userId)) return Result.fail(403, "无权操作");
        quizMapper.deleteById(id);
        answerMapper.delete(new LambdaQueryWrapper<QuizAnswer>().eq(QuizAnswer::getQuizId, id));
        return Result.success("已删除");
    }

    /**
     * 获取活动的题目列表。
     */
    @GetMapping("/list/{activityId}")
    public Result<?> getQuizList(@PathVariable Long activityId) {
        List<ActivityQuiz> list = quizMapper.selectList(
                new LambdaQueryWrapper<ActivityQuiz>()
                        .eq(ActivityQuiz::getActivityId, activityId)
                        .orderByAsc(ActivityQuiz::getSortOrder));
        return Result.success(list);
    }

    // ============= 申请人答题 =============

    /**
     * 提交答卷。
     */
    @PostMapping("/answer")
    public Result<?> submitAnswer(@RequestHeader(value = "Authorization", required = false) String auth,
                                  @RequestBody Map<String, Object> body) {
        Long userId = getUserIdFromToken(auth);
        if (userId == null) return Result.fail(401, "请先登录");

        Long activityId = Long.valueOf(body.get("activityId").toString());
        Activity a = activityMapper.selectById(activityId);
        if (a == null) return Result.fail(404, "活动不存在");
        if (a.getUserId().equals(userId)) return Result.fail("不能答自己的题");
        if (a.getTeamComplete() != null && a.getTeamComplete() == 1) return Result.fail("活动已组队完成");

        // 检查是否已答过
        Long existing = answerMapper.selectCount(new LambdaQueryWrapper<QuizAnswer>()
                .eq(QuizAnswer::getActivityId, activityId).eq(QuizAnswer::getUserId, userId));
        if (existing > 0) return Result.fail("你已经答过题了");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> answers = (List<Map<String, Object>>) body.get("answers");
        if (answers == null || answers.isEmpty()) return Result.fail("请填写答案");

        for (Map<String, Object> ans : answers) {
            String ansText = (String) ans.get("answerText");
            String bad = SensitiveWordFilter.detect(ansText);
            if (bad != null) return Result.fail("答案包含违规词汇「" + bad + "」");

            QuizAnswer qa = new QuizAnswer();
            qa.setActivityId(activityId);
            qa.setQuizId(Long.valueOf(ans.get("quizId").toString()));
            qa.setUserId(userId);
            qa.setAnswerText(ansText);
            qa.setAnswerImages((String) ans.get("answerImages"));
            qa.setAnswerFiles((String) ans.get("answerFiles"));
            qa.setStatus(0);
            qa.setCreateTime(LocalDateTime.now());
            answerMapper.insert(qa);
        }

        // 同时创建 match_request 记录
        Long existMatch = matchRequestMapper.selectCount(new LambdaQueryWrapper<MatchRequest>()
                .eq(MatchRequest::getActivityId, activityId).eq(MatchRequest::getFromUserId, userId));
        if (existMatch == 0) {
            MatchRequest mr = new MatchRequest();
            mr.setActivityId(activityId);
            mr.setFromUserId(userId);
            mr.setToUserId(a.getUserId());
            mr.setMessage("已提交答卷，等待批改");
            mr.setStatus(0);
            matchRequestMapper.insert(mr);
        }

        return Result.success("答卷已提交，等待发布人批改");
    }

    // ============= 发布人批改 =============

    /**
     * 获取所有申请人的答卷（发布人查看）。
     */
    @GetMapping("/papers/{activityId}")
    public Result<?> getPapers(@RequestHeader(value = "Authorization", required = false) String auth,
                               @PathVariable Long activityId) {
        Long userId = getUserIdFromToken(auth);
        if (userId == null) return Result.fail(401, "请先登录");
        Activity a = activityMapper.selectById(activityId);
        if (a == null || !a.getUserId().equals(userId)) return Result.fail(403, "只有发布者可以查看");

        // 按用户分组
        List<QuizAnswer> allAnswers = answerMapper.selectList(
                new LambdaQueryWrapper<QuizAnswer>().eq(QuizAnswer::getActivityId, activityId));

        Map<Long, List<QuizAnswer>> grouped = allAnswers.stream()
                .collect(Collectors.groupingBy(QuizAnswer::getUserId));

        List<Map<String, Object>> papers = new ArrayList<>();
        for (Map.Entry<Long, List<QuizAnswer>> entry : grouped.entrySet()) {
            Map<String, Object> paper = new LinkedHashMap<>();
            Long uid = entry.getKey();
            User user = userMapper.selectById(uid);
            paper.put("userId", uid);
            paper.put("nickname", user != null ? user.getNickname() : "未知");
            UserProfile profile = profileMapper.selectOne(
                    new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, uid));
            if (profile != null) {
                paper.put("city", profile.getCity());
                paper.put("age", profile.getAge());
                paper.put("gender", profile.getGender());
            }
            // 整体状态：全部通过=1，有不通过=2，否则=0
            List<QuizAnswer> answers = entry.getValue();
            boolean allPassed = answers.stream().allMatch(q -> q.getStatus() == 1);
            boolean anyFailed = answers.stream().anyMatch(q -> q.getStatus() == 2);
            paper.put("status", allPassed ? 1 : anyFailed ? 2 : 0);
            paper.put("answers", answers);
            papers.add(paper);
        }
        return Result.success(papers);
    }

    /**
     * 批改（通过/不通过某人的全部答卷）。
     */
    @PostMapping("/grade/{activityId}/{targetUserId}")
    public Result<?> grade(@RequestHeader(value = "Authorization", required = false) String auth,
                           @PathVariable Long activityId, @PathVariable Long targetUserId,
                           @RequestBody Map<String, Object> body) {
        Long userId = getUserIdFromToken(auth);
        if (userId == null) return Result.fail(401, "请先登录");
        Activity a = activityMapper.selectById(activityId);
        if (a == null || !a.getUserId().equals(userId)) return Result.fail(403, "只有发布者可以批改");

        Integer result = Integer.valueOf(body.get("status").toString()); // 1通过 2不通过
        String remark = (String) body.getOrDefault("remark", "");

        // 更新该用户所有答案状态
        List<QuizAnswer> answers = answerMapper.selectList(new LambdaQueryWrapper<QuizAnswer>()
                .eq(QuizAnswer::getActivityId, activityId).eq(QuizAnswer::getUserId, targetUserId));
        for (QuizAnswer qa : answers) {
            qa.setStatus(result);
            qa.setRemark(remark);
            qa.setUpdateTime(LocalDateTime.now());
            answerMapper.updateById(qa);
        }

        // 更新 match_request 状态
        MatchRequest mr = matchRequestMapper.selectOne(new LambdaQueryWrapper<MatchRequest>()
                .eq(MatchRequest::getActivityId, activityId).eq(MatchRequest::getFromUserId, targetUserId));
        if (mr != null) {
            mr.setStatus(result); // 1同意 2拒绝
            matchRequestMapper.updateById(mr);
        }

        // 如果通过，更新活动已加入人数
        if (result == 1) {
            a.setJoinedCount((a.getJoinedCount() != null ? a.getJoinedCount() : 0) + 1);
            activityMapper.updateById(a);

            // 自动创建/加入群聊
            GroupChat group = groupChatMapper.selectOne(
                    new LambdaQueryWrapper<GroupChat>().eq(GroupChat::getActivityId, activityId));
            if (group == null) {
                group = new GroupChat();
                group.setActivityId(activityId);
                group.setName(a.getTitle());
                group.setCreateTime(LocalDateTime.now());
                groupChatMapper.insert(group);
                // 群主（发布人）
                GroupMemberInfo owner = new GroupMemberInfo();
                owner.setGroupId(group.getId());
                owner.setUserId(userId);
                owner.setRole(1);
                owner.setCreateTime(LocalDateTime.now());
                groupMemberMapper.insert(owner);
            }
            // 加入新成员
            Long exists = groupMemberMapper.selectCount(new LambdaQueryWrapper<GroupMemberInfo>()
                    .eq(GroupMemberInfo::getGroupId, group.getId()).eq(GroupMemberInfo::getUserId, targetUserId));
            if (exists == 0) {
                GroupMemberInfo member = new GroupMemberInfo();
                member.setGroupId(group.getId());
                member.setUserId(targetUserId);
                member.setRole(0);
                member.setCreateTime(LocalDateTime.now());
                groupMemberMapper.insert(member);
            }
        }

        return Result.success(result == 1 ? "已通过，已自动拉入群聊" : "已标记不通过");
    }

    private Long getUserIdFromToken(String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) return null;
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            return Long.valueOf(Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(auth.substring(7)).getPayload().getSubject());
        } catch (Exception e) { return null; }
    }
}
