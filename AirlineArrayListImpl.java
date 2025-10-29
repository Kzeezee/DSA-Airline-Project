import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class AirlineArrayListImpl {
    private HashMap<String, List<AirlineReview>> airlineReviews;
    private String airline;

    public AirlineArrayListImpl(HashMap<String, List<AirlineReview>> airlineReviews, String airline) {
        this.airlineReviews = airlineReviews;
        this.airline = airline;
    }

    public Pair<ArrayList<WordCount>, ArrayList<WordCount>> getTop10MostCommonWords() {
        List<AirlineReview> reviews = airlineReviews.get(airline);
        if (reviews == null || reviews.isEmpty()) {
            return Pair.of(new ArrayList<>(), new ArrayList<>());
        }
        ArrayList<String> goodWords = new ArrayList<>();
        ArrayList<String> badWords = new ArrayList<>();

        for (AirlineReview review : reviews) {
            String rec = review.getRecommended();
            if (rec != null && rec.replace("\"", "").equals("1")) {
                goodWords.addAll(Arrays.asList(review.getTokenizedReview()));
            } else {
                badWords.addAll(Arrays.asList(review.getTokenizedReview()));
            }
        }

        Collections.sort(goodWords);
        Collections.sort(badWords);

        ArrayList<WordCount> goodCounts = compressCounts(goodWords);
        ArrayList<WordCount> badCounts = compressCounts(badWords);

        Pair<ArrayList<WordCount>, ArrayList<WordCount>> filtered = removeNearCommonWords(goodCounts, badCounts, 0.30);

        ArrayList<WordCount> topGood = takeTopKWords(filtered.getLeft(), 10);
        ArrayList<WordCount> topBad = takeTopKWords(filtered.getRight(), 10);

        return Pair.of(topGood, topBad);
    }

    private static ArrayList<WordCount> compressCounts(ArrayList<String> words) {
        ArrayList<WordCount> counts = new ArrayList<>();
        if (words.isEmpty()) {
            return counts;
        }
        int total = words.size();
        String currentWord = words.get(0);
        int runLength = 1;
        for (int i = 1; i < words.size(); i++) {
            String word = words.get(i);
            if (word.equals(currentWord)) {
                runLength++;
            } else {
                counts.add(new WordCount(currentWord, runLength, total));
                currentWord = word;
                runLength = 1;
            }
        }
        counts.add(new WordCount(currentWord, runLength, total));
        return counts;
    }

    private static Pair<ArrayList<WordCount>, ArrayList<WordCount>> removeNearCommonWords(
            ArrayList<WordCount> goodCounts,
            ArrayList<WordCount> badCounts,
            double relativeDifferenceThreshold) {
        ArrayList<WordCount> filteredGood = new ArrayList<>();
        ArrayList<WordCount> filteredBad = new ArrayList<>();

        int i = 0;
        int j = 0;
        while (i < goodCounts.size() && j < badCounts.size()) {
            WordCount gw = goodCounts.get(i);
            WordCount bw = badCounts.get(j);
            int cmp = gw.getWord().compareTo(bw.getWord());
            if (cmp == 0) {
                double p1 = gw.getPercent();
                double p2 = bw.getPercent();
                double relDiff = (Math.max(p1, p2) == 0.0) ? 0.0 : Math.abs(p1 - p2) / Math.max(p1, p2);
                if (relDiff > relativeDifferenceThreshold) {
                    if (p1 > p2) {
                        filteredGood.add(gw);
                    } else {
                        filteredBad.add(bw);
                    }
                }
                i++;
                j++;
            } else if (cmp < 0) {
                filteredGood.add(gw);
                i++;
            } else {
                filteredBad.add(bw);
                j++;
            }
        }
        while (i < goodCounts.size()) {
            filteredGood.add(goodCounts.get(i++));
        }
        while (j < badCounts.size()) {
            filteredBad.add(badCounts.get(j++));
        }
        return Pair.of(filteredGood, filteredBad);
    }

    private static ArrayList<WordCount> takeTopKWords(ArrayList<WordCount> counts, int k) {
        ArrayList<WordCount> sorted = new ArrayList<>(counts);
        sorted.sort((a, b) -> Integer.compare(b.getCount(), a.getCount()));
        ArrayList<WordCount> result = new ArrayList<>();
        int limit = Math.min(k, sorted.size());
        for (int idx = 0; idx < limit; idx++) {
            result.add(sorted.get(idx));
        }
        return result;
    }
    /**
     * Get ArrayList of AirlineReviews for a specific airline
     * From that, make two ArrayLists of "Good" and "Bad" reviews
     * Have a method that finds the top 10 most common words in those two ArrayLists
     * Return the top 10 most common words in the Good ArrayList 
     * and the top 10 most common words in the Bad ArrayList
     * If a word has the around the same frequency in both ArrayLists, then it should be nullified, 
     * as this is likely a common word that is used in both good and bad reviews, rather than an indicator of a good or bad review.
     * Show how frequent each word appears, what the word is, and the percentage of the word in the ArrayList
    */
}

class WordCount {
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

    public String toString() {
        return "Word: " + word + ", Count: " + count + ", Percent: " + getPercent();
    }
}

class Pair<L, R> {
    private final L left;
    private final R right;

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public L getLeft() {
        return left;
    }

    public R getRight() {
        return right;
    }

    public static <L, R> Pair<L, R> of(L left, R right) {
        return new Pair<>(left, right);
    }
}