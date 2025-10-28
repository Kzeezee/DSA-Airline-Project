import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import javax.xml.crypto.dsig.keyinfo.KeyValue;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.slf4j.event.KeyValuePair;
import org.apache.lucene.analysis.en.PorterStemFilter;

/*
 * Helping indexes in CSV
 * Index 0 - Airlines
 * Index 6 - Review content
 * Index 11 - Overall rating
 * Index 19 - Recommended
 */

public class Main {
    public static void main(String[] args) throws IOException {
        List<String[]> records = new ArrayList<>();

        // Read values from CSV
        try (BufferedReader br = new BufferedReader(new FileReader("airline.csv"))) {
            String line;
            line = br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                
                String[] values = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1); // Handle CSV with quotes
                String[] selectedValues = new String[4]; // Fixed size array for our 4 required fields
                
                // Fill the array with selected indices, handling missing or out of bounds values
                selectedValues[0] = values.length > 0 ? (values[0].isEmpty() ? null : values[0]) : null;  // airline
                selectedValues[1] = values.length > 6 ? (values[6].isEmpty() ? null : values[6]) : null;  // review content
                selectedValues[2] = values.length > 11 ? (values[11].isEmpty() ? null : values[11]) : null; // overall rating
                selectedValues[3] = values.length > 19 ? (values[19].isEmpty() ? null : values[19]) : null; // recommended
                
                records.add(selectedValues);
            }
        } catch (Exception e) {
            System.out.println("Error reading CSV: " + e.getMessage());
            e.printStackTrace();
        }

        // Create an analyzer that provides tokenization, stop word removal, and stemming
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
                        
                        // Collect all stemmed tokens
                        while (stemmedStream.incrementToken()) {
                            tokens.add(attr.toString());
                        }
                        stemmedStream.end();
                    }

                    if (!tokens.isEmpty()) {
                        tokenizedReviews.add(new AirlineReview(records.get(i)[0], tokens.toArray(new String[tokens.size()]), records.get(i)[2], records.get(i)[3]));
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
        
        // Now we have a list of AirlineReviews with the important information + Tokenized stemmed and stopped words removed Review
        // We now set it to a HashMap where key is the airline, and the value is a list of the tokenized
        // AirlineReview class. We do this as our problem is frequency of words for a specific airline,
        // hence it okay to assume we have the reviews sorted for a specific airlines
        HashMap<String, List<AirlineReview>> airlineReviews = new HashMap<>();
        for (AirlineReview airlineReview : tokenizedReviews) {
            if (!airlineReviews.containsKey(airlineReview.getAirline())) {
                airlineReviews.put(airlineReview.getAirline(), new ArrayList<>());
            }
            airlineReviews.get(airlineReview.getAirline()).add(airlineReview);
        }

        // Now we have a hashmap of all tokenized airlinereviews class belonging to a specific airline
        System.out.println(airlineReviews);
    }
}