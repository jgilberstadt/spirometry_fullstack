<?php
require_once 'DB_Functions.php';
$db = new DB_Functions();

$target_path = 'uploads/';
$target_path = $target_path . basename($_FILES['uploaded_file']['name']);

if(move_uploaded_file($_FILES['uploaded_file']['tmp_name'], $target_path)) {
echo 'The file '. basename( $_FILES['uploaded_file']['name']).
' has been uploaded';
} else{
echo "There was an error uploading the file, please try again!";
}
$file_name = basename( $_FILES['uploaded_file']['name']); // file name is patient id
if(strpos($file_name, 'yesVarianceYesSymptoms') {
	// send email to enrolling center
} elseif(strpos($file_name, 'yesVarianceNoSymptoms') {
    // store questionnaire and store test results
}

$ans_file = fopen($target_path, 'r') or die('Unable to open file');
$ans_text = fread($ans_file, filesize($target_path));

// extract data field for spiro, pulseox, survey
$dataArr = explode("\n", $data);
$patient_id = $dataArr[0];
// blow
$fev1_array = array();
$pulse_boolean;
$o2sat_boolean;
$spiro_boolean;
$blows = explode("!", $dataArr[1]);
for($x=0; $x<6; $x++) {
  array_push($fev1_array, $blows[$x]);
}
$pulseox = explode("!", $dataArr[2]);
$lowestSat = $pulseox[0];
$minHR = $pulseox[1];
$maxHR = $pulseox[2];
$timeAbnormal = $pulseox[3];
$timeMinRate = $pulseox[4];
if($minHR < 60 || $maxHR > 100) {
  $pulse_boolean = 1;
}
else {
  $pulse_boolean = 0;
}
if($lowestSat > 97) {
  $o2sat_boolean = 0;
}
else if($lowestSat <= 97 && $lowestSat > 91) {
  $o2sat_boolean = 1;
}
else {
  $o2sat_boolean = 2;
}
$result = $db->storeFVCRecordsToPostgres($patient_id, $pef, $fev1_array[0], $fev1_array[1], $fev1_array[2], $fev1_array[3], $fev1_array[4], $fev1_array[5], $fvc, $fev1_fevc, $fev6, $fef2575)


if ($result) {
  return "success";
} else {
  return "data not stored";
}

?>
