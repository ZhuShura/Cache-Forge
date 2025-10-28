package fun.redis.cacheforge.storage.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;

/**
 * CacheForge列表实体
 * @author huangtaiji
 * @date 2025/10/27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListEntry {
	private List<String> value;
}
