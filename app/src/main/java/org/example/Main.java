package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.en.PorterStemFilter;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

/*
 * Helping indexes in CSV
 * Index 0 - Airlines
 * Index 6 - Review content
 * Index 11 - Overall rating
 * Index 19 - Recommended
 */

public class Main {
    // Useless words to filter out during tokenization
    public static Set<String> uselessWords = Set.of(
        "i", "you", "we", "my", "were", "have", "had", "us", "our",
        "flight", "seat", "from", "was", "been", "be"
    );
    
    public static void main(String[] args) throws IOException {
        List<String[]> records = new ArrayList<>();

        // Read values from CSV
        InputStream input = Main.class.getClassLoader().getResourceAsStream("airline.csv");
        try (CSVReader reader = new CSVReader(new InputStreamReader(input))) {
            List<String[]> allRows = reader.readAll();
            for (String[] row : allRows) {
                String[] selectedValues = new String[4];

                selectedValues[0] = row.length > 0 ? (row[0].isEmpty() ? null : row[0]) : null; // airline
                selectedValues[1] = row.length > 6 ? (row[6].isEmpty() ? null : row[6]) : null; // review content
                selectedValues[2] = row.length > 11 ? (row[11].isEmpty() ? null : row[11]) : null; // overall rating
                selectedValues[3] = row.length > 19 ? (row[19].isEmpty() ? null : row[19]) : null; // recommended

                for (String value : selectedValues) {
                    if (value == null) {
                        continue;
                    }
                }
                records.add(selectedValues);
            }

        } catch (IOException | CsvException e) {
            System.out.println("Error reading CSV: " + e.getMessage());
            e.printStackTrace();
        }

        // Create an analyzer that provides tokenization, stop word removal, and
        // stemming
        List<AirlineReview> tokenizedReviews = new ArrayList<>();
        try (Analyzer analyzer = new StandardAnalyzer(EnglishAnalyzer.getDefaultStopSet())) {
            // Now tokenize the inputs
            int i = 0;
            for (String[] review : records) {
                if (review[1] != null) { // Check if review content exists
                    List<String> tokens = new ArrayList<>();

                    // Create token stream with stemming
                    try (TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(review[1]));
                            TokenStream stemmedStream = new PorterStemFilter(tokenStream)) {
                        CharTermAttribute attr = stemmedStream.addAttribute(CharTermAttribute.class);
                        stemmedStream.reset();

                        // Collect all stemmed tokens, filtering out useless words
                        while (stemmedStream.incrementToken()) {
                            String token = attr.toString();
                            // Only add tokens that are NOT in the useless words list
                            if (!uselessWords.contains(token)) {
                                tokens.add(token);
                            }
                        }
                        stemmedStream.end();
                    }

                    if (!tokens.isEmpty()) {
                        tokenizedReviews.add(new AirlineReview(records.get(i)[0],
                                tokens.toArray(new String[tokens.size()]), records.get(i)[2], records.get(i)[3]));
                    }

                    // System.out.println("Review for airline: " + review[0]);
                    // System.out.println("Processed review tokens: " + tokens);
                    // System.out.println("Rating: " + review[2] + ", Recommended: " + review[3]);
                } else {
                    // System.out.println("Skipping review with null content");
                }
                i++;
            }
        } catch (IOException e) {
            System.out.println("Error processing reviews: " + e.getMessage());
            e.printStackTrace();
        }

        // Now we have a list of AirlineReviews with the important information +
        // Tokenized stemmed and stopped words removed Review
        // We now set it to a HashMap where key is the airline, and the value is a list
        // of the tokenized
        // AirlineReview class. We do this as our problem is frequency of words for a
        // specific airline,
        // hence it okay to assume we have the reviews sorted for a specific airlines
        HashMap<String, List<AirlineReview>> airlineReviews = new HashMap<>();
        for (AirlineReview airlineReview : tokenizedReviews) {
            if (!airlineReviews.containsKey(airlineReview.getAirline())) {
                airlineReviews.put(airlineReview.getAirline(), new ArrayList<>());
            }
            airlineReviews.get(airlineReview.getAirline()).add(airlineReview);
        }

        // Now we have a hashmap of all tokenized airlinereviews class belonging to a
        // specific airline
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("AIRLINE REVIEW ANALYSIS - PRIORITY QUEUE IMPLEMENTATION");
        System.out.println("Data Structure: PriorityQueue (Min-Heap) | Complexity: O(N log K)");
        System.out.println("=".repeat(70));
        
        System.out.println("\nDataset Statistics:");
        System.out.println("-".repeat(50));
        System.out.println("Total Reviews Analyzed: " + tokenizedReviews.size());
        System.out.println("Total Unique Airlines: " + airlineReviews.size());
        
        // Count overall good/bad for context
        int totalGood = 0, totalBad = 0;
        for (AirlineReview review : tokenizedReviews) {
            if ("1".equals(review.getRecommended())) totalGood++;
            else if ("0".equals(review.getRecommended())) totalBad++;
        }
        System.out.println("Overall Good Reviews: " + totalGood);
        System.out.println("Overall Bad Reviews: " + totalBad);
        System.out.printf("Overall Recommendation Rate: %.1f%%\n", (totalGood * 100.0) / tokenizedReviews.size());

        // ========== AIRLINE-SPECIFIC ANALYSIS (MAIN FOCUS) ==========
        analyzeSpecificAirlines(airlineReviews, tokenizedReviews);
    }

    /**
     * Separate method for airline-specific analysis
     * This keeps the overall analysis and specific airline analysis separate
     */
    private static void analyzeSpecificAirlines(HashMap<String, List<AirlineReview>> airlineReviews, 
                                                 List<AirlineReview> tokenizedReviews) {
        System.out.println("\n\n" + "=".repeat(70));
        System.out.println("AIRLINE-SPECIFIC ANALYSIS MODULE");
        System.out.println("Using Priority Queue for Per-Airline Word Frequency");
        System.out.println("=".repeat(70));
        
        // Find best and worst airlines
        WordFrequencyCounter.findBestAndWorst(airlineReviews, tokenizedReviews);
        
        // Detailed analysis of top airlines by review count
        System.out.println("\n\n" + "=".repeat(70));
        System.out.println("DETAILED ANALYSIS - TOP AIRLINES BY REVIEW COUNT");
        System.out.println("=".repeat(70));
        
        // Get list of unique airlines and sort by number of reviews
        List<Map.Entry<String, List<AirlineReview>>> sortedAirlines = new ArrayList<>(airlineReviews.entrySet());
        sortedAirlines.sort((a, b) -> b.getValue().size() - a.getValue().size());
        
        // Analyze top 5 airlines with most reviews
        System.out.println("\nAnalyzing top 5 airlines with most reviews...\n");
        List<WordFrequencyCounter.AirlineAnalysis> analyses = new ArrayList<>();
        
        int count = 0;
        for (Map.Entry<String, List<AirlineReview>> entry : sortedAirlines) {
            if (count >= 5) break;
            
            String airline = entry.getKey();
            WordFrequencyCounter.AirlineAnalysis analysis = 
                WordFrequencyCounter.analyzeAirline(airline, tokenizedReviews, 10);
            
            analyses.add(analysis);
            WordFrequencyCounter.printAirlineAnalysis(analysis);
            count++;
        }
        
        // Print comparison table
        WordFrequencyCounter.compareAirlines(analyses);
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("Airline-specific analysis complete!");
        System.out.println("=".repeat(70) + "\n");
    }
}