package me.limhax.openAC.util;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class SamplesBuffer {
  private final int capacity;
  private final double[] values;
  private int size;
  private int head;

  public SamplesBuffer(int capacity) {
    if (capacity <= 0) {
      throw new IllegalArgumentException("Capacity must be positive");
    }
    this.capacity = capacity;
    this.values = new double[capacity];
    this.size = 0;
    this.head = 0;
  }

  public void add(double value) {
    values[head] = value;
    head = (head + 1) % capacity;
    if (size < capacity) {
      size++;
    }
  }

  public double get(int index) {
    if (index < 0 || index >= size) {
      throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
    }
    int actualIndex = (size < capacity) ? index : (head + index) % capacity;
    return values[actualIndex];
  }

  public List<Double> asList() {
    List<Double> list = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      list.add(get(i));
    }
    return list;
  }

  public double getLast() {
    if (size == 0) return 0;
    int lastIndex = (head - 1 + capacity) % capacity;
    return values[lastIndex];
  }

  public int size() {
    return size;
  }

  public boolean isEmpty() {
    return size == 0;
  }

  public boolean isFull() {
    return size == capacity;
  }

  public void clear() {
    size = 0;
    head = 0;
  }

  public double[] toArray() {
    double[] array = new double[size];
    for (int i = 0; i < size; i++) {
      array[i] = get(i);
    }
    return array;
  }

  public float getMin() {
    if (isEmpty()) {
      return 0.0f;
    }

    float min = Float.MAX_VALUE;
    for (int i = 0; i < size; i++) {
      min = Math.min(min, (float) get(i));
    }
    return min;
  }

  public float getMax() {
    if (isEmpty()) {
      return 0.0f;
    }
    float max = Float.MIN_VALUE;
    for (int i = 0; i < size; i++) {
      max = Math.max(max, (float) get(i));
    }
    return max;
  }

  public float getPercentile(float percentile) {
    if (isEmpty()) return 0.0f;

    List<Double> sorted = new ArrayList<>(asList());
    Collections.sort(sorted);

    percentile = Math.max(0.0f, Math.min(percentile, 1.0f));

    int index = (int) Math.floor((sorted.size() - 1) * percentile);
    return sorted.get(index).floatValue();
  }


  public double getAverage() {
    if (isEmpty()) return 0.0;
    double sum = 0.0;
    for (double d : toArray()) {
      sum += d;
    }
    return sum / size;
  }

  public int getDuplicates(double threshold) {
    if (size == 0) return 0;

    double[] sorted = toArray();
    Arrays.sort(sorted);

    boolean[] isDuplicate = new boolean[sorted.length];

    for (int i = 0; i < sorted.length - 1; i++) {
      for (int j = i + 1; j < sorted.length && sorted[j] - sorted[i] <= threshold; j++) {
        isDuplicate[i] = true;
        isDuplicate[j] = true;
      }
    }

    int duplicates = 0;
    for (boolean dup : isDuplicate) {
      if (dup) duplicates++;
    }

    return duplicates;
  }

  public int getDuplicates() {
    return size - (int) asList().stream().distinct().count();
  }

  public int getDistinct() {
    return (int) asList().stream().distinct().count();
  }

  public double getCPS() {
    double avg = getAverage();
    return avg == 0 ? 0 : (20.0 / avg * 50.0);
  }

  public double getEntropy() {
    return getEntropy(15);
  }

  public double getEntropy(int bins) {
    if (isEmpty() || size < 2) return 0.0;

    double min = getMin();
    double max = getMax();

    if (Math.abs(max - min) < 1e-10) return 0.0;

    Map<Integer, Integer> binFrequency = new HashMap<>();
    double binWidth = (max - min) / bins;

    for (int i = 0; i < size; i++) {
      double value = get(i);
      int binIndex = (int) Math.min(bins - 1, Math.floor((value - min) / binWidth));
      binFrequency.put(binIndex, binFrequency.getOrDefault(binIndex, 0) + 1);
    }

    double entropy = 0.0;
    for (int frequency : binFrequency.values()) {
      if (frequency > 0) {
        double probability = (double) frequency / size;
        entropy -= probability * (Math.log(probability) / Math.log(2));
      }
    }

    return entropy;
  }

  public double getRange() {
    return getMax() - getMin();
  }

  public double getVariance() {
    if (isEmpty()) return 0.0;

    double sum = 0.0;
    for (int i = 0; i < size; i++) {
      sum += get(i);
    }
    final double average = sum / size;

    double variance = 0.0;
    for (int i = 0; i < size; i++) {
      variance += Math.pow(get(i) - average, 2.0);
    }
    return variance;
  }

  public double getStandardDeviation() {
    final double variance = getVariance();
    return Math.sqrt(variance);
  }

  public double getSkewness() {
    if (size < 2) return 0.0;

    double sum = 0.0;
    List<Double> numbers = new ArrayList<>();

    for (int i = 0; i < size; i++) {
      double value = get(i);
      sum += value;
      numbers.add(value);
    }

    Collections.sort(numbers);

    final double mean = sum / size;
    final double median = (size % 2 != 0)
        ? numbers.get(size / 2)
        : ((numbers.get((size - 1) / 2) + numbers.get(size / 2)) / 2.0);
    final double variance = getVariance();

    if (variance == 0.0) return 0.0;

    return 3.0 * (mean - median) / variance;
  }

  public double getKurtosis() {
    if (size < 3) return 0.0;

    double sum = 0.0;
    for (int i = 0; i < size; i++) {
      sum += get(i);
    }

    final double efficiencyFirst = size * (size + 1.0) / ((size - 1.0) * (size - 2.0) * (size - 3.0));
    final double efficiencySecond = 3.0 * Math.pow(size - 1.0, 2.0) / ((size - 2.0) * (size - 3.0));
    final double average = sum / size;

    double variance = 0.0;
    double varianceSquared = 0.0;

    for (int i = 0; i < size; i++) {
      double value = get(i);
      variance += Math.pow(average - value, 2.0);
      varianceSquared += Math.pow(average - value, 4.0);
    }

    return efficiencyFirst * (varianceSquared / Math.pow(variance / sum, 2.0)) - efficiencySecond;
  }

  public void exportSnapshot(
      String groupId,
      double value,
      double avg,
      double stdDev,
      double entropy,
      double kurtosis,
      double variance
  ) {
    File folder = new File("plugins/AntiCheat/rotation_data");
    if (!folder.exists()) folder.mkdirs();
    File file = new File(folder, "samples.txt");
    try (FileWriter writer = new FileWriter(file, true)) {
      writer.write(groupId + "," + value + "," + avg + "," + stdDev + "," + entropy + "," + kurtosis + "," + variance + "\n");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
