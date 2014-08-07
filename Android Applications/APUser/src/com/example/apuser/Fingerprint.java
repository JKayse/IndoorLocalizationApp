package com.example.apuser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class Fingerprint 
{
	private int id;
	private double xLoc;
	private double yLoc;
	private HashMap<String, Double> macIdtoStrengthMap;
	
	
	
	public HashMap<String, Double> GetMacIdtoStrengthMap()
	{
		return macIdtoStrengthMap;
	}
	
	public int getID()
	{
		return id;
	}
	
	public double getX()
	{
		return xLoc;
	}
	
	public double getY()
	{
		return yLoc;
	}
	
	public Fingerprint(int id, double x, double y, HashMap<String, Double> macIdStrength) 
	{
	    this.id = id;
	    xLoc = x;
	    yLoc = y;
	    macIdtoStrengthMap = macIdStrength;
	}

	private double euclideanDistancePenalty(Fingerprint fingerprint) 
	{
		HashMap<String, Double> macIdStregth = fingerprint.GetMacIdtoStrengthMap();
		HashSet<String> combinedKeys = new HashSet<String>();
		combinedKeys.addAll(macIdStregth.keySet());
		combinedKeys.addAll(macIdtoStrengthMap.keySet());

		return euclideanDistance(macIdStregth, combinedKeys);
	}


	private double euclideanDistance(HashMap<String, Double> macIdStregth,
			HashSet<String> combinedKeys) 
	{
		double distance = 0.0;
		for (String key : combinedKeys) {
			double value = 0;
			Double fStrength = macIdStregth.get(key);
			Double mStrength = macIdtoStrengthMap.get(key);
			value = (fStrength == null) ? -100 : (double) fStrength;
			value -= (mStrength == null) ? -100 : (double) mStrength;
			distance += value * value;
		}
		return distance;
	} 
	
	private double euclideanDistanceCommon(Fingerprint fingerprint) 
	{		
		HashMap<String, Double> macIdStregth = fingerprint.GetMacIdtoStrengthMap();
		HashSet<String> fKeys =  new HashSet<String>(macIdStregth.keySet());
		Set<String> mKeys = new HashSet<String>(macIdtoStrengthMap.keySet());
		mKeys.retainAll(fKeys);
		return euclideanDistance(macIdStregth, (HashSet<String>) mKeys);		
		
	} 
	
	private HashMap<Fingerprint, Double> getScoreMatchPenalty(ArrayList<Fingerprint> fingerprints) 
	{	    
		HashMap<Fingerprint, Double> fingerprintDistMap = new HashMap <Fingerprint, Double>();
		
	    if(fingerprints != null) 
	    {
    	    for(Fingerprint fingerprint : fingerprints) 
    	    {
    	    	double score = euclideanDistancePenalty(fingerprint);
    	    	fingerprintDistMap.put(fingerprint, score);	         
    	    }	        
	    }
	    return fingerprintDistMap;
	}
	
	private HashMap<Fingerprint, Double> getScoreMatchCommon(ArrayList<Fingerprint> fingerprints) 
	{	    
		HashMap<Fingerprint, Double> fingerprintDistMap = new HashMap <Fingerprint, Double>();
		
	    if(fingerprints != null) 
	    {
    	    for(Fingerprint fingerprint : fingerprints) 
    	    {
    	    	double score = euclideanDistanceCommon(fingerprint);
    	    	fingerprintDistMap.put(fingerprint, score);	         
    	    }	        
	    }
	    return fingerprintDistMap;
	}
	
	public Fingerprint getClosestMatchPenalty(ArrayList<Fingerprint> fingerprints) 
	{
		Fingerprint closest = null;
	    double maxScore = Double.MAX_VALUE;

	    if(fingerprints != null) 
	    {
    	    for(Fingerprint fingerprint : fingerprints) 
    	    {
    	        double score = euclideanDistancePenalty(fingerprint);
    	        if(maxScore > score) 
    	        {
    	        	maxScore = score;
    	            closest = fingerprint;
    	        }
    	    }	        
	    }
	    return closest;
	}
	
	public Fingerprint getClosestMatchCommon(ArrayList<Fingerprint> fingerprints) 
	{
		Fingerprint closest = null;
	    double maxScore = Double.MAX_VALUE;

	    if(fingerprints != null) 
	    {
    	    for(Fingerprint fingerprint : fingerprints) 
    	    {
    	        double score = euclideanDistanceCommon(fingerprint);
    	        if(maxScore > score) 
    	        {
    	        	maxScore = score;
    	            closest = fingerprint;
    	        }
    	    }	        
	    }
	    return closest;
	}

		

	}
