<?php
require_once 'DB_Functions.php';
$db = new DB_Functions();

// json response array
$response = array("next_response" => 0);

if(isset($_POST['patient_id'])) {
	$patient_id = $_POST['patient_id'];
	$pef = $_POST['pef'];
	$fev1 = $_POST['fev1'];
	$peftime = $_POST['peftime'];
	$evol = $_POST['evol'];

	$db->storePefFEV1Records($patient_id, $pef, $fev1, $peftime, $evol);

	echo json_encode($response);
}

?>
