
public class Vector extends Line
{

	public Resources.Directions Direction;
	public Vector(Line line, Resources.Directions direction)
	{
		super(line);
		Direction = direction;
	}

	public Vector(Point point1, Point point2, String name) throws Exception
	{
		super(point1, point2, name);
		Direction = super.GetDiretion(point1);
	}

	public String toString()
	{
		return "{" + super.toString() + "}, <" + Resources.GetDirectionString(Direction) + ">";
	}

	public boolean equals(Object obj)
	{
		return super.equals((Line) obj);
	}

}
