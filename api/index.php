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
			$insertVertex = "INSERT INTO vertices(X, Y) VALUE(:coordinateX, :coordinateY)";
			$stmt = $db->prepare($insertVertex);
			$stmt->bindParam("coordinateX", $coordinate[0]);
			$stmt->bindParam("coordinateY", $coordinate[1]);
			$stmt->execute();
			$vertexId = $db->lastInsertId();
		}
		else{
			$vertexId = $vertexId[0]->idVertex;
		}
		$insertDirection = "INSERT INTO directiontime(direction) VALUE(:direction)";
		$stmt = $db->prepare($insertDirection);
		$stmt->bindParam("direction", $results['Direction']);
		$stmt->execute();
		$directionId = $db->lastInsertId();
        foreach ($results['Tests'] as $test) {
            foreach($test as $accesspoint){
				$selectMac = "SELECT idMac FROM mac WHERE macAddress=:address";
				$stmt = $db->prepare($selectMac);
				$stmt->bindParam("address", $accesspoint['Accesspoint']);
				$stmt->execute();
				$macId = $stmt->fetchAll(PDO::FETCH_OBJ);
				if(empty($macId)){
					$insertMac = "INSERT INTO mac(macAddress) VALUE(:address)";
					$stmt = $db->prepare($insertMac);
					$stmt->bindParam("address", $accesspoint['Accesspoint']);
					$stmt->execute();
					$macId = $db->lastInsertId();
				}
				else{
					$macId = $macId[0]->idMac;
				}
				$finalInsert = "INSERT INTO data(idVertex, idMac, idDirectionTime, strength) VALUE(:vertex, :mac, :direction, :strength)";
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

/**
* A function that sets up the connection to the database
*/
function getConnection() {
    $dbhost="localhost";
    $dbuser="root";
    $dbpass="0000";
    $dbname="accesspoint";
    $dbh = new PDO("mysql:host=$dbhost;dbname=$dbname", $dbuser, $dbpass);  
    $dbh->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    return $dbh;
}

?>
