package com.drama.tracker.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

@RestController
@RequestMapping("/api/group")
public class GroupChatController {

    @Autowired private GroupChatMapper groupChatMapper;
    @Autowired private GroupMemberInfoMapper memberMapper;
    @Autowired private GroupMessageMapper messageMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private ActivityMapper activityMapper;

    @Value("${jwt.secret:drama-tracker-jwt-secret}")
    private String jwtSecret;

    /**
     * 我的群聊列表。
     */
    @GetMapping("/list")
    public Result<?> myGroups(@RequestHeader(value = "Authorization", required = false) String auth) {
        Long userId = getUserIdFromToken(auth);
        if (userId == null) return Result.fail(401, "请先登录");

        List<GroupMemberInfo> memberships = memberMapper.selectList(
                new LambdaQueryWrapper<GroupMemberInfo>().eq(GroupMemberInfo::getUserId, userId));

        List<Map<String, Object>> result = new ArrayList<>();
        for (GroupMemberInfo m : memberships) {
            GroupChat group = groupChatMapper.selectById(m.getGroupId());
            if (group == null) continue;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("groupId", group.getId());
            item.put("activityId", group.getActivityId());
            item.put("name", group.getName());
            item.put("role", m.getRole());
            // 成员数
            Long count = memberMapper.selectCount(new LambdaQueryWrapper<GroupMemberInfo>()
                    .eq(GroupMemberInfo::getGroupId, group.getId()));
            item.put("memberCount", count);
            // 最新消息
            GroupMessage last = messageMapper.selectOne(new LambdaQueryWrapper<GroupMessage>()
                    .eq(GroupMessage::getGroupId, group.getId())
                    .orderByDesc(GroupMessage::getCreateTime).last("LIMIT 1"));
            if (last != null) {
                User sender = userMapper.selectById(last.getUserId());
                item.put("lastMessage", (sender != null ? sender.getNickname() + ": " : "") + last.getContent());
                item.put("lastTime", last.getCreateTime());
            }
            result.add(item);
        }
        // 按最新消息时间排序
        result.sort((a, b) -> {
            Object ta = a.get("lastTime"); Object tb = b.get("lastTime");
            if (ta == null && tb == null) return 0;
            if (ta == null) return 1; if (tb == null) return -1;
            return tb.toString().compareTo(ta.toString());
        });
        return Result.success(result);
    }

    /**
     * 群聊消息列表。
     */
    @GetMapping("/messages/{groupId}")
    public Result<?> getMessages(@RequestHeader(value = "Authorization", required = false) String auth,
                                 @PathVariable Long groupId,
                                 @RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "50") Integer size) {
        Long userId = getUserIdFromToken(auth);
        if (userId == null) return Result.fail(401, "请先登录");

        // 验证是否是群成员
        Long isMember = memberMapper.selectCount(new LambdaQueryWrapper<GroupMemberInfo>()
                .eq(GroupMemberInfo::getGroupId, groupId).eq(GroupMemberInfo::getUserId, userId));
        if (isMember == 0) return Result.fail(403, "你不是该群成员");

        Page<GroupMessage> p = messageMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<GroupMessage>().eq(GroupMessage::getGroupId, groupId)
                        .orderByDesc(GroupMessage::getCreateTime));

        List<Map<String, Object>> messages = new ArrayList<>();
        for (GroupMessage msg : p.getRecords()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", msg.getId());
            item.put("userId", msg.getUserId());
            User sender = userMapper.selectById(msg.getUserId());
            item.put("nickname", sender != null ? sender.getNickname() : "未知");
            item.put("content", msg.getContent());
            item.put("createTime", msg.getCreateTime());
            item.put("isMine", msg.getUserId().equals(userId));
            messages.add(item);
        }
        Collections.reverse(messages);

        // 群信息
        GroupChat group = groupChatMapper.selectById(groupId);
        Long memberCount = memberMapper.selectCount(new LambdaQueryWrapper<GroupMemberInfo>()
                .eq(GroupMemberInfo::getGroupId, groupId));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("messages", messages);
        result.put("total", p.getTotal());
        result.put("groupName", group != null ? group.getName() : "");
        result.put("memberCount", memberCount);
        return Result.success(result);
    }

    /**
     * 发送群消息（只支持文字）。
     */
    @PostMapping("/send")
    public Result<?> sendMessage(@RequestHeader(value = "Authorization", required = false) String auth,
                                 @RequestBody Map<String, Object> body) {
        Long userId = getUserIdFromToken(auth);
        if (userId == null) return Result.fail(401, "请先登录");

        Long groupId = Long.valueOf(body.get("groupId").toString());
        String content = (String) body.get("content");
        if (content == null || content.trim().isEmpty()) return Result.fail("消息不能为空");

        // 验证群成员
        Long isMember = memberMapper.selectCount(new LambdaQueryWrapper<GroupMemberInfo>()
                .eq(GroupMemberInfo::getGroupId, groupId).eq(GroupMemberInfo::getUserId, userId));
        if (isMember == 0) return Result.fail(403, "你不是该群成员");

        // 敏感词过滤
        content = SensitiveWordFilter.filter(content);

        GroupMessage msg = new GroupMessage();
        msg.setGroupId(groupId);
        msg.setUserId(userId);
        msg.setContent(content);
        msg.setCreateTime(LocalDateTime.now());
        messageMapper.insert(msg);
        return Result.success(msg.getId());
    }

    /**
     * 群成员列表。
     */
    @GetMapping("/members/{groupId}")
    public Result<?> getMembers(@RequestHeader(value = "Authorization", required = false) String auth,
                                @PathVariable Long groupId) {
        Long userId = getUserIdFromToken(auth);
        if (userId == null) return Result.fail(401, "请先登录");

        List<GroupMemberInfo> members = memberMapper.selectList(
                new LambdaQueryWrapper<GroupMemberInfo>().eq(GroupMemberInfo::getGroupId, groupId));

        List<Map<String, Object>> result = new ArrayList<>();
        for (GroupMemberInfo m : members) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("userId", m.getUserId());
            item.put("role", m.getRole());
            User user = userMapper.selectById(m.getUserId());
            item.put("nickname", user != null ? user.getNickname() : "未知");
            result.add(item);
        }
        return Result.success(result);
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
