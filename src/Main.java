import main.java.ua.fedytion.loader.DictionaryLoader;
import main.java.ua.fedytion.loader.FrequencyLoader;
import main.java.ua.fedytion.restorer.WordRestorer;
import main.java.ua.fedytion.segmenter.TextSegmenter;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws Exception {
        Set<String> dictionary = DictionaryLoader.loadDictionary("resources/words_alpha.txt");
        Map<String, Long> unigram = FrequencyLoader.loadUnigramFrequencies("resources/count_1w.txt");
        Map<String, Map<String, Long>> bigram = FrequencyLoader.loadBigramFrequencies("resources/count_2w.txt");

        dictionary.retainAll(unigram.keySet());
        WordRestorer restorer = new WordRestorer(dictionary, unigram);
        TextSegmenter segmenter = new TextSegmenter(restorer, unigram, bigram);

        String noisy = "A***ew*sbegninignt*g*tv***tried*f*s***ing*y*e*srtseionthebnkaadnofvhaingntohnigtod*";

        List<String> result = segmenter.segmentText(noisy);
        System.out.println("✅ Відновлений текст:");
        System.out.println(String.join(" ", result));
    }
}