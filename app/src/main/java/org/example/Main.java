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
    public static Set<String> uselessWord = Set.of("i", "you", "we", "my", "were", "have", "had"
        ,"us","our"
    );
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
        InputStream input = Main.class.getClassLoader().getResourceAsStream("airline.csv");
        try (CSVReader reader = new CSVReader(new InputStreamReader(input))) {
            List<String[]> allRows = reader.readAll();
            allRows.removeFirst(); // Remove first header row
            for (String[] row : allRows) {
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