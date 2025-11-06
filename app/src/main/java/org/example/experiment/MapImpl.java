package org.example.experiment;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;

import org.example.AirlineReview;

public class MapImpl {
    // Return freq with Hashmap implementation
    public static void topFreqWithHashMap(String airline, List<AirlineReview> reviews) {
        Long startTime = Instant.now().toEpochMilli();
        HashMap<String, Integer> freqGood = new HashMap<>();
        HashMap<String, Integer> freqBad = new HashMap<>();
        for (AirlineReview ar : reviews) {
            String[] tokenizedReview = ar.getTokenizedReview();
            // For recommended
            if (Integer.parseInt(ar.getRecommended()) == 1) {
                for (String tokenizedWord : tokenizedReview) {
                    if (!freqGood.containsKey(tokenizedWord)) {
                        freqGood.put(tokenizedWord, 0);
                    }
                    freqGood.put(tokenizedWord, freqGood.get(tokenizedWord) + 1);
                }
            } else { // For not recommended
                for (String tokenizedWord : tokenizedReview) {
                    if (!freqBad.containsKey(tokenizedWord)) {
                        freqBad.put(tokenizedWord, 0);
                    }
                    freqBad.put(tokenizedWord, freqBad.get(tokenizedWord) + 1);
                }
            }
        }

        // Now get top 10 words for good and bad review
        // Min-heap to keep top 10 frequencies
        PriorityQueue<Map.Entry<String, Integer>> minHeap = new PriorityQueue<>(
                Comparator.comparingInt(Map.Entry::getValue));

        for (Map.Entry<String, Integer> entry : freqGood.entrySet()) {
            minHeap.offer(entry);
            if (minHeap.size() > 10) {
                minHeap.poll(); // Remove smallest frequency
            }
        }

        // Extract top 10 words for good in descending order
        List<Map.Entry<String, Integer>> top10Good = new ArrayList<>(minHeap);
        top10Good.sort(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()));

        // Reinitialize for bad reviews
        minHeap = new PriorityQueue<>(
                Comparator.comparingInt(Map.Entry::getValue));

        for (Map.Entry<String, Integer> entry : freqBad.entrySet()) {
            minHeap.offer(entry);
            if (minHeap.size() > 10) {
                minHeap.poll(); // Remove smallest frequency
            }
        }

        // Extract top 10 words for bad in descending order
        List<Map.Entry<String, Integer>> top10Bad = new ArrayList<>(minHeap);
        top10Bad.sort(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()));

        Long timeTakenInMilli = Instant.now().toEpochMilli() - startTime;
        System.out.println("Freq Hash Map time taken: " + timeTakenInMilli);
        System.out.println("Freq Hash Map key-value pairs number: " + freqGood.size());
        System.out.println("Top 10 words in Good Reviews for " + airline + ":");
        for (Map.Entry<String, Integer> entry : top10Good) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        System.out.println("Top 10 words in Bad Reviews for " + airline + ":");
        for (Map.Entry<String, Integer> entry : top10Bad) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }

        return;
    }
}
