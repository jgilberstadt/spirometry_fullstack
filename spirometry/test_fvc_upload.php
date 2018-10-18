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
?>
