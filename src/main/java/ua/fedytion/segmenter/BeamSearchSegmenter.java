package main.java.ua.fedytion.segmenter;

import main.java.ua.fedytion.restorer.WordRestorer;

import java.util.*;

public class BeamSearchSegmenter {
    private final WordRestorer restorer;
    private final Map<String, Long> unigramFreq;
    private final Map<String, Map<String, Long>> bigramFreq;
    private final int beamWidth;

    public BeamSearchSegmenter(WordRestorer restorer,
                               Map<String, Long> unigramFreq,
                               Map<String, Map<String, Long>> bigramFreq,
                               int beamWidth) {
        this.restorer = restorer;
        this.unigramFreq = unigramFreq;
        this.bigramFreq = bigramFreq;
        this.beamWidth = beamWidth;
    }

    public List<String> segmentText(String noisyText) {
        PriorityQueue<BeamState> beam = new PriorityQueue<>();
        beam.add(new BeamState(0, new ArrayList<>(), null, 0.0));

        while (!beam.isEmpty()) {
            List<BeamState> nextBeam = new ArrayList<>();

            for (BeamState state : beam) {
                for (int end = state.position + 1; end <= Math.min(noisyText.length(), state.position + 20); end++) {
                    String chunk = noisyText.substring(state.position, end);
                    List<String> candidates = restorer.restoreCandidates(chunk);

                    for (String candidate : candidates) {
                        double score = state.score + getWordScore(candidate, state.prevWord);
                        List<String> newWords = new ArrayList<>(state.words);
                        newWords.add(candidate);
                        nextBeam.add(new BeamState(end, newWords, candidate, score));
                    }
                }
            }

            if (nextBeam.isEmpty()) break;

            nextBeam.sort(Comparator.comparingDouble(s -> -s.score));
            beam = new PriorityQueue<>(nextBeam.subList(0, Math.min(beamWidth, nextBeam.size())));
        }

        return beam.isEmpty() ? List.of("unable", "to", "restore") : beam.peek().words;
    }

    private double getWordScore(String word, String prev) {
        double score = Math.log(unigramFreq.getOrDefault(word, 1L));
        if (prev != null) {
            Map<String, Long> inner = bigramFreq.get(prev);
            if (inner != null) {
                long bigramCount = inner.getOrDefault(word, 0L);
                if (bigramCount > 0) {
                    score += Math.log(bigramCount + 1.0);
                } else {
                    score -= 5.0;
                }
            }
        }

        if (word.length() > 8) score -= 1.0;
        return score;
    }

    private static class BeamState implements Comparable<BeamState> {
        int position;
        List<String> words;
        String prevWord;
        double score;

        BeamState(int position, List<String> words, String prevWord, double score) {
            this.position = position;
            this.words = words;
            this.prevWord = prevWord;
            this.score = score;
        }

        @Override
        public int compareTo(BeamState other) {
            return Double.compare(other.score, this.score); // max-heap
        }
    }
}