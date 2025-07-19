package main.java.ua.fedytion.segmenter;

import main.java.ua.fedytion.restorer.WordRestorer;

import java.util.*;

public class TextSegmenter {
    private final WordRestorer restorer;
    private final Map<String, Long> unigramFreq;
    private final Map<String, Map<String, Long>> bigramFreq;

    private final int MAX_WORD_LEN = 20;

    public TextSegmenter(WordRestorer restorer, Map<String, Long> unigramFreq,
                         Map<String, Map<String, Long>> bigramFreq) {
        this.restorer = restorer;
        this.unigramFreq = unigramFreq;
        this.bigramFreq = bigramFreq;
    }

    public List<String> segmentText(String noisyText) {
        Map<Integer, Result> memo = new HashMap<>();
        Result result = dfs(noisyText.toLowerCase(), 0, null, null, 0, memo);
        return result == null ? Collections.singletonList("unable to restore") : result.words;
    }

    private Result dfs(String text, int index, String prevPrev, String prev, int shortCount, Map<Integer, Result> memo) {
        if (index == text.length()) return new Result(0.0, new ArrayList<>());
        if (memo.containsKey(index)) return memo.get(index);

        double bestScore = Double.NEGATIVE_INFINITY;
        List<String> bestWords = null;

        for (int len = 2; len <= MAX_WORD_LEN && index + len <= text.length(); len++) {
            String fragment = text.substring(index, index + len);
            List<String> candidates = restorer.restoreCandidates(fragment);

            // Сортуємо за частотою
            candidates.sort(Comparator.comparingLong((String w) -> unigramFreq.getOrDefault(w, 1L)).reversed());
            if (candidates.size() > 10) {
                candidates = candidates.subList(0, 10);
            }

            for (String candidate : candidates) {
                double score = getWordScore(candidate, prev);

                // 🧠 Додаємо штраф за короткі слова поспіль
                int newShortCount = candidate.length() <= 3 ? shortCount + 1 : 0;
                double penalty = newShortCount >= 3 ? -5.0 : 0.0;

                Result next = dfs(text, index + len, prev, candidate, newShortCount, memo);
                if (next == null) continue;

                double total = score + next.score + penalty;
                if (total > bestScore) {
                    bestScore = total;
                    bestWords = new ArrayList<>();
                    bestWords.add(candidate);
                    bestWords.addAll(next.words);
                }
            }
        }

        Result result = bestWords == null ? null : new Result(bestScore, bestWords);
        memo.put(index, result);
        return result;
    }

    private double getWordScore(String word, String prev) {
        long unigram = unigramFreq.getOrDefault(word, 1L);
        double lenBonus = 1 + 0.4 * Math.max(0, word.length() - 3); // даємо перевагу довшим словам

        double score = Math.log(unigram + 1) * lenBonus;

        if (prev != null && bigramFreq.containsKey(prev)) {
            long bigram = bigramFreq.get(prev).getOrDefault(word, 1L);
            score += 0.5 * Math.log(bigram + 1); // зменшили вагу біграми
        }

        return score;
    }

    private static class Result {
        double score;
        List<String> words;

        public Result(double score, List<String> words) {
            this.score = score;
            this.words = words;
        }
    }
}
