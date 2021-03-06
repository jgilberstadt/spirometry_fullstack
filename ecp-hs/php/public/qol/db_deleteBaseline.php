<?php

require_once '/data/ecp-hs/php/public/qol/android_login_api/include/DB_Functions.php';

$db = new DB_Functions();

if (isset($_POST['tempId'])) {
	$tempId = $_POST['tempId'];
	$delete_response = $db->deleteBaseline($tempId);
	if ($delete_response == "success") {
		$response["error_msg"] = "success";
		$response["error"] = false;
	} else {
		$response["error_msg"] = $delete_response;
		$response["error"] = true;
	}
  echo json_encode($response);
	exit();

} else {
    // required get params is missing
    $response["error"] = TRUE;
    $response["error_msg"] = "No tempId";
    echo json_encode($response);
}

 ?>
