package com.github.romualdrousseau.archery.classifier;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.github.romualdrousseau.archery.commons.types.CollectionUtils;
import com.github.romualdrousseau.archery.commons.preprocessing.Text;

public class TrainingSetBuilder {
    private static final float TRAININGSET_AUGMENT_COEF = 0.5f;

    public static List<TrainingEntry> gatherConflicts(final List<TrainingEntry> entries) {
        return entries.parallelStream().filter(x -> entries.stream().anyMatch(x::isConflict)).distinct().toList();
    }

    public static List<TrainingEntry> buildValidationSet(final List<TrainingEntry> entries) {
        return entries.parallelStream().filter(x -> !entries.stream().anyMatch(x::isConflict)).distinct().toList();
    }

    public static List<TrainingEntry> buildTrainingSet(final List<TrainingEntry> validationSet) {

        // Collect distribution stats on each classes

        final var cntPerClass = validationSet.stream()
                .collect(TrainingSetBuilder.groupByLabel()).entrySet().stream()
                .collect(TrainingSetBuilder.countValues());
        final var biggestClassCnt = cntPerClass.values().stream().max((a, b) -> Integer.compare(a, b)).get();

        // Generate mutated samples from the original dataset

        final Function<TrainingEntry, Stream<TrainingEntry>> mutator = x -> TrainingSetBuilder.mutateSample(cntPerClass,
                biggestClassCnt, x);
        final var augmentedTrainingSet = CollectionUtils
                .shuffle(validationSet.stream().flatMap(mutator).collect(Collectors.toList()));

        // Remove conflicts with validation set

        final var cleanedTrainingSet = augmentedTrainingSet.parallelStream()
                .filter(x -> !validationSet.stream().anyMatch(x::isConflict)).distinct().toList();

        // Concat original dataset and the mutated samples

        final long p = Math.round((float) cleanedTrainingSet.size() * TrainingSetBuilder.TRAININGSET_AUGMENT_COEF);
        return Stream.concat(validationSet.stream(), cleanedTrainingSet.stream().limit(p)).toList();
    }

    private static Stream<TrainingEntry> mutateSample(
            final Map<List<Integer>, Integer> cntPerClass,
            final int biggestClassCnt,
            final TrainingEntry sample) {
        final var part1 = sample.getEntity();
        final var part2 = sample.getName();
        final var part3 = sample.getContext().stream().filter(x -> x != 0).toList();
        final var size = biggestClassCnt / cntPerClass.get(sample.getLabel()) - 1;
        return IntStream.range(0, size).boxed()
                .map(i -> Text.pad_sequence(Text.mutate_sequence(part3), NetTagClassifier.IN_CONTEXT_SIZE))
                .map(x -> Stream.of(part1, part2, x).flatMap(Collection::stream).toList())
                .map(x -> new TrainingEntry(x, sample.getLabel()));
    }

    private static Collector<TrainingEntry, ?, Map<List<Integer>, List<TrainingEntry>>> groupByLabel() {
        return Collectors.groupingBy(
                TrainingEntry::getLabel,
                Collectors.collectingAndThen(Collectors.toList(), Function.identity()));
    }

    private static Collector<Entry<List<Integer>, List<TrainingEntry>>, ?, Map<List<Integer>, Integer>> countValues() {
        return Collectors.groupingBy(
                Entry::getKey,
                Collectors.summingInt(x -> x.getValue().size()));
    }
}
