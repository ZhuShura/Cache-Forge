package fun.redis.cacheforge.command.handler.impl.string;

import fun.redis.cacheforge.command.handler.ReadCommandHandler;
import fun.redis.cacheforge.command.model.Command;
import fun.redis.cacheforge.common.CacheForgeCodecException;
import fun.redis.cacheforge.protocol.model.array.ArrayMessage;
import fun.redis.cacheforge.protocol.model.bulkString.FullBulkStringMessage;
import fun.redis.cacheforge.protocol.model.generalizedInline.IntegerMessage;
import fun.redis.cacheforge.storage.repo.StringStore;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static fun.redis.cacheforge.utils.MessageUtil.*;

@Slf4j
public class LCSCommandHandler implements ReadCommandHandler {
	@Override
	public void handle(ChannelHandlerContext ctx, Command command) {
		try {
			String[] args = command.getArgs();
			if (args.length >= 2) {
				String key1 = args[0];
				String key2 = args[1];

				String value1 = StringStore.get(key1);
				String value2 = StringStore.get(key2);
				/**
				 * Redis官方返回空字符串，这里返回 nil
				 */
				if (value1 == null || value2 == null) {
					ctx.writeAndFlush(FullBulkStringMessage.NULL_INSTANCE);
					return;
				}

				List<Object> lcs = LCS(value1, value2);
				String lcsMatch = (String) lcs.get(0);
				List<int[]> positions = (List<int[]>) lcs.get(1);
				int lcsLen = lcsMatch.length();

				if (args.length == 2) {
					ctx.writeAndFlush(toFullBulkStringMessage(lcsMatch));
				} else if (args.length == 3 && args[2].equalsIgnoreCase(Rule.LEN.name())) {
					ctx.writeAndFlush(toIntegerMessage(lcsLen));
				} else if (args.length >= 3 && args[2].equalsIgnoreCase(Rule.IDX.name())) {
					List<MatchSegment> segments = mergeToSegments(positions);
					// 组装响应
					FullBulkStringMessage response1  = toFullBulkStringMessage("matches");
					FullBulkStringMessage response3 = toFullBulkStringMessage("len");
					IntegerMessage response4 = toIntegerMessage(lcsLen);

					if (args.length == 3) {
						ArrayMessage response2 = assembleResponse2(segments, false);
						ctx.writeAndFlush(toArrayMessage(response1, response2, response3, response4));

					} else if (args.length >= 5 && args[3].equalsIgnoreCase("MINMATCHLEN")) {
						List<MatchSegment> filteredSegments = new ArrayList<>();
						for (MatchSegment segment : segments) {
							if (segment.getLength() >= Integer.parseInt(args[4])) {
								filteredSegments.add(segment);
							}
						}

						if (args.length == 6 && args[5].equalsIgnoreCase("WITHMATCHLEN")) {
							ArrayMessage response2 = assembleResponse2(filteredSegments, true);
							ctx.writeAndFlush(toArrayMessage(response1, response2, response3, response4));

						} else if (args.length == 5) {
							ArrayMessage response2 = assembleResponse2(filteredSegments, false);
							ctx.writeAndFlush(toArrayMessage(response1, response2, response3, response4));

						} else {
							throw new CacheForgeCodecException("lcs命令参数错误");
						}
					} else {
						throw new CacheForgeCodecException("lcs命令参数错误");
					}
				} else {
					throw new CacheForgeCodecException("lcs命令参数错误");
				}
			} else {
				log.error("lcs命令参数错误");
				// todo
				ctx.writeAndFlush(toErrorMessage(Err.ERR));
			}
		} catch (Exception e) {
			log.error("lcs命令异常{}", String.valueOf(e));
			// todo
			ctx.writeAndFlush(toErrorMessage(Err.ERR));
		}
	}

	private ArrayMessage assembleResponse2(List<MatchSegment> segments, boolean withMatchLen) {
		List<ArrayMessage> children = new ArrayList<>();
		for (MatchSegment segment : segments) {
			IntegerMessage start1 = toIntegerMessage(segment.getStart1());
			IntegerMessage end1 = toIntegerMessage(segment.getEnd1());
			ArrayMessage part1 = toArrayMessage(end1, start1);
			IntegerMessage start2 = toIntegerMessage(segment.getStart2());
			IntegerMessage end2 = toIntegerMessage(segment.getEnd2());
			ArrayMessage part2 = toArrayMessage(end2, start2);

			if (withMatchLen) {
				IntegerMessage matchLen = toIntegerMessage(segment.getLength());
				ArrayMessage oneSegment = toArrayMessage(part1, part2, matchLen);
				children.add(oneSegment);
			} else {
				ArrayMessage oneSegment = toArrayMessage(part1, part2);
				children.add(oneSegment);
			}
		}
		return toArrayMessage(children.toArray(new ArrayMessage[0]));
	}

	/**
	 * 最长公共子序列
	 * @param s1 字符串1
	 * @param s2 字符串2
	 * @return 最长公共子序列(全信息)
	 * i,j分别代表s1,s2的索引
	 */
	private List<Object> LCS(String s1, String s2) {
		List<Object> result = new ArrayList<>();

		// 正序dp找lcs
		int len1 = s1.length(), len2 = s2.length();
		int[][] dp = new int[len1 + 1][len2 + 1];
		for (int i = 1; i <= len1; i++) {
			for (int j = 1; j <= len2; j++) {
				if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
					dp[i][j] = dp[i - 1][j - 1] + 1;
				} else {
					dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
				}
			}
		}

		if (dp[len1][len2] == 0) {
			result.add("");
			result.add(Collections.EMPTY_LIST);
			return result;
		}

		// 逆序拼接lcs
		int i = len1, j = len2;
		StringBuilder lcs = new StringBuilder();
		List<int[]> positions = new ArrayList<>();
		while (i > 0 && j > 0) {
			if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
				// 字符匹配，加入lcs
				lcs.append(s1.charAt(i - 1));
				i--;
				j--;
				positions.add(new int[]{i, j});
			} else if (dp[i - 1][j] > dp[i][j - 1]) {
				i--;
			} else {
				j--;
			}
		}

		// 处理信息
		result.add(lcs.reverse().toString());
		result.add(positions);
		return result;
	}


	/**
	 * 规则
	 */
	private enum Rule {
		LEN, IDX
	}


	/**
	 * 匹配连续片段
	 */
	@Data
	private static class MatchSegment {
		private int start1, start2, end1, end2, length;

		public MatchSegment(int start1, int end1, int start2, int end2) {
			this.start1 = start1;
			this.start2 = start2;
			this.end1 = end1;
			this.end2 = end2;
			this.length = start1 - end1 + 1;
		}
	}

	/**
	 * 合并匹配片段为片段列表
	 * @param positions 匹配片段列表
	 * @return 片段列表
	 */
	private static List<MatchSegment> mergeToSegments(List<int[]> positions) {
		List<MatchSegment> segments = new ArrayList<>();
		if (positions.isEmpty()) return segments;

		// 初始化第一个片段
		int[] first = positions.get(0);
		int start1 = first[0], end1 = first[0];
		int start2 = first[1], end2 = first[1];

		// 遍历剩余索引，合并连续片段
		for (int k = 1; k < positions.size(); k++) {
			int[] curr = positions.get(k);
			int prevS1 = positions.get(k - 1)[0];
			int prevS2 = positions.get(k - 1)[1];

			// 检查是否连续：当前索引 = 上一个索引 - 1（s1和s2都需连续）
			if (curr[0] == prevS1 - 1 && curr[1] == prevS2 - 1) {
				end1 = curr[0];
				end2 = curr[1];
			} else {
				// 不连续，结束上一个片段，开始新片段
				segments.add(new MatchSegment(start1, end1, start2, end2));
				start1 = end1 = curr[0];
				start2 = end2 = curr[1];
			}
		}

		// 添加最后一个片段
		segments.add(new MatchSegment(start1, end1, start2, end2));
		return segments;
	}
}
