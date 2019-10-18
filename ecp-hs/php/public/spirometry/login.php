<?php
require_once '/data/ecp-hs/php/public/spirometry/DB_Functions.php';
$db = new DB_Functions();

// json response array
$response = array("error" => FALSE);
$version = "1.0";

if (isset($_POST['imei_num']) ){

    // receiving the post params
    $imei_num = $_POST['imei_num'];

    // get the patient's normal range by patient id
    $user = $db->getNormalRange($imei_num);

    if ($user != false) {
        // user is found
        $response["error"] = FALSE;
        $response["user"]["normal_range"] = $user["normal_range"];
        $response["user"]["imei_num"] = $user["imei_num"];
        $response["user"]["mode"] = $user["mode"];
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
    $response["error_msg"] = "Required parameter imei_num is missing!";
	$response["version"] = $version;
    echo json_encode($response);
}
?>
