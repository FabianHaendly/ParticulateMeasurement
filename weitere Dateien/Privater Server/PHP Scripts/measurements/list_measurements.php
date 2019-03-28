<?php
include 'db/db_connect.php';
//Query to select movie id and movie name
$query = "SELECT synchronization_date FROM measurements";
$result = array();
$measurementArray = array();
$response = array();
//Prepare the query
if($stmt = $con->prepare($query)){
	$stmt->execute();
	//Bind the fetched data to $movieId and $movieName
	$stmt->bind_result($synchronizationDate);
	//Fetch 1 row at a time					
	while($stmt->fetch()){
		//Populate the movie array
		$measurementArray["synchronization_date"] = $synchronizationDate;
		$result[]=$measurementArray;
	}
	$stmt->close();
	$response["success"] = 1;
	$response["data"] = $result;
	
 
}else{
	//Some error while fetching data
	$response["success"] = 0;
	$response["message"] = mysqli_error($con);
	
}
//Display JSON response
echo json_encode($response);
 
?>