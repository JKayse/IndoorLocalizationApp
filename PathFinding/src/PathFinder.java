import java.util.List;
import java.util.Stack;


public class PathFinder
{

	public static void main(String[] args) throws Exception
	{
		MainPath mainPath = new MainPath();
		mainPath.Print();
		List<Room> rooms = Resources.GetRooms();

		System.out.println("-------------------------------------------------------------------------------");

		Resources.PrintRooms(rooms);

		System.out.println("-------------------------------------------------------------------------------");

		Room randomRoom = Resources.GetRandomRoom(rooms);

		System.out.println("Destination: {" + randomRoom + "}");

		Point startingPoint = new Point(650, 510);	
		Line startingLine = mainPath.FindNearestLine(startingPoint, 0);
		Point connectingPoint = startingLine.GetPointOnLineCosestToPoint(startingPoint);
		
		System.out.println("Starting Point: {" + startingPoint + "} Connecting Point: {" + connectingPoint + "} on Line: {" + startingLine + "}");
		System.out.println("---------------------------------Navigating-------------------------------------");

		Stack<Vector> answer = mainPath.Navigate(connectingPoint, startingLine, randomRoom, new Stack<Vector>());

		System.out.println("---------------------------------Solution-------------------------------------");

		List<Vector> path = Resources.GetPath(answer, startingPoint, connectingPoint, startingLine);
		for( Vector item : path)
		{
			System.out.println("Solution: {" + item + "}");
		}

	}

}
