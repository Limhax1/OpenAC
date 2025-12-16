package me.limhax.openAC.util;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class SamplesBuffer {
  private final LinkedList<Double> samples;
  private final int maxSize;

  public SamplesBuffer(final int maxSize) {
    this.samples = new LinkedList<>();
    this.maxSize = maxSize;
  }

  public void add(final double sample) {
    if (samples.size() >= maxSize) {
      samples.removeFirst();
    }
    samples.add(sample);
  }

  public void add(final Number sample) {
    add(sample.doubleValue());
  }

  public void clear() {
    samples.clear();
  }

  public boolean isFilled() {
    return samples.size() >= maxSize;
  }

  public int size() {
    return samples.size();
  }

  public int capacity() {
    return maxSize;
  }

  public Collection<Double> getSamples() {
    return samples;
  }

  public double getVariance() {
    int count = 0;
    double sum = 0.0;
    double variance = 0.0;
    for (final Number number : samples) {
      sum += number.doubleValue();
      ++count;
    }
    final double average = sum / count;
    for (final Number number : samples) {
      variance += Math.pow(number.doubleValue() - average, 2.0);
    }
    return variance;
  }

  public double getStandardDeviation() {
    final double variance = getVariance();
    return Math.sqrt(variance);
  }

  public double getSkewness() {
    double sum = 0.0;
    int count = 0;
    final List<Double> numbers = Lists.newArrayList();
    for (final Number number : samples) {
      sum += number.doubleValue();
      ++count;
      numbers.add(number.doubleValue());
    }
    Collections.sort(numbers);
    final double mean = sum / count;
    final double median = (count % 2 != 0) ? numbers.get(count / 2) : ((numbers.get((count - 1) / 2) + numbers.get(count / 2)) / 2.0);
    final double variance = getVariance();
    return 3.0 * (mean - median) / variance;
  }

  public double getAverage() {
    if (samples == null || samples.isEmpty()) {
      return 0.0;
    }
    double sum = 0.0;
    for (final Number number : samples) {
      sum += number.doubleValue();
    }
    return sum / samples.size();
  }

  public double getKurtosis() {
    double sum = 0.0;
    int count = 0;
    for (final Number number : samples) {
      sum += number.doubleValue();
      ++count;
    }
    if (count < 3.0) {
      return 0.0;
    }
    final double efficiencyFirst = count * (count + 1.0) / ((count - 1.0) * (count - 2.0) * (count - 3.0));
    final double efficiencySecond = 3.0 * Math.pow(count - 1.0, 2.0) / ((count - 2.0) * (count - 3.0));
    final double average = sum / count;
    double variance = 0.0;
    double varianceSquared = 0.0;
    for (final Number number2 : samples) {
      variance += Math.pow(average - number2.doubleValue(), 2.0);
      varianceSquared += Math.pow(average - number2.doubleValue(), 4.0);
    }
    return efficiencyFirst * (varianceSquared / Math.pow(variance / sum, 2.0)) - efficiencySecond;
  }

  public int getMode() {
    int mode = (int)samples.toArray()[0];
    int maxCount = 0;
    for (final Number value : samples) {
      int count = 1;
      for (final Number i : samples) {
        if (i.equals(value)) {
          ++count;
        }
        if (count > maxCount) {
          mode = (int)value;
          maxCount = count;
        }
      }
    }
    return mode;
  }

  public double getCps() {
    return 20.0 / getAverage() * 50.0;
  }

  public int getDuplicates() {
    return (int)(samples.size() - samples.stream().distinct().count());
  }

  public int getDistinct() {
    return (int)samples.stream().distinct().count();
  }
}