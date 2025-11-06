package org.example;

import java.util.*;

/**
 * WordFrequencyCounter uses a PriorityQueue (Min-Heap) to efficiently find the top K most frequent words.
 * This is an optimal approach for the top-K problem with O(N log K) time complexity.
 */
public class WordFrequencyCounter {

    /**
     * Finds the top K most frequent words using a PriorityQueue (Min-Heap).
     * 
     * @param words List of words to analyze
     * @param k Number of top frequent words to return
     * @return List of Map entries containing the top K words and their frequencies, sorted in descending order
     */
    public static List<Map.Entry<String, Integer>> getTopKWords(List<String> words, int k) {
        if (words == null || words.isEmpty() || k <= 0) {
            return new ArrayList<>();
        }

        // Step 1: Count the frequency of each word using a HashMap
        Map<String, Integer> wordFrequency = new HashMap<>();
        for (String word : words) {
            if (word != null && !word.isEmpty()) {
                wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
            }
        }

        // Step 2: Use a PriorityQueue (min-heap) to maintain the top K frequent words
        // The heap keeps the smallest frequency at the top, so we can easily remove it when size exceeds K
        PriorityQueue<Map.Entry<String, Integer>> minHeap = new PriorityQueue<>(
            Comparator.comparingInt(Map.Entry::getValue)
        );

        // Step 3: Process each word-frequency pair
        for (Map.Entry<String, Integer> entry : wordFrequency.entrySet()) {
            minHeap.offer(entry);
            // If heap size exceeds K, remove the word with smallest frequency
            if (minHeap.size() > k) {
                minHeap.poll();
            }
        }

        // Step 4: Extract the top K words from the heap and sort them in descending order of frequency
        List<Map.Entry<String, Integer>> topKWords = new ArrayList<>(minHeap);
        topKWords.sort((a, b) -> b.getValue() - a.getValue());

        return topKWords;
    }

    /**
     * Filters and returns only good reviews (recommended = "1")
     */
    public static List<AirlineReview> getGoodReviews(List<AirlineReview> reviews) {
        List<AirlineReview> goodReviews = new ArrayList<>();
        for (AirlineReview review : reviews) {
            if ("1".equals(review.getRecommended())) {
                goodReviews.add(review);
            }
        }
        return goodReviews;
    }

    /**
     * Filters and returns only bad reviews (recommended = "0")
     */
    public static List<AirlineReview> getBadReviews(List<AirlineReview> reviews) {
        List<AirlineReview> badReviews = new ArrayList<>();
        for (AirlineReview review : reviews) {
            if ("0".equals(review.getRecommended())) {
                badReviews.add(review);
            }
        }
        return badReviews;
    }

    /**
     * Extracts all words from a list of reviews
     */
    public static List<String> extractWordsFromReviews(List<AirlineReview> reviews) {
        List<String> allWords = new ArrayList<>();
        for (AirlineReview review : reviews) {
            if (review.getTokenizedReview() != null) {
                allWords.addAll(Arrays.asList(review.getTokenizedReview()));
            }
        }
        return allWords;
    }

    /**
     * Performs nullity check: removes words that appear in both good and bad top K lists
     * This filters out general words that don't correlate with review quality
     */
    public static List<Map.Entry<String, Integer>> removeCommonWords(
            List<Map.Entry<String, Integer>> topWords,
            Set<String> wordsToRemove) {
        
        List<Map.Entry<String, Integer>> filtered = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : topWords) {
            if (!wordsToRemove.contains(entry.getKey())) {
                filtered.add(entry);
            }
        }
        return filtered;
    }

    /**
     * Gets the set of words from a list of word-frequency entries
     */
    public static Set<String> getWordSet(List<Map.Entry<String, Integer>> wordFrequencies) {
        Set<String> words = new HashSet<>();
        for (Map.Entry<String, Integer> entry : wordFrequencies) {
            words.add(entry.getKey());
        }
        return words;
    }

    /**
     * Prints the word frequency list in a formatted way
     */
    public static void printWordFrequencies(String title, List<Map.Entry<String, Integer>> wordFrequencies) {
        System.out.println("\n" + title);
        System.out.println("=".repeat(50));
        int rank = 1;
        for (Map.Entry<String, Integer> entry : wordFrequencies) {
            System.out.printf("%2d. %-20s : %d%n", rank++, entry.getKey(), entry.getValue());
        }
    }

    /**
     * Analyzes a specific airline's reviews and returns good/bad word indicators
     */
    public static class AirlineAnalysis {
        public String airlineName;
        public int totalReviews;
        public int goodReviews;
        public int badReviews;
        public List<Map.Entry<String, Integer>> topGoodWords;
        public List<Map.Entry<String, Integer>> topBadWords;
        public List<Map.Entry<String, Integer>> filteredGoodWords;
        public List<Map.Entry<String, Integer>> filteredBadWords;
        
        public AirlineAnalysis(String airlineName) {
            this.airlineName = airlineName;
        }
    }

    /**
     * Analyzes reviews for a specific airline
     */
    public static AirlineAnalysis analyzeAirline(String airlineName, List<AirlineReview> allReviews, int k) {
        AirlineAnalysis analysis = new AirlineAnalysis(airlineName);
        
        // Filter reviews for this airline
        List<AirlineReview> airlineReviews = new ArrayList<>();
        for (AirlineReview review : allReviews) {
            if (airlineName.equals(review.getAirline())) {
                airlineReviews.add(review);
            }
        }
        
        analysis.totalReviews = airlineReviews.size();
        
        // Separate good and bad reviews
        List<AirlineReview> goodReviews = getGoodReviews(airlineReviews);
        List<AirlineReview> badReviews = getBadReviews(airlineReviews);
        
        analysis.goodReviews = goodReviews.size();
        analysis.badReviews = badReviews.size();
        
        // Extract words
        List<String> goodWords = extractWordsFromReviews(goodReviews);
        List<String> badWords = extractWordsFromReviews(badReviews);
        
        // Get top K words - useless words already filtered out in Main.java
        analysis.topGoodWords = getTopKWords(goodWords, k);
        analysis.topBadWords = getTopKWords(badWords, k);
        
        // Perform nullity check: Remove words that appear in BOTH top 10 lists
        // These are airline-specific words (e.g., "toronto", "ba", "roug") that don't indicate quality
        Set<String> goodWordSet = getWordSet(analysis.topGoodWords);
        Set<String> badWordSet = getWordSet(analysis.topBadWords);
        Set<String> commonWords = new HashSet<>(goodWordSet);
        commonWords.retainAll(badWordSet);
        
        analysis.filteredGoodWords = removeCommonWords(analysis.topGoodWords, commonWords);
        analysis.filteredBadWords = removeCommonWords(analysis.topBadWords, commonWords);
        
        return analysis;
    }

    /**
     * Prints analysis results for a specific airline
     */
    public static void printAirlineAnalysis(AirlineAnalysis analysis) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("ANALYSIS FOR: " + analysis.airlineName.toUpperCase());
        System.out.println("=".repeat(70));
        System.out.println("Total Reviews: " + analysis.totalReviews);
        System.out.println("Good Reviews: " + analysis.goodReviews);
        System.out.println("Bad Reviews: " + analysis.badReviews);
        
        if (analysis.totalReviews > 0) {
            double goodPercentage = (analysis.goodReviews * 100.0) / analysis.totalReviews;
            System.out.printf("Recommendation Rate: %.1f%%\n", goodPercentage);
        }
        
        printWordFrequencies("TOP POSITIVE WORDS", analysis.filteredGoodWords);
        printWordFrequencies("TOP NEGATIVE WORDS", analysis.filteredBadWords);
    }

    /**
     * Compares multiple airlines and prints a comparison table
     */
    public static void compareAirlines(List<AirlineAnalysis> analyses) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("AIRLINE COMPARISON");
        System.out.println("=".repeat(70));
        System.out.printf("%-30s %10s %10s %10s %10s%n", 
            "Airline", "Total", "Good", "Bad", "Rating %");
        System.out.println("-".repeat(70));
        
        for (AirlineAnalysis analysis : analyses) {
            double rating = analysis.totalReviews > 0 
                ? (analysis.goodReviews * 100.0) / analysis.totalReviews 
                : 0;
            System.out.printf("%-30s %10d %10d %10d %9.1f%%%n",
                analysis.airlineName,
                analysis.totalReviews,
                analysis.goodReviews,
                analysis.badReviews,
                rating);
        }
    }

    /**
     * Finds and displays the best and worst airlines based on recommendation rate
     * Only considers airlines with at least 50 reviews for statistical significance
     */
    public static void findBestAndWorst(HashMap<String, List<AirlineReview>> airlineReviews, 
                                        List<AirlineReview> allReviews) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("BEST AND WORST AIRLINES");
        System.out.println("=".repeat(70));
        
        // Calculate recommendation rates for all airlines with at least 50 reviews
        List<AirlineRating> ratings = new ArrayList<>();
        
        for (Map.Entry<String, List<AirlineReview>> entry : airlineReviews.entrySet()) {
            String airline = entry.getKey();
            List<AirlineReview> reviews = entry.getValue();
            
            if (reviews.size() >= 50) { // Only consider airlines with enough reviews
                int total = reviews.size();
                int good = 0;
                for (AirlineReview review : reviews) {
                    if ("1".equals(review.getRecommended())) {
                        good++;
                    }
                }
                double rate = (good * 100.0) / total;
                ratings.add(new AirlineRating(airline, total, good, rate));
            }
        }
        
        // Sort by recommendation rate
        ratings.sort((a, b) -> Double.compare(b.recommendationRate, a.recommendationRate));
        
        // Show top 3 best
        System.out.println("\n🏆 TOP 3 BEST AIRLINES:");
        System.out.println("-".repeat(70));
        for (int i = 0; i < Math.min(3, ratings.size()); i++) {
            AirlineRating rating = ratings.get(i);
            System.out.printf("%d. %-30s %.1f%% (%d/%d reviews)%n", 
                i + 1, rating.airline, rating.recommendationRate, 
                rating.goodReviews, rating.totalReviews);
        }
        
        // Show bottom 3 worst
        System.out.println("\n💔 TOP 3 WORST AIRLINES:");
        System.out.println("-".repeat(70));
        for (int i = ratings.size() - 1; i >= Math.max(0, ratings.size() - 3); i--) {
            AirlineRating rating = ratings.get(i);
            int rank = ratings.size() - i;
            System.out.printf("%d. %-30s %.1f%% (%d/%d reviews)%n", 
                rank, rating.airline, rating.recommendationRate, 
                rating.goodReviews, rating.totalReviews);
        }
        
        System.out.println("=".repeat(70));
    }

    /**
     * Helper class to store airline ratings
     */
    private static class AirlineRating {
        String airline;
        int totalReviews;
        int goodReviews;
        double recommendationRate;
        
        AirlineRating(String airline, int total, int good, double rate) {
            this.airline = airline;
            this.totalReviews = total;
            this.goodReviews = good;
            this.recommendationRate = rate;
        }
    }
}
