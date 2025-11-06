package org.example.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.en.PorterStemFilter;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import org.example.AirlineBSTImpl;
import org.example.WordFrequencyCounter;
import org.example.ds.arraylist.AirlineArrayListImpl;
import org.example.model.AirlineReview;
import org.example.model.util.Pair;
import org.example.model.util.TextAnalysisUtils;
import org.example.model.util.WordCount;

/*
 * Combined Implementation - Shows both BST and Priority Queue approaches
 * Index mappings from CSV:
 * Index 0  - Airlines
 * Index 6  - Review content
 * Index 11 - Overall rating
 * Index 19 - Recommended
 */

public class Main {
    public static Set<String> uselessWord = Set.of("i", "you", "we", "my", "were", "have", "had", "us", "our");

    /**
     * Parse CSV line handling quoted fields with commas
     */
    private static String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder field = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(field.toString());
                field = new StringBuilder();
            } else {
                field.append(c);
            }
        }
        result.add(field.toString());
        return result.toArray(new String[0]);
    }

    /**
     * Test BST implementation for word frequency counting
     */
    public static void bstImplementationTest(HashMap<String, List<AirlineReview>> airlineReviews, String airline) {
        System.out.println("\n========================================");
        System.out.println("      BST Implementation Test");
        System.out.println("========================================");

        AirlineBSTImpl airlineBSTImpl = new AirlineBSTImpl(airlineReviews, airline);
        Pair<List<WordCount>, List<WordCount>> top10MostCommonWords = airlineBSTImpl.getTop10MostCommonWords();

        // Count positive vs negative reviews
        List<AirlineReview> reviews = airlineReviews.get(airline);
        int positiveCount = 0;
        int negativeCount = 0;

        if (reviews != null) {
            for (AirlineReview review : reviews) {
                if (TextAnalysisUtils.isPositiveRecommendation(review.getRecommended())) {
                    positiveCount++;
                } else {
                    negativeCount++;
                }
            }
        }

        int totalReviews = positiveCount + negativeCount;
        double positivePercent = totalReviews > 0 ? (positiveCount * 100.0 / totalReviews) : 0;
        double negativePercent = totalReviews > 0 ? (negativeCount * 100.0 / totalReviews) : 0;

        System.out.println("\n[GOOD] Top 10 most common words in POSITIVE reviews:");
        System.out.println("-----------------------------------------");
        int goodRank = 1;
        for (WordCount wc : top10MostCommonWords.getLeft()) {
            System.out.printf("%2d. %s\n", goodRank++, wc);
        }

        System.out.println("\n[BAD] Top 10 most common words in NEGATIVE reviews:");
        System.out.println("-----------------------------------------");
        int badRank = 1;
        for (WordCount wc : top10MostCommonWords.getRight()) {
            System.out.printf("%2d. %s\n", badRank++, wc);
        }

        // Overall sentiment analysis
        System.out.println("\n========================================");
        System.out.println("      Overall Sentiment Analysis");
        System.out.println("========================================");
        System.out.printf("Total Reviews: %d\n", totalReviews);
        System.out.printf("Positive Reviews: %d (%.1f%%)\n", positiveCount, positivePercent);
        System.out.printf("Negative Reviews: %d (%.1f%%)\n", negativeCount, negativePercent);

        System.out.println("\n----------------------------------------");
        if (positiveCount > negativeCount) {
            System.out.println("VERDICT: MOSTLY POSITIVE");
            System.out.printf("Customers generally recommend this airline!\n");
        } else if (negativeCount > positiveCount) {
            System.out.println("VERDICT: MOSTLY NEGATIVE");
            System.out.printf("Customers generally do NOT recommend this airline.\n");
        } else {
            System.out.println("VERDICT: MIXED");
            System.out.printf("Reviews are evenly split.\n");
        }
        System.out.println("----------------------------------------");
    }

    /**
     * Test ArrayList implementation for word frequency counting
     */
    public static void arrayListImplementationTest(HashMap<String, List<AirlineReview>> airlineReviews,
            String airline) {
        System.out.println("\n========================================");
        System.out.println("      ArrayList Implementation Test");
        System.out.println("========================================");

        AirlineArrayListImpl airlineArrayListImpl = new AirlineArrayListImpl(airlineReviews, airline);
        Pair<List<WordCount>, List<WordCount>> top10MostCommonWords = airlineArrayListImpl.getTop10MostCommonWords();

        // Count positive vs negative reviews
        List<AirlineReview> reviews = airlineReviews.get(airline);
        int positiveCount = 0;
        int negativeCount = 0;

        if (reviews != null) {
            for (AirlineReview review : reviews) {
                if (TextAnalysisUtils.isPositiveRecommendation(review.getRecommended())) {
                    positiveCount++;
                } else {
                    negativeCount++;
                }
            }
        }

        int totalReviews = positiveCount + negativeCount;
        double positivePercent = totalReviews > 0 ? (positiveCount * 100.0 / totalReviews) : 0;
        double negativePercent = totalReviews > 0 ? (negativeCount * 100.0 / totalReviews) : 0;

        System.out.println("\n[GOOD] Top 10 most common words in POSITIVE reviews:");
        System.out.println("-----------------------------------------");
        int goodRank = 1;
        for (WordCount wc : top10MostCommonWords.getLeft()) {
            System.out.printf("%2d. %s\n", goodRank++, wc);
        }

        System.out.println("\n[BAD] Top 10 most common words in NEGATIVE reviews:");
        System.out.println("-----------------------------------------");
        int badRank = 1;
        for (WordCount wc : top10MostCommonWords.getRight()) {
            System.out.printf("%2d. %s\n", badRank++, wc);
        }

        // Overall sentiment analysis
        System.out.println("\n========================================");
        System.out.println("      Overall Sentiment Analysis");
        System.out.println("========================================");
        System.out.printf("Total Reviews: %d\n", totalReviews);
        System.out.printf("Positive Reviews: %d (%.1f%%)\n", positiveCount, positivePercent);
        System.out.printf("Negative Reviews: %d (%.1f%%)\n", negativeCount, negativePercent);

        System.out.println("\n----------------------------------------");
        if (positiveCount > negativeCount) {
            System.out.println("VERDICT: MOSTLY POSITIVE");
            System.out.printf("Customers generally recommend this airline!\n");
        } else if (negativeCount > positiveCount) {
            System.out.println("VERDICT: MOSTLY NEGATIVE");
            System.out.printf("Customers generally do NOT recommend this airline.\n");
        } else {
            System.out.println("VERDICT: MIXED");
            System.out.printf("Reviews are evenly split.\n");
        }
        System.out.println("----------------------------------------");
    }

    /**
     * Test Priority Queue implementation for word frequency counting
     */
    public static void priorityQueueImplementationTest(HashMap<String, List<AirlineReview>> airlineReviews,
            List<AirlineReview> tokenizedReviews,
            String airline) {
        System.out.println("\n========================================");
        System.out.println("      Priority Queue Implementation Test");
        System.out.println("========================================");

        // Use WordFrequencyCounter to analyze this specific airline
        WordFrequencyCounter.AirlineAnalysis analysis = WordFrequencyCounter.analyzeAirline(airline, tokenizedReviews,
                10);

        int totalReviews = analysis.totalReviews;
        int positiveCount = analysis.goodReviews;
        int negativeCount = analysis.badReviews;
        double positivePercent = totalReviews > 0 ? (positiveCount * 100.0 / totalReviews) : 0;
        double negativePercent = totalReviews > 0 ? (negativeCount * 100.0 / totalReviews) : 0;

        System.out.println("\n[GOOD] Top 10 most common words in POSITIVE reviews:");
        System.out.println("-----------------------------------------");
        int goodRank = 1;
        for (Map.Entry<String, Integer> entry : analysis.topGoodWords) {
            double percent = positiveCount > 0 ? (entry.getValue() * 100.0 / positiveCount) : 0;
            System.out.printf("%2d. Word: %-15s Count: %5d  (%.2f%%)\n",
                    goodRank++, entry.getKey(), entry.getValue(), percent);
        }

        System.out.println("\n[BAD] Top 10 most common words in NEGATIVE reviews:");
        System.out.println("-----------------------------------------");
        int badRank = 1;
        for (Map.Entry<String, Integer> entry : analysis.topBadWords) {
            double percent = negativeCount > 0 ? (entry.getValue() * 100.0 / negativeCount) : 0;
            System.out.printf("%2d. Word: %-15s Count: %5d  (%.2f%%)\n",
                    badRank++, entry.getKey(), entry.getValue(), percent);
        }

        // Overall sentiment analysis
        System.out.println("\n========================================");
        System.out.println("      Overall Sentiment Analysis");
        System.out.println("========================================");
        System.out.printf("Total Reviews: %d\n", totalReviews);
        System.out.printf("Positive Reviews: %d (%.1f%%)\n", positiveCount, positivePercent);
        System.out.printf("Negative Reviews: %d (%.1f%%)\n", negativeCount, negativePercent);

        System.out.println("\n----------------------------------------");
        if (positiveCount > negativeCount) {
            System.out.println("VERDICT: MOSTLY POSITIVE");
            System.out.printf("Customers generally recommend this airline!\n");
        } else if (negativeCount > positiveCount) {
            System.out.println("VERDICT: MOSTLY NEGATIVE");
            System.out.printf("Customers generally do NOT recommend this airline.\n");
        } else {
            System.out.println("VERDICT: MIXED");
            System.out.printf("Reviews are evenly split.\n");
        }
        System.out.println("----------------------------------------");
    }

    public static void main(String[] args) throws IOException {
        List<String[]> records = new ArrayList<>();

        // Read CSV from resources folder
        InputStream csvStream = Main.class.getClassLoader().getResourceAsStream("airline.csv");

        if (csvStream == null) {
            System.out.println("ERROR: Could not find airline.csv in resources folder!");
            return;
        }

        System.out.println("✓ Found airline.csv in resources");

        // Read values from CSV
        try (BufferedReader br = new BufferedReader(new InputStreamReader(csvStream))) {
            String line = br.readLine(); // Skip header
            StringBuilder currentLine = new StringBuilder();
            boolean inQuotes = false;

            while ((line = br.readLine()) != null) {
                currentLine.append(line);

                // Check if we're inside a quoted field
                for (char c : line.toCharArray()) {
                    if (c == '"')
                        inQuotes = !inQuotes;
                }

                // If still in quotes, this is a multi-line field
                if (inQuotes) {
                    currentLine.append("\n");
                    continue;
                }

                // Parse the complete line
                String[] row = parseCSVLine(currentLine.toString());
                String[] selectedValues = new String[4];

                selectedValues[0] = row.length > 0 ? (row[0].isEmpty() ? null : row[0]) : null;
                selectedValues[1] = row.length > 6 ? (row[6].isEmpty() ? null : row[6]) : null;
                selectedValues[2] = row.length > 11 ? (row[11].isEmpty() ? null : row[11]) : null;
                selectedValues[3] = row.length > 19 ? (row[19].isEmpty() ? null : row[19]) : null;

                records.add(selectedValues);
                currentLine = new StringBuilder();
            }
        } catch (Exception e) {
            System.out.println("Error reading CSV: " + e.getMessage());
            e.printStackTrace();
        }

        // Tokenize reviews
        List<AirlineReview> tokenizedReviews = new ArrayList<>();
        try (Analyzer analyzer = new StandardAnalyzer(EnglishAnalyzer.getDefaultStopSet())) {
            int i = 0;
            for (String[] review : records) {
                if (review[1] != null) {
                    List<String> tokens = new ArrayList<>();

                    try (TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(review[1]));
                            TokenStream stemmedStream = new PorterStemFilter(tokenStream)) {
                        CharTermAttribute attr = stemmedStream.addAttribute(CharTermAttribute.class);
                        stemmedStream.reset();

                        // Collect all stemmed tokens, filtering out useless words
                        while (stemmedStream.incrementToken()) {
                            if (!uselessWord.contains(attr.toString())) {
                                tokens.add(attr.toString());
                            }
                        }
                        stemmedStream.end();
                    }

                    if (!tokens.isEmpty()) {
                        tokenizedReviews.add(new AirlineReview(
                                records.get(i)[0],
                                tokens.toArray(new String[0]),
                                records.get(i)[2],
                                records.get(i)[3]));
                    }
                }
                i++;
            }
        } catch (IOException e) {
            System.out.println("Error processing reviews: " + e.getMessage());
            e.printStackTrace();
        }

        // Group reviews by airline
        HashMap<String, List<AirlineReview>> airlineReviews = new HashMap<>();
        for (AirlineReview airlineReview : tokenizedReviews) {
            if (!airlineReviews.containsKey(airlineReview.getAirline())) {
                airlineReviews.put(airlineReview.getAirline(), new ArrayList<>());
            }
            airlineReviews.get(airlineReview.getAirline()).add(airlineReview);
        }

        // ========== BST IMPLEMENTATION ANALYSIS ==========
        String testAirline = "spirit-airlines";
        System.out.println("\n" + "=".repeat(70));
        System.out.println("  BST IMPLEMENTATION - AIRLINE ANALYSIS");
        System.out.println("  Data Structure: Binary Search Tree | Complexity: O(N log N)");
        System.out.println("=".repeat(70));
        System.out.println("  Analyzing: " + testAirline);
        System.out.println("=".repeat(70));

        bstImplementationTest(airlineReviews, testAirline);

        System.out.println("\n========================================");
        System.out.println("      BST Implementation Summary");
        System.out.println("========================================");
        System.out.println("* Uses Binary Search Tree for counting");
        System.out.println("* Automatically maintains alphabetical order");
        System.out.println("* Counts word occurrences during insertion");
        System.out.println("* More memory efficient (stores unique words only)");
        System.out.println("\nInsight: Top words reveal what passengers love/hate!");

        // ========== ARRAYLIST IMPLEMENTATION ANALYSIS ==========
        System.out.println("\n\n" + "=".repeat(70));
        System.out.println("  ARRAYLIST IMPLEMENTATION - AIRLINE ANALYSIS");
        System.out.println("  Data Structure: ArrayList (Dynamic Array) | Complexity: O(N log N)");
        System.out.println("=".repeat(70));
        System.out.println("  Analyzing: " + testAirline);
        System.out.println("=".repeat(70));

        arrayListImplementationTest(airlineReviews, testAirline);

        System.out.println("\n========================================");
        System.out.println("      ArrayList Implementation Summary");
        System.out.println("========================================");
        System.out.println("* Uses ArrayList for storing and sorting words");
        System.out.println("* Efficient for sequential access and iteration");
        System.out.println("* Sorting-based approach for word frequency");
        System.out.println("* Simple and straightforward implementation");
        System.out.println("\nInsight: ArrayList provides flexible word frequency analysis!");

        // ========== PRIORITY QUEUE IMPLEMENTATION ANALYSIS ==========
        System.out.println("\n\n" + "=".repeat(70));
        System.out.println("  PRIORITY QUEUE IMPLEMENTATION - AIRLINE ANALYSIS");
        System.out.println("  Data Structure: PriorityQueue (Min-Heap) | Complexity: O(N log K)");
        System.out.println("=".repeat(70));
        System.out.println("  Analyzing: " + testAirline);
        System.out.println("=".repeat(70));

        priorityQueueImplementationTest(airlineReviews, tokenizedReviews, testAirline);

        System.out.println("\n========================================");
        System.out.println("      Priority Queue Implementation Summary");
        System.out.println("========================================");
        System.out.println("* Uses PriorityQueue (Min-Heap) for top-K word selection");
        System.out.println("* Most efficient for finding top K elements");
        System.out.println("* O(N log K) complexity - optimal for top-K problem");
        System.out.println("* Automatically maintains top words while processing");
        System.out.println("\nInsight: Priority Queue is optimized for top-K word frequency!");

        // ========== COMPREHENSIVE ANALYSIS (All Airlines) ==========
        // System.out.println("\n\n" + "=".repeat(70));
        // System.out.println(" COMPREHENSIVE ANALYSIS - ALL AIRLINES");
        // System.out.println(" Using Priority Queue for Multi-Airline Comparison");
        // System.out.println("=".repeat(70));

        // System.out.println("\nDataset Statistics:");
        // System.out.println("-".repeat(50));
        // System.out.println("Total Reviews Analyzed: " + tokenizedReviews.size());
        // System.out.println("Total Unique Airlines: " + airlineReviews.size());

        // // Count overall good/bad for context
        // int totalGood = 0, totalBad = 0;
        // for (AirlineReview review : tokenizedReviews) {
        // if ("1".equals(review.getRecommended()))
        // totalGood++;
        // else if ("0".equals(review.getRecommended()))
        // totalBad++;
        // }
        // System.out.println("Overall Good Reviews: " + totalGood);
        // System.out.println("Overall Bad Reviews: " + totalBad);
        // System.out.printf("Overall Recommendation Rate: %.1f%%\n", (totalGood *
        // 100.0) / tokenizedReviews.size());

        // // Airline-specific analysis using Priority Queue
        // analyzeSpecificAirlines(airlineReviews, tokenizedReviews);
    }

    /**
     * Separate method for airline-specific analysis using Priority Queue
     * This keeps the overall analysis and specific airline analysis separate
     */
    // private static void analyzeSpecificAirlines(HashMap<String,
    // List<AirlineReview>> airlineReviews,
    // List<AirlineReview> tokenizedReviews) {
    // System.out.println("\n\n" + "=".repeat(70));
    // System.out.println("AIRLINE-SPECIFIC ANALYSIS MODULE");
    // System.out.println("Using Priority Queue for Per-Airline Word Frequency");
    // System.out.println("=".repeat(70));

    // // Find best and worst airlines
    // WordFrequencyCounter.findBestAndWorst(airlineReviews, tokenizedReviews);

    // // Detailed analysis of top airlines by review count
    // System.out.println("\n\n" + "=".repeat(70));
    // System.out.println("DETAILED ANALYSIS - TOP AIRLINES BY REVIEW COUNT");
    // System.out.println("=".repeat(70));

    // // Get list of unique airlines and sort by number of reviews
    // List<Map.Entry<String, List<AirlineReview>>> sortedAirlines = new
    // ArrayList<>(airlineReviews.entrySet());
    // sortedAirlines.sort((a, b) -> b.getValue().size() - a.getValue().size());

    // // Analyze top 5 airlines with most reviews
    // System.out.println("\nAnalyzing top 5 airlines with most reviews...\n");
    // List<WordFrequencyCounter.AirlineAnalysis> analyses = new ArrayList<>();

    // int count = 0;
    // for (Map.Entry<String, List<AirlineReview>> entry : sortedAirlines) {
    // if (count >= 5)
    // break;

    // String airline = entry.getKey();
    // WordFrequencyCounter.AirlineAnalysis analysis =
    // WordFrequencyCounter.analyzeAirline(airline,
    // tokenizedReviews, 10);

    // analyses.add(analysis);
    // WordFrequencyCounter.printAirlineAnalysis(analysis);
    // count++;
    // }

    // // Print comparison table
    // WordFrequencyCounter.compareAirlines(analyses);

    // System.out.println("\n" + "=".repeat(70));
    // System.out.println("Airline-specific analysis complete!");
    // System.out.println("=".repeat(70) + "\n");
    // }

}