<?php
    $con = mysqli_connect("localhost", "root", "mysql_db", "spirometry_db");
    if (isset($_POST['name'])) {
	           $name = $_POST['name'];
		      }

       if (isset($_POST['age'])) {
	             $age = $_POST['age'];
		         }

       if (isset($_POST['patientid'])) {
	              $patientid = $_POST['patientid'];
		         }

       if (isset($_POST['password'])) {
	              $password = $_POST['password'];
       }
	        $statement = mysqli_prepare($con, "INSERT INTO user (name, patientid, age, password) VALUES (?, ?, ?, ?)");
        mysqli_stmt_bind_param($statement, "ssis", $name, $patientid, $age, $password);
        mysqli_stmt_execute($statement);
	    
	    $response = array();
	    $response["success"] = true;  
	        
	        echo json_encode($response);
?>

