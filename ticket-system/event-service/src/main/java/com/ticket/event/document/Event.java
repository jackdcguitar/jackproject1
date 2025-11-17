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
 * 使用MongoDB存储灵活的节目信息，支持复杂的嵌套数据结构
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "events")
public class Event {

    @Id
    private String id;

    /**
     * 节目名称
     */
    private String name;

    /**
     * 类别：CONCERT(演唱会), MOVIE(电影), DRAMA(话剧), SPORTS(体育赛事)
     */
    private String category;

    /**
     * 场馆信息
     */
    private Venue venue;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 海报URL
     */
    private String poster;

    /**
     * 详细介绍（支持富文本）
     */
    private String description;

    /**
     * 演出者/主演信息（灵活结构）
     */
    private List<Performer> performers;

    /**
     * 票价区间
     */
    private List<PriceZone> priceZones;

    /**
     * 总座位数
     */
    private Integer totalSeats;

    /**
     * 可用座位数
     */
    private Integer availableSeats;

    /**
     * 状态：UPCOMING(即将上映), ON_SALE(售票中), SOLD_OUT(售罄), CANCELLED(已取消), ENDED(已结束)
     */
    private String status;

    /**
     * 标签（灵活的标签系统）
     */
    private List<String> tags;

    /**
     * 元数据（灵活的扩展字段）
     * 例如：{"language": "中文", "subtitle": "英文", "duration": "120分钟"}
     */
    private Map<String, Object> metadata;

    /**
     * 评分信息
     */
    private Rating rating;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 场馆信息（嵌套文档）
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Venue {
        private String name;        // 场馆名称
        private String address;     // 地址
        private String city;        // 城市
        private Double latitude;    // 纬度
        private Double longitude;   // 经度
        private Integer capacity;   // 容量
        private Map<String, Object> facilities;  // 设施信息
    }

    /**
     * 演出者信息（嵌套文档）
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Performer {
        private String name;        // 名称
        private String role;        // 角色（主演/导演/乐队等）
        private String avatar;      // 头像
        private String bio;         // 简介
    }

    /**
     * 票价区域（嵌套文档）
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceZone {
        private String zoneName;    // 区域名称（VIP/A/B/C等）
        private BigDecimal price;   // 价格
        private String color;       // 区域颜色（用于前端显示）
        private Integer totalSeats; // 该区域总座位数
        private Integer availableSeats;  // 该区域可用座位数
    }

    /**
     * 评分信息（嵌套文档）
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Rating {
        private Double score;       // 评分（0-10）
        private Integer count;      // 评分人数
        private Map<Integer, Integer> distribution;  // 评分分布（1星到5星的人数）
    }
}
