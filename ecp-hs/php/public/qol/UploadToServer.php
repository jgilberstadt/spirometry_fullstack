<?php
error_log("UploadToServer.php called");
$target_path = '/data/ecp-hs/php/data/uploads/qol/';

$target_path = $target_path . basename($_FILES['uploaded_file']['name']);

if(move_uploaded_file($_FILES['uploaded_file']['tmp_name'], $target_path)) {
    echo 'The file '. basename( $_FILES['uploaded_file']['name']). ' has been uploaded';
} else{
	error_log("There was an error uploading the file, please try again!");
    echo "There was an error uploading the file, please try again!";
}

	//Parse answer file
	$ans_file = fopen($target_path, 'r') or die('Unable to open file');
	$ans_text = fread($ans_file, filesize($target_path));
	$title_line = explode("\n",$ans_text);
	require_once '/data/ecp-hs/php/public/qol/android_login_api/include/DB_Functions.php';

	$db = new DB_Functions();

	$insert_id = $db->storeRecords($title_line[59], $title_line[60], $title_line[61], $title_line[62], $title_line[63], $title_line[64], $title_line[65],
	$title_line[66], $title_line[67], $title_line[68], $title_line[69], $title_line[70]);

	// put all answers into array
	$ans_array_nonencrypt = array('survey_answer_counter'=>$insert_id,
	'q1'=>$title_line[0], 'q2'=>$title_line[1],
	'q3'=>$title_line[2], 'q4'=>$title_line[3],
	'q5'=>$title_line[4], 'q6'=>$title_line[5],
	'q7'=>$title_line[6], 'q8'=>$title_line[7],
	'q9'=>$title_line[8], 'q10'=>$title_line[9],
	 'q11'=>$title_line[10], 'q12'=>$title_line[11],
	 'q13'=>$title_line[12], 'q14'=>$title_line[13],
	 'q15'=>$title_line[14], 'q16'=>$title_line[15],
	 'q17'=>$title_line[16], 'q18'=>$title_line[17],
	 'q19'=>$title_line[18], 'q20'=>$title_line[19],
	 'q21'=>$title_line[20], 'q22'=>$title_line[21],
	 'q23'=>$title_line[22], 'q24'=>$title_line[23],
	 'q25'=>$title_line[24], 'q26'=>$title_line[25],
	 'q27'=>$title_line[26], 'q28'=>$title_line[27],
	 'q29'=>$title_line[28], 'q30'=>$title_line[29],
	 'q31'=>$title_line[30], 'q32'=>$title_line[31],
	 'q33'=>$title_line[32],'q34'=>$title_line[33],
	'q35'=>$title_line[34],'q36'=>$title_line[35],'q37'=>$title_line[36],
	'q38'=>$title_line[37],'q39'=>$title_line[38],'q40'=>$title_line[39],
	'q41'=>$title_line[40],'q42'=>$title_line[41],'q43'=>$title_line[42],
	'q44'=>$title_line[43],'q45'=>$title_line[44],'q46'=>$title_line[45],
	'q47'=>$title_line[46],'q48'=>$title_line[47],'q49'=>$title_line[48],
	'q50'=>$title_line[49],'q51'=>$title_line[50],'q52'=>$title_line[51],
	'q53'=>$title_line[52],'q54'=>$title_line[53],'q55'=>$title_line[54],
	'q56'=>$title_line[55],'q57'=>$title_line[56],'q58'=>$title_line[57],
	'q59'=>$title_line[58]);

	$db->storeToFullTable($ans_array_nonencrypt);
	//$db->writeToCSV($insert_id,$title_line, $ans_array);

	echo "yes";

?>
