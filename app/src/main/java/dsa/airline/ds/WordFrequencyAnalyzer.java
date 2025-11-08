package dsa.airline.ds;

import java.util.List;

import dsa.airline.model.util.Pair;
import dsa.airline.model.util.WordCount;

public interface WordFrequencyAnalyzer {
    Pair<List<WordCount>, List<WordCount>> getTop10MostCommonWords();
}


