<?php

require_once '/data/ecp-hs/php/public/qol/android_login_api/include/DB_Functions.php';

$db = new DB_Functions();

if (isset($_POST['tempId']) && isset($_POST['patientId'])) {
	$tempId = $_POST['tempId'];
  $patientId = $_POST['patientId'];
	$enroll_response = $db->enrollBaseline($patientId, $tempId);
	if ($enroll_response == "success") {
		$response["error_msg"] = "success";
		$response["error"] = false;

	} else {
		$response["error_msg"] = $enroll_response;
		$response["error"] = true;
	}
  echo json_encode($response);

} else {
    // required get params is missing
    $response["error"] = TRUE;
    $response["error_msg"] = "No tempId or patientId";
    echo json_encode($response);
}

 ?>
