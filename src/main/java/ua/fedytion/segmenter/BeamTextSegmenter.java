package main.java.ua.fedytion.segmenter;

import main.java.ua.fedytion.restorer.WordRestorer;

import java.util.*;

public class BeamTextSegmenter {
    private final WordRestorer restorer;
    private final Map<String, Long> unigramFreq;
    private final Map<String, Map<String, Long>> bigramFreq;
    private final int maxWordLen = 20;
    private final int beamWidth;

    public BeamTextSegmenter(WordRestorer restorer,
                             Map<String, Long> unigramFreq,
                             Map<String, Map<String, Long>> bigramFreq,
                             int beamWidth) {
        this.restorer = restorer;
        this.unigramFreq = unigramFreq;
        this.bigramFreq = bigramFreq;
        this.beamWidth = beamWidth;
    }

    public List<String> segmentText(String text) {
        class BeamCandidate {
            int index;
            List<String> words;
            double score;
            String prev;

            BeamCandidate(int index, List<String> words, double score, String prev) {
                this.index = index;
                this.words = words;
                this.score = score;
                this.prev = prev;
            }
        }

        PriorityQueue<BeamCandidate> beam = new PriorityQueue<>(
                Comparator.comparingDouble(c -> -c.score)
        );

        beam.add(new BeamCandidate(0, new ArrayList<>(), 0.0, null));

        while (!beam.isEmpty()) {
            List<BeamCandidate> nextBeam = new ArrayList<>();

            for (BeamCandidate cand : beam) {
                if (cand.index == text.length()) {
                    return cand.words;
                }

                for (int len = 2; len <= maxWordLen && cand.index + len <= text.length(); len++) {
                    String fragment = text.substring(cand.index, cand.index + len);
                    List<String> candidates = restorer.restoreCandidates(fragment);

                    if (candidates.isEmpty()) {
                        // Якщо немає кандидатів — додай сам фрагмент як fallback
                        candidates = new ArrayList<>(List.of(fragment));
                    }

                    candidates.sort(Comparator.comparingLong(
                            (String w) -> unigramFreq.getOrDefault(w, 1L)
                    ).reversed());

                    candidates.removeIf(w -> w.length() > 12);

                    candidates.sort(Comparator.comparingLong(
                            (String w) -> unigramFreq.getOrDefault(w, 1L)
                    ).reversed());

                    if (candidates.size() > 10) {
                        candidates = candidates.subList(0, 10);
                    }

                    for (String word : candidates) {
                        double wordScore = getWordScore(word, cand.prev);
                        List<String> newWords = new ArrayList<>(cand.words);
                        newWords.add(word);
                        nextBeam.add(new BeamCandidate(
                                cand.index + len,
                                newWords,
                                cand.score + wordScore,
                                word
                        ));
                    }
                }
            }

            if (nextBeam.isEmpty()) {
                System.out.println("❌ Beam Search зупинився — не знайдено кандидатів.");
                // fallback: повертаємо найкращий частковий результат
                return beam.stream()
                        .max(Comparator.comparingDouble(c -> c.score))
                        .map(c -> c.words)
                        .orElse(Collections.singletonList("unable to restore"));
            }

            beam = new PriorityQueue<>(Comparator.comparingDouble(c -> -c.score));
            nextBeam.sort(Comparator.comparingDouble(c -> -c.score));
            for (int i = 0; i < Math.min(beamWidth, nextBeam.size()); i++) {
                beam.add(nextBeam.get(i));
            }
        }

        return Collections.singletonList("unable to restore");
    }

    private double getWordScore(String word, String prev) {
        double score = Math.log(unigramFreq.getOrDefault(word, 1L));

        if (prev != null) {
            long prevCount = unigramFreq.getOrDefault(prev, 1L);
            Map<String, Long> inner = bigramFreq.get(prev);
            long bigramCount = 0;
            if (inner != null) {
                bigramCount = inner.getOrDefault(word, 0L);
            }

            // Ймовірність появи слова word після prev
            score += Math.log((bigramCount + 1.0) / (prevCount + 1.0));
        }

        // Мʼякий штраф за надто довгі слова
        if (word.length() > 12) {
            score -= 2.0;
        }

        // Мʼякий бонус за “нормальні” слова (4–8 букв)
        if (word.length() >= 4 && word.length() <= 8) {
            score += 0.5;
        }

        return score;
    }
}
