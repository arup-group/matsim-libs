/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2016 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package playground.jbischoff.parking.agentLogic;

import java.util.Iterator;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.contrib.dynagent.DynAction;
import org.matsim.contrib.dynagent.DynActivity;
import org.matsim.contrib.dynagent.DynAgent;
import org.matsim.contrib.dynagent.DynAgentLogic;
import org.matsim.contrib.dynagent.StaticDriverDynLeg;
import org.matsim.contrib.dynagent.StaticDynActivity;
import org.matsim.contrib.dynagent.StaticPassengerDynLeg;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.pt.routes.ExperimentalTransitRoute;
import org.matsim.vehicles.Vehicle;

import playground.jbischoff.parking.ParkingUtils;
import playground.jbischoff.parking.manager.ParkingManager;
import playground.jbischoff.parking.manager.WalkLegFactory;
import playground.jbischoff.parking.routing.ParkingRouter;

/**
 * @author jbischoff
 *
 */
public class ParkingAgentLogic implements DynAgentLogic {

	enum LastParkActionState  {
			
			// we have the following cases of ending dynacts:
			NONCARTRIP,	// non-car trip arrival: start Activity
			CARTRIP, // car-trip arrival: add park-car activity 
			PARKACTIVITY, // park-car activity: get next PlanElement & add walk leg to activity location
			WALKFROMPARK ,// walk-leg to act: start next PlanElement Activity
			ACTIVITY, // ordinary activity: get next Leg, if car: go to car, otherwise add ordinary leg by other mode
			WALKTOPARK, // walk-leg to car: add unpark activity
			UNPARKACTIVITY // unpark activity: find the way to the next route & start leg
	}
	private LastParkActionState lastParkActionState;
	private DynAgent agent;
	private Iterator<PlanElement> planElemIter;
	private PlanElement currentPlanElement;
	private ParkingManager parkingManager;
	private WalkLegFactory walkLegFactory;
	private ParkingRouter parkingRouter;
	/**
	 * @param plan
	 *            (always starts with Activity)
	 */
	public ParkingAgentLogic(Plan plan, ParkingManager parkingManager, WalkLegFactory walkLegFactory, ParkingRouter parkingRouter) {
		planElemIter = plan.getPlanElements().iterator();
		this.parkingManager = parkingManager;
		this.walkLegFactory = walkLegFactory;
		this.parkingRouter = parkingRouter;
		
	}

	@Override
	public DynActivity computeInitialActivity(DynAgent adapterAgent) {
		this.agent = adapterAgent;
		this.lastParkActionState = LastParkActionState.ACTIVITY;
		this.currentPlanElement = planElemIter.next();
		Activity act = (Activity) currentPlanElement;
		return new StaticDynActivity(act.getType(), act.getEndTime());
	}

	@Override
	public DynAgent getDynAgent() {
		return agent;
	}

	@Override
	public DynAction computeNextAction(DynAction oldAction, double now) {
		// we have the following cases of ending dynacts:
		// non-car trip arrival: start Activity
		// car-trip arrival: add park-car activity 
		// park-car activity: get next PlanElement & add walk leg to activity location
		// walk-leg to act: start next PlanElement Activity
		// ordinary activity: get next Leg, if car: go to car, otherwise add ordinary leg by other mode
		// walk-leg to car: add unpark activity
		// unpark activity: find the way to the next route & start leg
		switch (lastParkActionState){
		case ACTIVITY:
			return nextStateAfterActivity(oldAction, now);
				
		case CARTRIP:
			return nextStateAfterCarTrip(oldAction,now);
			
		case NONCARTRIP:
			return nextStateAfterNonCarTrip(oldAction,now);
			
		case PARKACTIVITY:
			return nextStateAfterParkActivity(oldAction,now);
		
		case UNPARKACTIVITY:
			return nextStateAfterUnParkActivity(oldAction,now);
			
		case WALKFROMPARK:
			return nextStateAfterWalkFromPark(oldAction,now);
			
		case WALKTOPARK:
			return nextStateAfterWalkToPark(oldAction,now);
		
		}
		throw new RuntimeException("unreachable code");
		
		
		
		
		

	}

	private DynAction nextStateAfterUnParkActivity(DynAction oldAction, double now) {
		// we have unparked, now we need to get going by car again.
		
		Leg currentPlannedLeg = (Leg) currentPlanElement;
		NetworkRoute plannedRoute = (NetworkRoute) currentPlannedLeg.getRoute();
		NetworkRoute actualRoute = this.parkingRouter.getRouteFromParkingToDestination(plannedRoute, now, agent.getCurrentLinkId());
		Id<Vehicle> vehicleId = Id.create(this.agent.getId(), Vehicle.class);

		if (this.parkingManager.unParkVehicleHere(vehicleId, agent.getCurrentLinkId(), now)){
			this.lastParkActionState = LastParkActionState.CARTRIP;
			return new StaticDriverDynLeg(TransportMode.car, actualRoute);
		
		}
		else throw new RuntimeException("parking location mismatch");
		
	}

	private DynAction nextStateAfterWalkToPark(DynAction oldAction, double now) {
		//unpark activity, driving leg comes next
		this.lastParkActionState = LastParkActionState.UNPARKACTIVITY;
		return new StaticDynActivity(ParkingUtils.UNPARKACTIVITYTYPE, now + ParkingUtils.UNPARKDURATION);
	}

	private DynAction nextStateAfterWalkFromPark(DynAction oldAction, double now) {
		return null;
		
		
	}

	private DynAction nextStateAfterParkActivity(DynAction oldAction, double now) {
		// TODO Auto-generated method stub
		return null;
	}

	private DynAction nextStateAfterNonCarTrip(DynAction oldAction, double now) {
		// TODO Auto-generated method stub
		return null;
	}

	private DynAction nextStateAfterCarTrip(DynAction oldAction, double now) {
		// TODO Auto-generated method stub
		return null;
	}

	private DynAction nextStateAfterActivity(DynAction oldAction, double now) {
		// we could either depart by car or not next
		this.currentPlanElement = planElemIter.next();
		Leg currentLeg = (Leg) currentPlanElement;
		if (currentLeg.getMode().equals(TransportMode.car)){
			Id<Vehicle> vehicleId = Id.create(this.agent.getId(), Vehicle.class);
			Id<Link> parkLink = this.parkingManager.getVehicleParkingLocation(vehicleId);
			Leg walkleg = walkLegFactory.createWalkLeg(agent.getCurrentLinkId(), parkLink, now, TransportMode.access_walk);
			this.lastParkActionState = LastParkActionState.WALKTOPARK;
			return new StaticPassengerDynLeg(walkleg.getRoute(), walkleg.getMode());
		}
		else if (currentLeg.getMode().equals(TransportMode.pt)) {
			if (currentLeg.getRoute() instanceof ExperimentalTransitRoute){
				throw new IllegalStateException ("not yet implemented");
			}
			else {
				this.lastParkActionState = LastParkActionState.NONCARTRIP;
				return new StaticPassengerDynLeg(currentLeg.getRoute(), currentLeg.getMode());
			}
		//teleport or pt route	
		} 
		else {
		//teleport	
			this.lastParkActionState = LastParkActionState.NONCARTRIP;
			return new StaticPassengerDynLeg(currentLeg.getRoute(), currentLeg.getMode());
		}
		
	}

}
