<?php
require_once 'DB_Functions.php';
$db = new DB_Functions();

$target_path = '/data/ecp-hs/php/data/hs_uploads/';
$target_path = $target_path . basename($_FILES['uploaded_file']['name']);

if(move_uploaded_file($_FILES['uploaded_file']['tmp_name'], $target_path)) {
echo 'The file '. basename( $_FILES['uploaded_file']['name']).
' has been uploaded';
} else{
echo "There was an error uploading the file, please try again! (May consider changing the ownership to be apache for all associated files and folders)";
}
$file_name = basename( $_FILES['uploaded_file']['name']); // file name is patient id
if(strpos($file_name, 'yesVarianceYesSymptoms')) {
	// send email to enrolling center
} elseif(strpos($file_name, 'yesVarianceNoSymptoms')) {
    // store questionnaire and store test results
}

$ans_file = fopen($target_path, 'r') or die('Unable to open file');
$ans_text = fread($ans_file, filesize($target_path));

// extract data field for spiro, pulseox, survey
$dataArr = explode("\n", $ans_text);
$meta = explode("!",$dataArr[0]);
$imei_num = $meta[0];
$test_date = $meta[1];
$spiro_boolean = $meta[2];
$repeated_counter = $meta[3];

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

$raw_fvc = $dataArr[3];
$raw_pef = $dataArr[4];
$raw_pulse = implode("\n",array_slice($dataArr,5,-2));


$unique_id = $db->storeRecordsToPostgres($imei_num, $test_date, $fev1_array[0], $fev1_array[1], $fev1_array[2], $fev1_array[3], $fev1_array[4], $fev1_array[5], $pulse_boolean, $o2sat_boolean, $spiro_boolean, $repeated_counter, $minHR, $maxHR, $lowestSat, $timeAbnormal, $timeMinRate,  $raw_fvc, $raw_pef, $raw_pulse);

if($spiro_boolean==1 and $unique_id!=false) {
  $survey_meta = $dataArr[count($dataArr)-2];
  $survey = $dataArr[count($dataArr)-1];
  $db->storeSurveyResults($imei_num, $test_date, $survey_meta, $survey, $unique_id);
}

/*

if ($result) {
  return "success";
} else {
  return "data not stored";
}
*/

?>
