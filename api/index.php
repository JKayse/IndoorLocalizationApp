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


$app->run();


/**
* A function to add a class
*/
function addScanResults(){


    $results = json_decode(Slim::getInstance()->request()->post('scanResults'), true);
    try {

        $insertResult = "INSERT INTO tests(coordinates, direction, reading) VALUE(PointFromText(:coordinates), :direction, :reading)";
        $db = getConnection();
        $coordinate = $results['Coordinates'];

        $finalCoordinate = 'POINT(' . $coordinate[0] . ' ' . $coordinate[1] . ')';
        $direction = $results['Direction'];

        echo $direction;
        echo $finalCoordinate;
        foreach ($results['Tests'] as $test) {
            $stmt = $db->prepare($insertResult);

            $stringTest = json_encode($test);

            $stmt->bindParam("coordinates", $finalCoordinate);
            $stmt->bindParam("direction", $direction);
            $stmt->bindParam("reading", $stringTest);

            echo $stringTest;
            $stmt->execute();
        }

        $db = null;
    } catch(PDOException $e) {
    echo '{"error":{"text":'. $e->getMessage() .'}}'; 
    }   
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
