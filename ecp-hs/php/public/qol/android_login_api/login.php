<?php
require_once '/data/ecp-hs/php/public/qol/android_login_api/include/DB_Functions.php';
$db = new DB_Functions();

// json response array
$response = array("error" => FALSE);
$version = "1.0";

if (isset($_POST['email']) && isset($_POST['password'])) {

    // receiving the post params
    $email = $_POST['email'];
    $password = $_POST['password'];

    // get the user by email and password
    $user = $db->getUserByEmailAndPassword($email, $password);

    if ($user != false) {
        // user is found
        $response["error"] = FALSE;
        $response["user"]["site_id"] = $user["site_id"];
        $response["user"]["email"] = $user["email"];
		$response["version"] = $version;
        echo json_encode($response);
    } else {
        // user is not found with the credentials
        $response["error"] = TRUE;
        $response["error_msg"] = "Login credentials are wrong. Please try again!";
		$response["version"] = $version;
        echo json_encode($response);
    }
} else {
    // required post params is missing
    $response["error"] = TRUE;
    $response["error_msg"] = "Required parameters email or password is missing!";
	$response["version"] = $version;
    echo json_encode($response);
}
?>
