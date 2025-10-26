package fun.redis.cacheforge;

import fun.redis.cacheforge.server.RedisServer;
import fun.redis.cacheforge.utils.WordArtUtil;

/**
 * 项目启动器
 * @author huangtaiji
 * @date 2025/10/25
 */
public class CacheForgeApplication {
	public static void main(String[] args) {
		WordArtUtil.wordArtDraw();
		RedisServer.start();
	}
}
