public class WordCount {
    private final String word;
    private final int count;
    private final int totalWordsInList;

    public WordCount(String word, int count, int totalWordsInList) {
        this.word = word;
        this.count = count;
        this.totalWordsInList = totalWordsInList;
    }

    public String getWord() {
        return word;
    }

    public int getCount() {
        return count;
    }

    public double getPercent() {
        if (totalWordsInList == 0) return 0.0;
        return (double) count / (double) totalWordsInList;
    }

    @Override
    public String toString() {
        return "Word: " + word + ", Count: " + count + ", Percent: " + getPercent();
    }
}

