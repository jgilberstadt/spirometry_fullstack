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

$db->storeMetadata($file_name); // check metadata for patient_id which right now is the tablet serial

$data = gzinflate(substr($ans_text, 2));
$dataArr = explode("\n", $data);
//$dataArr[0] = gzdeflate($dataArr[0], 9);
$dataArr[1] = gzdeflate($dataArr[1], 9);
$dataArr[2] = gzdeflate($dataArr[2], 9);

$pulseData = explode("!", $dataArr[3]);

$result = $db->storeData($file_name, $dataArr, $pulseData);

if ($result) {
  return "success";
} else {
  return "data not stored";
}

?>
