<?php 
require_once '/data/ecp-hs/php/public/qol/android_login_api/include/DB_Functions.php'; 
$db = new DB_Functions(); 
 
// json response array 
$response = array("error" => FALSE); 
 
if (isset($_POST['serial_num']) && isset($_POST['siteID'])) { 
 
    // receiving the post params 
    $serial_num = $_POST['serial_num']; 
    $siteID = $_POST['siteID']; 
 
    // get the tabletID 
    $tabletResponse = $db->getTabletID($serial_num, $siteID); 
 
    if ($tabletResponse != false) { 
        // use is found 
        $response["error"] = FALSE; 
        $response["tabletId"] = $tabletResponse["tabletId"]; 
        $response["baseline_counter"] = $tabletResponse["baseline_counter"]; 
        echo json_encode($response); 
    } else { 
        $response["error"] = TRUE; 
        $response["error_msg"] = "Couldn't get or insert a row for this tablet"; 
        echo json_encode($response); 
    } 
} else { 
    // required post params is missing 
    $response["error"] = TRUE; 
    $response["error_msg"] = "No tablet serial number or site ID!"; 
    echo json_encode($response); 
} 
?>
