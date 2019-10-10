<?php

require_once '/data/ecp-hs/php/public/qol/android_login_api/include/DB_Functions.php';

$db = new DB_Functions();
$a = array();
$b = array();

$user_responses = $db->getAllUsers();
if($user_responses != false) {
    while ($row = pg_fetch_array($user_responses)) {
        $b["patient_id"] = $row["patient_id"];
        $b["which_response"] = $row["which_response"];
        $b["time_complete"] = $row["time_complete"];
        array_push($a,$b);
    }
    echo json_encode($a);
}

 ?>
