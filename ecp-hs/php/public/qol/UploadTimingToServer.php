<?php

$target_path = '/data/ecp-hs/php/data/uploads/qol';

$target_path = $target_path . basename( $_FILES['uploaded_file']['name']);

if(move_uploaded_file($_FILES['uploaded_file']['tmp_name'], $target_path)) {
echo 'The file '. basename( $_FILES['uploaded_file']['name']).
' has been uploaded';
} else{
echo 'There was an error uploading the file, please try again!';
}

?>
