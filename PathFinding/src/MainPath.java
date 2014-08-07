package com.example.apuser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import android.util.Log;


public class MainPath
{
	public HashMap<Point, ArrayList<Vector>> Verticies;
	public static List<Line> Lines;
	public MainPath() throws Exception
	{
		Verticies = new HashMap<Point, ArrayList<Vector>>();
		Lines = new ArrayList<Line>();
		Lines.add(new Line(530, 400, 530, 545, "1.1"));
		Lines.add(new Line(530, 545, 530, 675, "1.2"));
		Lines.add(new Line(530, 545, 405, 545, "2"));
		Lines.add(new Line(405, 545, 405, 705, "3"));
		Lines.add(new Line(405, 705, 495, 705, "4"));
		Lines.add(new Line(495, 705, 495, 935, "5"));
		Lines.add(new Line(495, 935, 435, 935, "6"));
		for(Line line : Lines)
		{
			if(!Verticies.containsKey(line.StartPoint))
			{
				ArrayList<Vector> list = new ArrayList<Vector>();
				list.add(new Vector(line, line.GetDiretion(line.StartPoint)));
				Verticies.put(line.StartPoint, list);
			}
			else
			{
				ArrayList<Vector> list = Verticies.get(line.StartPoint);
				list.add(new Vector(line, line.GetDiretion(line.StartPoint)));
				Verticies.put(line.StartPoint, list);
			}

			if(!Verticies.containsKey(line.EndPoint))
			{
				ArrayList<Vector> list = new ArrayList<Vector>();
				list.add(new Vector(line, line.GetDiretion(line.EndPoint)));
				Verticies.put(line.EndPoint, list);
			}
			else
			{
				ArrayList<Vector> list = Verticies.get(line.EndPoint);
				list.add(new Vector(line, line.GetDiretion(line.EndPoint)));
				Verticies.put(line.EndPoint, list);
			}
		}
	}

	public Vector GetBestVector(Point vertex, Room destination, Resources.Directions direction)
	{
		List<Vector> choices = Verticies.get(vertex);
		for(Vector choice : choices)
		{
			if(destination.Line == choice)
			{
				return choice;
			}
		}

		for(Vector choice : choices)
		{
			if(choice.Direction == direction)
			{
				return choice;
			}
		}

		return null;
	}


	public Point RemoveVector(Line line, Point startingPoint)
	{
		List<Vector> startingPointList = Verticies.get(line.StartPoint);
		List<Vector> endPointList = Verticies.get(line.EndPoint);
		Vector spRemove = null, epRemove = null;
		for( Vector item : startingPointList)
		{
			if(item.equals(line))
			{
				spRemove = item;
				break;
			}
		}
		for( Vector item : endPointList)
		{
			if (item.equals(line))
			{
				epRemove = item;
				break;
			}
		}
		if(spRemove != null)
		{
			startingPointList.remove(spRemove);
			if(startingPointList.size() == 0)
			{
				Verticies.remove(line.StartPoint);
			}
		}
		if(epRemove != null)
		{
			endPointList.remove(epRemove);
			if (endPointList.size() == 0)
			{
				Verticies.remove(line.EndPoint);
			}
		}
		return line.GetOtherPoint(startingPoint);
	}

	public void Print()
	{
		for (Entry<Point, ArrayList<Vector>> path : Verticies.entrySet())
		{
			System.out.println(path.getKey());
			for (Vector vector : path.getValue())
			{
				System.out.println("         " + vector);
			}
		}
	}

	public Line FindNearestLine(Point point, int distance) throws Exception
	{
		Point connectingPoint = point.Up(distance);
		Line line = connectingPoint.WhosLineIsItAnyWay();
		if(line == null)
		{
			connectingPoint = point.Down(distance);
			line = connectingPoint.WhosLineIsItAnyWay();
			if(line == null)
			{
				connectingPoint = point.Left(distance);
				line = connectingPoint.WhosLineIsItAnyWay();
				if(line == null)
				{
					connectingPoint = point.Right(distance);
					line = connectingPoint.WhosLineIsItAnyWay();
					if(line == null)
					{
						line = FindNearestLine(point, distance + 1);
					}
				}
			}
		}
		return line;
	}

	public Stack<Vector> Navigate(Point startingPoint, Line startingLine, Room destination, Stack<Vector> path) throws Exception
	{
		if (startingPoint == null || destination == null)
		{
			return null;
		}
		if (startingLine == null)
		{
			startingLine = startingPoint.WhosLineIsItAnyWay();
		}
		if (startingLine.equals(destination.Line))
		{
			if (path.size() > 0)
			{
				path.pop();
			}
			Point vert = path.peek().ElectricBoogalo(destination);
			Vector vect = new Vector(vert, destination, "end");
			path.push(vect);
			return path;
		}
		else
		{
			Point vertex = startingLine.GetNearestVertex(destination);
			vertex.Visited = true;
			Vector vector = null;
			do
			{
				Resources.Directions vertical = Resources.GetVertical(destination, vertex);
				Resources.Directions horizontal = Resources.GetHorizontal(destination, vertex);

				vector = GetBestVector(vertex, destination, horizontal);
				if (vector == null)
				{
					vector = GetBestVector(vertex, destination, vertical);
				}
				if (vector == null)
				{
					vertex = RemoveVector(startingLine, vertex);
					if (path.size() > 0)
					{
						path.pop();
					}
				}
			} while (vector == null);
			if (path.size() == 0 || !startingLine.equals(vector))
			{
				path.push(vector);
			}
			return Navigate(vector.GetOtherPoint(vertex), vector, destination, path);
		}
	}
}
