package org.example.ds;

import java.time.Instant;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.example.model.AirlineReview;

public class AirlineMapImpl {
    public static void MapImplementationTest(Map<String, List<AirlineReview>> airlineReviews, String airline) {
        Long startTime = Instant.now().toEpochMilli();
        HashMap<String, Integer> freqGood = new HashMap<>();
        HashMap<String, Integer> freqBad = new HashMap<>();
        Integer badReviews = 0, goodReviews = 0;
        for (AirlineReview ar : airlineReviews.get(airline)) {
            String[] tokenizedReview = ar.getTokenizedReview();
            // For recommended
            if (Integer.parseInt(ar.getRecommended()) == 1) {
                goodReviews++;
                for (String tokenizedWord : tokenizedReview) {
                    if (!freqGood.containsKey(tokenizedWord)) {
                        freqGood.put(tokenizedWord, 0);
                    }
                    freqGood.put(tokenizedWord, freqGood.get(tokenizedWord) + 1);
                }
            } else { // For not recommended
                badReviews++;
                for (String tokenizedWord : tokenizedReview) {
                    if (!freqBad.containsKey(tokenizedWord)) {
                        freqBad.put(tokenizedWord, 0);
                    }
                    freqBad.put(tokenizedWord, freqBad.get(tokenizedWord) + 1);
                }
            }
        }

        // Now get top 10 words for good and bad review
        // Use a TreeMap now to store frequency
        TreeMap<Integer, Set<String>> treeMap = new TreeMap<>(Collections.reverseOrder());
        for (Map.Entry<String, Integer> e : freqGood.entrySet()) {
            treeMap.putIfAbsent(e.getValue(), new HashSet<>());
            treeMap.get(e.getValue()).add(e.getKey());
        }

        // Initialize and extract top 10 words list for good and bad in descending order
        List<Map.Entry<String, Integer>> top10Good = addTop10ToList(treeMap);

        // Similarly, do it for bad reviews
        treeMap = new TreeMap<>(Collections.reverseOrder());
        for (Map.Entry<String, Integer> e : freqBad.entrySet()) {
            treeMap.putIfAbsent(e.getValue(), new HashSet<>());
            treeMap.get(e.getValue()).add(e.getKey());
        }
        List<Map.Entry<String, Integer>> top10Bad = addTop10ToList(treeMap);

        Long timeTakenInMilli = Instant.now().toEpochMilli() - startTime;
        System.out.println("Freq Hash Map time taken: " + timeTakenInMilli);
        System.out.println("Freq Hash Map key-value pairs number: " + freqGood.size());
        System.out.println("Top 10 words in Good Reviews (" + goodReviews + ") for " + airline + ":");
        for (Map.Entry<String, Integer> entry : top10Good) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        System.out.println("Top 10 words in Bad Reviews (" + badReviews + ") for " + airline + ":");
        for (Map.Entry<String, Integer> entry : top10Bad) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }

        return;
    }

    private static List<Entry<String, Integer>> addTop10ToList(TreeMap<Integer, Set<String>> treeMap) {
        List<Entry<String, Integer>> top10 = new ArrayList<>();
        // Populate top10Good from treeMap
        for (Map.Entry<Integer, Set<String>> entry : treeMap.entrySet()) {
            for (String word : entry.getValue()) {
                top10.add(new AbstractMap.SimpleEntry<>(word, entry.getKey()));
                if (top10.size() >= 10) {
                    break;
                }
            }
            if (top10.size() >= 10) {
                break;
            }
        }

        return top10;
    }
}
