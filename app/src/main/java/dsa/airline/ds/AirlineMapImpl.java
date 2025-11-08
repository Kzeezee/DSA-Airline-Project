package dsa.airline.ds;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import dsa.airline.model.AirlineReview;
import dsa.airline.model.util.Pair;
import dsa.airline.model.util.WordCount;

public class AirlineMapImpl implements WordFrequencyAnalyzer {
    private Map<String, List<AirlineReview>> airlineReviews;
    private String airline;

    public AirlineMapImpl(Map<String, List<AirlineReview>> airlineReviews, String airline) {
        this.airlineReviews = airlineReviews;
        this.airline = airline;
    }

    // Same logic as MapImplementationTest, reformatted to match other implementations
    public Pair<List<WordCount>, List<WordCount>> getTop10MostCommonWords() {
        HashMap<String, Integer> freqGood = new HashMap<>();
        HashMap<String, Integer> freqBad = new HashMap<>();
        Integer badReviews = 0, goodReviews = 0;
        for (AirlineReview ar : airlineReviews.get(airline)) {
            String[] tokenizedReview = ar.getTokenizedReview();
            if (Integer.parseInt(ar.getRecommended()) == 1) {
                goodReviews++;
                for (String tokenizedWord : tokenizedReview) {
                    if (!freqGood.containsKey(tokenizedWord)) {
                        freqGood.put(tokenizedWord, 0);
                    }
                    freqGood.put(tokenizedWord, freqGood.get(tokenizedWord) + 1);
                }
            } else {
                badReviews++;
                for (String tokenizedWord : tokenizedReview) {
                    if (!freqBad.containsKey(tokenizedWord)) {
                        freqBad.put(tokenizedWord, 0);
                    }
                    freqBad.put(tokenizedWord, freqBad.get(tokenizedWord) + 1);
                }
            }
        }

        // Convert to WordCount lists (format parity with other implementations)
        List<WordCount> goodList = topKFromMap(freqGood, goodReviews, 10);
        List<WordCount> badList = topKFromMap(freqBad, badReviews, 10);

        return Pair.of(goodList, badList);
    }

    // Map-specific Top-K using TreeMap bucketing (keeps original selection logic)
    private List<WordCount> topKFromMap(HashMap<String, Integer> freq, int totalWords, int k) {
        TreeMap<Integer, Set<String>> treeMap = new TreeMap<>(Collections.reverseOrder());
        for (Map.Entry<String, Integer> e : freq.entrySet()) {
            treeMap.putIfAbsent(e.getValue(), new HashSet<>());
            treeMap.get(e.getValue()).add(e.getKey());
        }
        ArrayList<WordCount> topK = new ArrayList<>();
        for (Map.Entry<Integer, Set<String>> entry : treeMap.entrySet()) {
            for (String word : entry.getValue()) {
                topK.add(new WordCount(word, entry.getKey(), totalWords));
                if (topK.size() >= k) {
                    break;
                }
            }
            if (topK.size() >= k) {
                break;
            }
        }
        return topK;
    }
}
