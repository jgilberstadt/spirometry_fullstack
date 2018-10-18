<?php
    $con = mysqli_connect("localhost", "root", "mysql_db", "spirometry_db");

     if (isset($_POST['patientid'])) {
	  $patientid = $_POST['patientid'];
		                           }

     if (isset($_POST['password'])) { 
	$password = $_POST['password'];
				            }


    $statement = mysqli_prepare($con, "SELECT * FROM user WHERE patientid = ? AND password = ?");
    mysqli_stmt_bind_param($statement, "ss", $patientid, $password);
    mysqli_stmt_execute($statement);

    mysqli_stmt_store_result($statement);
    mysqli_stmt_bind_result($statement, $userID, $name, $patientid, $age, $password);

    $response = array();
    $response["success"] = false;

    while(mysqli_stmt_fetch($statement)){
        $response["success"] = true;
        $response["name"] = $name;
        $response["age"] = $age;
        $response["patientid"] = $patientid;
        $response["password"] = $password;
    }

    echo json_encode($response);
?>


