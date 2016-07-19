package net.netcoding.nifty.core.util.misc;

import net.netcoding.nifty.core.util.NumberUtil;
import net.netcoding.nifty.core.util.StringUtil;
import net.netcoding.nifty.core.util.concurrent.Concurrent;
import net.netcoding.nifty.core.util.concurrent.linked.ConcurrentLinkedMap;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class Vector implements Cloneable, Serializable {

	private static final double EPSILON = 1.0E-6D;
	protected double x;
	protected double y;
	protected double z;

	public Vector() {
		this(0, 0, 0);
	}

	public Vector(int x, int y, int z) {
		this((double)x, y, z);
	}

	public Vector(float x, float y, float z) {
		this((double)x, y, z);
	}

	public Vector(double x, double y, double z) {
		this.setX(x);
		this.setY(y);
		this.setZ(z);
	}

	public final Vector add(Vector vector) {
		this.setX(this.getX() + vector.getX());
		this.setY(this.getY() + vector.getY());
		this.setZ(this.getZ() + vector.getZ());
		return this;
	}

	public final float angle(Vector other) {
		double dot = this.dot(other) / (this.length() * other.length());
		return (float) Math.acos(dot);
	}

	@Override
	public final Vector clone() {
		try {
			return (Vector)super.clone();
		} catch (CloneNotSupportedException cnsex) {
			return new Vector(this.getX(), this.getY(), this.getZ());
		}
	}

	public final Vector copy(Vector vector) {
		this.setX(vector.getX());
		this.setY(vector.getY());
		this.setZ(vector.getZ());
		return this;
	}

	public final Vector crossProduct(Vector vector) {
		double newX = this.getX() * vector.getZ() - vector.getY() * this.getZ();
		double newY = this.getZ() * vector.getX() - vector.getZ() * this.getX();
		double newZ = this.getX() * vector.getY() - vector.getX() * this.getY();
		this.setX(newX);
		this.setY(newY);
		this.setZ(newZ);
		return this;
	}

	public static Vector deserialize(Map<String, Object> args) {
		double x = 0.0D;
		double y = 0.0D;
		double z = 0.0D;

		if (args.containsKey("x"))
			x = NumberUtil.to(args.get("x"), Double.class);

		if (args.containsKey("y"))
			y = NumberUtil.to(args.get("y"), Double.class);

		if (args.containsKey("z"))
			z = NumberUtil.to(args.get("z"), Double.class);

		return new Vector(x, y, z);
	}

	public final double distance(Vector vector) {
		return Math.sqrt(NumberUtil.square(this.getX() - vector.getX()) + NumberUtil.square(this.getY() - vector.getY()) + NumberUtil.square(this.getZ() - vector.getZ()));
	}

	public final double distanceSquared(Vector vector) {
		return NumberUtil.square(this.getX() - vector.getX()) + NumberUtil.square(this.getY() - vector.getY()) + NumberUtil.square(this.getZ() - vector.getZ());
	}

	public final Vector divide(Vector vector) {
		this.setX(this.getX() / vector.getX());
		this.setY(this.getY() / vector.getY());
		this.setZ(this.getZ() / vector.getZ());
		return this;
	}

	public final double dot(Vector vector) {
		return this.getX() * vector.getX() + this.getY() * vector.getY() + this.getZ() * vector.getZ();
	}

	@Override
	public final boolean equals(Object obj) {
		if (!(obj instanceof Vector))
			return false;
		else {
			Vector other = (Vector) obj;
			return Math.abs(this.getX() - other.getX()) < 1.0E-6D && Math.abs(this.getY() - other.getY()) < 1.0E-6D && Math.abs(this.getZ() - other.getZ()) < 1.0E-6D;
		}
	}

	public final Vector getCrossProduct(Vector vector) {
		double x = this.getY() * vector.getZ() - vector.getY() * this.getZ();
		double y = this.getZ() * vector.getX() - vector.getZ() * this.getX();
		double z = this.getX() * vector.getY() - vector.getX() * this.getY();
		return new Vector(x, y, z);
	}

	public final Vector getMidpoint(Vector vector) {
		double x = (this.getX() + vector.getX()) / 2.0D;
		double y = (this.getY() + vector.getY()) / 2.0D;
		double z = (this.getZ() + vector.getZ()) / 2.0D;
		return new Vector(x, y, z);
	}

	public final int getBlockX() {
		return NumberUtil.floor(this.getX());
	}

	public final int getBlockY() {
		return NumberUtil.floor(this.getY());
	}

	public final int getBlockZ() {
		return NumberUtil.floor(this.getZ());
	}

	public static double getEpsilon() {
		return EPSILON;
	}

	public static Vector getMaximum(Vector v1, Vector v2) {
		return new Vector(Math.max(v1.x, v2.x), Math.max(v1.y, v2.y), Math.max(v1.z, v2.z));
	}

	public static Vector getMinimum(Vector v1, Vector v2) {
		return new Vector(Math.min(v1.x, v2.x), Math.min(v1.y, v2.y), Math.min(v1.z, v2.z));
	}

	public static Vector getRandom() {
		return new Vector(ThreadLocalRandom.current().nextDouble(), ThreadLocalRandom.current().nextDouble(), ThreadLocalRandom.current().nextDouble());
	}

	public double getX() {
		return this.x;
	}

	public double getY() {
		return this.y;
	}

	public double getZ() {
		return this.z;
	}

	@Override
	public final int hashCode() {
		int result = 79 * 7 + (int) (Double.doubleToLongBits(this.getX()) ^ Double.doubleToLongBits(this.getX()) >>> 32);
		result = 79 * result + (int) (Double.doubleToLongBits(this.getY()) ^ Double.doubleToLongBits(this.getY()) >>> 32);
		result = 79 * result + (int) (Double.doubleToLongBits(this.getZ()) ^ Double.doubleToLongBits(this.getZ()) >>> 32);
		return result;
	}

	public final boolean isInAABB(Vector min, Vector max) {
		return this.getX() >= min.getX() && this.getX() <= max.getX() && this.getY() >= min.getY() && this.getY() <= max.getY() && this.getZ() >= min.getZ() && this.getZ() <= max.getZ();
	}

	public final boolean isInSphere(Vector origin, double radius) {
		return NumberUtil.square(origin.getX() - this.getX()) + NumberUtil.square(origin.getY() - this.getY()) + NumberUtil.square(origin.getZ() - this.getZ()) <= NumberUtil.square(radius);
	}

	public final double length() {
		return Math.sqrt(NumberUtil.square(this.getX()) + NumberUtil.square(this.getY()) + NumberUtil.square(this.getZ()));
	}

	public final double lengthSquared() {
		return NumberUtil.square(this.getX()) + NumberUtil.square(this.getY()) + NumberUtil.square(this.getZ());
	}

	public final Vector midpoint(Vector other) {
		this.setX((this.getX() + other.getX()) / 2.0D);
		this.setY((this.getY() + other.getY()) / 2.0D);
		this.setZ((this.getZ() + other.getZ()) / 2.0D);
		return this;
	}

	public final Vector multiply(int m) {
		return this.multiply((double)m);
	}

	public final Vector multiply(float m) {
		return this.multiply((double)m);
	}

	public final Vector multiply(double m) {
		this.setX(this.getX() * m);
		this.setY(this.getY() * m);
		this.setZ(this.getZ() * m);
		return this;
	}

	public final Vector multiply(Vector vector) {
		this.setX(this.getX() * vector.getX());
		this.setY(this.getY() * vector.getY());
		this.setZ(this.getZ() * vector.getZ());
		return this;
	}

	public final Vector subtract(Vector vector) {
		this.setX(this.getX() - vector.getX());
		this.setY(this.getY() - vector.getY());
		this.setZ(this.getZ() - vector.getZ());
		return this;
	}

	public final Vector normalize() {
		double length = this.length();
		this.setX(this.getX() / length);
		this.setY(this.getY() / length);
		this.setZ(this.getZ() / length);
		return this;
	}

	@Override
	public final Map<String, Object> serialize() {
		ConcurrentLinkedMap<String, Object> result = Concurrent.newLinkedMap();
		result.put("x", this.getX());
		result.put("y", this.getY());
		result.put("z", this.getZ());
		return result;
	}

	public final Vector setX(int x) {
		return this.setX((double)x);
	}

	public final Vector setX(float x) {
		return this.setX((double)x);
	}

	public Vector setX(double x) {
		this.x = x;
		return this;
	}

	public final Vector setY(int y) {
		return this.setY((double)y);
	}

	public final Vector setY(float y) {
		return this.setY((double)y);
	}

	public Vector setY(double y) {
		this.y = y;
		return this;
	}

	public final Vector setZ(int z) {
		return this.setZ((double)z);
	}

	public final Vector setZ(float z) {
		return this.setZ((double)z);
	}

	public Vector setZ(double z) {
		this.z = z;
		return this;
	}

	public final String toString() {
		return StringUtil.format("'{'{0},{1},{2}'}'", this.getX(), this.getY(), this.getZ());
	}

	public final Vector zero() {
		this.setX(0);
		this.setY(0);
		this.setZ(0);
		return this;
	}

}