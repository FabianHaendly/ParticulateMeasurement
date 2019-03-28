<?php
define('DB_USER', "root");
define('DB_PASSWORD', "");
define('DB_DATABASE', "measurements"); // database name
define('DB_SERVER', "localhost"); // db server
 
$con = mysqli_connect(DB_SERVER,DB_USER,DB_PASSWORD,DB_DATABASE);
 
// Check connection
if(mysqli_connect_errno()){
	echo "Failed to connect to MySQL: " . mysqli_connect_error();
}
?>