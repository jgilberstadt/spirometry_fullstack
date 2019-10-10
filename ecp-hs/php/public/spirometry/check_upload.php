<?php
  //File to confirm the text file is successfully uploaded.
  $path = '/data/ecp-hs/php/data/hs_uploads/';
  // json response array
  $response = array("error" => TRUE);
  if(isset($_POST['file_name'])){
    // check whether this file exists
    $file_name = $_POST['file_name'];
    if(file_exists($path.$file_name)) {
      $response["error"] = FALSE;
    }
  }
  echo json_encode($response);
?>
