<?php 
require_once '/data/ecp-hs/php/public/qol/android_login_api/include/DB_Functions.php'; 
$db = new DB_Functions(); 
 
// json response array 
$response = array("error" => FALSE); 
 
if (isset($_POST['tablet_id']) && isset($_POST['numBaseline'])) { 
 
    // receiving the post params 
    $tabletId = $_POST['tablet_id']; 
    $numBaseline = $_POST['numBaseline']; 
 
    // get the tabletID 
    $updatedCount = $db->updateBaselineCounter($tabletId, $numBaseline); 
 
    if ($updatedCount != false) { 
        // use is found 
        $response["error"] = FALSE; 
        $response["updated_count"] = $updatedCount; 
        echo json_encode($response); 
    } else { 
        $response["error"] = TRUE; 
        $response["error_msg"] = "Couldn't update baseline counter"; 
        echo json_encode($response); 
    } 
} else { 
    // required post params is missing 
    $response["error"] = TRUE; 
    $response["error_msg"] = "No tablet id or last assigned count!"; 
    echo json_encode($response); 
} 
?>
