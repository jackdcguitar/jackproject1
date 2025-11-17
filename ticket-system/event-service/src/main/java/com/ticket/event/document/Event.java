package com.ticket.event.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Event MongoDB Document - 节目详情
 *
 * <p>为什么使用MongoDB存储节目信息？
 * <ul>
 *   <li><b>灵活的Schema：</b>不同类型的节目有不同的字段需求
 *     <ul>
 *       <li>演唱会：歌手、乐队、曲目列表</li>
 *       <li>电影：导演、主演、时长、语言、字幕</li>
 *       <li>话剧：演员、剧目、场次</li>
 *       <li>体育赛事：参赛队伍、赛制、比分预测</li>
 *     </ul>
 *   </li>
 *   <li><b>复杂嵌套结构：</b>场馆信息、演出者列表、票价区域等都是嵌套对象，MongoDB天然支持</li>
 *   <li><b>快速迭代：</b>新增字段无需修改数据库表结构，直接添加即可</li>
 *   <li><b>高性能查询：</b>支持复杂查询、全文搜索、地理位置查询等</li>
 *   <li><b>易于扩展：</b>metadata字段可以存储任意扩展信息</li>
 * </ul>
 *
 * <p>与MySQL的配合使用：
 * <ul>
 *   <li><b>MySQL：</b>存储强一致性数据（订单、支付、座位状态等）</li>
 *   <li><b>MongoDB：</b>存储灵活数据（节目信息、日志、座位布局等）</li>
 * </ul>
 *
 * <p>数据示例：
 * <pre>
 * {
 *   "_id": "event123",
 *   "name": "周杰伦演唱会",
 *   "category": "CONCERT",
 *   "venue": {
 *     "name": "鸟巢",
 *     "city": "北京",
 *     "capacity": 80000
 *   },
 *   "performers": [
 *     {"name": "周杰伦", "role": "主唱"}
 *   ],
 *   "priceZones": [
 *     {"zoneName": "VIP", "price": 1280, "color": "#FFD700"},
 *     {"zoneName": "A区", "price": 680, "color": "#FF6347"}
 *   ],
 *   "tags": ["流行音乐", "华语", "演唱会"],
 *   "metadata": {
 *     "duration": "180分钟",
 *     "setList": ["七里香", "稻香", "晴天"]
 *   }
 * }
 * </pre>
 *
 * @author Ticket System
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "events")  // MongoDB集合名称
public class Event {

    /**
     * 节目ID（MongoDB自动生成）
     * 例如："507f1f77bcf86cd799439011"
     */
    @Id
    private String id;

    /**
     * 节目名称
     * 例如："周杰伦演唱会"、"流浪地球2"、"话剧：雷雨"
     */
    private String name;

    /**
     * 节目类别
     *
     * <p>可选值：
     * <ul>
     *   <li>CONCERT - 演唱会</li>
     *   <li>MOVIE - 电影</li>
     *   <li>DRAMA - 话剧</li>
     *   <li>SPORTS - 体育赛事</li>
     *   <li>EXHIBITION - 展览</li>
     *   <li>OTHER - 其他</li>
     * </ul>
     */
    private String category;

    /**
     * 场馆信息（嵌套对象）
     *
     * <p>包含场馆名称、地址、经纬度、容量等信息
     * 嵌套存储避免了关联查询，提高查询性能
     */
    private Venue venue;

    /**
     * 开始时间
     * 例如：2024-05-20 19:30:00
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     * 例如：2024-05-20 22:00:00
     */
    private LocalDateTime endTime;

    /**
     * 海报URL
     * 例如："https://cdn.example.com/posters/event123.jpg"
     */
    private String poster;

    /**
     * 详细介绍
     *
     * <p>支持富文本HTML格式，可包含：
     * <ul>
     *   <li>节目简介</li>
     *   <li>演出亮点</li>
     *   <li>注意事项</li>
     *   <li>图片、视频链接</li>
     * </ul>
     */
    private String description;

    /**
     * 演出者/主演信息列表（嵌套对象数组）
     *
     * <p>灵活的设计，适用于不同类型的节目：
     * <ul>
     *   <li>演唱会：[{"name": "周杰伦", "role": "主唱"}, {"name": "方文山", "role": "作词"}]</li>
     *   <li>电影：[{"name": "吴京", "role": "主演"}, {"name": "郭帆", "role": "导演"}]</li>
     *   <li>话剧：[{"name": "濮存昕", "role": "主演"}]</li>
     * </ul>
     */
    private List<Performer> performers;

    /**
     * 票价区域列表（嵌套对象数组）
     *
     * <p>不同区域有不同的价格和座位数，例如：
     * <ul>
     *   <li>VIP区：1280元，500个座位</li>
     *   <li>A区：680元，2000个座位</li>
     *   <li>B区：380元，3000个座位</li>
     * </ul>
     */
    private List<PriceZone> priceZones;

    /**
     * 总座位数
     *
     * <p>等于所有票价区域的座位数之和
     * 用于快速判断节目规模
     */
    private Integer totalSeats;

    /**
     * 可用座位数
     *
     * <p>实时更新，每卖出一张票减1
     * 用于判断是否售罄
     */
    private Integer availableSeats;

    /**
     * 节目状态
     *
     * <p>状态流转：
     * <ul>
     *   <li>UPCOMING - 即将上映（未开始售票）</li>
     *   <li>ON_SALE - 售票中（正常售票状态）</li>
     *   <li>SOLD_OUT - 售罄（可用座位数为0）</li>
     *   <li>CANCELLED - 已取消（节目取消，需退票）</li>
     *   <li>ENDED - 已结束（演出结束）</li>
     * </ul>
     */
    private String status;

    /**
     * 标签列表（用于分类和搜索）
     *
     * <p>灵活的标签系统，支持多维度分类：
     * <ul>
     *   <li>音乐类型：["流行音乐", "摇滚", "古典"]</li>
     *   <li>适合人群：["亲子", "情侣", "学生"]</li>
     *   <li>特色：["热门", "首映", "限量"]</li>
     * </ul>
     *
     * <p>用于前端筛选和推荐系统
     */
    private List<String> tags;

    /**
     * 元数据（灵活的扩展字段）
     *
     * <p>存储各类型节目的特殊字段，无需修改数据库结构：
     * <ul>
     *   <li>演唱会：{"duration": "180分钟", "setList": ["歌曲1", "歌曲2"]}</li>
     *   <li>电影：{"language": "中文", "subtitle": "英文", "rating": "PG-13", "imdbId": "tt1234567"}</li>
     *   <li>体育赛事：{"homeTeam": "北京国安", "awayTeam": "上海上港", "league": "中超"}</li>
     * </ul>
     *
     * <p>MongoDB的优势：可以存储任意JSON结构，非常灵活
     */
    private Map<String, Object> metadata;

    /**
     * 评分信息（嵌套对象）
     *
     * <p>包含评分、评分人数、评分分布等
     * 用于展示节目质量和热度
     */
    private Rating rating;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     *
     * <p>每次修改节目信息时更新
     * 用于缓存失效判断
     */
    private LocalDateTime updateTime;

    /**
     * 创建人
     * 例如：管理员ID或用户ID
     */
    private String createBy;

    /**
     * 场馆信息（嵌套文档）
     *
     * <p>为什么使用嵌套文档而不是关联查询？
     * <ul>
     *   <li>减少查询次数：一次查询即可获取完整信息</li>
     *   <li>提高性能：避免JOIN操作</li>
     *   <li>数据一致性：场馆信息作为节目的一部分，一起存储</li>
     * </ul>
     *
     * <p>示例数据：
     * <pre>
     * {
     *   "name": "国家体育场（鸟巢）",
     *   "address": "北京市朝阳区国家体育场南路1号",
     *   "city": "北京",
     *   "latitude": 39.9928,
     *   "longitude": 116.3912,
     *   "capacity": 80000,
     *   "facilities": {
     *     "parking": true,
     *     "wifi": true,
     *     "wheelchair": true,
     *     "restaurant": ["中餐厅", "西餐厅", "快餐"]
     *   }
     * }
     * </pre>
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Venue {
        /** 场馆名称，例如："国家体育场（鸟巢）" */
        private String name;

        /** 详细地址，例如："北京市朝阳区国家体育场南路1号" */
        private String address;

        /** 所在城市，用于地区筛选，例如："北京" */
        private String city;

        /** 纬度，用于地图显示和附近场馆查询，例如：39.9928 */
        private Double latitude;

        /** 经度，用于地图显示和附近场馆查询，例如：116.3912 */
        private Double longitude;

        /** 场馆容量（总座位数），例如：80000 */
        private Integer capacity;

        /**
         * 设施信息（灵活的Map结构）
         *
         * <p>可包含：
         * <ul>
         *   <li>parking: 是否有停车场</li>
         *   <li>wifi: 是否有WiFi</li>
         *   <li>wheelchair: 是否支持轮椅</li>
         *   <li>restaurant: 餐厅列表</li>
         *   <li>metro: 地铁站信息</li>
         * </ul>
         */
        private Map<String, Object> facilities;
    }

    /**
     * 演出者信息（嵌套文档）
     *
     * <p>灵活的设计，适用于各种类型的节目：
     * <ul>
     *   <li>演唱会：歌手、乐队成员、伴奏</li>
     *   <li>电影：导演、主演、配角</li>
     *   <li>话剧：演员、导演、编剧</li>
     *   <li>体育赛事：球队、教练、裁判</li>
     * </ul>
     *
     * <p>示例数据：
     * <pre>
     * {
     *   "name": "周杰伦",
     *   "role": "主唱",
     *   "avatar": "https://cdn.example.com/artists/jaychou.jpg",
     *   "bio": "华语流行音乐天王，代表作：七里香、稻香、晴天等"
     * }
     * </pre>
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Performer {
        /** 演出者名称，例如："周杰伦"、"吴京"、"濮存昕" */
        private String name;

        /** 角色/职责，例如："主唱"、"主演"、"导演"、"乐队" */
        private String role;

        /** 头像URL，例如："https://cdn.example.com/artists/jaychou.jpg" */
        private String avatar;

        /** 简介，例如："华语流行音乐天王，代表作：七里香、稻香、晴天等" */
        private String bio;
    }

    /**
     * 票价区域（嵌套文档）
     *
     * <p>不同位置的座位有不同的价格，票价区域用于划分座位等级
     *
     * <p>典型的票价区域划分：
     * <ul>
     *   <li>VIP区：最佳视野，价格最高</li>
     *   <li>A区：视野良好，价格适中</li>
     *   <li>B区：普通视野，价格较低</li>
     *   <li>C区：最远位置，价格最低</li>
     * </ul>
     *
     * <p>示例数据：
     * <pre>
     * {
     *   "zoneName": "VIP",
     *   "price": 1280.00,
     *   "color": "#FFD700",
     *   "totalSeats": 500,
     *   "availableSeats": 320
     * }
     * </pre>
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceZone {
        /** 区域名称，例如："VIP"、"A区"、"B区"、"C区" */
        private String zoneName;

        /** 票价（元），使用BigDecimal避免精度问题 */
        private BigDecimal price;

        /**
         * 区域颜色（16进制颜色码）
         *
         * <p>用于前端座位图显示，不同区域用不同颜色标识：
         * <ul>
         *   <li>VIP: #FFD700（金色）</li>
         *   <li>A区: #FF6347（番茄红）</li>
         *   <li>B区: #4169E1（皇家蓝）</li>
         *   <li>C区: #32CD32（酸橙绿）</li>
         * </ul>
         */
        private String color;

        /** 该区域总座位数 */
        private Integer totalSeats;

        /** 该区域可用座位数，实时更新 */
        private Integer availableSeats;
    }

    /**
     * 评分信息（嵌套文档）
     *
     * <p>用于展示节目的用户评价和热度
     *
     * <p>示例数据：
     * <pre>
     * {
     *   "score": 8.7,
     *   "count": 15230,
     *   "distribution": {
     *     "5": 8500,  // 5星：8500人
     *     "4": 4200,  // 4星：4200人
     *     "3": 2100,  // 3星：2100人
     *     "2": 350,   // 2星：350人
     *     "1": 80     // 1星：80人
     *   }
     * }
     * </pre>
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Rating {
        /** 平均评分（0-10分），例如：8.7 */
        private Double score;

        /** 评分人数，例如：15230 */
        private Integer count;

        /**
         * 评分分布（星级 -> 人数）
         *
         * <p>用于绘制评分柱状图，展示评分详情：
         * <ul>
         *   <li>Key: 星级（1-5）</li>
         *   <li>Value: 该星级的人数</li>
         * </ul>
         */
        private Map<Integer, Integer> distribution;
    }
}
