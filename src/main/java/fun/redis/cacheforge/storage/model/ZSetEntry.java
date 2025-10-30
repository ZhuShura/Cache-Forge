package fun.redis.cacheforge.storage.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * zSet实体类, 用于定义排序规则和实现zSet
 *
 * @author hua
 * @date 2025/10/26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZSetEntry {

    private NavigableSet<ZSetValue> values = new ConcurrentSkipListSet<>();
    // 添加成员到分数的映射，提高查询效率
    private transient Map<String, Double> memberToScore = new ConcurrentHashMap<>();


}
