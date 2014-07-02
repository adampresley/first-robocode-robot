package com.adampresley;

import robocode.*;

public class Enemy {
	private double _bearing;
	private double _distance;
	private double _energy;
	private double _heading;
	private String _name;
	private double _velocity;
	private double _x;
	private double _y;
	private ScannedRobotEvent _e;

	public Enemy() {
		reset();
	}

	public Enemy(ScannedRobotEvent e) {
		update(e, null);
	}

	public double getBearing() { return _bearing; }
	public double getDistance() { return _distance; }
	public double getEnergy() { return _energy; }
	public double getHeading() { return _heading; }
	public String getName() { return _name; }
	public double getVelocity() { return _velocity; }
	public double getX() { return _x; }
	public double getY() { return _y; }
	public ScannedRobotEvent getScannedRobotEvent() { return _e; }

	public double getFutureX(long when) {
		return _x + Math.sin(Math.toRadians(getHeading())) * getVelocity() * when;
	}

	public double getFutureY(long when) {
		return _y + Math.cos(Math.toRadians(getHeading())) * getVelocity() * when;
	}

	public void update(ScannedRobotEvent e) {
		update(e, null);
	}

	public void update(ScannedRobotEvent e, Robot robot) {
		_bearing = e.getBearing();
		_distance = e.getDistance();
		_energy = e.getEnergy();
		_heading = e.getHeading();
		_name = e.getName();
		_velocity = e.getVelocity();
		_e = e;

		if (robot != null) {
			double absBearingDegrees = (robot.getHeading() + e.getBearing());
			if (absBearingDegrees < 0) absBearingDegrees += 360;

			_x = robot.getX() + Math.sin(Math.toRadians(absBearingDegrees)) * e.getDistance();
			_y = robot.getY() + Math.cos(Math.toRadians(absBearingDegrees)) * e.getDistance();
		}
	}

	public void reset() {
		_bearing = 0.0;
		_distance = 0.0;
		_energy = 0.0;
		_heading = 0.0;
		_name = "";
		_velocity = 0.0;
		_x = 0.0;
		_y = 0.0;
	}

	public boolean none() {
		return (_name.equals(""));
	}

	@Override
	public boolean equals(Object o) {
		return ((Enemy) o).getName().equals(getName());
	}
}