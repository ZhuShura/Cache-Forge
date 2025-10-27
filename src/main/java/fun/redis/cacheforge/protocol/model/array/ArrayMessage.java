package fun.redis.cacheforge.protocol.model.array;

import fun.redis.cacheforge.protocol.model.Message;
import io.netty.util.AbstractReferenceCounted;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ReferenceCounted;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.StringUtil;

import java.util.Collections;
import java.util.List;

/**
 * 数组消息
 * @author huangtaiji
 * @date 2025/10/26
 */
public class ArrayMessage extends AbstractReferenceCounted implements Message {

	/**
	 * {@code @instance}nil数组消息实例 *-1\r\n
	 */
	public static final ArrayMessage NULL_INSTANCE = new ArrayMessage(){
		@Override
		public boolean isNull() {
			return true;
		}

		@Override
		public String toString() {
			return "NullArrayMessage";
		}
	};

	/**
	 * {@code @instance}空数组消息实例 *0\r\n
	 */
	public static final ArrayMessage EMPTY_INSTANCE = new ArrayMessage(){
		@Override
		public String toString() {
			return "EmptyArrayMessage";
		}
	};



	private final List<Message> children;

	private ArrayMessage() {
		this.children = Collections.emptyList();
	}

	public ArrayMessage(List<Message> children) {
		this.children = ObjectUtil.checkNotNull(children, "children");
	}

	public List<Message> children() {
		return children;
	}

	public boolean isNull() {
		return false;
	}


	/**
	 * 释放数组消息及其所有子元素
	 */
	@Override
	protected void deallocate() {
		for (Message child : children) {
			ReferenceCountUtil.release(child);
		}
	}

	/**
	 * 记录整个数组消息及其所有子元素的访问轨迹
	 */
	@Override
	public ReferenceCounted touch(Object hint) {
		for (Message child : children) {
			ReferenceCountUtil.touch(child);
		}
		return this;
	}


	@Override
	public String toString() {
		return new StringBuilder(StringUtil.simpleClassName(this))
				.append('[')
				.append("children=")
				.append(children.size())
				.append(']').toString();
	}
}
