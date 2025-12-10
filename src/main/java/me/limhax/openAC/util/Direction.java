package me.limhax.openAC.util;

public enum Direction {
  DOWN(1, "down", AxisDirection.NEGATIVE, Axis.Y, new Vec3(0.0, -1.0, 0.0)),
  UP(0, "up", AxisDirection.POSITIVE, Axis.Y, new Vec3(0.0, 1.0, 0.0)),
  NORTH(3, "north", AxisDirection.NEGATIVE, Axis.Z, new Vec3(0.0, 0.0, -1.0)),
  SOUTH(2, "south", AxisDirection.POSITIVE, Axis.Z, new Vec3(0.0, 0.0, 1.0)),
  WEST(5, "west", AxisDirection.NEGATIVE, Axis.X, new Vec3(-1.0, 0.0, 0.0)),
  EAST(4, "east", AxisDirection.POSITIVE, Axis.X, new Vec3(1.0, 0.0, 0.0));

  public static final Direction[] VALUES = values();

  private final int oppositeIndex;
  private final String name;
  private final Axis axis;
  private final AxisDirection axisDirection;
  private final Vec3 normal;

  Direction(int oppositeIndex, String name, AxisDirection axisDirection, Axis axis, Vec3 normal) {
    this.oppositeIndex = oppositeIndex;
    this.name = name;
    this.axis = axis;
    this.axisDirection = axisDirection;
    this.normal = normal;
  }

  public Direction getOpposite() {
    return VALUES[this.oppositeIndex];
  }

  public Axis getAxis() {
    return this.axis;
  }

  public AxisDirection getAxisDirection() {
    return this.axisDirection;
  }

  public Vec3 step() {
    return this.normal;
  }

  public int getStepX() {
    return this.axis == Axis.X ? this.axisDirection.getStep() : 0;
  }

  public int getStepY() {
    return this.axis == Axis.Y ? this.axisDirection.getStep() : 0;
  }

  public int getStepZ() {
    return this.axis == Axis.Z ? this.axisDirection.getStep() : 0;
  }

  public String getName() {
    return this.name;
  }

  public enum Axis {
    X("x") {
      @Override
      public int choose(int x, int y, int z) {
        return x;
      }

      @Override
      public double choose(double x, double y, double z) {
        return x;
      }
    },
    Y("y") {
      @Override
      public int choose(int x, int y, int z) {
        return y;
      }

      @Override
      public double choose(double x, double y, double z) {
        return y;
      }
    },
    Z("z") {
      @Override
      public int choose(int x, int y, int z) {
        return z;
      }

      @Override
      public double choose(double x, double y, double z) {
        return z;
      }
    };

    public static final Axis[] VALUES = values();
    private final String name;

    Axis(String name) {
      this.name = name;
    }

    public abstract int choose(int x, int y, int z);

    public abstract double choose(double x, double y, double z);

    public String getName() {
      return this.name;
    }
  }

  public enum AxisDirection {
    POSITIVE(1, "Towards positive"),
    NEGATIVE(-1, "Towards negative");

    private final int step;
    private final String description;

    AxisDirection(int step, String description) {
      this.step = step;
      this.description = description;
    }

    public int getStep() {
      return this.step;
    }

    public String toString() {
      return this.description;
    }
  }
}
