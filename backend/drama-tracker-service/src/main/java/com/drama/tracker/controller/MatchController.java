package com.drama.tracker.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.drama.tracker.common.result.Result;
import com.drama.tracker.common.util.AesUtil;
import com.drama.tracker.dao.entity.*;
import com.drama.tracker.dao.mapper.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(name = "匹配与聊天", description = "搭子匹配和聊天接口")
@RestController
@RequestMapping("/api/match")
@RequiredArgsConstructor
public class MatchController {

    private final MatchRequestMapper matchMapper;
    private final ActivityMapper activityMapper;
    private final ChatMessageMapper chatMapper;
    private final UserMapper userMapper;
    private final UserProfileMapper profileMapper;

    @Value("${jwt.secret:drama-tracker-jwt-secret}")
    private String jwtSecret;

    @Value("${app.chat-secret:buddy-finder-chat-aes256-secret}")
    private String chatSecret;

    private Long getUserIdFromToken(String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) return null;
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            return Long.valueOf(Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(auth.substring(7)).getPayload().getSubject());
        } catch (Exception e) { return null; }
    }

    /**
     * 申请加入活动。
     */
    @Operation(summary = "申请加入活动")
    @PostMapping("/apply")
    public Result<String> apply(@RequestHeader(value = "Authorization", required = false) String auth,
                                @RequestBody Map<String, Object> body) {
        Long userId = getUserIdFromToken(auth);
        if (userId == null) return Result.fail(401, "请先登录");

        Long activityId = Long.valueOf(body.get("activityId").toString());
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) return Result.fail(404, "活动不存在");
        if (activity.getUserId().equals(userId)) return Result.fail(400, "不能申请自己发布的活动");
        if (activity.getStatus() != 1) return Result.fail(400, "活动已关闭或已满员");

        // 检查是否已申请
        MatchRequest existing = matchMapper.selectOne(new LambdaQueryWrapper<MatchRequest>()
                .eq(MatchRequest::getActivityId, activityId)
                .eq(MatchRequest::getFromUserId, userId));
        if (existing != null) return Result.fail(400, "已申请过，请勿重复申请");

        MatchRequest mr = new MatchRequest();
        mr.setActivityId(activityId);
        mr.setFromUserId(userId);
        mr.setToUserId(activity.getUserId());
        mr.setMessage((String) body.get("message"));
        mr.setStatus(0);
        matchMapper.insert(mr);
        return Result.success("申请已发送，等待对方确认");
    }

    /**
     * 收到的申请列表。
     */
    @Operation(summary = "收到的申请")
    @GetMapping("/received")
    public Result<List<Map<String, Object>>> received(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        Long userId = getUserIdFromToken(auth);
        if (userId == null) return Result.fail(401, "请先登录");

        List<MatchRequest> list = matchMapper.selectList(new LambdaQueryWrapper<MatchRequest>()
                .eq(MatchRequest::getToUserId, userId).orderByDesc(MatchRequest::getCreateTime));

        List<Map<String, Object>> result = new ArrayList<>();
        for (MatchRequest mr : list) {
            Map<String, Object> item = matchToMap(mr);
            attachUserInfo(item, "applicant", mr.getFromUserId());
            Activity a = activityMapper.selectById(mr.getActivityId());
            if (a != null) item.put("activityTitle", a.getTitle());
            result.add(item);
        }
        return Result.success(result);
    }

    /**
     * 我发出的申请列表。
     */
    @Operation(summary = "我发出的申请")
    @GetMapping("/sent")
    public Result<List<Map<String, Object>>> sent(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        Long userId = getUserIdFromToken(auth);
        if (userId == null) return Result.fail(401, "请先登录");

        List<MatchRequest> list = matchMapper.selectList(new LambdaQueryWrapper<MatchRequest>()
                .eq(MatchRequest::getFromUserId, userId).orderByDesc(MatchRequest::getCreateTime));

        List<Map<String, Object>> result = new ArrayList<>();
        for (MatchRequest mr : list) {
            Map<String, Object> item = matchToMap(mr);
            attachUserInfo(item, "author", mr.getToUserId());
            Activity a = activityMapper.selectById(mr.getActivityId());
            if (a != null) item.put("activityTitle", a.getTitle());
            result.add(item);
        }
        return Result.success(result);
    }

    /**
     * 同意/拒绝申请。
     */
    @Operation(summary = "处理申请")
    @PostMapping("/handle/{id}")
    public Result<String> handle(@RequestHeader(value = "Authorization", required = false) String auth,
                                  @PathVariable Long id,
                                  @RequestParam Integer action) {
        Long userId = getUserIdFromToken(auth);
        if (userId == null) return Result.fail(401, "请先登录");

        MatchRequest mr = matchMapper.selectById(id);
        if (mr == null) return Result.fail(404, "申请不存在");
        if (!mr.getToUserId().equals(userId)) return Result.fail(403, "无权操作");
        if (mr.getStatus() != 0) return Result.fail(400, "申请已处理");

        mr.setStatus(action == 1 ? 1 : 2);
        matchMapper.updateById(mr);

        if (action == 1) {
            // 匹配成功，更新活动人数
            Activity a = activityMapper.selectById(mr.getActivityId());
            if (a != null) {
                a.setJoinedCount(a.getJoinedCount() + 1);
                if (a.getJoinedCount() >= a.getMaxPeople()) a.setStatus(2);
                activityMapper.updateById(a);
            }
            // 发送系统消息
            ChatMessage msg = new ChatMessage();
            msg.setMatchId(mr.getId());
            msg.setSenderId(userId);
            msg.setReceiverId(mr.getFromUserId());
            msg.setContent(AesUtil.encrypt("匹配成功！你们可以开始聊天了 🎉", chatSecret));
            msg.setMsgType(0);
            msg.setIsRead(0);
            chatMapper.insert(msg);
        }
        return Result.success(action == 1 ? "已同意" : "已拒绝");
    }

    /**
     * 我的匹配成功列表（聊天列表）。
     */
    @Operation(summary = "聊天列表")
    @GetMapping("/chats")
    public Result<List<Map<String, Object>>> chatList(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        Long userId = getUserIdFromToken(auth);
        if (userId == null) return Result.fail(401, "请先登录");

        // 我发出的已同意 + 我收到的已同意
        List<MatchRequest> list = matchMapper.selectList(new LambdaQueryWrapper<MatchRequest>()
                .eq(MatchRequest::getStatus, 1)
                .and(w -> w.eq(MatchRequest::getFromUserId, userId).or().eq(MatchRequest::getToUserId, userId))
                .orderByDesc(MatchRequest::getUpdateTime));

        List<Map<String, Object>> result = new ArrayList<>();
        for (MatchRequest mr : list) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("matchId", mr.getId());
            item.put("activityId", mr.getActivityId());
            Activity a = activityMapper.selectById(mr.getActivityId());
            if (a != null) item.put("activityTitle", a.getTitle());

            Long otherUserId = mr.getFromUserId().equals(userId) ? mr.getToUserId() : mr.getFromUserId();
            attachUserInfo(item, "partner", otherUserId);

            // 最新一条消息
            ChatMessage lastMsg = chatMapper.selectOne(new LambdaQueryWrapper<ChatMessage>()
                    .eq(ChatMessage::getMatchId, mr.getId())
                    .orderByDesc(ChatMessage::getCreateTime).last("LIMIT 1"));
            if (lastMsg != null) {
                item.put("lastMessage", AesUtil.decrypt(lastMsg.getContent(), chatSecret));
                item.put("lastTime", lastMsg.getCreateTime());
            }

            // 未读消息数
            Long unread = chatMapper.selectCount(new LambdaQueryWrapper<ChatMessage>()
                    .eq(ChatMessage::getMatchId, mr.getId())
                    .eq(ChatMessage::getReceiverId, userId)
                    .eq(ChatMessage::getIsRead, 0));
            item.put("unreadCount", unread);

            result.add(item);
        }
        return Result.success(result);
    }

    /**
     * 发送消息。
     */
    @Operation(summary = "发送消息")
    @PostMapping("/chat/send")
    public Result<Long> sendMessage(@RequestHeader(value = "Authorization", required = false) String auth,
                                    @RequestBody Map<String, Object> body) {
        Long userId = getUserIdFromToken(auth);
        if (userId == null) return Result.fail(401, "请先登录");

        Long matchId = Long.valueOf(body.get("matchId").toString());
        MatchRequest mr = matchMapper.selectById(matchId);
        if (mr == null || mr.getStatus() != 1) return Result.fail(400, "匹配不存在或未成功");
        if (!mr.getFromUserId().equals(userId) && !mr.getToUserId().equals(userId))
            return Result.fail(403, "无权操作");

        Long receiverId = mr.getFromUserId().equals(userId) ? mr.getToUserId() : mr.getFromUserId();

        ChatMessage msg = new ChatMessage();
        msg.setMatchId(matchId);
        msg.setSenderId(userId);
        msg.setReceiverId(receiverId);
        msg.setContent(AesUtil.encrypt((String) body.get("content"), chatSecret));
        msg.setMsgType(body.get("msgType") != null ? Integer.parseInt(body.get("msgType").toString()) : 0);
        msg.setIsRead(0);
        chatMapper.insert(msg);
        return Result.success(msg.getId());
    }

    /**
     * 获取聊天记录。
     */
    @Operation(summary = "聊天记录")
    @GetMapping("/chat/messages/{matchId}")
    public Result<Map<String, Object>> getMessages(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Long matchId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "50") Integer size) {
        Long userId = getUserIdFromToken(auth);
        if (userId == null) return Result.fail(401, "请先登录");

        MatchRequest mr = matchMapper.selectById(matchId);
        if (mr == null) return Result.fail(404, "匹配不存在");
        if (!mr.getFromUserId().equals(userId) && !mr.getToUserId().equals(userId))
            return Result.fail(403, "无权查看");

        // 标记消息已读
        chatMapper.selectList(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getMatchId, matchId)
                .eq(ChatMessage::getReceiverId, userId)
                .eq(ChatMessage::getIsRead, 0)).forEach(m -> {
            m.setIsRead(1);
            chatMapper.updateById(m);
        });

        Page<ChatMessage> pageResult = chatMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getMatchId, matchId)
                        .orderByDesc(ChatMessage::getCreateTime));

        List<Map<String, Object>> messages = new ArrayList<>();
        for (ChatMessage m : pageResult.getRecords()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", m.getId());
            item.put("senderId", m.getSenderId());
            item.put("receiverId", m.getReceiverId());
            item.put("content", AesUtil.decrypt(m.getContent(), chatSecret));
            item.put("msgType", m.getMsgType());
            item.put("isRead", m.getIsRead());
            item.put("createTime", m.getCreateTime());
            item.put("isMine", m.getSenderId().equals(userId));
            messages.add(item);
        }
        Collections.reverse(messages);

        // 获取对方信息 + 微信号（匹配成功后可见）
        Long otherUserId = mr.getFromUserId().equals(userId) ? mr.getToUserId() : mr.getFromUserId();
        Map<String, Object> partner = new LinkedHashMap<>();
        attachUserInfo(partner, "partner", otherUserId);
        UserProfile otherProfile = profileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, otherUserId));
        if (otherProfile != null) partner.put("partnerWechat", otherProfile.getWechat());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("messages", messages);
        result.put("total", pageResult.getTotal());
        result.putAll(partner);

        Activity a = activityMapper.selectById(mr.getActivityId());
        if (a != null) result.put("activityTitle", a.getTitle());

        return Result.success(result);
    }

    private void attachUserInfo(Map<String, Object> item, String prefix, Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            item.put(prefix + "Id", userId);
            item.put(prefix + "Name", user.getNickname());
        }
        UserProfile profile = profileMapper.selectOne(
                new LambdaQueryWrapper<UserProfile>().eq(UserProfile::getUserId, userId));
        if (profile != null) {
            item.put(prefix + "Avatar", profile.getAvatarUrl());
            item.put(prefix + "City", profile.getCity());
            item.put(prefix + "Age", profile.getAge());
            item.put(prefix + "Gender", profile.getGender());
        }
    }

    private Map<String, Object> matchToMap(MatchRequest mr) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", mr.getId());
        m.put("activityId", mr.getActivityId());
        m.put("fromUserId", mr.getFromUserId());
        m.put("toUserId", mr.getToUserId());
        m.put("message", mr.getMessage());
        m.put("status", mr.getStatus());
        m.put("createTime", mr.getCreateTime());
        return m;
    }
}
