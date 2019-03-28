<?php
include 'db/db_connect.php';
$response = array();
 
//Check for mandatory parameters
if(isset($_POST['pm_ten'])&&isset($_POST['pm_twenty_five'])&&isset($_POST['measurement_date'])&&isset($_POST['latitude'])&&isset($_POST['longitude'])&&isset($_POST['altitude'])&&isset($_POST['sensor_id'])){
	$pmTen = $_POST['pm_ten'];
	$pmTwentyFive = $_POST['pm_twenty_five'];
	$measurementDate = $_POST['measurement_date'];
	$latitude = $_POST['latitude'];
	$longitude = $_POST['longitude'];
	$altitude = $_POST['altitude'];
	$sensorId = $_POST['sensor_id'];
	
	$query = "INSERT INTO measurements(pm_ten, pm_twenty_five, measurement_date, latitude, longitude, altitude, sensor_id) VALUES (?,?,?,?,?,?,?)";
	if($stmt = $con->prepare($query)){
		$stmt->bind_param("sssssss",$pmTen,$pmTwentyFive,$measurementDate,$latitude,$longitude,$altitude,$sensorId);
		$stmt->execute();
		if($stmt->affected_rows == 1){
			$response["success"] = 1;			
			$response["message"] = "Measurement Successfully Added";			
			
		}else{
			$response["success"] = 0;
			$response["message"] = "Error while adding measurement";
		}					
	}else{
		$response["success"] = 0;
		$response["message"] = mysqli_error($con);
	}
 
}else{
	$response["success"] = 0;
	$response["message"] = "missing mandatory parameters";
}
echo json_encode($response);
?>