<?php

/**
* index.php.  This file contains all the backend functions that run the website
* Uses Slim framework.  
*/
require 'Slim/Slim.php';

$app = new Slim();

//Sets up the links to the functions
$app->get(
    '/',
    function() use($app) {
        $response = $app->response();
        $response->status(200);
        $response->write('The API is working');
    });

/**
* Add a scan result
*/
$app->post('/AddScanResults', 'addScanResults');

$app->get('/Vertices', 'getVertices');

$app->get('/Test/:userId', 'getTest');

$app->get('/Data', 'getAllData');

$app->get('/DataAverage', 'getDataAverage');

$app->get('/DataNoDirection', 'getDataNoDirection');



$app->run();


function getTest($userId){
	echo $userId;
}


/**
* A function to add a class
*/
function addScanResults(){


    $results = json_decode(Slim::getInstance()->request()->post('scanResults'), true);
    try {
	    $db = getConnection();
		$select = "SELECT idVertex FROM Vertices WHERE X=:coordinateX and Y=:coordinateY";
		$coordinate = $results['Coordinates'];
		$stmt = $db->prepare($select);
		$stmt->bindParam("coordinateX", $coordinate[0]);
		$stmt->bindParam("coordinateY", $coordinate[1]);
		$stmt->execute();
		$vertexId = $stmt->fetchAll(PDO::FETCH_OBJ);
		if(empty($vertexId)){
			$insertVertex = "INSERT INTO Vertices(X, Y) VALUE(:coordinateX, :coordinateY)";
			$stmt = $db->prepare($insertVertex);
			$stmt->bindParam("coordinateX", $coordinate[0]);
			$stmt->bindParam("coordinateY", $coordinate[1]);
			$stmt->execute();
			$vertexId = $db->lastInsertId();
		}
		else{
			$vertexId = $vertexId[0]->idVertex;
		}
		$insertDirection = "INSERT INTO DirectionTime(direction) VALUE(:direction)";
		$stmt = $db->prepare($insertDirection);
		$stmt->bindParam("direction", $results['Direction']);
		$stmt->execute();
		$directionId = $db->lastInsertId();
        foreach ($results['Tests'] as $test) {
            foreach($test as $accesspoint){
				$selectMac = "SELECT idMac FROM Mac WHERE macAddress=:address";
				$stmt = $db->prepare($selectMac);
				$stmt->bindParam("address", $accesspoint['AccessPoint']);
				$stmt->execute();
				$macId = $stmt->fetchAll(PDO::FETCH_OBJ);
				if(empty($macId)){
					$insertMac = "INSERT INTO Mac(macAddress) VALUE(:address)";
					$stmt = $db->prepare($insertMac);
					$stmt->bindParam("address", $accesspoint['AccessPoint']);
					$stmt->execute();
					$macId = $db->lastInsertId();
				}
				else{
					$macId = $macId[0]->idMac;
				}
				$finalInsert = "INSERT INTO Data(idVertex, idMac, idDirectionTime, strength) VALUE(:vertex, :mac, :direction, :strength)";
				$stmt = $db->prepare($finalInsert);
				$stmt->bindParam("vertex", $vertexId);
				$stmt->bindParam("mac", $macId);
				$stmt->bindParam("direction", $directionId);
				$stmt->bindParam("strength", $accesspoint['SignalStrength']);
				$stmt->execute();
			
			}
        }

        $db = null;
    } catch(PDOException $e) {
    echo '{"error":{"text":'. $e->getMessage() .'}}'; 
    }   
}

function getVertices(){
	$db = getConnection();
	$select = "SELECT X, Y FROM Vertices";
	$stmt = $db->prepare($select);
	$stmt->execute();
	$vertices = $stmt->fetchAll(PDO::FETCH_OBJ);
	echo '{"Vertices": ' . json_encode($vertices) . '}';
	$db = null;
}

function getAllData(){
	$db = getConnection();
	$select = "SELECT idVertex, X, Y FROM Vertices";
	$stmt = $db->prepare($select);
	$stmt->execute();
	$vertices = $stmt->fetchAll(PDO::FETCH_OBJ);
	echo '{"Vertices": [';
	$i = 0;
	foreach ($vertices as $vertex) {
		if($i != 0) {
			echo ',';
		} else {
			$i++;
		}
		echo '{"X": ' . $vertex->X . ', "Y": ' . $vertex->Y . ', "Directions": {';
		$select = "SELECT distinct Data.idMac, macAddress from Data join DirectionTime join Mac join Vertices where Data.idDIrectionTime=DirectionTime.idDirectionTime and Data.idVertex=Vertices.idVertex and Data.idMac=Mac.idMac and Vertices.idVertex=:vertexId and DirectionTime.direction='N'";
		$stmt = $db->prepare($select);
		$stmt->bindParam("vertexId", $vertex->idVertex);
		$stmt->execute();
		$north = $stmt->fetchAll(PDO::FETCH_OBJ);
		echo '"North": [';
		$i = 0;
		foreach ($north as $mac) {
			if($i != 0) {
			echo ',';
			} else {
				$i++;
			}
			echo '{"AccessPoint": "';
			echo $mac->macAddress;
			echo '", "Strengths": [';
			$select = "SELECT strength from Data join DirectionTime join Mac join Vertices where Data.idDIrectionTime=DirectionTime.idDirectionTime and Data.idVertex=Vertices.idVertex and Data.idMac=Mac.idMac and Vertices.idVertex=:vertexId and DirectionTime.direction='N' and Mac.idMac=:macId";
			$stmt = $db->prepare($select);
			$stmt->bindParam("vertexId", $vertex->idVertex);
			$stmt->bindParam("macId", $mac->idMac);
			$stmt->execute();
			$strengths = $stmt->fetchAll(PDO::FETCH_OBJ);
			$i = 0;
			foreach ($strengths as $strength) {
				if($i != 0) {
				echo ',';
				} else {
					$i++;
				}
				echo $strength->strength;
			}
			echo']}';
		}
		$select = "SELECT distinct Data.idMac, macAddress from Data join DirectionTime join Mac join Vertices where Data.idDIrectionTime=DirectionTime.idDirectionTime and Data.idVertex=Vertices.idVertex and Data.idMac=Mac.idMac and Vertices.idVertex=:vertexId and DirectionTime.direction='E'";
		$stmt = $db->prepare($select);
		$stmt->bindParam("vertexId", $vertex->idVertex);
		$stmt->execute();
		$east = $stmt->fetchAll(PDO::FETCH_OBJ);
		echo '], "East": [';
		$i = 0;
		foreach ($east as $mac) {
			if($i != 0) {
			echo ',';
			} else {
				$i++;
			}
			echo '{"AccessPoint": "';
			echo $mac->macAddress;
			echo '", "Strengths": [';
			$select = "SELECT strength from Data join DirectionTime join Mac join Vertices where Data.idDIrectionTime=DirectionTime.idDirectionTime and Data.idVertex=Vertices.idVertex and Data.idMac=Mac.idMac and Vertices.idVertex=:vertexId and DirectionTime.direction='E' and Mac.idMac=:macId";
			$stmt = $db->prepare($select);
			$stmt->bindParam("vertexId", $vertex->idVertex);
			$stmt->bindParam("macId", $mac->idMac);
			$stmt->execute();
			$strengths = $stmt->fetchAll(PDO::FETCH_OBJ);
			$i = 0;
			foreach ($strengths as $strength) {
				if($i != 0) {
				echo ',';
				} else {
					$i++;
				}
				echo $strength->strength;
			}
			echo']}';
		}
		$select = "SELECT distinct Data.idMac, macAddress from Data join DirectionTime join Mac join Vertices where Data.idDIrectionTime=DirectionTime.idDirectionTime and Data.idVertex=Vertices.idVertex and Data.idMac=Mac.idMac and Vertices.idVertex=:vertexId and DirectionTime.direction='S'";
		$stmt = $db->prepare($select);
		$stmt->bindParam("vertexId", $vertex->idVertex);
		$stmt->execute();
		$south = $stmt->fetchAll(PDO::FETCH_OBJ);
		echo '], "South": [';
		$i = 0;
		foreach ($south as $mac) {
			if($i != 0) {
			echo ',';
			} else {
				$i++;
			}
			echo '{"AccessPoint": "';
			echo $mac->macAddress;
			echo '", "Strengths": [';
			$select = "SELECT strength from Data join DirectionTime join Mac join Vertices where Data.idDIrectionTime=DirectionTime.idDirectionTime and Data.idVertex=Vertices.idVertex and Data.idMac=Mac.idMac and Vertices.idVertex=:vertexId and DirectionTime.direction='S' and Mac.idMac=:macId";
			$stmt = $db->prepare($select);
			$stmt->bindParam("vertexId", $vertex->idVertex);
			$stmt->bindParam("macId", $mac->idMac);
			$stmt->execute();
			$strengths = $stmt->fetchAll(PDO::FETCH_OBJ);
			$i = 0;
			foreach ($strengths as $strength) {
				if($i != 0) {
				echo ',';
				} else {
					$i++;
				}
				echo $strength->strength;
			}
			echo']}';
		}
		$select = "SELECT distinct Data.idMac, macAddress from Data join DirectionTime join Mac join Vertices where Data.idDIrectionTime=DirectionTime.idDirectionTime and Data.idVertex=Vertices.idVertex and Data.idMac=Mac.idMac and Vertices.idVertex=:vertexId and DirectionTime.direction='W'";
		$stmt = $db->prepare($select);
		$stmt->bindParam("vertexId", $vertex->idVertex);
		$stmt->execute();
		$west = $stmt->fetchAll(PDO::FETCH_OBJ);
		echo '], "West": [';
		$i = 0;
		foreach ($west as $mac) {
			if($i != 0) {
			echo ',';
			} else {
				$i++;
			}
			echo '{"AccessPoint": "';
			echo $mac->macAddress;
			echo '", "Strengths": [';
			$select = "SELECT strength from Data join DirectionTime join Mac join Vertices where Data.idDIrectionTime=DirectionTime.idDirectionTime and Data.idVertex=Vertices.idVertex and Data.idMac=Mac.idMac and Vertices.idVertex=:vertexId and DirectionTime.direction='W' and Mac.idMac=:macId";
			$stmt = $db->prepare($select);
			$stmt->bindParam("vertexId", $vertex->idVertex);
			$stmt->bindParam("macId", $mac->idMac);
			$stmt->execute();
			$strengths = $stmt->fetchAll(PDO::FETCH_OBJ);
			$i = 0;
			foreach ($strengths as $strength) {
				if($i != 0) {
				echo ',';
				} else {
					$i++;
				}
				echo $strength->strength;
			}
			echo']}';
		}

		echo ']}';
	}
	echo ']}';
	$db = null;
}


function getDataAverage(){
	$db = getConnection();
	$select = "SELECT idVertex, X, Y FROM Vertices";
	$stmt = $db->prepare($select);
	$stmt->execute();
	$vertices = $stmt->fetchAll(PDO::FETCH_OBJ);
	echo '{"Vertices": [';
	$i = 0;
	foreach ($vertices as $vertex) {
		if($i != 0) {
			echo ',';
		} else {
			$i++;
		}
		echo '{"X": ' . $vertex->X . ', "Y": ' . $vertex->Y . ', "Directions": {';
		$select = "SELECT distinct Data.idMac, macAddress from Data join DirectionTime join Mac join Vertices where Data.idDIrectionTime=DirectionTime.idDirectionTime and Data.idVertex=Vertices.idVertex and Data.idMac=Mac.idMac and Vertices.idVertex=:vertexId and DirectionTime.direction='N'";
		$stmt = $db->prepare($select);
		$stmt->bindParam("vertexId", $vertex->idVertex);
		$stmt->execute();
		$north = $stmt->fetchAll(PDO::FETCH_OBJ);
		echo '"North": [';
		$i = 0;
		foreach ($north as $mac) {
			if($i != 0) {
			echo ',';
			} else {
				$i++;
			}
			echo '{"AccessPoint": "';
			echo $mac->macAddress;
			echo '", "AverageStrength": ';
			$select = "SELECT AVG(strength) as strength from Data join DirectionTime join Mac join Vertices where Data.idDIrectionTime=DirectionTime.idDirectionTime and Data.idVertex=Vertices.idVertex and Data.idMac=Mac.idMac and Vertices.idVertex=:vertexId and DirectionTime.direction='N' and Mac.idMac=:macId";
			$stmt = $db->prepare($select);
			$stmt->bindParam("vertexId", $vertex->idVertex);
			$stmt->bindParam("macId", $mac->idMac);
			$stmt->execute();
			$strengths = $stmt->fetchAll(PDO::FETCH_OBJ);
			echo $strengths[0]->strength. '}';
		}
		$select = "SELECT distinct Data.idMac, macAddress from Data join DirectionTime join Mac join Vertices where Data.idDIrectionTime=DirectionTime.idDirectionTime and Data.idVertex=Vertices.idVertex and Data.idMac=Mac.idMac and Vertices.idVertex=:vertexId and DirectionTime.direction='E'";
		$stmt = $db->prepare($select);
		$stmt->bindParam("vertexId", $vertex->idVertex);
		$stmt->execute();
		$east = $stmt->fetchAll(PDO::FETCH_OBJ);
		echo '], "East": [';
		$i = 0;
		foreach ($east as $mac) {
			if($i != 0) {
			echo ',';
			} else {
				$i++;
			}
			echo '{"AccessPoint": "';
			echo $mac->macAddress;
			echo '", "AverageStrength": ';
			$select = "SELECT AVG(strength) as strength from Data join DirectionTime join Mac join Vertices where Data.idDIrectionTime=DirectionTime.idDirectionTime and Data.idVertex=Vertices.idVertex and Data.idMac=Mac.idMac and Vertices.idVertex=:vertexId and DirectionTime.direction='E' and Mac.idMac=:macId";
			$stmt = $db->prepare($select);
			$stmt->bindParam("vertexId", $vertex->idVertex);
			$stmt->bindParam("macId", $mac->idMac);
			$stmt->execute();
			$strengths = $stmt->fetchAll(PDO::FETCH_OBJ);
			echo $strengths[0]->strength. '}';
		}
		$select = "SELECT distinct Data.idMac, macAddress from Data join DirectionTime join Mac join Vertices where Data.idDIrectionTime=DirectionTime.idDirectionTime and Data.idVertex=Vertices.idVertex and Data.idMac=Mac.idMac and Vertices.idVertex=:vertexId and DirectionTime.direction='S'";
		$stmt = $db->prepare($select);
		$stmt->bindParam("vertexId", $vertex->idVertex);
		$stmt->execute();
		$south = $stmt->fetchAll(PDO::FETCH_OBJ);
		echo '], "South": [';
		$i = 0;
		foreach ($south as $mac) {
			if($i != 0) {
			echo ',';
			} else {
				$i++;
			}
			echo '{"AccessPoint": "';
			echo $mac->macAddress;
			echo '", "AverageStrength": ';
			$select = "SELECT AVG(strength) as strength from Data join DirectionTime join Mac join Vertices where Data.idDIrectionTime=DirectionTime.idDirectionTime and Data.idVertex=Vertices.idVertex and Data.idMac=Mac.idMac and Vertices.idVertex=:vertexId and DirectionTime.direction='S' and Mac.idMac=:macId";
			$stmt = $db->prepare($select);
			$stmt->bindParam("vertexId", $vertex->idVertex);
			$stmt->bindParam("macId", $mac->idMac);
			$stmt->execute();
			$strengths = $stmt->fetchAll(PDO::FETCH_OBJ);
			echo $strengths[0]->strength. '}';
		}
		$select = "SELECT distinct Data.idMac, macAddress from Data join DirectionTime join Mac join Vertices where Data.idDIrectionTime=DirectionTime.idDirectionTime and Data.idVertex=Vertices.idVertex and Data.idMac=Mac.idMac and Vertices.idVertex=:vertexId and DirectionTime.direction='W'";
		$stmt = $db->prepare($select);
		$stmt->bindParam("vertexId", $vertex->idVertex);
		$stmt->execute();
		$west = $stmt->fetchAll(PDO::FETCH_OBJ);
		echo '], "West": [';
		$i = 0;
		foreach ($west as $mac) {
			if($i != 0) {
			echo ',';
			} else {
				$i++;
			}
			echo '{"AccessPoint": "';
			echo $mac->macAddress;
			echo '", "AverageStrength": ';
			$select = "SELECT AVG(strength) as strength from Data join DirectionTime join Mac join Vertices where Data.idDIrectionTime=DirectionTime.idDirectionTime and Data.idVertex=Vertices.idVertex and Data.idMac=Mac.idMac and Vertices.idVertex=:vertexId and DirectionTime.direction='W' and Mac.idMac=:macId";
			$stmt = $db->prepare($select);
			$stmt->bindParam("vertexId", $vertex->idVertex);
			$stmt->bindParam("macId", $mac->idMac);
			$stmt->execute();
			$strengths = $stmt->fetchAll(PDO::FETCH_OBJ);
			echo $strengths[0]->strength. '}';
		}

		echo ']}';
	}
	echo ']}';
	$db = null;
}

function getDataNoDirection(){
	$db = getConnection();
	$select = "SELECT idVertex, X, Y FROM Vertices";
	$stmt = $db->prepare($select);
	$stmt->execute();
	$vertices = $stmt->fetchAll(PDO::FETCH_OBJ);
	echo '{"Vertices": [';
	$i = 0;
	foreach ($vertices as $vertex) {
		if($i != 0) {
			echo ',';
		} else {
			$i++;
		}
		echo '{"X": ' . $vertex->X . ', "Y": ' . $vertex->Y . ', "AccessPoints": [';
		$select = "SELECT distinct Data.idMac, macAddress from Data join DirectionTime join Mac join Vertices where Data.idDIrectionTime=DirectionTime.idDirectionTime and Data.idVertex=Vertices.idVertex and Data.idMac=Mac.idMac and Vertices.idVertex=:vertexId";
		$stmt = $db->prepare($select);
		$stmt->bindParam("vertexId", $vertex->idVertex);
		$stmt->execute();
		$accesspoints = $stmt->fetchAll(PDO::FETCH_OBJ);
		$i = 0;
		foreach ($accesspoints as $mac) {
			if($i != 0) {
			echo ',';
			} else {
				$i++;
			}
			echo '{"AccessPoint": "';
			echo $mac->macAddress;
			echo '", "AverageStrength": ';
			$select = "SELECT AVG(strength) as strength from Data join DirectionTime join Mac join Vertices where Data.idDIrectionTime=DirectionTime.idDirectionTime and Data.idVertex=Vertices.idVertex  and Data.idMac=Mac.idMac and Vertices.idVertex=:vertexId  and Mac.idMac=:macId";
			$stmt = $db->prepare($select);
			$stmt->bindParam("vertexId", $vertex->idVertex);
			$stmt->bindParam("macId", $mac->idMac);
			$stmt->execute();
			$strengths = $stmt->fetchAll(PDO::FETCH_OBJ);
			echo $strengths[0]->strength. '}';
		}
		echo ']}';
	}
	echo ']}';
	$db = null;
}


/**
* A function that sets up the connection to the database
*/
function getConnection() {
    $dbhost="localhost";
    $dbuser="root";
    $dbpass="";
    $dbname="accesspoint";
    $dbh = new PDO("mysql:host=$dbhost;dbname=$dbname", $dbuser, $dbpass);  
    $dbh->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    return $dbh;
}

?>
