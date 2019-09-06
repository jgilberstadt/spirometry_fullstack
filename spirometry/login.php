<?php
require_once '/data/ecp-hs/php/public/spirometry/DB_Functions.php';
$db = new DB_Functions();

// json response array
$response = array("error" => FALSE);
$version = "1.0";

if (isset($_POST['patient_id']) ){

    // receiving the post params
    $patient_id = $_POST['patient_id'];

    // get the patient's normal range by patient id
    $user = $db->getNormalRange($patient_id);

    if ($user != false) {
        // user is found
        $response["error"] = FALSE;
        $response["user"]["normal_range"] = $user["normal_range"];
        $response["user"]["patient_id"] = $user["patient_id"];
        echo json_encode($response);
    } else {
        // user is not found with the credentials
        $response["error"] = TRUE;
        $response["error_msg"] = "PatientId is wrong. Please try again!";
		$response["version"] = $version;
        echo json_encode($response);
    }
} else {
    // required post params is missing
    $response["error"] = TRUE;
    $response["error_msg"] = "Required parameter patient_id is missing!";
	$response["version"] = $version;
    echo json_encode($response);
}
?>
