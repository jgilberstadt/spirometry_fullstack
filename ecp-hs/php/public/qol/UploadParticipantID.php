<?php
error_log("UploadParticipantID.php called");

  if (!isset($_POST["pid"])) {
    echo "Not enough params";
    exit();
  }

  $pid = $_POST["pid"];

	require_once '/data/ecp-hs/php/public/qol/android_login_api/include/DB_Functions.php';

	$db = new DB_Functions();

  $success = $db -> uploadPID($pid);

  if (!$success) {
    echo "The participant ID is likely already taken";
  } else {
    echo "success";
  }

?>
