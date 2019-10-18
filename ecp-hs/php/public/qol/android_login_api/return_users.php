<?php

require_once '/data/ecp-hs/php/public/android_login_api/Get_ID.php';

$db = new Get_ID();
$a = array();
$b = array();

$users = $db->query_users();
if($users != false) {
	while ($row = mysqli_fetch_array($users)) {
		$b["email"] = $row["email"];
		$b["encrypted_password"] = $row["encrypted_password"];
		$b["salt"] = $row["salt"];
		array_push($a,$b);
	}
	echo json_encode($a);
}

 ?>
