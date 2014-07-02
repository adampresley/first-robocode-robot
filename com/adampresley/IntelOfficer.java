package com.adampresley;

import robocode.*;
import com.adampresley.Enemy;
import java.util.Hashtable;

public class IntelOfficer {
	private Hashtable<String, Enemy> _potentialTargets;

	public IntelOfficer() {
		_potentialTargets = new Hashtable<String, Enemy>();
	}

	public Enemy getTargetByName(String targetName) {
		return _potentialTargets.get(targetName);
	}

	public boolean isRegisteredTarget(String targetName) {
		return _potentialTargets.containsKey(targetName);
	}

	public void recordTargetDeath(String targetName) {
		_potentialTargets.remove(targetName);
	}

	public void registerTarget(Enemy target) {
		System.out.println("Registering " + target.getName() + " with intel");
		_potentialTargets.put(target.getName(), target);
	}

	public Enemy searchForWeakerTarget(Enemy compareTo) {
		Enemy result = null;

		for (Enemy potential : _potentialTargets.values()) {
			if (
					potential.getDistance() < compareTo.getDistance() - 150 &&
					potential.getEnergy() < compareTo.getEnergy()
				) {
				result = potential;
				break;
			}
		}

		return result;
	}
	public void updateRegistrationRecord(Enemy target) {
		_potentialTargets.put(target.getName(), target);
	}
}