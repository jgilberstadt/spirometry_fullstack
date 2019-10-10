<?php

require_once '/data/ecp-hs/php/public/qol/android_login_api/include/DB_Functions.php';

$db = new DB_Functions();

if(isset($_POST['isBaseline']) && isset($_POST['id']) && isset($_POST['time_stamp'])) {
  $surveyExist = $db->doesSurveyExist($_POST['isBaseline'], $_POST['id'], $_POST['time_stamp']);
  echo $surveyExist;
} else {
  echo "not enough params";
}
 ?>
