package org.example.ds;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

import org.example.model.AirlineReview;
import org.example.model.util.Pair;
import org.example.model.util.TextAnalysisUtils;
import org.example.model.util.WordCount;

public class AirlineBSTImpl implements WordFrequencyAnalyzer {
    private HashMap<String, List<AirlineReview>> airlineReviews;
    private String airline;

    public AirlineBSTImpl(HashMap<String, List<AirlineReview>> airlineReviews, String airline) {
        this.airlineReviews = airlineReviews;
        this.airline = airline;
    }

    public Pair<List<WordCount>, List<WordCount>> getTop10MostCommonWords() {
        List<AirlineReview> reviews = airlineReviews.get(airline);
        if (reviews == null || reviews.isEmpty()) {
            return Pair.of(new ArrayList<>(), new ArrayList<>());
        }

        // Build BSTs for good and bad words
        WordFrequencyBST goodBST = new WordFrequencyBST();
        WordFrequencyBST badBST = new WordFrequencyBST();

        int goodTotal = 0;
        int badTotal = 0;

        for (AirlineReview review : reviews) {
            if (TextAnalysisUtils.isPositiveRecommendation(review.getRecommended())) {
                for (String word : review.getTokenizedReview()) {
                    goodBST.insert(word);
                    goodTotal++;
                }
            } else {
                for (String word : review.getTokenizedReview()) {
                    badBST.insert(word);
                    badTotal++;
                }
            }
        }

        // Get top 10 directly from BSTs using heap-based traversal (no ArrayList build)
        List<WordCount> topGood = goodBST.topK(goodTotal, 10);
        List<WordCount> topBad = badBST.topK(badTotal, 10);

        return Pair.of(topGood, topBad);
    }

    // Top-K moved into WordFrequencyBST to avoid materializing full list

    // Inner class: BST Node
    private static class BSTNode {
        String word;
        int count;
        BSTNode left;
        BSTNode right;

        BSTNode(String word) {
            this.word = word;
            this.count = 1;
            this.left = null;
            this.right = null;
        }
    }

    // Inner class: Word Frequency BST
    private static class WordFrequencyBST {
        private BSTNode root;

        public WordFrequencyBST() {
            this.root = null;
        }

        // Insert word or increment count if exists
        public void insert(String word) {
            root = insertRec(root, word);
        }

        private BSTNode insertRec(BSTNode node, String word) {
            if (node == null) {
                return new BSTNode(word);
            }

            int cmp = word.compareTo(node.word);
            if (cmp < 0) {
                node.left = insertRec(node.left, word);
            } else if (cmp > 0) {
                node.right = insertRec(node.right, word);
            } else {
                // Word already exists, increment count
                node.count++;
            }

            return node;
        }

        // Heap-based Top-K without building a full list
        public List<WordCount> topK(int totalWords, int k) {
            PriorityQueue<WordCount> minHeap = new PriorityQueue<>(k, (a, b) -> Integer.compare(a.getCount(), b.getCount()));
            inOrderCollectTopK(root, minHeap, totalWords, k);
            ArrayList<WordCount> result = new ArrayList<>(minHeap);
            result.sort((a, b) -> Integer.compare(b.getCount(), a.getCount()));
            return result;
        }

        private void inOrderCollectTopK(BSTNode node, PriorityQueue<WordCount> heap, int totalWords, int k) {
            if (node == null) return;
            inOrderCollectTopK(node.left, heap, totalWords, k);
            WordCount wc = new WordCount(node.word, node.count, totalWords);
            if (heap.size() < k) {
                heap.offer(wc);
            } else if (!heap.isEmpty() && wc.getCount() > heap.peek().getCount()) {
                heap.poll();
                heap.offer(wc);
            }
            inOrderCollectTopK(node.right, heap, totalWords, k);
        }
    }
}