package org.example;

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

/*
 * BST Implementation Branch
 * Testing Binary Search Tree approach for word frequency analysis
 * 
 * Index mappings from CSV:
 * Index 0  - Airlines
 * Index 6  - Review content
 * Index 11 - Overall rating
 * Index 19 - Recommended
 */

public class Main {
    /**
     * Properly parse a CSV line handling quoted fields with commas and newlines
     */
    private static String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder field = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                // Check if it's an escaped quote (two consecutive quotes)
                if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++; // Skip the next quote
                } else {
                    // Toggle quote state
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // Field separator found outside quotes
                result.add(field.toString());
                field = new StringBuilder();
            } else {
                // Regular character
                field.append(c);
            }
        }

        // Add the last field
        result.add(field.toString());

        return result.toArray(new String[0]);
    }

    /**
     * Test BST implementation for word frequency counting
     * Creates two separate BSTs: one for good reviews, one for bad reviews
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
            String line;
            line = br.readLine(); // Skip header

            StringBuilder fullLine = new StringBuilder();
            boolean inQuotes = false;

            while ((line = br.readLine()) != null) {
                // Handle multi-line quoted fields
                fullLine.append(line);

                // Count quotes to determine if we're inside a quoted field
                for (char c : line.toCharArray()) {
                    if (c == '"') {
                        inQuotes = !inQuotes;
                    }
                }

                // If we're still inside quotes, continue reading
                if (inQuotes) {
                    fullLine.append(" "); // Replace newline with space
                    continue;
                }

                // Process complete line
                String[] values = parseCSVLine(fullLine.toString());
                String[] selectedValues = new String[4];

                selectedValues[0] = values.length > 0 ? (values[0].isEmpty() ? null : values[0]) : null;
                selectedValues[1] = values.length > 6 ? (values[6].isEmpty() ? null : values[6]) : null;
                selectedValues[2] = values.length > 11 ? (values[11].isEmpty() ? null : values[11]) : null;
                selectedValues[3] = values.length > 19 ? (values[19].isEmpty() ? null : values[19]) : null;

                records.add(selectedValues);
                fullLine = new StringBuilder();
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

                        while (stemmedStream.incrementToken()) {
                            tokens.add(attr.toString());
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

        // Test BST implementation
        String testAirline = "spirit-airlines";
        System.out.println("\n===================================================");
        System.out.println("  BST Analysis for: " + testAirline);
        System.out.println("===================================================");

        bstImplementationTest(airlineReviews, testAirline);

        // Summary
        System.out.println("\n========================================");
        System.out.println("      BST Implementation Summary");
        System.out.println("========================================");
        System.out.println("* Uses Binary Search Tree for counting");
        System.out.println("* Automatically maintains alphabetical order");
        System.out.println("* Counts word occurrences during insertion");
        System.out.println("* More memory efficient (stores unique words only)");
        System.out.println("\nInsight: Top words reveal what passengers love/hate!\n");
    }
}