package com.adampresley;

import robocode.*;
import java.awt.Color;
import java.awt.geom.Point2D;
import com.adampresley.Enemy;
import com.adampresley.IntelOfficer;

public class AdamP extends AdvancedRobot {
	private final int WALL_MARGIN = 65;

	private Enemy enemy = new Enemy();
	private IntelOfficer intel = new IntelOfficer();
	private int moveDirection = 1;
	private int radarDirection = 1;
	private int closeToWall = 0;

	public void run() {
		addCustomEvent(new Condition("closeToWall") {
			public boolean test() {
				return (
					getX() <= WALL_MARGIN ||
					getX() >= getBattleFieldWidth() - WALL_MARGIN ||
					getY() <= WALL_MARGIN ||
					getY() >= getBattleFieldHeight() - WALL_MARGIN
				);
			}
		});

		colorMe();

		setAdjustRadarForGunTurn(true);
		setAdjustGunForRobotTurn(true);
		enemy.reset();

		setTurnRadarRight(360);

		while (true) {
			updateRadar();
			updateGun();
			updateMovement();
			execute();
		}
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		Enemy scannedTarget = new Enemy(e);

		/*
		 * Is Intel tracking this target yet? Register it if not,
		 * and get an update on its status if so.
		 */
		if (!intel.isRegisteredTarget(scannedTarget.getName())) {
			intel.registerTarget(scannedTarget);
		} else {
			intel.updateRegistrationRecord(scannedTarget);
		}

		/*
		 * If we have no enemy currently attacking start with
		 * this one we have scanned. If we do, ask Intel if
		 * there is a potentially weaker enemy we can fire on
		 * to get the kill bonus.
		 */
		if (enemy.none()) {
			enemy.update(e, this);
			System.out.println("Tracking " + enemy.getName());
		} else {
			enemy.update(e, this);

			Enemy potential = intel.searchForWeakerTarget(enemy);
			if (potential != null) enemy.update(potential.getScannedRobotEvent(), this);
		}
	}

	public void onCustomEvent(CustomEvent e) {
		if (e.getCondition().getName().equals("closeToWall")) {
			if (closeToWall <= 0) {
				closeToWall += WALL_MARGIN;
				setMaxVelocity(0);
			}
		}
	}

	public void onHitRobot(HitRobotEvent e) {
		closeToWall = 0;
		setTurnRight(e.getBearing() + 90);

		/*
		 * If the the robot we collided with is hitting
		 * me on purpose target them.
		 */
		if (!e.isMyFault()) {
			Enemy agressor = intel.getTargetByName(e.getName());

			if (agressor != null) {
				enemy.update(agressor.getScannedRobotEvent(), this);
				System.out.println(e.getName() + " hit me! Targeting");
			}
		}
	}

	public void onHitByBullet(HitByBulletEvent e) {
	}

	public void onHitWall(HitWallEvent e) {
		System.out.println("Hit a wall.");
	}

	public void onRobotDeath(RobotDeathEvent e) {
		/*
		 * If this is the robot we were tracking
		 * reset the tracking variable. Also spin
		 * radar just to be sure, and remove this
		 * robot from Intel tracking.
		 */
		intel.recordTargetDeath(e.getName());
		System.out.println("Removed " + e.getName() + " from potential targets");

		if (e.getName().equals(enemy.getName())) enemy.reset();
		setTurnRadarRight(360);
	}

	public double normalizeAngle(double angle) {
		/*
		 * Ensure a given angle is between
		 * -180 and +180 degrees.
		 */
		while (angle > 180) angle -= 360;
		while (angle < -180) angle += 360;

		return angle;
	}

	private void colorMe() {
		setBodyColor(Color.BLACK);
		setGunColor(Color.GREEN);
		setRadarColor(Color.LIGHT_GRAY);
	}

	private void updateRadar() {
		/*
		 * If we have no enemy we are targeting spin
		 * our radar around. If we do have one turn the
		 * radar in a small +-45 degree arc in around our
		 * current target. This keeps him somewhat targeted
		 * and not all spaztastic.
		 */
		if (enemy.none())
			setTurnRadarRight(360);
		else {
			double turn = getHeading() - getRadarHeading() + enemy.getBearing();
			turn += 45 * radarDirection;
			setTurnRadarRight(normalizeAngle(turn));
			radarDirection *= -1;
		}
	}
	private void updateGun() {
		if (enemy.none()) return;

		double firePower = Math.min(400 / enemy.getDistance(), 3);
		double bulletSpeed = 20 - firePower * 3;
		long time = (long) (enemy.getDistance() / bulletSpeed);

		/*
		 * Point our guns the right way
		 */
		double futureX = enemy.getFutureX(time);
		double futureY = enemy.getFutureY(time);
		double turn = normalizeAngle(getAbsoluteBearingBetweenPoints(getX(), getY(), futureX, futureY) - getGunHeading());
		setTurnGunRight(turn);

		/*
		 * Set our firepower based on how close we our to our enemy.
		 * Be sure that our gun has turned enough before firing,
		 * and don't fire if we are overheating.
		 */
		if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10)
			setFire(firePower);

	}

	private void updateMovement() {
		setTurnRight(normalizeAngle(enemy.getBearing() + 90 - (25 * moveDirection)));
		if (closeToWall > 0) closeToWall--;

		if (getVelocity() == 0) {
			moveDirection *= -1;
			setMaxVelocity(8);
			setAhead(10000 * moveDirection);
		} else {
			if (getTime() % 50 == 0) {
				moveDirection *= -1;
				//setAhead(550 * moveDirection);
			}

			setAhead(enemy.getDistance() * moveDirection);
		}
	}

	private double getAbsoluteBearingBetweenPoints(double x1, double y1, double x2, double y2) {
		double xOffset = x2 - x1;
		double yOffset = y2 - y1;
		double hyp = Point2D.distance(x1, y1, x2, y2);
		double arcSin = Math.toDegrees(Math.asin(xOffset / hyp));
		double bearing = 0;

		if (xOffset > 0 && yOffset > 0) {
			bearing = arcSin;
		} else if (xOffset < 0 && yOffset > 0) {
			bearing = 360 + arcSin;
		} else if (xOffset > 0 && yOffset < 0) {
			bearing = 180 - arcSin;
		} else if (xOffset < 0 && yOffset < 0) {
			bearing = 180 - arcSin;
		}

		return bearing;
	}
}
