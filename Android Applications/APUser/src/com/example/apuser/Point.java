package com.example.apuser;

import android.util.Log;


public class Point
{
	public double XCoord;
	public double YCoord;
	public boolean Visited;
	public Point(double x, double y)
	{
		XCoord = x;
		YCoord = y;
		Visited = false;
	}

	public Line WhosLineIsItAnyWay() throws Exception
	{
		if(MainPath.Lines == null)
		{
			return null;
		}
		for (Line line : MainPath.Lines)
		{
			if(line.Slope == 0)
			{
				if((YCoord == line.StartPoint.YCoord) && (Resources.IsPointBetweenPoints(line.StartPoint.XCoord, line.EndPoint.XCoord, XCoord)))
				{
					return line;
				}
			}
			else if(line.Slope == Double.MAX_VALUE)
			{
				if ((XCoord == line.StartPoint.XCoord) && (Resources.IsPointBetweenPoints(line.StartPoint.YCoord, line.EndPoint.YCoord, YCoord)))
				{
					return line;
				}
			}
			else
			{
				throw new Exception("this is bad");
			}
		}
		return null;
	}

	public Point Up(int distance)
	{
		return new Point(XCoord, YCoord - distance);
	}
	public Point Down(int distance)
	{
		return new Point(XCoord, YCoord + distance);
	}
	public Point Left(int distance)
	{
		return new Point(XCoord - distance, YCoord);
	}
	public Point Right(int distance)
	{
		return new Point(XCoord + distance, YCoord);
	}
	public boolean equals(Object obj)
	{
			Point point = (Point) obj;
			Double xDiff = Math.abs(XCoord - point.XCoord);
			Double yDiff = Math.abs(YCoord - point.YCoord);
			if((xDiff < .0001) && (yDiff < .0001))
			{
				return true;
			}
		
		return false;
	}

	public String toString()
	{
		return "({" + XCoord + "}, {" + YCoord + "})";
	}

	public int hashCode()
	{
		
		String temp = XCoord + "" + YCoord;
//		System.out.println(temp);
		return temp.hashCode();
	}
}
