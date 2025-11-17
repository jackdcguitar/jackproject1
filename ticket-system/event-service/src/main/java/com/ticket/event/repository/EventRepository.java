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
 * Event MongoDB Repository
 * 利用MongoDB的强大查询能力
 */
@Repository
public interface EventRepository extends MongoRepository<Event, String> {

    /**
     * 根据类别查询
     */
    List<Event> findByCategory(String category);

    /**
     * 根据状态查询
     */
    List<Event> findByStatus(String status);

    /**
     * 根据城市查询
     */
    @Query("{'venue.city': ?0}")
    List<Event> findByCity(String city);

    /**
     * 根据时间范围查询
     */
    List<Event> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    /**
     * 根据名称模糊查询
     */
    List<Event> findByNameContaining(String keyword);

    /**
     * 复杂查询：根据类别、状态、城市查询（分页）
     */
    @Query("{ $and: [ " +
           "  {'category': ?0}, " +
           "  {'status': ?1}, " +
           "  {'venue.city': ?2} " +
           "]}")
    Page<Event> findByMultipleConditions(String category, String status, String city, Pageable pageable);

    /**
     * 根据标签查询
     */
    @Query("{'tags': {$in: ?0}}")
    List<Event> findByTags(List<String> tags);

    /**
     * 查询即将开始的活动
     */
    @Query("{ $and: [ " +
           "  {'startTime': {$gte: ?0}}, " +
           "  {'status': 'ON_SALE'} " +
           "], $orderby: {'startTime': 1}}")
    List<Event> findUpcomingEvents(LocalDateTime now);

    /**
     * 查询热门活动（根据评分）
     */
    @Query(value = "{'status': 'ON_SALE'}", sort = "{'rating.score': -1}")
    List<Event> findPopularEvents();

    /**
     * 统计某个类别的活动数量
     */
    long countByCategory(String category);

    /**
     * 统计某个状态的活动数量
     */
    long countByStatus(String status);
}
