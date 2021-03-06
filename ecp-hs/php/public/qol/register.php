<?php

require_once '/data/ecp-hs/php/public/qol/android_login_api/include/DB_Functions.php';
$db = new DB_Functions();

$site_id_array = array("100","101","102");

// json response array
$response = array("error" => FALSE);

if (isset($_POST['site_id']) && isset($_POST['email']) && isset($_POST['password'])) {

    // receiving the post params
    $site_id = $_POST['site_id'];
    $email = $_POST['email'];
    $password = $_POST['password'];

    // check if user is already existed with the same email
    if ($db->isUserExisted($email) && in_array($site_id,$site_id_array)) {
        // user already existed
        $response["error"] = TRUE;
        $response["error_msg"] = "User already existed with " . $email;
        echo json_encode($response);
    }
    elseif (!in_array($site_id,$site_id_array)) {
      // wrong work ID
      $response["error"] = TRUE;
      $response["error_msg"] = "Wrong work ID";
      echo json_encode($response);
    }
    else {
        // create a new user
        $user = $db->storeUser($site_id, $email, $password);
        if ($user) {
            // user stored successfully
            $response["error"] = FALSE;
            $response["uid"] = $user["unique_id"];
            $response["user"]["site_id"] = $user["site_id"];
            $response["user"]["email"] = $user["email"];
            /*
            $response["user"]["created_at"] = $user["created_at"];
            $response["user"]["updated_at"] = $user["updated_at"];
            */
            echo json_encode($response);
        } else {
            // user failed to store
            $response["error"] = TRUE;
            $response["error_msg"] = "Unknown error occurred in registration!";
            echo json_encode($response);
        }
    }
} else {
    $response["error"] = TRUE;
    $response["error_msg"] = "Required parameters (name, email or password) is missing!";
    echo json_encode($response);
}
?>
