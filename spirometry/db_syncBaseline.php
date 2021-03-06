<?php

require_once '/data/ecp-hs/php/public/qol/android_login_api/include/DB_Functions.php';

$db = new DB_Functions();
$a = array();
$b = array();

if (isset($_POST['siteId'])) {
	$siteId = $_POST['siteId'];
	$baseline_surveys = $db->getAllBaseline($siteId);
		if($baseline_surveys != false) {
		while ($row = pg_fetch_array($baseline_surveys)) {
			$b["error"] = FALSE;
			$b["temp_id"] = $row["temp_id"];
			$b["time_complete"] = $row["time_complete"];
			$b["site_id"] = $siteId;
			array_push($a,$b);
		}
		echo json_encode($a);
	} else {
		$response["error"] = TRUE;
		$response["error_msg"] = "Couldn't get surveys with siteId";
		echo json_encode($response);
	}
} else {
    // required get params is missing
    $response["error"] = TRUE;
    $response["error_msg"] = "No siteId";
    echo json_encode($response);
}

 ?>
