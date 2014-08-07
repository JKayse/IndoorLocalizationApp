package com.example.apuser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Stack;


public class Resources
{
	enum Directions { None, North, South, East, West}
	public static Random rand = new Random();
	public static String GetDirectionString(Directions direction)
	{
		switch(direction)
		{
			case None:
				return "None";
			case North:
				return "North";
			case South:
				return "South";
			case East:
				return "East";
			case West:
				return "West";
		}
		return "";
	}

	public static List<Room> GetRooms() throws Exception
	{
		return Arrays.asList(
			new Room("1818", 495.0, 916.0),
			new Room("1819", 495.0, 876.0),
			new Room("1820", 495.0, 827.0),
			new Room("1821", 495.0, 816.0),
			new Room("1822", 495.0, 743.0),
			new Room("1823", 495.0, 748.0)
		);
	}

	public static void PrintRooms(List<Room> rooms)
	{
		for (Room room : rooms)
		{
			System.out.println(room);
		}
	}

	public static Room GetRandomRoom(List<Room> rooms)
	{
		return rooms.get(rand.nextInt(rooms.size()));
	}

	public static boolean IsPointBetweenPoints(double compare1, double compare2, double point)
	{
		double lower = Math.min(compare1, compare2);
		double higher = Math.max(compare1, compare2);
		return (point >= lower) && (point <= higher);
	}

	public static Point RandomPointOnLine() throws Exception
	{
		Line randomLine = MainPath.Lines.get(rand.nextInt(MainPath.Lines.size()));
		return randomLine.GetRandomPoint();
	}

	public static Point RandomPointOnLine(Line line) throws Exception
	{
		return line.GetRandomPoint();
	}

	public static double RandomDouble(double val1, double val2)
	{
		double lower = Math.min(val1, val2);
		double higher = Math.max(val1, val2);
		return rand.nextDouble() * (higher - lower) + lower;
	}

	public static Directions GetVertical(Room destination, Point startingPoint)
	{
		if (destination.YCoord < startingPoint.YCoord)
		{
			return Directions.North;
		}
		else if (destination.YCoord > startingPoint.YCoord)
		{
			return Directions.South;
		}
		else
		{
			return Directions.None;
		}
	}

	public static Directions GetHorizontal(Room destination, Point startingPoint)
	{
		if (destination.XCoord < startingPoint.XCoord)
		{
			return Directions.West;
		}
		else if (destination.XCoord > startingPoint.XCoord)
		{
			return Directions.East;
		}
		else
		{
			return Directions.None;
		}
	}

	public static List<Vector> GetPath(Stack<Vector> vectorStack, Point startingPoint, Point connectingPoint, Line startingLine) throws Exception
	{
		List<Vector> path = new ArrayList<Vector>();

		for (Vector vector : vectorStack)
		{
			if (!vector.equals(startingLine))
			{
				path.add(vector);
			}
		}
		Point firstVertex = startingLine.GetSharedPoint(path.get(0));
		if (firstVertex != null)
		{
			Vector mainPathStart = new Vector(connectingPoint, firstVertex, "Main Path Start");
			path.add(mainPathStart);
		}
		if(!startingPoint.equals(connectingPoint))
		{
			Vector startVector = new Vector(startingPoint, connectingPoint, "Path to main path");
			path.add(startVector);
		}
		return path;
	}
}
