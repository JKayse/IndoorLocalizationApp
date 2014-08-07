package com.example.apuser;

public class Line
{
	public Point StartPoint;
	public Point EndPoint;
	public double Slope;
	public String Name;
	public Line(double x1, double y1, double x2, double y2, String name)
	{
		StartPoint = new Point(x1, y1);
		EndPoint = new Point(x2, y2);
		Name = name;
		Slope = DetermineSlope(StartPoint, EndPoint);
	}
	public Line(Point point1, Point point2, String name)
	{
		StartPoint = point1;
		EndPoint = point2;
		Name = name;
		Slope = DetermineSlope(StartPoint, EndPoint);
	}
	public Line(Line line)
	{
		StartPoint = line.StartPoint;
		EndPoint = line.EndPoint;
		Slope = line.Slope;
		Name = line.Name;
	}
	private double DetermineSlope(Point one, Point two)
	{
		if (one.XCoord == two.XCoord)
		{
			return Double.MAX_VALUE;
		}
		else if (one.YCoord == two.YCoord)
		{
			return 0;
		}
		else
		{
			return (two.YCoord - one.YCoord) / (two.XCoord - one.XCoord);
		}
	}
	public Point GetOtherPoint(Point point)
	{
		if(point.equals(StartPoint))
		{
			return EndPoint;
		}
		else if(point.equals(EndPoint))
		{
			return StartPoint;
		}
		else
		{
			return null;
		}
	}
	/// <summary>
	/// 
	/// </summary>
	/// <param name="point"> pivot point</param>
	/// <returns>Direction of the other point in relation to the one passed in</returns>
	public Resources.Directions GetDiretion(Point point) throws Exception
	{
		Point otherPoint = GetOtherPoint(point);
		if (otherPoint == null)
		{
			return Resources.Directions.None;
		}
		else
		{
			//can make logic simpler and more extensible by using | with the enum
			//too much work for now, but should revisit if can
			if (Slope == 0)
			{
				if (otherPoint.XCoord < point.XCoord)
				{
					return Resources.Directions.West;
				}
				else
				{
					return Resources.Directions.East;
				}
			}
			else if (Slope == Double.MAX_VALUE)
			{
				if (otherPoint.YCoord < point.YCoord)
				{
					return Resources.Directions.North;
				}
				else
				{
					return Resources.Directions.South;
				}
			}
			else
			{
				throw new Exception("welp");
			}
		}
	}
	
	public Point GetPointOnLineCosestToPoint(Point point) throws Exception
	{
		if (Slope == 0)
		{
			return new Point(point.XCoord, StartPoint.YCoord);
		}
		else if (Slope == Double.MAX_VALUE)
		{
			return new Point(StartPoint.XCoord, point.YCoord);
		}
		else
		{
			throw new Exception("welp");
		}
	}

	public Point GetRandomPoint() throws Exception
	{
		if (Slope == 0)
		{
			Double xVal = Resources.RandomDouble(StartPoint.XCoord, EndPoint.XCoord);
			return new Point(xVal, StartPoint.YCoord);
		}
		else if (Slope == Double.MAX_VALUE)
		{
			Double yVal = Resources.RandomDouble(StartPoint.YCoord, EndPoint.YCoord);
			return new Point(StartPoint.XCoord, yVal);
		}
		else
		{
			throw new Exception("Oh Man");
		}
	}

	public boolean equals(Object obj)
	{
		Line line = (Line) obj;
		return (line.StartPoint == StartPoint) && (line.EndPoint == EndPoint);
	}

	public String toString()
	{
		return "{" + Name + "}: [{" + StartPoint + "} - {" + EndPoint + "}]";
	}

	public Point GetSharedPoint(Vector vector)
	{
		Point startPoint = vector.StartPoint;
		Point endPoint = vector.EndPoint;
		if(startPoint.equals(StartPoint) || startPoint.equals(EndPoint))
		{
			return startPoint;
		}
		else if(endPoint.equals(StartPoint) || endPoint.equals(EndPoint))
		{
			return endPoint;
		}
		else
		{
			return null;
		}
	}
	public Point ElectricBoogalo(Room destination) throws Exception
	{
		if (Slope == 0)
		{
			Point lower, higher;
			if (StartPoint.XCoord < EndPoint.XCoord)
			{
				lower = StartPoint;
				higher = EndPoint;
			}
			else
			{
				lower = EndPoint;
				higher = StartPoint;
			}
			if (destination.XCoord < lower.XCoord)
			{
				return lower;
			}
			else
			{
				return higher;
			}
		}
		else if (Slope == Double.MAX_VALUE)
		{
			Point lower, higher;
			if (StartPoint.YCoord < EndPoint.YCoord)
			{
				lower = StartPoint;
				higher = EndPoint;
			}
			else
			{
				lower = EndPoint;
				higher = StartPoint;
			}
			if (destination.YCoord < lower.YCoord)
			{
				return lower;
			}
			else
			{
				return higher;
			}
		}
		else
		{
			throw new Exception("dun messed up");
		}
	}

	public Point GetNearestVertex(Room destination) throws Exception
	{
		if (StartPoint.Visited)
		{
			return EndPoint;
		}
		else if (EndPoint.Visited)
		{
			return StartPoint;
		}
		if (Slope == 0)
		{
			Point lower, higher;
			if (StartPoint.XCoord < EndPoint.XCoord)
			{
				lower = StartPoint;
				higher = EndPoint;
			}
			else
			{
				lower = EndPoint;
				higher = StartPoint;
			}
			if (destination.XCoord < lower.XCoord)
			{
				return lower;
			}
			else
			{
				return higher;
			}
		}
		else if (Slope == Double.MAX_VALUE)
		{
			Point lower, higher;
			if (StartPoint.YCoord < EndPoint.YCoord)
			{
				lower = StartPoint;
				higher = EndPoint;
			}
			else
			{
				lower = EndPoint;
				higher = StartPoint;
			}
			if (destination.YCoord < lower.YCoord)
			{
				return lower;
			}
			else
			{
				return higher;
			}
		}
		else
		{
			throw new Exception("dun messed up");
		}
	}
}
