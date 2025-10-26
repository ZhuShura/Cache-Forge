package fun.redis.cacheforge.storage.model;

/**
 * zSet实体类, 用于定义排序规则和实现zSet
 * @author hua
 * @date 2025/10/26
 */
public class ZSetEntry implements Comparable<ZSetEntry>{
    private String member;
    private Double score;

    public ZSetEntry(String member, Double score) {
        this.member = member;
        this.score = score;
    }

    public ZSetEntry() {
    }

    public String getMember() {
        return member;
    }

    public void setMember(String member) {
        this.member = member;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return member + ":" + score;
    }

    @Override
    public int compareTo(ZSetEntry other) {
        // 先按分数排序，分数相同则按字典序排序
        int scoreComparison = Double.compare(this.score, other.score);
        if (scoreComparison != 0) {
            return scoreComparison;
        }
        return this.member.compareTo(other.member);
    }
}
