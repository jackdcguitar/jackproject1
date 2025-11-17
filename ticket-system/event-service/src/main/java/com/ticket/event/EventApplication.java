package com.ticket.event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Event Service - 节目服务启动类
 *
 * <p>服务职责：
 * <ul>
 *   <li>管理节目信息：创建、查询、更新、删除节目</li>
 *   <li>节目搜索：支持多维度搜索（类别、城市、标签、时间等）</li>
 *   <li>节目推荐：热门节目、即将开始的节目</li>
 *   <li>数据统计：节目数量统计、评分统计</li>
 * </ul>
 *
 * <p>技术栈：
 * <ul>
 *   <li>数据库：MongoDB 5.0（存储灵活的节目信息）</li>
 *   <li>缓存：Redis 7.0（缓存热门节目、减轻数据库压力）</li>
 *   <li>服务注册：Nacos（注册到注册中心，供其他服务调用）</li>
 *   <li>服务调用：OpenFeign（调用其他微服务）</li>
 * </ul>
 *
 * <p>为什么使用MongoDB？
 * <ul>
 *   <li>灵活的Schema：不同类型的节目有不同的属性</li>
 *   <li>复杂嵌套：场馆信息、演出者列表、票价区域等</li>
 *   <li>高性能查询：支持复杂查询、全文搜索、地理位置查询</li>
 *   <li>易于扩展：通过metadata字段存储扩展信息</li>
 * </ul>
 *
 * <p>服务端口：8081（可在application.yml中配置）
 * <p>数据库：MongoDB - events数据库
 *
 * @author Ticket System
 * @version 1.0
 */
@SpringBootApplication  // Spring Boot应用标识，包含@Configuration、@EnableAutoConfiguration、@ComponentScan
@EnableDiscoveryClient  // 启用服务发现客户端，注册到Nacos注册中心
@EnableFeignClients     // 启用Feign客户端，用于调用其他微服务（如座位服务、订单服务）
@EnableMongoRepositories(basePackages = "com.ticket.event.repository")  // 启用MongoDB仓储，指定Repository接口所在包
@ComponentScan(basePackages = {"com.ticket.event", "com.ticket.common"})  // 扫描组件，包括本服务和公共模块
public class EventApplication {

    /**
     * 服务启动入口
     *
     * <p>启动流程：
     * <ol>
     *   <li>加载application.yml配置文件</li>
     *   <li>连接MongoDB数据库</li>
     *   <li>连接Redis缓存</li>
     *   <li>注册到Nacos注册中心</li>
     *   <li>启动Web服务（默认端口8081）</li>
     * </ol>
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(EventApplication.class, args);
    }
}
