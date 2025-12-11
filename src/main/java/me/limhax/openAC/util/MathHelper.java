package me.limhax.openAC.util;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

@UtilityClass
public class MathHelper {
  public static final double EXPANDER = 1.6777216E7D;

  public double sin(double value) {
    return Math.sin(value);
  }

  public double cos(double value) {
    return Math.cos(value);
  }

  public static double hypot(final double x, final double z) {
    return Math.sqrt(x * x + z * z);
  }

  public static double decimalRound(final double val, int scale) {
    return BigDecimal.valueOf(val).setScale(scale, RoundingMode.HALF_EVEN).doubleValue();
  }


  public static int getMode(final Collection<? extends Number> array) {
    int mode = (int) array.toArray()[0];
    int maxCount = 0;
    for (final Number value : array) {
      int count = 1;
      for (final Number i : array) {
        if (i.equals(value)) {
          ++count;
        }
        if (count > maxCount) {
          mode = (int) value;
          maxCount = count;
        }
      }
    }
    return mode;
  }

  public static float clamp180(float value) {

    value %= 360F;

    if (value >= 180.0F) value -= 360.0F;

    if (value < -180.0F) value += 360.0F;

    return value;
  }

  public static long getAbsoluteGcd(final float current, final float last) {

    final long currentExpanded = (long) (current * EXPANDER);

    final long lastExpanded = (long) (last * EXPANDER);

    return getGcd(currentExpanded, lastExpanded);
  }

  public static long getGcd(final long current, final long previous) {
    return (previous <= 16384L) ? current : getGcd(previous, current % previous);
  }

  public static double getGcd(final double a, final double b) {
    if (a < b) {
      return getGcd(b, a);
    }
    if (Math.abs(b) < 0.001) {
      return a;
    }
    return getGcd(b, a - Math.floor(a / b) * b);
  }
}



