<?php
require_once 'DB_Functions.php';
$db = new DB_Functions();

// json response array
$response = array("next_response" => 0);

if(isset($_POST['patient_id'])) {
	$patient_id = $_POST['patient_id'];
	$pef = $_POST['pef'];
	$fev1 = $_POST['fev1'];
	$fvc = $_POST['fvc'];
	$fev1_fvc = $_POST['fev1_fvc'];
	$fev6 = $_POST['fev6'];
	$fef2575 = $_POST['fef2575'];

	$db->storeFVCRecords($patient_id, $pef, $fev1, $fvc, $fev1_fvc, $fev6, $fef2575);

	echo json_encode($response);
}

// if(isset($_POST['patient_id'])) {
// 	$patient_id = $_POST['patient_id'];
// 	// $pef = $_POST['pef'];
// 	$fev11 = $_POST['fev11'];
// 	$fev12 = $_POST['fev12'];
// 	$fev13 = $_POST['fev13'];
// 	$fev14 = $_POST['fev14'];
// 	$fev15 = $_POST['fev15'];
// 	$fev16 = $_POST['fev16'];
// 	// $fvc = $_POST['fvc'];
// 	// $fev1_fvc = $_POST['fev1_fvc'];
// 	// $fev6 = $_POST['fev6'];
// 	// $fef2575 = $_POST['fef2575'];

// 	$db->storeFVCRecordsToPostgres($patient_id, $pef, $fev11,$fev12,$fev13,$fev14,$fev15,$fev16, $fvc, $fev1_fvc, $fev6, $fef2575);

// 	echo json_encode($response);
// }

?>
