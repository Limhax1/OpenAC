package me.limhax.openAC.util;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.particle.Particle;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerParticle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.limhax.openAC.data.PlayerData;

@Getter
@AllArgsConstructor
public class AABB {
  private static final double PRECISION_THRESHOLD = 1.0E-7;
  public double minX;
  public double minY;
  public double minZ;
  public double maxX;
  public double maxY;
  public double maxZ;

  public AABB() {
    this(0, 0, 0);
  }

  public AABB(double x, double y, double z) {
    this.set(x, y, z);
  }

  public AABB(AABB other) {
    this.minX = other.minX;
    this.minY = other.minY;
    this.minZ = other.minZ;
    this.maxX = other.maxX;
    this.maxY = other.maxY;
    this.maxZ = other.maxZ;
  }

  public static double getPrecisionThreshold() {
    return PRECISION_THRESHOLD;
  }

  public AABB contain(AABB other) {
    return this.contain(other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ);
  }

  public AABB contain(double x0, double y0, double z0, double x1, double y1, double z1) {
    this.minX = Math.min(x0, this.minX);
    this.minY = Math.min(y0, this.minY);
    this.minZ = Math.min(z0, this.minZ);
    this.maxX = Math.max(x1, this.maxX);
    this.maxY = Math.max(y1, this.maxY);
    this.maxZ = Math.max(z1, this.maxZ);
    return this;
  }

  public AABB contain(double x, double y, double z) {
    this.contain(x, y, z, x, y, z);
    return this;
  }

  public AABB set(AABB other) {
    this.minX = other.minX;
    this.minY = other.minY;
    this.minZ = other.minZ;
    this.maxX = other.maxX;
    this.maxY = other.maxY;
    this.maxZ = other.maxZ;
    return this;
  }

  public AABB set(double x, double y, double z) {
    this.minX = x;
    this.minY = y;
    this.minZ = z;

    this.maxX = x;
    this.maxY = y;
    this.maxZ = z;

    return this;
  }

  public AABB expand(double x, double y, double z) {
    this.add(-x, -y, -z, x, y, z);
    return this;
  }

  public AABB expand(double amount) {
    this.add(-amount, -amount, -amount, amount, amount, amount);
    return this;
  }

  public void add(double x, double y, double z) {
    this.add(x
        , y, z, x, y, z);
  }

  public void add(double x0, double y0, double z0, double x1, double y1, double z1) {
    this.minX += x0;
    this.minY += y0;
    this.minZ += z0;

    this.maxX += x1;
    this.maxY += y1;
    this.maxZ += z1;
  }

  public AABB addCoord(double x, double y, double z) {
    double x0 = x > 0 ? 0 : x;
    double y0 = y > 0 ? 0 : y;
    double z0 = z > 0 ? 0 : z;

    double x1 = x > 0 ? x : 0;
    double y1 = y > 0 ? y : 0;
    double z1 = z > 0 ? z : 0;

    this.add(x0, y0, z0, x1, y1, z1);
    return this;
  }

  public AABB interpolate(AABB destination, int interpolation) {
    this.minX = this.interpolate(this.minX, destination.minX, interpolation);
    this.maxX = this.interpolate(this.maxX, destination.maxX, interpolation);
    this.minY = this.interpolate(this.minY, destination.minY, interpolation);
    this.maxY = this.interpolate(this.maxY, destination.maxY, interpolation);
    this.minZ = this.interpolate(this.minZ, destination.minZ, interpolation);
    this.maxZ = this.interpolate(this.maxZ, destination.maxZ, interpolation);
    return this;
  }

  public AABB inner(AABB other) {
    this.minX = Math.max(other.minX, this.minX);
    this.minY = Math.max(other.minY, this.minY);
    this.minZ = Math.max(other.minZ, this.minZ);
    this.maxX = Math.min(other.maxX, this.maxX);
    this.maxY = Math.min(other.maxY, this.maxY);
    this.maxZ = Math.min(other.maxZ, this.maxZ);
    return this;
  }

  public double distanceX(double x) {
    return x >= this.minX && x <= this.maxX ? 0.0 : Math.min(Math.abs(x - this.minX), Math.abs(x - this.maxX));
  }

  public double distanceY(double y) {
    return y >= this.minY && y <= this.maxY ? 0.0 : Math.min(Math.abs(y - this.minY), Math.abs(y - this.maxY));
  }

  public double distanceZ(double z) {
    return z >= this.minZ && z <= this.maxZ ? 0.0 : Math.min(Math.abs(z - this.minZ), Math.abs(z - this.maxZ));
  }

  public boolean isInside(double x, double y, double z) {
    return x > this.minX && x < this.maxX && y > this.minY && y < this.maxY && z > this.minZ && z < maxZ;
  }

  public AABB expandTowards(double x, double y, double z) {
    double d0 = this.minX;
    double d1 = this.minY;
    double d2 = this.minZ;
    double d3 = this.maxX;
    double d4 = this.maxY;
    double d5 = this.maxZ;
    if (x < 0.0) {
      d0 += x;
    }
    if (x > 0.0) {
      d3 += x;
    }
    if (y < 0.0) {
      d1 += y;
    }
    if (y > 0.0) {
      d4 += y;
    }
    if (z < 0.0) {
      d2 += z;
    }
    if (z > 0.0) {
      d5 += z;
    }
    return new AABB(d0, d1, d2, d3, d4, d5);
  }

  public AABB move(double x, double y, double z) {
    return new AABB(this.minX + x, this.minY + y, this.minZ + z, this.maxX + x, this.maxY + y, this.maxZ + z);
  }

  public boolean intersects(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
    return this.minX <= maxX && this.maxX >= minX && this.minY <= maxY && this.maxY >= minY && this.minZ <= maxZ && this.maxZ >= minZ;
  }

  public boolean intersects(AABB other) {
    return this.intersects(other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ);
  }

  public AABB inflate(double x, double y, double z) {
    return new AABB(this.minX - x, this.minY - y, this.minZ - z, this.maxX + x, this.maxY + y, this.maxZ + z);
  }

  public AABB inflate(double value) {
    return this.inflate(value, value, value);
  }

  public double getXsize() {
    return this.maxX - this.minX;
  }

  public double getYsize() {
    return this.maxY - this.minY;
  }

  public double getZsize() {
    return this.maxZ - this.minZ;
  }

  public double collideX(AABB other, double movement) {
    if (movement == 0.0) return movement;

    if ((other.minY - this.maxY) >= -PRECISION_THRESHOLD || (other.maxY - this.minY) <= PRECISION_THRESHOLD)
      return movement;
    if ((other.minZ - this.maxZ) >= -PRECISION_THRESHOLD || (other.maxZ - this.minZ) <= PRECISION_THRESHOLD)
      return movement;

    if (movement > 0.0) {
      double distance = this.minX - other.maxX;
      if (distance < -PRECISION_THRESHOLD) return movement;
      return Math.min(distance, movement);
    } else {
      double distance = this.maxX - other.minX;
      if (distance > PRECISION_THRESHOLD) return movement;
      return Math.max(distance, movement);
    }
  }

  public double collideY(AABB other, double movement) {
    if (movement == 0.0) return movement;

    if ((other.minX - this.maxX) >= -PRECISION_THRESHOLD || (other.maxX - this.minX) <= PRECISION_THRESHOLD)
      return movement;
    if ((other.minZ - this.maxZ) >= -PRECISION_THRESHOLD || (other.maxZ - this.minZ) <= PRECISION_THRESHOLD)
      return movement;

    if (movement > 0.0) {
      double distance = this.minY - other.maxY;
      if (distance < -PRECISION_THRESHOLD) return movement;
      return Math.min(distance, movement);
    } else {
      double distance = this.maxY - other.minY;
      if (distance > PRECISION_THRESHOLD) return movement;
      return Math.max(distance, movement);
    }
  }

  public double collideZ(AABB other, double movement) {
    if (movement == 0.0) return movement;

    if ((other.minX - this.maxX) >= -PRECISION_THRESHOLD || (other.maxX - this.minX) <= PRECISION_THRESHOLD)
      return movement;
    if ((other.minY - this.maxY) >= -PRECISION_THRESHOLD || (other.maxY - this.minY) <= PRECISION_THRESHOLD)
      return movement;

    if (movement > 0.0) {
      double distance = this.minZ - other.maxZ;
      if (distance < -PRECISION_THRESHOLD) return movement;
      return Math.min(distance, movement);
    } else {
      double distance = this.maxZ - other.minZ;
      if (distance > PRECISION_THRESHOLD) return movement;
      return Math.max(distance, movement);
    }
  }

  private double interpolate(double value, double destination, int interpolation) {
    return value + (destination - value) / (double) interpolation;
  }

  public AABB copy() {
    return new AABB(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
  }

  public AABB union(AABB other) {
    return new AABB(
        Math.min(this.minX, other.minX),
        Math.min(this.minY, other.minY),
        Math.min(this.minZ, other.minZ),
        Math.max(this.maxX, other.maxX),
        Math.max(this.maxY, other.maxY),
        Math.max(this.maxZ, other.maxZ)
    );
  }

  public void draw(PlayerData data) {
    double step = 0.3;

    drawLine(this.minX, this.minY, this.minZ, this.maxX, this.minY, this.minZ, step, data);
    drawLine(this.minX, this.minY, this.maxZ, this.maxX, this.minY, this.maxZ, step, data);
    drawLine(this.minX, this.minY, this.minZ, this.minX, this.minY, this.maxZ, step, data);
    drawLine(this.maxX, this.minY, this.minZ, this.maxX, this.minY, this.maxZ, step, data);

    drawLine(this.minX, this.maxY, this.minZ, this.maxX, this.maxY, this.minZ, step, data);
    drawLine(this.minX, this.maxY, this.maxZ, this.maxX, this.maxY, this.maxZ, step, data);
    drawLine(this.minX, this.maxY, this.minZ, this.minX, this.maxY, this.maxZ, step, data);
    drawLine(this.maxX, this.maxY, this.minZ, this.maxX, this.maxY, this.maxZ, step, data);

    drawLine(this.minX, this.minY, this.minZ, this.minX, this.maxY, this.minZ, step, data);
    drawLine(this.maxX, this.minY, this.minZ, this.maxX, this.maxY, this.minZ, step, data);
    drawLine(this.minX, this.minY, this.maxZ, this.minX, this.maxY, this.maxZ, step, data);
    drawLine(this.maxX, this.minY, this.maxZ, this.maxX, this.maxY, this.maxZ, step, data);
  }

  private void drawLine(double x1, double y1, double z1, double x2, double y2, double z2, double step, PlayerData data) {
    double dx = x2 - x1;
    double dy = y2 - y1;
    double dz = z2 - z1;
    double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
    int points = (int) (distance / step);

    for (int i = 0; i <= points; i++) {
      double t = points == 0 ? 0 : (double) i / points;
      double x = x1 + dx * t;
      double y = y1 + dy * t;
      double z = z1 + dz * t;

      spawnParticle(x, y, z, data);
    }
  }

  private void spawnParticle(double x, double y, double z, PlayerData data) {
    WrapperPlayServerParticle packet = new WrapperPlayServerParticle(
        new Particle(ParticleTypes.FLAME),
        true,
        new com.github.retrooper.packetevents.util.Vector3d(x, y, z),
        new Vector3f(0, 0, 0),
        0.0f,
        1
    );

    PacketEvents.getAPI().getPlayerManager().getUser(data.getPlayer()).sendPacket(packet);
  }

  @Override
  public String toString() {
    return "AABB[" + minX + ", " + minY + ", " + minZ + " -> " + maxX + ", " + maxY + ", " + maxZ + "]";
  }
}
