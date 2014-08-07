package com.example.apdeveloper;

import java.util.ArrayList;


public class ConnectionTest {

	String mTestNumber;
	ArrayList<APStrength> strengths;
	
	public ConnectionTest(){
		mTestNumber = "placeHolder";
	}
	
	//Gets the array of grades for a category and sets it.
	public void setAPs(ArrayList<APStrength> values){
		this.strengths = values;
		//calculate the percentage from the grades given.
	}
	
	//Returns the strength of a specific AP.
	public String getStrength(Integer position){
		return strengths.get(position).getStrength();
	}
	
	//Returns the name of a specific AP.
	public String getGradePercentage(Integer position){
		return strengths.get(position).getName();
	}
	
	//Returns a certain grade.
	public APStrength getAP(Integer position){
		return strengths.get(position);
	}
	
	//Returns the list of grades.
	public ArrayList<APStrength> getAPs(){
		return strengths;
	}
	
	//Sets the name of the category.
	public void setTestName(String value){
		this.mTestNumber = value;
	}
	
	//Returns the name of the category.
	public String getTestName(){
		return mTestNumber;
	}
	
	
	//Turns the category name to a string.
	public String toString(){
		return mTestNumber;
	}

}
