/**
 * 
 */
package org.matsim.contrib.parking.parkingsearch.search;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.parking.parkingsearch.ParkingUtils;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.network.NetworkUtils;
import org.matsim.vehicles.Vehicle;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * @author tschlenther
 *
 *Agents drive to destination first. Knowledge about surrounding streets is assumed. If no parking slot is available, they always look
 *for a slot on the one outgoing link that has the shortest distance to their destination and is unknown to them so far. If every outlink
 *is known they choose the next link to search on randomly.
 *
 */
public class DistanceMemoryParkingSearchLogic implements ParkingSearchLogic {

	private static final Logger logger = LogManager.getLogger(DistanceMemoryParkingSearchLogic.class);

	private static final boolean doLogging = false;
	
	private Network network;
	private HashSet<Id<Link>> knownLinks;   
	
	/**
	 * @param network 
	 * 
	 */
	public DistanceMemoryParkingSearchLogic(Network network) {
		this.network = network;
		this.knownLinks = new HashSet<Id<Link>>();
	}

	public Id<Link> getNextLink(Id<Link> currentLinkId, Id<Link> destLinkId, Id<Vehicle> vehicleId, String mode) {

		List<Link> outLinks = ParkingUtils.getOutgoingLinksForMode(network.getLinks().get(currentLinkId), mode);
		double shortestDistance = Double.MAX_VALUE;
		int nrKnownLinks = 0;
		Id<Link> nextLink = null;
		
		if(doLogging) logger.info("number of outlinks of link " + currentLinkId + ": " + outLinks.size());

		for (Link outLink : outLinks) {
			Id<Link> outLinkId = outLink.getId();
			if (this.knownLinks.contains(outLinkId)) nrKnownLinks++;
			else{
				double distToDest = NetworkUtils.getEuclideanDistance(outLink.getCoord(), network.getLinks().get(destLinkId).getCoord());
				if (distToDest < shortestDistance){
					nextLink = outLinkId;
					shortestDistance = distToDest;
					if(doLogging) logger.info("currently chosen link: " + nextLink + " distToDest: " + shortestDistance);
				}
				else if(distToDest == shortestDistance){
					String message = "link " + nextLink + " and link " + outLinkId + " both are " + distToDest + "m away from destination.";
						if (Math.random() > 0.5)
							nextLink = outLinkId;
						if(doLogging) logger.info(message + " link " + nextLink + " is chosen.");
				}
				else{
					if (doLogging){
						logger.info("link " + outLinkId + " was not chosen because it is " + distToDest + "m away whereas shortest distance is " + shortestDistance);
					}
				}
			}
		}
		if(doLogging)logger.error("vehicle " + vehicleId + " knew " + nrKnownLinks + " out of " + outLinks.size() + " outlinks of link " + currentLinkId);
		if(outLinks.size() == nrKnownLinks ){
			if(doLogging)logger.error("vehicle " + vehicleId + " knows all outlinks of link " + currentLinkId);
			
			//return random Link
			int index = MatsimRandom.getRandom().nextInt(outLinks.size());
			Iterator<Link> iter = outLinks.iterator();
			for (int i = 0; i < index; i++) {
			    iter.next();
			}
			nextLink = iter.next().getId();
		}				
		
		if(nextLink == null){
			throw new RuntimeException("the next Link Id for vehicle " + vehicleId + " on current link " + currentLinkId + " couldn't be calculated.");
		}
		if(doLogging)logger.error("vehicle " + vehicleId + " takes link " + nextLink + " as next link");
		this.knownLinks.add(nextLink);
		return nextLink;
	}

	@Override
	public void reset() {
	}

	@Override
	public Id<Link> getNextLink(Id<Link> currentLinkId, Id<Vehicle> vehicleId, String mode) {
		throw new RuntimeException("shouldn't happen - method not implemented");
	}

	public void addToKnownLinks(Id<Link> linkId){
		this.knownLinks.add(linkId);
	}
	
}
