package net.netcoding.nifty.core.util.misc;

import net.netcoding.nifty.core.util.NumberUtil;
import net.netcoding.nifty.core.util.StringUtil;
import net.netcoding.nifty.core.util.concurrent.linked.ConcurrentLinkedMap;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class Vector implements Cloneable, Serializable {

	private static final double EPSILON = 1.0E-6D;
	protected double x;
	protected double y;
	protected double z;

	public Vector() {
		this.x = 0.0D;
		this.y = 0.0D;
		this.z = 0.0D;
	}

	public Vector(int x, int y, int z) {
		this.x = (double)x;
		this.y = (double)y;
		this.z = (double)z;
	}

	public Vector(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector(float x, float y, float z) {
		this.x = (double)x;
		this.y = (double)y;
		this.z = (double)z;
	}

	public Vector add(Vector vec) {
		this.x += vec.x;
		this.y += vec.y;
		this.z += vec.z;
		return this;
	}

	public float angle(Vector other) {
		double dot = this.dot(other) / (this.length() * other.length());
		return (float) Math.acos(dot);
	}

	@SuppressWarnings("CloneDoesntCallSuperClone")
	public Vector clone() {
		return new Vector(this.x, this.y, this.z);
	}

	public Vector copy(Vector vec) {
		this.x = vec.x;
		this.y = vec.y;
		this.z = vec.z;
		return this;
	}

	public Vector crossProduct(Vector o) {
		double newX = this.y * o.z - o.y * this.z;
		double newY = this.z * o.x - o.z * this.x;
		double newZ = this.x * o.y - o.x * this.y;
		this.x = newX;
		this.y = newY;
		this.z = newZ;
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

	public double distance(Vector o) {
		return Math.sqrt(NumberUtil.square(this.x - o.x) + NumberUtil.square(this.y - o.y) + NumberUtil.square(this.z - o.z));
	}

	public double distanceSquared(Vector o) {
		return NumberUtil.square(this.x - o.x) + NumberUtil.square(this.y - o.y) + NumberUtil.square(this.z - o.z);
	}

	public Vector divide(Vector vec) {
		this.x /= vec.x;
		this.y /= vec.y;
		this.z /= vec.z;
		return this;
	}

	public double dot(Vector other) {
		return this.x * other.x + this.y * other.y + this.z * other.z;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Vector))
			return false;
		else {
			Vector other = (Vector) obj;
			return Math.abs(this.x - other.x) < 1.0E-6D && Math.abs(this.y - other.y) < 1.0E-6D && Math.abs(this.z - other.z) < 1.0E-6D && this.getClass().equals(obj.getClass());
		}
	}

	public Vector getCrossProduct(Vector o) {
		double x = this.y * o.z - o.y * this.z;
		double y = this.z * o.x - o.z * this.x;
		double z = this.x * o.y - o.x * this.y;
		return new Vector(x, y, z);
	}

	public Vector getMidpoint(Vector other) {
		double x = (this.x + other.x) / 2.0D;
		double y = (this.y + other.y) / 2.0D;
		double z = (this.z + other.z) / 2.0D;
		return new Vector(x, y, z);
	}

	public int getBlockX() {
		return NumberUtil.floor(this.x);
	}

	public int getBlockY() {
		return NumberUtil.floor(this.y);
	}

	public int getBlockZ() {
		return NumberUtil.floor(this.z);
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
	public int hashCode() {
		byte hash = 7;
		int hash1 = 79 * hash + (int) (Double.doubleToLongBits(this.x) ^ Double.doubleToLongBits(this.x) >>> 32);
		hash1 = 79 * hash1 + (int) (Double.doubleToLongBits(this.y) ^ Double.doubleToLongBits(this.y) >>> 32);
		hash1 = 79 * hash1 + (int) (Double.doubleToLongBits(this.z) ^ Double.doubleToLongBits(this.z) >>> 32);
		return hash1;
	}

	public boolean isInAABB(Vector min, Vector max) {
		return this.x >= min.x && this.x <= max.x && this.y >= min.y && this.y <= max.y && this.z >= min.z && this.z <= max.z;
	}

	public boolean isInSphere(Vector origin, double radius) {
		return NumberUtil.square(origin.x - this.x) + NumberUtil.square(origin.y - this.y) + NumberUtil.square(origin.z - this.z) <= NumberUtil.square(radius);
	}

	public double length() {
		return Math.sqrt(NumberUtil.square(this.x) + NumberUtil.square(this.y) + NumberUtil.square(this.z));
	}

	public double lengthSquared() {
		return NumberUtil.square(this.x) + NumberUtil.square(this.y) + NumberUtil.square(this.z);
	}

	public Vector midpoint(Vector other) {
		this.x = (this.x + other.x) / 2.0D;
		this.y = (this.y + other.y) / 2.0D;
		this.z = (this.z + other.z) / 2.0D;
		return this;
	}

	public Vector multiply(int m) {
		this.x *= (double)m;
		this.y *= (double)m;
		this.z *= (double)m;
		return this;
	}

	public Vector multiply(double m) {
		this.x *= m;
		this.y *= m;
		this.z *= m;
		return this;
	}

	public Vector multiply(float m) {
		this.x *= (double)m;
		this.y *= (double)m;
		this.z *= (double)m;
		return this;
	}

	public Vector multiply(Vector vec) {
		this.x *= vec.x;
		this.y *= vec.y;
		this.z *= vec.z;
		return this;
	}

	public Vector subtract(Vector vec) {
		this.x -= vec.x;
		this.y -= vec.y;
		this.z -= vec.z;
		return this;
	}

	public Vector normalize() {
		double length = this.length();
		this.x /= length;
		this.y /= length;
		this.z /= length;
		return this;
	}

	@Override
	public Map<String, Object> serialize() {
		ConcurrentLinkedMap<String, Object> result = new ConcurrentLinkedMap<>();
		result.put("x", this.getX());
		result.put("y", this.getY());
		result.put("z", this.getZ());
		return result;
	}

	public Vector setX(int x) {
		this.x = (double) x;
		return this;
	}

	public Vector setX(double x) {
		this.x = x;
		return this;
	}

	public Vector setX(float x) {
		this.x = (double) x;
		return this;
	}

	public Vector setY(int y) {
		this.y = (double) y;
		return this;
	}

	public Vector setY(double y) {
		this.y = y;
		return this;
	}

	public Vector setY(float y) {
		this.y = (double) y;
		return this;
	}

	public Vector setZ(int z) {
		this.z = (double) z;
		return this;
	}

	public Vector setZ(double z) {
		this.z = z;
		return this;
	}

	public Vector setZ(float z) {
		this.z = (double) z;
		return this;
	}

	public String toString() {
		return StringUtil.format("'{'{0},{1},{2}'}'", this.x, this.y, this.z);
	}

	public Vector zero() {
		this.x = 0.0D;
		this.y = 0.0D;
		this.z = 0.0D;
		return this;
	}

}