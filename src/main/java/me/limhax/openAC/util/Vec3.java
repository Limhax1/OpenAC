package me.limhax.openAC.util;

import lombok.Getter;
import org.joml.Vector3f;
@Getter
public class Vec3 {
  public static final Vec3 ZERO = new Vec3(0.0, 0.0, 0.0);
  public double x;
  public double y;
  public double z;

  public Vec3(double x, double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public Vec3(Vector3f p_253821_) {
    this(p_253821_.x(), p_253821_.y(), p_253821_.z());
  }

  public static Vec3 fromRGB24(int p_82502_) {
    double d0 = (double) (p_82502_ >> 16 & 0xFF) / 255.0;
    double d1 = (double) (p_82502_ >> 8 & 0xFF) / 255.0;
    double d2 = (double) (p_82502_ & 0xFF) / 255.0;
    return new Vec3(d0, d1, d2);
  }

  public Vec3 vectorTo(Vec3 p_82506_) {
    return new Vec3(p_82506_.x - this.x, p_82506_.y - this.y, p_82506_.z - this.z);
  }

  public Vec3 normalize() {
    double d0 = Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    return d0 < 1.0E-5F ? ZERO : new Vec3(this.x / d0, this.y / d0, this.z / d0);
  }

  public double dot(Vec3 p_82527_) {
    return this.x * p_82527_.x + this.y * p_82527_.y + this.z * p_82527_.z;
  }

  public Vec3 cross(Vec3 p_82538_) {
    return new Vec3(
        this.y * p_82538_.z - this.z * p_82538_.y,
        this.z * p_82538_.x - this.x * p_82538_.z,
        this.x * p_82538_.y - this.y * p_82538_.x
    );
  }

  public Vec3 subtract(Vec3 p_82547_) {
    return this.subtract(p_82547_.x, p_82547_.y, p_82547_.z);
  }

  public Vec3 subtract(double p_365229_) {
    return this.subtract(p_365229_, p_365229_, p_365229_);
  }

  public Vec3 subtract(double x, double y, double z) {
    return this.add(-x, -y, -z);
  }

  public Vec3 add(double other) {
    return this.add(other, other, other);
  }

  public Vec3 add(Vec3 other) {
    return this.add(other.x, other.y, other.z);
  }

  public Vec3 add(double x, double y, double z) {
    return new Vec3(this.x + x, this.y + y, this.z + z);
  }

  public double distanceTo(Vec3 other) {
    double d0 = other.x - this.x;
    double d1 = other.y - this.y;
    double d2 = other.z - this.z;
    return Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
  }

  public double distanceToSqr(Vec3 other) {
    double d0 = other.x - this.x;
    double d1 = other.y - this.y;
    double d2 = other.z - this.z;
    return d0 * d0 + d1 * d1 + d2 * d2;
  }

  public double distanceToSqr(double p_82532_, double p_82533_, double p_82534_) {
    double d0 = p_82532_ - this.x;
    double d1 = p_82533_ - this.y;
    double d2 = p_82534_ - this.z;
    return d0 * d0 + d1 * d1 + d2 * d2;
  }

  public Vec3 scale(double p_82491_) {
    return this.multiply(p_82491_, p_82491_, p_82491_);
  }

  public Vec3 reverse() {
    return this.scale(-1.0);
  }

  public Vec3 multiply(Vec3 p_82560_) {
    return this.multiply(p_82560_.x, p_82560_.y, p_82560_.z);
  }

  public Vec3 multiply(double x, double y, double z) {
    return new Vec3(this.x * x, this.y * y, this.z * z);
  }

  public Vec3 horizontal() {
    return new Vec3(this.x, 0.0, this.z);
  }

  public double length() {
    return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
  }

  public double lengthSqr() {
    return this.x * this.x + this.y * this.y + this.z * this.z;
  }

  public double horizontalDistance() {
    return Math.sqrt(this.x * this.x + this.z * this.z);
  }

  public double horizontalDistanceSqr() {
    return this.x * this.x + this.z * this.z;
  }

  @Override
  public boolean equals(Object p_82552_) {
    if (this == p_82552_) {
      return true;
    } else if (!(p_82552_ instanceof Vec3 vec3)) {
      return false;
    } else if (Double.compare(vec3.x, this.x) != 0) {
      return false;
    } else {
      return Double.compare(vec3.y, this.y) == 0 && Double.compare(vec3.z, this.z) == 0;
    }
  }

  @Override
  public int hashCode() {
    long j = Double.doubleToLongBits(this.x);
    int i = (int) (j ^ j >>> 32);
    j = Double.doubleToLongBits(this.y);
    i = 31 * i + (int) (j ^ j >>> 32);
    j = Double.doubleToLongBits(this.z);
    return 31 * i + (int) (j ^ j >>> 32);
  }

  @Override
  public String toString() {
    return "(" + this.x + ", " + this.y + ", " + this.z + ")";
  }

  public final double x() {
    return this.x;
  }

  public final double y() {
    return this.y;
  }

  public final double z() {
    return this.z;
  }

  public Vector3f toVector3f() {
    return new Vector3f((float) this.x, (float) this.y, (float) this.z);
  }

  public Vec3 projectedOn(Vec3 p_368324_) {
    return p_368324_.lengthSqr() == 0.0 ? p_368324_ : p_368324_.scale(this.dot(p_368324_)).scale(1.0 / p_368324_.lengthSqr());
  }
  public double get(Direction.Axis axis) {
    return axis.choose(this.x, this.y, this.z);
  }

  public Vec3 with(Direction.Axis axis, double value) {
    return new Vec3(
        axis == Direction.Axis.X ? value : this.x,
        axis == Direction.Axis.Y ? value : this.y,
        axis == Direction.Axis.Z ? value : this.z
    );
  }
}