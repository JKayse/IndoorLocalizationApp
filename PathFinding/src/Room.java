
public class Room extends Point
{
	public String Name;
	public Line Line;
	public Room(String name, double x, double y) throws Exception
	{
		super(x, y);
		Name = name;
		Line = super.WhosLineIsItAnyWay();
	}

	public String toString()
	{
		return "{" + Name + "}: {" + super.toString() + "} on {" + Line + "}";
	}

}
