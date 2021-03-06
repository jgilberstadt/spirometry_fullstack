<?php

/**
*/

class DB_Functions {

  private $conn;

  // constructor
  function __construct() {
    require_once 'DB_Connect.php';
    // connecting to database
    $db = new DB_Connect();
    $this->conn = $db->connect();
  }

  // destructor
  function __destruct() {
    // pg_close($conn);
  }

  /**
   * Storing new user
   * returns user details
   */

   public function storeUser($site_id, $email, $password) {
     $uuid = uniqid('',true);
     $hash = $this->hashSSHA($password);
     $encrypted_password = $hash["encrypted"]; // encrypted password
     $salt = $hash["salt"]; // salt

     // prepare a query for execution
     pg_prepare($this->conn, "insert", "INSERT INTO users(site_id, email, encrypted_password, salt) VALUES($1, $2, $3, $4)");
     $result = pg_execute($this->conn, "insert", array($site_id, $email, $encrypted_password, $salt));
    // check for successful store
    if ($result) {

        pg_prepare($this->conn, "query2", "SELECT * FROM users WHERE email = $1");
        $result = pg_execute($this->conn, "query2", array($email));

        return $result;
    } else {
        return false;
    }

    }

    /**
     * Get user by email and password
     */
    public function getUserByEmailAndPassword($email, $password) {

        
        pg_prepare($this->conn, "query1", "SELECT * FROM users WHERE email=$1");
        $result = pg_execute($this->conn, "query1", array($email));


        while($row=pg_fetch_array($result)) {
          $salt = $row["salt"];
          $encrypted_password = $row["encrypted_password"];
          $hash = $this->checkhashSSHA($salt, $password);
          if($encrypted_password == $hash) {
            // user authentication details are correct
            return $row;
          }
          else {
            return NULL;
          }
        }
    }

    /**
     * Check user is existed or not
     */
    public function isUserExisted($email) {

        pg_prepare($this->conn, "query", "SELECT * FROM users WHERE email = $1");
        $user = pg_execute($this->conn, "query", array($email)) ;

        $num_rows = pg_num_rows($user);

        if($num_rows > 0) {
          return true;
        }
        else {
          return false;
        }
    }

    /**
     * Encrypting password
     * @param password
     * returns salt and encrypted password
     */
    public function hashSSHA($password) {

        $salt = sha1(rand());
        $salt = substr($salt, 0, 10);
        $encrypted = base64_encode(sha1($password . $salt, true) . $salt);
        $hash = array("salt" => $salt, "encrypted" => $encrypted);
        return $hash;
    }

    /**
     * Decrypting password
     * @param salt, password
     * returns hash string
     */
    public function checkhashSSHA($salt, $password) {

        $hash = base64_encode(sha1($password . $salt, true) . $salt);

        return $hash;
    }

    public function getTabletID($serial_num, $site_id) {
      $response = array();

      pg_prepare($this->conn, "query1", "SELECT tablet_id, baseline_counter FROM tablets WHERE serial_num = $1");
      $result = pg_execute($this->conn, "query1", array($serial_num));
      $row=pg_fetch_array($result, 0);
      $tabletID = $row["tablet_id"];
      $baseline_counter = $row["baseline_counter"];

      if($tabletID==NULL) {
        pg_prepare($this->conn, "insert", "INSERT INTO tablets(serial_num, site_id, baseline_counter) VALUES ($1, $2, $3) RETURNING tablet_id");
        $counter = 1;
        $insert_result = pg_execute($this->conn, "insert", array($serial_num, $site_id, $counter));
        if($insert_result) {
          $tabletID = pg_fetch_array($insert_result ,0)["tablet_id"];
          $response["tabletId"] = $tabletID;
          $response["baseline_counter"] = 1;
          return $response;
        }
        else {
            return false;
        }
      }
      else {
        $response["tabletId"] = $tabletID;
        $response["baseline_counter"] = $baseline_counter;
        return $response;
      }

    }


    public function getAllBaseline($siteId) {
      pg_prepare($this->conn, "query1", "SELECT temp_id, time_complete, nurse_email FROM baseline_metadata WHERE site_id = $1");
      $result = pg_execute($this->conn, "query1", array($siteId));
      /*
      while($row=pg_fetch_array($result)) {
        return $row;
      }
      */
      return $result;
    }

    public function updateBaselineCounter($tabletId, $numBaseline) {
      
      pg_prepare($this->conn, "query1", "UPDATE tablets SET baseline_counter=baseline_counter+$1 WHERE tablet_id=$2 RETURNING baseline_counter");
      $result = pg_execute($this->conn, "query1", array($numBaseline, $tabletId));
      $updated_count = pg_fetch_array($result,0)["baseline_counter"];
      if(!$updated_count) {
        return false;
      }
      else {
        return $updated_count;
      }

    }

    public function storeRecords($patient_id, $which_response, $time_complete, $is_completed, $assisted, $inside_window, $rationale_num, $duration, $nurse_email, $mode, $site, $site_id) {

      pg_prepare($this->conn, "insert1", "INSERT INTO survey_metadata(patient_id, nurse_email, which_response, time_complete, is_completed, assisted, inside_window, rationale, duration, modeOfAdmin, siteOfAdmin, site_id) VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12)");
      $result = pg_execute($this->conn, "insert1", array($patient_id, $nurse_email, $which_response, $time_complete, $is_completed, $assisted, $inside_window, $rationale_num, $duration, $mode, $site, $site_id));
      
      pg_prepare($this->conn, "query1", "SELECT * FROM survey_metadata WHERE time_complete = $1 AND patient_id = $2");
      $result = pg_execute($this->conn, "query1", array($time_complete, $patient_id));
      $insert_result = 0;
      while($row = pg_fetch_array($result)){
        $insert_result = $row["id"];
      }
      return $insert_result;
    }

     public function storeToFullTable($ans_array){
       foreach(array_keys($ans_array) as $key) {
         $fields[] = "$key";
         $values[] = "'".pg_escape_string($this->conn,$ans_array[$key])."'";
       }
       $fields = implode(",",$fields);
       $values = implode(",",$values);

       pg_query($this->conn, "INSERT INTO survey_responses($fields) VALUES ($values)");

     }

     public function storeToFullBaselineTable($ans_array){
       foreach(array_keys($ans_array) as $key) {
         $fields[] = $key;
         $values[] = "'".pg_escape_string($this->conn,$ans_array[$key])."'";
       }
       $fields = implode(",",$fields);
       $values = implode(",",$values);

       pg_query($this->conn, "INSERT INTO baseline_responses($fields) VALUES ($values)");
     }


     public function storeBaselineRecords($patient_id, $which_response, $time_complete, $is_completed, $assisted, $inside_window, $rationale_num, $duration, $nurse_email, $mode, $site, $site_id) {
       pg_prepare($this->conn, "insert1", "INSERT INTO baseline_metadata(temp_id, nurse_email, which_response, time_complete, is_completed, assisted, inside_window, rationale, duration, modeOfAdmin, siteOfAdmin, site_id) VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12)");
      $result = pg_execute($this->conn, "insert1", array($patient_id, $nurse_email, $which_response, $time_complete, $is_completed, $assisted, $inside_window, $rationale_num, $duration, $mode, $site, $site_id));
      
      pg_prepare($this->conn, "query1", "SELECT * FROM baseline_metadata WHERE time_complete = $1 AND temp_id = $2");
      $result = pg_execute($this->conn, "query1", array($time_complete, $patient_id));
      $insert_result = 0;
      while($row = pg_fetch_array($result)){
        $insert_result = $row["id"];
      }
      return $insert_result;
     }


     /**
      * Get records by patient_id
      */
     public function getRecords($patient_id) {
        pg_prepare($this->conn, "query", "SELECT * FROM answer_responses WHERE patient_id = $1");
        $result = pg_execute($this->conn, "query", array($patient_id));
        if($result) {
          $records = array();
          while($row=pg_fetch_array($result)) {
            $records[] = $row;
          }
        }
        else {
          return NULL;
        }
     }

     /**
      * Query which questionnaire should open
      */
     public function queryResponse($patient_id) {
        pg_prepare($this->conn, "query", "SELECT * FROM answer_responses WHERE patient_id = $1");
        $result = pg_execute($this->conn, "query", array($patient_id));
        if($result) {
          $response["which_response"] = 0;
          while($row = pg_fetch_array($result)) {
            $response["which_response"] = $response["which_response"] + 1;
            $response["time_complete"] = $row["time_complete"];
          }
          return $response;
        }
        else {
          return NULL;
        }
      }

     /**
      * Get records from all users
      */

    public function getAllUsers() {
      $result = pg_query($this->conn, "SELECT * FROM survey_metadata");
      return $result;
    }


     /**
      * Check record is existed or not
      */
     public function isRecordExisted($patient_id) {
         pg_prepare($this->conn, "query1", "SELECT patient_id from answer_responses WHERE patient_id = $1");
         $result = pg_execute($this->conn, "query1", array($patient_id));
         $num_rows = pg_num_rows($result);
         if($num_rows > 0) {
          return true;
         }
         else {
          return false;
         }
     }

     public function doesSurveyExist($isBaseline, $patient_id, $time_complete) {
         $num_rows = 0;
         if ($isBaseline == 1) {
          pg_prepare($this->conn, "query1", "SELECT id from baseline_metadata WHERE temp_id = $1 AND time_complete = $2");
          $result = pg_execute($this->conn, "query1", array($patient_id, $time_complete));
          $num_rows = pg_num_rows($result);
         } else {
          pg_prepare($this->conn, "query1", "SELECT id from survey_metadata WHERE patient_id = $1 AND time_complete = $2");
          $result = pg_execute($this->conn, "query1", array($patient_id, $time_complete));
          $num_rows = pg_num_rows($result);
         }
         if($num_rows>0) {
          return "true";
         }
         else {
          return "false";
         }
     }

     public function uploadPID($pid) {
        pg_prepare($this->conn, "insert", "INSERT INTO patient_ids(patient_id) VALUES($1)");
        $result = pg_execute($this->conn, "insert", array($pid));
        return $result;
     }

     public function writeToCSV($insert_id,$array1, $array2){
       $file = fopen("/data/ecp-hs/php/data/db_csv/survey_metadata.csv","a");
       error_log("writeToCSV". $insert_id);

       $row = $insert_id.",";
       for($i=59; $i<71; $i++){
         $row = $row.$array1[$i].",";
       }

       foreach(array_keys($array2) as $key) {
         $row = $row.$array2[$key].",";
       }
       fputcsv($file, explode(',',$row));
       error_log($row);
       fclose($file);
     }

     public function deleteBaseline($tempId) {

      pg_prepare($this->conn, "query1", "SELECT id FROM baseline_metadata WHERE temp_id = $1");
      $result = pg_execute($this->conn, "query1", array($tempId));

      $row = pg_fetch_array($result,0);
      $id = $row["id"];

      if(!$id) {
        return "Could not find survey in database";
        exit();
      }

      pg_prepare($this->conn, "insert1", "INSERT INTO archive_responses SELECT * FROM baseline_responses WHERE id = $1");
      pg_execute($this->conn, "insert1", array($id));

      pg_prepare($this->conn, "insert2", "INSERT INTO archive_metadata SELECT * FROM baseline_metadata WHERE id = $1");
      pg_execute($this->conn, "insert2", array($id));

      pg_prepare($this->conn, "delete1", "DELETE FROM baseline_responses WHERE id = $1");
      pg_execute($this->conn, "delete1", array($id));

      pg_prepare($this->conn, "delete2", "DELETE FROM baseline_metadata WHERE id = $1");
      pg_execute($this->conn, "delete2", array($id));

      return "success";

    }

    public function enrollBaseline($patientId, $tempId) {
      $old_survey_counter;
      $id = -1;
      $array1;
      $array2; //arrays used to store the new info in the new tables. Used to output .csv file
      // Get data from baseline database

      pg_prepare($this->conn, "query1", "SELECT id, temp_id, nurse_email, which_response, time_complete, is_completed, assisted, inside_window, rationale, duration, modeofadmin, siteofadmin, site_id FROM baseline_metadata WHERE temp_id = $1");
      $result = pg_execute($this->conn, "query1", array($tempId));
      if(!$result) {
        return "issue select";
      }
      $row = pg_fetch_array($result,0);
      $old_survey_counter = $row["id"];
      $temp_id = $row["temp_id"];
      $nurse_email = $row["nurse_email"];
      $which_response = $row["which_response"];
      $time_complete = $row["time_complete"];
      $is_completed = $row["is_completed"];
      $assisted = $row["assisted"];
      $inside_window = $row["inside_window"];
      $rationale = $row["rationale"];
      $duration = $row["duration"];
      $modeofadmin = $row["modeofadmin"];
      $siteofadmin = $row["siteofadmin"];
      $site_id = $row["site_id"];

      // Insert into answer database
      pg_prepare($this->conn, "insert1", "INSERT INTO survey_metadata (patient_id, nurse_email, which_response, time_complete, is_completed, assisted, inside_window, rationale, duration, modeofadmin, siteofadmin, site_id, temp_id) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13)");
      $result = pg_execute($this->conn, "insert1", array($patientId, $nurse_email, $which_response, $time_complete, $is_completed, $assisted, $inside_window, $rationale, $duration, $modeofadmin, $siteofadmin, $site_id, $temp_id));
      if(!$result) {
        return "issue insert";
      }

      //success
      //get survey meta data and the counter in the survey table
      pg_prepare($this->conn, "query2", "SELECT * FROM survey_metadata WHERE time_complete=$1 AND patient_id=$2");
      $result = pg_execute($this->conn, "query2", array($time_complete, $patientId));
      $row1 = pg_fetch_array($result,0);
      $id = $row1["id"];


      // move from baseline_responses to survey_responses
      pg_prepare($this->conn, "insert2", "INSERT INTO survey_responses SELECT * FROM baseline_responses WHERE id = $1");
      $result = pg_execute($this->conn, "insert2", array($old_survey_counter));
      if(!$result) {
        return "insert issue";
      }
      pg_prepare($this->conn, "update1", "UPDATE survey_responses SET id = $1 WHERE id=$2");
      $result = pg_execute($this->conn, "update1", array($id, $old_survey_counter));
      pg_prepare($this->conn, "query3", "SELECT * FROM survey_responses WHERE id=$1");
      $result = pg_execute($this->conn, "query3", array($id));
      $row2 = pg_fetch_array($result);


      //delete previous response
      pg_prepare($this->conn, "delete1", "DELETE FROM baseline_responses WHERE id = $1");
      $result = pg_execute($this->conn, "delete1", array($old_survey_counter));
      if(!$result) {
        return "delete issue";
      }


      // Delete from old metadata table
      pg_prepare($this->conn, "delete2", "DELETE FROM baseline_metadata WHERE temp_id = $1");
      $result = pg_execute($this->conn, "delete2", array($tempId));
      if(!$result) {
        return "failed to delete old baseline survey";
      }

      /*

      if(!empty($row1) && !empty($row2)) {
        $file = fopen("/data/ecp-hs/php/data/db_csv/survey_metadata.csv","a");
        $row="";
        foreach ($row1 as $val){
          $row = $row.$val.",";
        }
        foreach($row2 as $val){
          $row = $row.$val.",";
        }
        fputcsv($file, explode(',', $row));
        error_log($row);
        fclose($file);
      }
      */

      return "success";
    }

}
 ?>
