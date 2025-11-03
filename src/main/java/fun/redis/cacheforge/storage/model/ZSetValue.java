package fun.redis.cacheforge.storage.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * ZSetValue
 * @author hua
 * @date 2025/11/3
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZSetValue implements Comparable<ZSetValue> {
    private String member;
    private Double score;

    @Override
    public int compareTo(ZSetValue other) {
        // 先按分数排序，分数相同则按字典序排序
        int scoreComparison = Double.compare(this.score, other.score);
        return scoreComparison != 0 ? scoreComparison : this.member.compareTo(other.member);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ZSetValue zSetValue = (ZSetValue) o;
        return Objects.equals(member, zSetValue.member);
    }
}