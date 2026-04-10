package com.drama.tracker.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.drama.tracker.dao.entity.Drama;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * 数据初始化服务。
 * 内置精选全球影视数据，无需 TMDB API Key 即可快速填充数据库。
 *
 * @author drama-tracker
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataInitService {

    private final DramaService dramaService;

    /**
     * 初始化种子数据。
     *
     * @return 初始化结果
     */
    public Map<String, Object> initSeedData() {
        Map<String, Object> result = new LinkedHashMap<>();
        int saved = 0;
        int skipped = 0;

        List<Drama> seedData = buildSeedData();

        for (Drama drama : seedData) {
            // 去重检查
            LambdaQueryWrapper<Drama> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Drama::getTitle, drama.getTitle());
            if (dramaService.count(wrapper) > 0) {
                skipped++;
                continue;
            }
            dramaService.save(drama);
            saved++;
        }

        result.put("status", "success");
        result.put("totalSeed", seedData.size());
        result.put("saved", saved);
        result.put("skipped", skipped);
        result.put("message", String.format("种子数据初始化完成: 共 %d 条, 新增 %d 条, 跳过 %d 条", seedData.size(), saved, skipped));

        log.info("Seed data initialized: total={}, saved={}, skipped={}", seedData.size(), saved, skipped);
        return result;
    }

    /**
     * 构建种子数据 — 全球精选影视。
     */
    private List<Drama> buildSeedData() {
        List<Drama> list = new ArrayList<>();

        // ========== 中国大陆 (CN) ==========
        list.add(buildDrama("繁花", null, "CN", 1, "剧情,年代", 2,
                LocalDate.of(2023, 12, 27), 30, 30, 60,
                "王家卫", "秦雯", "胡歌,马伊琍,唐嫣,辛芷蕾,游本昌",
                "改编自金宇澄同名小说，以上世纪90年代初上海为背景，讲述阿宝在时代浪潮中的沉浮故事。",
                "https://image.tmdb.org/t/p/w500/lYi2ChhOFq5gqLQKhVLBPSNmyG1.jpg",
                8.5, 52000, 9800L, "30468", null, "腾讯视频"));

        list.add(buildDrama("漫长的季节", null, "CN", 1, "剧情,悬疑,犯罪", 2,
                LocalDate.of(2023, 4, 22), 12, 12, 55,
                "辛爽", "潘依然", "范伟,秦昊,陈明昊,李庚希",
                "一桩跨越十几年的碎尸案，牵出三个家庭的命运纠葛，在漫长的季节里寻找真相。",
                "https://image.tmdb.org/t/p/w500/lNzLiKhS2OXMKOD7R7fwvqMKOXk.jpg",
                9.4, 78000, 15600L, "36004600", null, "爱奇艺"));

        list.add(buildDrama("狂飙", null, "CN", 1, "剧情,犯罪", 2,
                LocalDate.of(2023, 1, 14), 39, 39, 45,
                "徐纪周", "朱俊懿", "张译,张颂文,李一桐,张志坚",
                "讲述刑警安欣与黑恶势力高启强长达二十年的正邪较量。",
                "https://image.tmdb.org/t/p/w500/nh2qkuIQSiNJvZzv7bf8LFblWCH.jpg",
                8.5, 120000, 18000L, "35728627", null, "央视,爱奇艺"));

        list.add(buildDrama("三体", "Three-Body", "CN", 1, "科幻,剧情", 2,
                LocalDate.of(2023, 1, 15), 30, 30, 50,
                "杨磊", "田良良", "张鲁一,于和伟,陈瑾,王子文,林永健",
                "根据刘慈欣同名小说改编，讲述纳米科学家汪淼接触到一个名为三体的神秘组织。",
                "https://image.tmdb.org/t/p/w500/gFrAClCv0WXkfOg7zuQhJ3l31jL.jpg",
                8.0, 65000, 12000L, "35196Mo", null, "腾讯视频"));

        list.add(buildDrama("庆余年2", "Joy of Life Season 2", "CN", 1, "古装,剧情,喜剧", 2,
                LocalDate.of(2024, 5, 16), 36, 36, 45,
                "孙皓", "王倦", "张若昀,李沁,陈道明,吴刚",
                "范闲历经家族恩怨与朝堂风波，再度踏上充满挑战的征途。",
                "https://image.tmdb.org/t/p/w500/9p7P15gBCfpBK28P3dKdIDsJfbq.jpg",
                7.8, 85000, 16500L, "35267629", null, "央视,腾讯视频"));

        list.add(buildDrama("风吹半夏", null, "CN", 1, "剧情,商战", 2,
                LocalDate.of(2022, 11, 27), 36, 36, 45,
                "傅东育", "张挺", "赵丽颖,欧豪,李光洁",
                "改编自阿耐小说，讲述许半夏在钢铁行业的创业奋斗史。",
                "https://image.tmdb.org/t/p/w500/b3o4VJB1CaGc5EDYVwQMZGTMbCj.jpg",
                8.3, 48000, 8500L, "35457862", null, "浙江卫视,爱奇艺"));

        list.add(buildDrama("人世间", null, "CN", 1, "剧情,家庭", 2,
                LocalDate.of(2022, 1, 28), 58, 58, 45,
                "李路", "王海鸰", "雷佳音,辛柏青,宋佳,殷桃",
                "改编自梁晓声同名小说，讲述东北一户周家三代人的生活变迁。",
                "https://image.tmdb.org/t/p/w500/hpZQfkfhvSghIKUGVRbMtFz0q03.jpg",
                8.4, 55000, 9200L, "35200erta", null, "央视,爱奇艺"));

        list.add(buildDrama("我的阿勒泰", null, "CN", 3, "剧情,治愈", 2,
                LocalDate.of(2024, 5, 7), 8, 8, 40,
                "滕丛丛", "彭奕宁", "马伊琍,于适,周依然",
                "改编自李娟同名散文集，讲述少女李文秀跟随母亲回到阿勒泰的故事。",
                "https://image.tmdb.org/t/p/w500/pUGa49ToW5eg0VO1UndCuCyxY4g.jpg",
                8.8, 42000, 11000L, "36317180", null, "央视,爱奇艺"));

        // ========== 韩国 (KR) ==========
        list.add(buildDrama("黑暗荣耀", "더 글로리", "KR", 1, "剧情,悬疑,复仇", 2,
                LocalDate.of(2022, 12, 30), 16, 16, 60,
                "安吉镐", "金恩淑", "宋慧乔,李到晛,林智妍,朴成勋",
                "一个因校园暴力而被毁掉人生的女人，用18年时间精心策划的复仇计划。",
                "https://image.tmdb.org/t/p/w500/nJ7PknbxHk7LnX2dDMsu0FIdkye.jpg",
                8.9, 95000, 15000L, null, null, "Netflix"));

        list.add(buildDrama("鱿鱼游戏", "오징어 게임", "KR", 1, "剧情,惊悚,悬疑", 1,
                LocalDate.of(2021, 9, 17), 9, 9, 55,
                "黄东赫", "黄东赫", "李政宰,朴海秀,魏嘏隽,郑浩妍",
                "456名负债累累的玩家接受神秘邀请参加一系列儿童游戏，赢家可以获得456亿韩元奖金。",
                "https://image.tmdb.org/t/p/w500/dDlEmu3EZ0Pgg93K2SVNLCjCSvE.jpg",
                8.1, 150000, 22000L, null, "tt10919420", "Netflix"));

        list.add(buildDrama("涉过愤怒的海", "분노의 바다를 건너", "KR", 2, "剧情,犯罪,惊悚", 2,
                LocalDate.of(2023, 11, 22), 1, 1, 130,
                "曹进穆", "曹进穆", "黄渤,周迅,祖峰,张宥浩",
                "一个父亲为找寻在日本遇害的女儿真相而踏上的复仇之路。",
                "https://image.tmdb.org/t/p/w500/d7IUMrUeAlYXLMhFSZRaBHLJGwL.jpg",
                7.5, 32000, 5800L, null, null, "院线"));

        list.add(buildDrama("非常律师禹英禑", "이상한 변호사 우영우", "KR", 1, "剧情,喜剧,法律", 2,
                LocalDate.of(2022, 6, 29), 16, 16, 70,
                "刘仁植", "文智媛", "朴恩斌,姜泰伍,姜其永",
                "天才自闭症律师禹英禑的成长故事。她以独特的视角解决各种案件。",
                "https://image.tmdb.org/t/p/w500/wSKT8VQAxUV5LtU3e3dPlcNgFrZ.jpg",
                8.7, 72000, 12500L, null, null, "Netflix,ENA"));

        list.add(buildDrama("财阀家的小儿子", "재벌집 막내아들", "KR", 1, "剧情,奇幻,商战", 2,
                LocalDate.of(2022, 11, 18), 16, 16, 70,
                "郑大允", "金泰熙", "宋仲基,李星民,申贤彬",
                "被财阀家族冤枉致死的秘书，穿越回到1987年成为该家族的小儿子。",
                "https://image.tmdb.org/t/p/w500/kIDYGF06LVFi7kEKVbBh4mACWjZ.jpg",
                8.5, 58000, 10800L, null, null, "JTBC"));

        list.add(buildDrama("僵尸校园", "지금 우리 학교는", "KR", 1, "恐怖,惊悚,动作", 2,
                LocalDate.of(2022, 1, 28), 12, 12, 55,
                "李在圭", "千成一", "朴持厚,尹赞荣,赵怡贤",
                "病毒在学校爆发，学生们被困校园与丧尸搏斗求生。",
                "https://image.tmdb.org/t/p/w500/rINvJbgmrmLCwDNTE6BpKaGHqHW.jpg",
                8.0, 65000, 11200L, null, null, "Netflix"));

        // ========== 日本 (JP) ==========
        list.add(buildDrama("非自然死亡", "アンナチュラル", "JP", 1, "剧情,悬疑,医疗", 2,
                LocalDate.of(2018, 1, 12), 10, 10, 60,
                "�的场正行", "野木亚纪子", "石原里美,井浦新,的场浩司,的场拓见",
                "法医学者三澄美琴在UDI法医学研究所揭开非正常死亡背后的真相。",
                "https://image.tmdb.org/t/p/w500/xStHdS3WbHfPL8TdsBqoJxiOqPL.jpg",
                9.3, 85000, 14000L, "27060078", "tt7564256", "TBS"));

        list.add(buildDrama("半泽直树", "半沢直樹", "JP", 1, "剧情,职场", 2,
                LocalDate.of(2013, 7, 7), 10, 10, 65,
                "福�的克则", "八津弘幸", "堺雅人,上户彩,片冈爱之助,及川光博",
                "银行员工半泽直树面对职场不公，以百倍奉还的方式反击。",
                "https://image.tmdb.org/t/p/w500/gNwF6hnJ6gLSxFpntjP9hOJsLk0.jpg",
                9.0, 72000, 12600L, "25262001", null, "TBS"));

        list.add(buildDrama("弥留之国的爱丽丝", "今際の国のアリス", "JP", 1, "科幻,惊悚,悬疑", 2,
                LocalDate.of(2020, 12, 10), 16, 16, 50,
                "佐藤信介", "佐藤信介", "山崎贤人,土屋太凤,村上�的",
                "突然被传送到一个平行东京，为了生存必须不断参加致命游戏。",
                "https://image.tmdb.org/t/p/w500/1fmh0JGLqkfXCBGHSRKvp2r5GiW.jpg",
                8.2, 58000, 10500L, null, "tt10919420", "Netflix"));

        list.add(buildDrama("孤独的美食家", "孤独のグルメ", "JP", 1, "美食,剧情,生活", 1,
                LocalDate.of(2012, 1, 4), 120, 120, 30,
                "沟口�的", "田口佳宏", "松重丰",
                "个体经营的进口杂货商井之头五郎，在工作间隙独自享受各种美食的故事。",
                "https://image.tmdb.org/t/p/w500/eZdP0yDRsYq7lEadU3EzIfCeIXW.jpg",
                8.8, 45000, 8900L, "21325810", null, "东京电视台"));

        list.add(buildDrama("轮到你了", "あなたの番です", "JP", 1, "悬疑,惊悚", 2,
                LocalDate.of(2019, 4, 14), 20, 20, 60,
                "佐久间�的信", "福原充则", "原田知世,田中圭,西野七濑",
                "一对新婚夫妇搬入公寓，卷入住户们的交换杀人游戏。",
                "https://image.tmdb.org/t/p/w500/aMZhZfXFB2R0rlBPSB7mJvMBQdp.jpg",
                8.4, 52000, 9600L, "30324891", null, "日本电视台"));

        list.add(buildDrama("进击的巨人 最终季", "進撃の巨人 The Final Season", "JP", 5, "动画,动作,奇幻", 2,
                LocalDate.of(2020, 12, 7), 28, 28, 25,
                "林祐一郎", "�的山创", "梶裕贵,石川由依,井上麻里奈,神谷浩史",
                "人类与巨人之间的最终之战，揭开世界的真相。",
                "https://image.tmdb.org/t/p/w500/hTP1DtLGFamjfu8WqjnuQdP1n4i.jpg",
                9.1, 110000, 17800L, "25896288", null, "NHK"));

        // ========== 美国 (US) ==========
        list.add(buildDrama("最后生还者", "The Last of Us", "US", 1, "剧情,科幻,冒险", 1,
                LocalDate.of(2023, 1, 15), 9, 9, 55,
                "克雷格·麦辛", "克雷格·麦辛", "佩德罗·帕斯卡,贝拉·拉姆齐",
                "改编自同名游戏，真菌感染摧毁文明后，走私客Joel护送少女Ellie穿越废土。",
                "https://image.tmdb.org/t/p/w500/uKvVjHNqB5VmOrdxqAt2F7J78ED.jpg",
                8.8, 130000, 19500L, null, "tt3581920", "HBO"));

        list.add(buildDrama("权力的游戏", "Game of Thrones", "US", 1, "奇幻,剧情,冒险", 2,
                LocalDate.of(2011, 4, 17), 73, 73, 55,
                "大卫·贝尼奥夫", "D·B·威斯", "基特·哈灵顿,艾米莉亚·克拉克,彼特·丁拉基",
                "七大王国之间的权力争夺，龙母、雪诺等人的命运交织。",
                "https://image.tmdb.org/t/p/w500/1XS1oqL89opfnbLl8WnZY1O1uJx.jpg",
                9.3, 200000, 25000L, "26584183", "tt0944947", "HBO"));

        list.add(buildDrama("绝命毒师", "Breaking Bad", "US", 1, "剧情,犯罪,惊悚", 2,
                LocalDate.of(2008, 1, 20), 62, 62, 50,
                "文斯·吉里根", "文斯·吉里根", "布莱恩·克兰斯顿,亚伦·保尔,安娜·冈",
                "高中化学老师身患癌症后开始制毒，一步步走向深渊。",
                "https://image.tmdb.org/t/p/w500/ggFHVNu6YYI5L9pCfOacjizRGt.jpg",
                9.5, 220000, 28000L, "1442012", "tt0903747", "AMC"));

        list.add(buildDrama("怪奇物语", "Stranger Things", "US", 1, "科幻,恐怖,剧情", 1,
                LocalDate.of(2016, 7, 15), 34, 34, 50,
                "达菲兄弟", "达菲兄弟", "米莉·博比·布朗,芬恩·沃尔夫哈德,盖顿·马塔拉佐",
                "印第安纳州小镇上，一群孩子面对超自然力量和颠倒世界的威胁。",
                "https://image.tmdb.org/t/p/w500/49WJfeN0moxb9IPfGn8AIqMGskD.jpg",
                8.7, 180000, 21000L, null, "tt4574334", "Netflix"));

        list.add(buildDrama("继承之战", "Succession", "US", 1, "剧情,喜剧", 2,
                LocalDate.of(2018, 6, 3), 39, 39, 60,
                "杰西·阿姆斯特朗", "杰西·阿姆斯特朗", "布莱恩·考克斯,杰瑞米·斯特朗,莎拉·斯努克",
                "媒体巨头罗伊家族围绕家族企业控制权的明争暗斗。",
                "https://image.tmdb.org/t/p/w500/cKEPbiKfCToW2rH1DcbduwH5Hh4.jpg",
                8.9, 85000, 16000L, null, "tt7660850", "HBO"));

        list.add(buildDrama("黄石", "Yellowstone", "US", 1, "剧情,西部", 1,
                LocalDate.of(2018, 6, 20), 47, 47, 55,
                "泰勒·谢里丹", "泰勒·谢里丹", "凯文·科斯特纳,卢克·格莱姆斯,凯利·莱利",
                "蒙大拿州最大牧场的达顿家族，守护祖传土地的西部史诗。",
                "https://image.tmdb.org/t/p/w500/peNC0eyc3TQJa6x4TdKcBPNP4t0.jpg",
                8.6, 78000, 15000L, null, "tt4057282", "Paramount"));

        list.add(buildDrama("白莲花度假村", "The White Lotus", "US", 1, "剧情,喜剧,悬疑", 1,
                LocalDate.of(2021, 7, 11), 13, 13, 60,
                "迈克·怀特", "迈克·怀特", "詹妮弗·库里奇,奥布瑞·普拉扎,米娜·马苏德",
                "豪华度假村中的上流社会客人之间的社会讽刺喜剧。",
                "https://image.tmdb.org/t/p/w500/bH3WL2CROqWovY3nxwTBL8s6al5.jpg",
                8.2, 62000, 12800L, null, "tt13406094", "HBO"));

        list.add(buildDrama("奥本海默", "Oppenheimer", "US", 2, "剧情,历史,传记", 2,
                LocalDate.of(2023, 7, 21), 1, 1, 180,
                "克里斯托弗·诺兰", "克里斯托弗·诺兰", "基里安·墨菲,小罗伯特·唐尼,艾米丽·布朗特,马特·达蒙",
                "原子弹之父奥本海默的传记故事，他在科学成就与道德困境间的挣扎。",
                "https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg",
                8.5, 160000, 20000L, "35593344", "tt15398776", "院线"));

        // ========== 英国 (UK) ==========
        list.add(buildDrama("黑镜", "Black Mirror", "UK", 1, "科幻,剧情,惊悚", 1,
                LocalDate.of(2011, 12, 4), 27, 27, 55,
                "查理·布鲁克", "查理·布鲁克", "布莱丝·达拉斯·霍华德,安东尼·麦凯,丹尼尔·卡卢亚",
                "一部独立单元剧，每集讲述一个关于科技对人类社会影响的黑暗故事。",
                "https://image.tmdb.org/t/p/w500/5UaYsGZOFhjFDwQh6GuLjjA1WlF.jpg",
                8.8, 120000, 16500L, null, "tt2085059", "Netflix,Channel 4"));

        list.add(buildDrama("神探夏洛克", "Sherlock", "UK", 1, "犯罪,悬疑,剧情", 2,
                LocalDate.of(2010, 7, 25), 15, 15, 90,
                "马克·加蒂斯", "马克·加蒂斯", "本尼迪克特·康伯巴奇,马丁·弗瑞曼",
                "经典福尔摩斯故事的现代改编，夏洛克与华生在伦敦破获各种离奇案件。",
                "https://image.tmdb.org/t/p/w500/7WTltoX1PHjAqYfJKjqDPFMFxlf.jpg",
                9.2, 110000, 18000L, "4023Mo", "tt1475582", "BBC"));

        list.add(buildDrama("浴血黑帮", "Peaky Blinders", "UK", 1, "犯罪,剧情", 2,
                LocalDate.of(2013, 9, 12), 36, 36, 60,
                "史蒂文·奈特", "史蒂文·奈特", "基里安·墨菲,海伦·麦克洛瑞,保罗·安德森",
                "一战后的伯明翰，谢尔比家族领导的帮派在黑社会与政界之间周旋。",
                "https://image.tmdb.org/t/p/w500/vUUqzWa2LnHIVqkaKVlVGkVcZIW.jpg",
                8.8, 95000, 15500L, null, "tt2442560", "BBC"));

        // ========== 欧洲 (EU) ==========
        list.add(buildDrama("暗黑", "Dark", "EU", 1, "科幻,悬疑,惊悚", 2,
                LocalDate.of(2017, 12, 1), 26, 26, 55,
                "巴兰·博·奥达尔", "扬特耶·弗里泽", "路易斯·霍夫曼,丽萨·维科夫,安德烈亚斯·皮茨施曼",
                "德国小镇温登，四个家庭的命运因时间旅行而纠缠在一起。",
                "https://image.tmdb.org/t/p/w500/apbrbWs8M9lyOpJYU5WXrpFbk1Z.jpg",
                8.8, 88000, 14500L, null, "tt5753856", "Netflix"));

        list.add(buildDrama("纸钞屋", "La Casa de Papel", "EU", 1, "犯罪,动作,悬疑", 2,
                LocalDate.of(2017, 5, 2), 41, 41, 50,
                "亚历克斯·皮纳", "亚历克斯·皮纳", "阿尔瓦罗·莫奇,乌苏拉·科贝罗,佩德罗·阿隆索",
                "一群以城市为代号的劫匪在教授的策划下，实施了对西班牙皇家铸币厂的惊天大劫案。",
                "https://image.tmdb.org/t/p/w500/reEMJA1uzscCbkpeRJeTT2bjqUp.jpg",
                8.3, 130000, 18000L, null, "tt6468322", "Netflix"));

        return list;
    }

    /**
     * 构建剧集实体。
     */
    private Drama buildDrama(String title, String originalTitle, String region, int type,
                             String genres, int status, LocalDate releaseDate,
                             int totalEpisodes, int airedEpisodes, int duration,
                             String directors, String writers, String actors,
                             String description, String posterUrl,
                             double rating, int ratingCount, long hotScore,
                             String doubanId, String imdbId, String platforms) {
        Drama drama = new Drama();
        drama.setTitle(title);
        drama.setOriginalTitle(originalTitle);
        drama.setRegion(region);
        drama.setType(type);
        drama.setGenres(genres);
        drama.setStatus(status);
        drama.setReleaseDate(releaseDate);
        drama.setTotalEpisodes(totalEpisodes);
        drama.setAiredEpisodes(airedEpisodes);
        drama.setEpisodeDuration(duration);
        drama.setDirectors(directors);
        drama.setWriters(writers);
        drama.setActors(actors);
        drama.setDescription(description);
        drama.setPosterUrl(posterUrl);
        drama.setUserRating(BigDecimal.valueOf(rating));
        drama.setTotalRating(BigDecimal.valueOf(rating));
        drama.setRatingCount(ratingCount);
        drama.setHotScore(hotScore);
        drama.setDoubanId(doubanId);
        drama.setImdbId(imdbId);
        drama.setPlatforms(platforms);
        return drama;
    }
}
