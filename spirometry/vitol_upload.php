<?php
require_once 'DB_Functions.php';
$db = new DB_Functions();

// json response array
$response = array("next_response" => 0);

if(isset($_POST['patient_id'])) {
	$patient_id = $_POST['patient_id'];
	$test_result = $_POST['test_result'];

	$db->storeVitolRecords($patient_id, $test_result);

	echo json_encode($response);
}
?>
