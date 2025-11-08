# DSA-Airline-Project

This repository contains an educational project that analyzes airline review text data and demonstrates multiple data-structure approaches to compute the top-k most common tokens in positive and negative reviews.

## What this project does

  - Binary Search Tree (BST) implementation (`AirlineBSTImpl`) — keeps a BST of unique words and counts occurrences on insert.
  - Red-Black Tree (RBT) implementation (`AirlineRBTreeImpl`) — a self-balancing BST with color-based rotations to guarantee O(log N) operations.
  - ArrayList-based implementation (`AirlineArrayListImpl`) — accumulates tokens then compresses sorted runs to counts.
  - Map/HashMap implementation (`AirlineMapImpl`) — classic word -> count HashMap counting.

## Project structure (high level)

  - `AirlineBSTImpl.java` — Binary Search Tree version
  - `AirlineRBTreeImpl.java` — Red-Black Tree (self-balancing) version
  - `AirlineArrayListImpl.java` — ArrayList-based version
  - `AirlineMapImpl.java` — Map / HashMap-based version
  - `WordFrequencyAnalyzer.java` — simple interface implemented by each approach
  - `AirlineReview.java` — review container (airline, tokenized review, rating, recommended)
  - `WordCount.java` — wrapper: word + count + total for percent computations
  - `Pair.java` — simple left/right pair returned by analyzers
  - `TextAnalysisUtils.java` — helpers for compressing counts, filtering near-common dominant words, and top-K helpers

## Implementation notes (how counting is performed)

  - Stores each unique token in a BST node with `count` field.
  - On insert: traverse tree, if token == node.word then `node.count++` else create node with `count = 1` and insert into BST.
  - After building the tree for positive/negative sets, each tree provides a `topK` method that traverses in-order and uses a min-heap (size K) to compute top-K without materializing the entire list.

  - Same logical counting approach as BST (each node stores a count).
  - New words create `RBNode` with `count = 1`; subsequent occurrences find the node and increment `count`.
  - On insertion of a new node the tree performs standard RBT balancing (recolor and rotations) to guarantee O(log N) depth.
  - Also supports a `topK` method that does an in-order traversal and maintains a min-heap of size K to select top tokens.

  ### Red-Black Tree complexity (short)

  - Insert / lookup (per token): O(log M) comparisons in the tree, where M is number of distinct tokens. Each string comparison costs O(L) where L is token length, so cost ≈ O(L · log M).
  - Building from T tokens: O(T · log M).
  - Top-K selection (in-order + size-K min-heap): O(M · log K) (for small K this is ≈ O(M)).
  - Space: O(M) nodes + O(K) heap extra; recursion stack O(log M).

  - Accumulates tokens into lists for positive/negative reviews.
  - Sorts the list then compresses runs of identical tokens into `WordCount` entries (word + run length).
  - Sorts the counts by frequency and returns the top-K.

  - Uses `HashMap<String, Integer>` to increment counts in O(1) average time: `map.put(word, map.getOrDefault(word, 0) + 1)`.
  - Converts the map to a TreeMap keyed by frequency (descending) to produce stable top-K results.

  - `TextAnalysisUtils` contains helper methods used by implementations for compressing counts, filtering tokens that are frequent in both positive and negative sets (near-common dominant filtering), and selecting top-K elements.
  - The analyzers and the interactive tool use `WordCount` objects so outputs have a consistent format.

## How to run

Open a terminal in the project root (Windows cmd / PowerShell) and run the Gradle wrapper. Use the commands below depending on your shell:

Windows (cmd.exe):

```bat
gradlew.bat run
```

PowerShell (or Git Bash / WSL):

```powershell
./gradlew run
```

What the run does:
  - Loads and tokenizes the dataset from `app/src/main/resources/airline.csv` (stemming + stop word removal).
  - Runs all four analyzers (BST, Red-Black Tree, ArrayList, Map) on the same airline and prints:
    - Time (median of 5) and Allocated (median KB) for `getTop10MostCommonWords()`.
    - Top 10 tokens for positive (GOOD) and negative (BAD) reviews in a consistent format.
  - Prompts to launch the interactive token comparison CLI.

## Example output (truncated)

```
[BST] Time (median of 5): 23 ms | Allocated (median): 330 KB
[GOOD] Top 10 most common words in POSITIVE reviews:
 1. Word: about          Count: 136
 ...
[BAD] Top 10 most common words in NEGATIVE reviews:
 1. Word: hour           Count: 679
 ...
```

## Interactive Comparison (detailed)

The project includes an interactive CLI to compare how a specific token (word stem) is used across airlines. This CLI is implemented in `InteractiveComparison` and is invoked at the end of the static runs from `Main.java`.

Flow and prompts:

- Prompt 1: "Would you like to compare airlines based on a specific word? (yes/no)" — answer `yes` to continue, `no` to skip and exit.
- Prompt 2: "Compare based on: 1. Positive reviews (good words) 2. Negative reviews (bad words)" — enter `1` or `2`.
- The program computes the top ~20 most common tokens for the chosen sentiment across the entire dataset, excluding a small set of uninformative stems.
- It prints the tokens (mapped to more readable words when available) and asks you to type the word you want to compare.

What the comparison computes:

- For every airline, it counts how many times the chosen token appears in reviews of the selected sentiment and how many reviews of that sentiment the airline has.
- Percentage = tokenCount / sentimentReviewCount (per-airline).
- Weighted score = percentage * log10(totalReviews + 1). Airlines with fewer than `MIN_REVIEWS` (50) receive a 0.5 penalty multiplier to de-prioritize low-data results.
- Airlines are ranked by the weighted score and the top 5 are shown with columns: Reviews, Count, Percent, Score. Low-data airlines are marked with `*`.

Important constants and behaviors:

- `EXCLUDED_COMPARISON_WORDS` — a small, hard-coded set of stems to ignore when presenting token choices.
- `STEM_TO_WORD` — a fixed mapping from stems to readable words (used for presentation and to accept readable input from the user).
- `MIN_REVIEWS = 50` — reliability threshold; below this, results are penalized.
- CLI uses `Scanner` for input; running in non-interactive environments will block the process — answer `no` to skip the interactive step or comment out the call in `Main.java`.

Example (mock) interaction:

```
Would you like to compare airlines based on a specific word? (yes/no): yes
Compare based on:
1. Positive reviews (good words)
2. Negative reviews (bad words)
Enter choice (1 or 2): 2

Most common meaningful words from NEGATIVE reviews:
 1. delay          (used 2,345 times)
 2. hour           (used 1,102 times)
 3. seat           (used 987 times)
 ...
Enter the word you want to compare: delay

TOP 5 Airlines with HIGHEST weighted score for "delay":
Rank  Airline                     Reviews   Count   Percent   Score
1     Airline A                    1,200     240     20.00%   26.45
2     Airline B                    3,400     540     15.88%   24.30
3     Airline C                      40      18     45.00%    4.32 *
```

## Notes and recommendations

  - Merge `master` into your feature branch first, resolve conflicts there, run tests and `gradlew run` until green, then merge your branch into `master` (squash merge recommended for a single feature commit).

## Next steps (practical)
1. Add unit tests for parity
  - Create a small, deterministic CSV (10–50 reviews) and write JUnit tests that run each implementation and assert that the top-K outputs are consistent.

2. Headless / CI-friendly run mode
  - Add command-line flags to `Main` (or environment variable) to skip the interactive comparison and to select which implementations to run.

3. Benchmarks
  - Add a micro-benchmark that measures wall-clock time and memory for each implementation over increasing dataset sizes; output CSV for plotting.

4. Automated sample runs
  - Add a script `scripts/run_all.sh` (or `.bat` for Windows) that runs `gradlew run` in headless mode with different dataset variants and stores outputs under `benchmarks/`.

5. Improve interactive mapping
  - Expand `STEM_TO_WORD` and add a small JSON mapping file to make the interactive UI more readable and easier to maintain.

## Timing and memory reporting

- Each analyzer is run via a unified harness in `Main.runAnalyzer(title, analyzer)`.
- Measurements:
  - Time: wall-clock time around `getTop10MostCommonWords()`; we perform 1 warm-up call, then 5 measured runs and report the median in milliseconds.
  - Memory (Allocated KB): total bytes allocated by the current thread during the call, captured via `ThreadMXBean.getThreadAllocatedBytes(...)` when supported, then converted to KB and reported as the median across runs.
- Why “Allocated KB” instead of retained heap:
  - Retained heap deltas after GC are typically tiny and dominated by the small result object, so they don’t reflect actual memory usage during computation.
  - Allocated bytes include temporary objects created and discarded during the algorithm and are therefore more representative of memory usage while running.
- Notes:
  - Allocation tracking requires a JVM that supports per-thread allocated bytes (most modern HotSpot builds do).
  - Results are best interpreted relatively (comparing implementations and dataset sizes). Actual numbers vary by machine/JVM.