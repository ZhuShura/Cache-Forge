package fun.redis.cacheforge.common;

import io.netty.handler.codec.CodecException;

public class CacheForgeCodecException extends CodecException {
	private static final long serialVersionUID = 1L;

	public CacheForgeCodecException(String message) {
		super(message);
	}

	public CacheForgeCodecException(Throwable cause) {
		super(cause);
	}
}
