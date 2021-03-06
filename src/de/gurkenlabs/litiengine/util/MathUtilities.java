package de.gurkenlabs.litiengine.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MathUtilities {
  private MathUtilities() {
    throw new UnsupportedOperationException();
  }

  public static boolean equals(double d1, double d2, double epsilon) {
    return Math.abs(d1 - d2) <= epsilon;
  }

  public static float round(float value, int places) {
    return (float) round((double) value, places);
  }

  public static double round(double value, int places) {
    if (places < 0) {
      throw new IllegalArgumentException();
    }

    BigDecimal bd = BigDecimal.valueOf(value);
    bd = bd.setScale(places, RoundingMode.HALF_UP);
    return bd.doubleValue();
  }

  /**
   * Modifies the value (if necessary) such that it lies within the boundaries of the specified minimum and maximum.
   * 
   * @param value
   *          The value to be clamped.
   * @param min
   *          The minimum value to be accepted.
   * @param max
   *          The maximum value to be accepted.
   * 
   * @return A value clamped to the specified boundaries.
   */
  public static double clamp(final double value, final double min, final double max) {
    return Math.max(min, Math.min(max, value));
  }

  /**
   * Modifies the value (if necessary) such that it lies within the boundaries of the specified minimum and maximum.
   * 
   * @param value
   *          The value to be clamped.
   * @param min
   *          The minimum value to be accepted.
   * @param max
   *          The maximum value to be accepted.
   * 
   * @return A value clamped to the specified boundaries.
   */
  public static float clamp(final float value, final float min, final float max) {
    return Math.max(min, Math.min(max, value));
  }

  /**
   * Modifies the value (if necessary) such that it lies within the boundaries of the specified minimum and maximum.
   * 
   * @param value
   *          The value to be clamped.
   * @param min
   *          The minimum value to be accepted.
   * @param max
   *          The maximum value to be accepted.
   * 
   * @return A value clamped to the specified boundaries.
   */
  public static byte clamp(final byte value, final byte min, final byte max) {
    if (value < min) {
      return min;
    }

    if (value > max) {
      return max;
    }

    return value;
  }

  /**
   * Modifies the value (if necessary) such that it lies within the boundaries of the specified minimum and maximum.
   * 
   * @param value
   *          The value to be clamped.
   * @param min
   *          The minimum value to be accepted.
   * @param max
   *          The maximum value to be accepted.
   * 
   * @return A value clamped to the specified boundaries.
   */
  public static short clamp(final short value, final short min, final short max) {

    if (value < min) {
      return min;
    }

    if (value > max) {
      return max;
    }

    return value;
  }

  /**
   * Modifies the value (if necessary) such that it lies within the boundaries of the specified minimum and maximum.
   * 
   * @param value
   *          The value to be clamped.
   * @param min
   *          The minimum value to be accepted.
   * @param max
   *          The maximum value to be accepted.
   * 
   * @return A value clamped to the specified boundaries.
   */
  public static int clamp(final int value, final int min, final int max) {
    if (value < min) {
      return min;
    }

    if (value > max) {
      return max;
    }

    return value;
  }

  /**
   * Modifies the value (if necessary) such that it lies within the boundaries of the specified minimum and maximum.
   * 
   * @param value
   *          The value to be clamped.
   * @param min
   *          The minimum value to be accepted.
   * @param max
   *          The maximum value to be accepted.
   * 
   * @return A value clamped to the specified boundaries.
   */
  public static long clamp(final long value, final long min, final long max) {
    if (value < min) {
      return min;
    }

    if (value > max) {
      return max;
    }

    return value;
  }

  public static double getAverage(final double[] numbers) {
    double sum = 0;
    for (final double number : numbers) {
      if (number != 0) {
        sum += number;
      }
    }

    return sum / numbers.length;
  }

  public static float getAverage(final float[] numbers) {
    float sum = 0;
    for (final float number : numbers) {
      if (number != 0) {
        sum += number;
      }
    }

    return sum / numbers.length;
  }

  public static int getAverage(final int[] numbers) {
    int sum = 0;
    for (final int number : numbers) {
      if (number != 0) {
        sum += number;
      }
    }

    return sum / numbers.length;
  }

  public static int getMax(final int... numbers) {
    int max = Integer.MIN_VALUE;
    for (int i = 0; i < numbers.length; i++) {
      if (numbers[i] > max) {
        max = numbers[i];
      }
    }
    return max;
  }

  /**
   * The index probabilities must sum up to 1;
   *
   * @param indexProbabilities
   *          The index with the probabilities for the related index.
   * @return A random index within the range of the specified array.
   */
  public static int getRandomIndex(final double[] indexProbabilities) {
    final double rnd = ThreadLocalRandom.current().nextDouble();
    double probSum = 0;
    for (int i = 0; i < indexProbabilities.length; i++) {
      final double newProbSum = probSum + indexProbabilities[i];
      if (rnd >= probSum && rnd < newProbSum) {
        return i;
      }

      probSum = newProbSum;
    }

    return 0;
  }

  public static boolean isInt(final double value) {
    return value == Math.floor(value) && !Double.isInfinite(value);
  }

  public static boolean isOddNumber(int num) {
    return (num & 1) != 0;
  }

  public static boolean probabilityIsTrue(final double probability) {
    double rnd = ThreadLocalRandom.current().nextDouble();
    return rnd < probability;
  }

  public static boolean randomBoolean() {
    return ThreadLocalRandom.current().nextDouble() < 0.5;
  }

  public static double randomInRange(final double min, final double max) {
    return randomInRange(min, max, ThreadLocalRandom.current());
  }

  public static double randomInRange(final double min, final double max, Random random) {
    if (min == max) {
      return min;
    }

    if (min > max) {
      throw new IllegalArgumentException("min value is > than max value");
    }

    return min + random.nextDouble() * (max - min);
  }

  public static int randomInRange(final int min, final int max) {
    return randomInRange(min, max, ThreadLocalRandom.current());
  }

  public static int randomInRange(final int min, final int max, Random random) {
    if (min == max) {
      return min;
    }

    if (min > max) {
      throw new IllegalArgumentException("min value is > than max value");
    }

    return random.nextInt(max - min) + min;
  }

  public static int randomSign() {
    return randomBoolean() ? 1 : -1;
  }

  public static int getFullPercent(double value, double fraction) {
    if (value == 0) {
      return 0;
    }

    return (int) ((fraction * 100.0f) / value);
  }

  public static double getPercent(double value, double fraction) {
    if (value == 0) {
      return 0;
    }

    return (float) fraction * 100 / value;
  }
}
