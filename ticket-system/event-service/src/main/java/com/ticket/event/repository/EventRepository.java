package com.ticket.event.repository;

import com.ticket.event.document.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Event MongoDB Repository - 节目仓储接口
 *
 * <p>Spring Data MongoDB提供的强大功能：
 * <ul>
 *   <li><b>方法命名查询：</b>根据方法名自动生成查询语句（如 findByCategory）</li>
 *   <li><b>@Query注解：</b>支持原生MongoDB查询语法，实现复杂查询</li>
 *   <li><b>嵌套字段查询：</b>支持点号访问嵌套对象（如 'venue.city'）</li>
 *   <li><b>分页查询：</b>通过Pageable参数实现分页</li>
 *   <li><b>排序查询：</b>支持多字段排序</li>
 *   <li><b>聚合统计：</b>支持count、sum等聚合操作</li>
 * </ul>
 *
 * <p>MongoDB查询语法说明：
 * <ul>
 *   <li>$and - 逻辑与</li>
 *   <li>$or - 逻辑或</li>
 *   <li>$in - 包含于（类似SQL的IN）</li>
 *   <li>$gte - 大于等于</li>
 *   <li>$lte - 小于等于</li>
 *   <li>$regex - 正则表达式匹配</li>
 * </ul>
 *
 * <p>参数占位符：
 * <ul>
 *   <li>?0 - 第一个参数</li>
 *   <li>?1 - 第二个参数</li>
 *   <li>?2 - 第三个参数</li>
 *   <li>以此类推...</li>
 * </ul>
 *
 * @author Ticket System
 * @version 1.0
 */
@Repository
public interface EventRepository extends MongoRepository<Event, String> {

    /**
     * 根据类别查询节目列表
     *
     * <p>方法命名查询：Spring Data会自动解析方法名并生成查询
     *
     * <p>生成的MongoDB查询：
     * <pre>db.events.find({"category": "CONCERT"})</pre>
     *
     * <p>使用场景：
     * <ul>
     *   <li>查询所有演唱会</li>
     *   <li>查询所有电影</li>
     *   <li>按类别展示节目</li>
     * </ul>
     *
     * @param category 节目类别（CONCERT/MOVIE/DRAMA/SPORTS等）
     * @return 该类别的所有节目
     */
    List<Event> findByCategory(String category);

    /**
     * 根据状态查询节目列表
     *
     * <p>生成的MongoDB查询：
     * <pre>db.events.find({"status": "ON_SALE"})</pre>
     *
     * <p>使用场景：
     * <ul>
     *   <li>查询正在售票的节目</li>
     *   <li>查询已售罄的节目</li>
     *   <li>后台管理：按状态筛选</li>
     * </ul>
     *
     * @param status 节目状态（UPCOMING/ON_SALE/SOLD_OUT/CANCELLED/ENDED）
     * @return 该状态的所有节目
     */
    List<Event> findByStatus(String status);

    /**
     * 根据城市查询节目列表（查询嵌套字段）
     *
     * <p>MongoDB查询嵌套字段：使用点号访问嵌套对象的属性
     *
     * <p>生成的MongoDB查询：
     * <pre>db.events.find({"venue.city": "北京"})</pre>
     *
     * <p>使用场景：
     * <ul>
     *   <li>查询北京的所有演出</li>
     *   <li>地域筛选：用户选择城市后筛选节目</li>
     *   <li>同城活动推荐</li>
     * </ul>
     *
     * <p>技术亮点：
     * <ul>
     *   <li>MongoDB支持嵌套查询，无需JOIN</li>
     *   <li>性能优于关系型数据库的多表关联</li>
     * </ul>
     *
     * @param city 城市名称，例如："北京"、"上海"
     * @return 该城市的所有节目
     */
    @Query("{'venue.city': ?0}")
    List<Event> findByCity(String city);

    /**
     * 根据时间范围查询节目列表
     *
     * <p>方法命名查询：Between会生成范围查询
     *
     * <p>生成的MongoDB查询：
     * <pre>
     * db.events.find({
     *   "startTime": {
     *     "$gte": "2024-05-01T00:00:00",
     *     "$lte": "2024-05-31T23:59:59"
     *   }
     * })
     * </pre>
     *
     * <p>使用场景：
     * <ul>
     *   <li>查询本周的演出</li>
     *   <li>查询本月的节目</li>
     *   <li>按日期筛选节目</li>
     * </ul>
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return 该时间段内开始的所有节目
     */
    List<Event> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    /**
     * 根据名称模糊查询（支持关键词搜索）
     *
     * <p>方法命名查询：Containing会生成包含查询（类似SQL的LIKE）
     *
     * <p>生成的MongoDB查询：
     * <pre>db.events.find({"name": {$regex: "周杰伦"}})</pre>
     *
     * <p>使用场景：
     * <ul>
     *   <li>搜索框：用户输入关键词搜索节目</li>
     *   <li>例如：搜索"周杰伦"，可以找到"周杰伦演唱会"、"周杰伦北京站"等</li>
     * </ul>
     *
     * <p>技术说明：
     * <ul>
     *   <li>MongoDB会使用正则表达式进行匹配</li>
     *   <li>建议在name字段上建立文本索引以提高性能</li>
     * </ul>
     *
     * @param keyword 搜索关键词，例如："周杰伦"、"演唱会"
     * @return 名称包含关键词的所有节目
     */
    List<Event> findByNameContaining(String keyword);

    /**
     * 复杂查询：根据多个条件查询并分页
     *
     * <p>MongoDB复杂查询示例：使用$and组合多个条件
     *
     * <p>生成的MongoDB查询：
     * <pre>
     * db.events.find({
     *   "$and": [
     *     {"category": "CONCERT"},
     *     {"status": "ON_SALE"},
     *     {"venue.city": "北京"}
     *   ]
     * }).skip(0).limit(10)
     * </pre>
     *
     * <p>使用场景：
     * <ul>
     *   <li>高级筛选：同时按类别、状态、城市筛选</li>
     *   <li>例如：查询"北京正在售票的演唱会"</li>
     *   <li>列表页分页展示</li>
     * </ul>
     *
     * <p>分页参数示例：
     * <pre>
     * // 查询第1页，每页10条
     * Pageable pageable = PageRequest.of(0, 10);
     * // 查询第1页，每页10条，按开始时间排序
     * Pageable pageable = PageRequest.of(0, 10, Sort.by("startTime").ascending());
     * </pre>
     *
     * @param category 节目类别
     * @param status   节目状态
     * @param city     城市
     * @param pageable 分页参数（页码、每页数量、排序）
     * @return 分页结果（包含数据列表、总数、总页数等）
     */
    @Query("{ $and: [ " +
           "  {'category': ?0}, " +
           "  {'status': ?1}, " +
           "  {'venue.city': ?2} " +
           "]}")
    Page<Event> findByMultipleConditions(String category, String status, String city, Pageable pageable);

    /**
     * 根据标签查询（查询数组字段）
     *
     * <p>MongoDB数组查询：$in操作符用于匹配数组中的任一元素
     *
     * <p>生成的MongoDB查询：
     * <pre>
     * db.events.find({
     *   "tags": {
     *     "$in": ["流行音乐", "华语"]
     *   }
     * })
     * </pre>
     *
     * <p>使用场景：
     * <ul>
     *   <li>标签筛选：用户选择多个标签，查询包含任一标签的节目</li>
     *   <li>例如：选择["流行音乐", "华语"]，可以找到所有流行音乐或华语节目</li>
     *   <li>推荐系统：根据用户兴趣标签推荐节目</li>
     * </ul>
     *
     * <p>技术说明：
     * <ul>
     *   <li>tags是数组字段，$in会检查数组中是否包含任一指定值</li>
     *   <li>建议在tags字段上建立多键索引以提高查询性能</li>
     * </ul>
     *
     * @param tags 标签列表，例如：["流行音乐", "华语", "演唱会"]
     * @return 包含任一标签的所有节目
     */
    @Query("{'tags': {$in: ?0}}")
    List<Event> findByTags(List<String> tags);

    /**
     * 查询即将开始的活动（带排序）
     *
     * <p>MongoDB查询+排序：$gte（大于等于）+ $orderby（排序）
     *
     * <p>生成的MongoDB查询：
     * <pre>
     * db.events.find({
     *   "$and": [
     *     {"startTime": {"$gte": "2024-05-01T00:00:00"}},
     *     {"status": "ON_SALE"}
     *   ]
     * }).sort({"startTime": 1})
     * </pre>
     *
     * <p>使用场景：
     * <ul>
     *   <li>首页展示：即将开始的热门活动</li>
     *   <li>按时间顺序展示：最近的活动排在前面</li>
     *   <li>活动预告</li>
     * </ul>
     *
     * <p>技术说明：
     * <ul>
     *   <li>$gte: 大于等于（Greater Than or Equal）</li>
     *   <li>$orderby: {字段: 1}表示升序，{字段: -1}表示降序</li>
     * </ul>
     *
     * @param now 当前时间，用于筛选未来的活动
     * @return 即将开始的活动列表，按开始时间升序排列
     */
    @Query("{ $and: [ " +
           "  {'startTime': {$gte: ?0}}, " +
           "  {'status': 'ON_SALE'} " +
           "], $orderby: {'startTime': 1}}")
    List<Event> findUpcomingEvents(LocalDateTime now);

    /**
     * 查询热门活动（根据评分排序）
     *
     * <p>MongoDB查询+排序：按嵌套字段排序
     *
     * <p>生成的MongoDB查询：
     * <pre>
     * db.events.find({
     *   "status": "ON_SALE"
     * }).sort({"rating.score": -1})
     * </pre>
     *
     * <p>使用场景：
     * <ul>
     *   <li>首页推荐：高评分的热门节目</li>
     *   <li>排行榜：按评分展示</li>
     *   <li>精选推荐</li>
     * </ul>
     *
     * <p>技术说明：
     * <ul>
     *   <li>sort参数：{"rating.score": -1}表示按评分降序（高分在前）</li>
     *   <li>支持嵌套字段排序</li>
     *   <li>建议限制返回数量（如Top 10）以提高性能</li>
     * </ul>
     *
     * @return 正在售票的节目列表，按评分降序排列
     */
    @Query(value = "{'status': 'ON_SALE'}", sort = "{'rating.score': -1}")
    List<Event> findPopularEvents();

    /**
     * 统计某个类别的节目数量
     *
     * <p>方法命名查询：count开头的方法会返回数量
     *
     * <p>生成的MongoDB查询：
     * <pre>db.events.count({"category": "CONCERT"})</pre>
     *
     * <p>使用场景：
     * <ul>
     *   <li>统计各类别节目数量</li>
     *   <li>数据报表</li>
     *   <li>分类标签显示数量（演唱会(120)、电影(350)）</li>
     * </ul>
     *
     * @param category 节目类别
     * @return 该类别的节目总数
     */
    long countByCategory(String category);

    /**
     * 统计某个状态的节目数量
     *
     * <p>生成的MongoDB查询：
     * <pre>db.events.count({"status": "ON_SALE"})</pre>
     *
     * <p>使用场景：
     * <ul>
     *   <li>后台统计：正在售票的节目数量</li>
     *   <li>监控：已售罄的节目数量</li>
     *   <li>数据看板</li>
     * </ul>
     *
     * @param status 节目状态
     * @return 该状态的节目总数
     */
    long countByStatus(String status);
}
