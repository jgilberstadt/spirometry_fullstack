<?php

require_once 'Config.php';
require_once 'DB_Connect.php';

 class DB_Functions {

   private $conn;

   // constructor
   function __construct() {
     // connecting to database
     $db = new Db_connect();
     $this->conn = $db->connect();
   }

   // destructor
   function __destruct() {

   }

   /**
    * Storing new FVC test record
    * returns record details
    */

    public function storeMetadata($patient_id) {
      $stmt = $this->conn->prepare("SELECT normal_range FROM meta_data WHERE patient_id=?");
      $stmt->bind_param("s", $patient_id);
      $stmt->execute();
      $user = $stmt->get_result()->fetch_assoc();
      $stmt->close();

      if (!$user) {
        $stmt = $this->conn->prepare("INSERT INTO meta_data(patient_id) VALUES (?)");
        $stmt->bind_param("s", $patient_id);
        $stmt->execute();
        $user = $stmt->get_result()->fetch_assoc();
        $stmt->close();
        return false;
      } else {
        return true;
      }

    }

    public function storeData($patient_id, $dataArr, $pulseArr) {
      $survey_data = $dataArr[0];
      $spiro_data = $dataArr[1];
      $pulse_data = $dataArr[2];

      $stmt = $this->conn->prepare("INSERT INTO survey_data(patient_id, text) VALUES(?, ?)");
      $stmt->bind_param("ss", $patient_id, $survey_data);
      $result = $stmt->execute();
      $stmt->close();

      $stmt = $this->conn->prepare("INSERT INTO spiro_data(patient_id, text) VALUES(?, ?)");
      $stmt->bind_param("ss", $patient_id, $spiro_data);
      $result1 = $stmt->execute();
      $stmt->close();

      $stmt = $this->conn->prepare("INSERT INTO pulse_data(patient_id, text, lowestSat, minRate, maxRate, timeAbnormal, timeMinRate) VALUES(?, ?, ?, ?, ?, ?, ?)");
      $stmt->bind_param("sssssss", $patient_id, $pulse_data, $pulseArr[0], $pulseArr[1], $pulseArr[2], $pulseArr[3], $pulseArr[4]);
      $result2 = $stmt->execute();
      $stmt->close();

      if ($result && $result1 && $result2) {
        return true;
      } else {
        return false;
      }
    }


    public function storeFVCRecords($patient_id, $pef, $fev1, $fvc, $fev1_fvc, $fev6, $fef2575) {
      $uuid = uniqid('',true);

      $stmt = $this->conn->prepare("INSERT INTO fvc_tests(unique_id, patient_id, pef, fev1, fvc, fev1_fvc, fev6, fef2575) VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
      $stmt->bind_param("ssssssss", $uuid, $patient_id, $pef, $fev1, $fvc, $fev1_fvc, $fev6, $fef2575);
      $result = $stmt->execute();
      $stmt->close();
     // check for successful store
     if ($result) {
         $stmt = $this->conn->prepare("SELECT * FROM fvc_tests WHERE patient_id = ?");
         $stmt->bind_param("s", $patient_id);
         $stmt->execute();
         $user = $stmt->get_result()->fetch_assoc();
         $stmt->close();

         return $user;
     } else {
         return false;
     }

     }

	/**
    * Storing new PefFEV1 test record
    * returns record details
    */

    public function storePefFEV1Records($patient_id, $pef, $fev1, $peftime, $evol) {
      $uuid = uniqid('',true);

      $stmt = $this->conn->prepare("INSERT INTO peffev1_tests(unique_id, patient_id, pef, fev1, peftime, evol) VALUES(?, ?, ?, ?, ?, ?)");
      $stmt->bind_param("ssssss", $uuid, $patient_id, $pef, $fev1, $peftime, $evol);
      $result = $stmt->execute();
      $stmt->close();
     // check for successful store
     if ($result) {
         $stmt = $this->conn->prepare("SELECT * FROM peffev1_tests WHERE patient_id = ?");
         $stmt->bind_param("s", $patient_id);
         $stmt->execute();
         $user = $stmt->get_result()->fetch_assoc();
         $stmt->close();

         return $user;
     } else {
         return false;
     }

     }

     /**
       * Storing new test record from vitolgraph
       * returns record details
       */

       public function storeVitolRecords($patient_id, $test_result) {
         $uuid = uniqid('',true);

         $stmt = $this->conn->prepare("INSERT INTO vitol_tests(unique_id, patient_id, test_text) VALUES(?, ?, ?)");
         $stmt->bind_param("sss", $uuid, $patient_id, $test_result);
         $result = $stmt->execute();
         $stmt->close();
        // check for successful store
        if ($result) {
            $stmt = $this->conn->prepare("SELECT * FROM vitol_tests WHERE patient_id = ?");
            $stmt->bind_param("s", $patient_id);
            $stmt->execute();
            $user = $stmt->get_result()->fetch_assoc();
            $stmt->close();

            return $user;
        } else {
            return false;
        }

        }

     /**
      * Get records by patient_id
      */
     public function getFVCRecords($patient_id) {

         $stmt = $this->conn->prepare("SELECT * FROM fvc_tests WHERE patient_id = ?");

         $stmt->bind_param("s", $patient_id);

         if ($stmt->execute()) {
            $records = array();
            while ($row = $stmt->get_result()->fetch_assoc()) {
                $records[] = $row;
            }
            $stmt->close();
            return $records;
         } else {
             return NULL;
         }
     }

	 /**
      * Get records by patient_id
      */
     public function getPefFEV1Records($patient_id) {

         $stmt = $this->conn->prepare("SELECT * FROM peffev1_tests WHERE patient_id = ?");

         $stmt->bind_param("s", $patient_id);

         if ($stmt->execute()) {
            $records = array();
            while ($row = $stmt->get_result()->fetch_assoc()) {
                $records[] = $row;
            }
            $stmt->close();
            return $records;
         } else {
             return NULL;
         }
     }

     /**
        * Get records by patient_id
        */
       public function getVitolRecords($patient_id) {

           $stmt = $this->conn->prepare("SELECT * FROM vitol_tests WHERE patient_id = ?");

           $stmt->bind_param("s", $patient_id);

           if ($stmt->execute()) {
              $records = array();
              while ($row = $stmt->get_result()->fetch_assoc()) {
                  $records[] = $row;
              }
              $stmt->close();
              return $records;
           } else {
               return NULL;
           }
       }


     /**
      * Query which questionnaire should open
      */
	/*
     public function queryFVCResponse($patient_id) {
        $stmt = $this->conn->prepare("SELECT * FROM fvc_tests WHERE patient_id = ?");

        $stmt->bind_param("s", $patient_id);

        if ($stmt->execute()) {
	    $result = $stmt->get_result();
            $response["which_response"] = 0;
            while ($row = $result->fetch_assoc()) {
                $response["which_response"] = $response["which_response"] + 1;
		$response["time_complete"] = $row["time_complete"];
            }
            $stmt->close();

            return $response;
          } else {
            return NULL;
        }

     }
	*/

	 /**
      * Query which questionnaire should open
      */
	/*
     public function queryPefFEV1Response($patient_id) {
        $stmt = $this->conn->prepare("SELECT * FROM peffev1_tests WHERE patient_id = ?");

        $stmt->bind_param("s", $patient_id);

        if ($stmt->execute()) {
	    $result = $stmt->get_result();
            $response["which_response"] = 0;
            while ($row = $result->fetch_assoc()) {
                $response["which_response"] = $response["which_response"] + 1;
		$response["time_complete"] = $row["time_complete"];
            }
            $stmt->close();

            return $response;
          } else {
            return NULL;
        }

     }
	*/

     /**
      * Get records from all users
      */

     public function getAllFVCResults() {
        $result = mysqli_query($this->conn, "SELECT * FROM fvc_tests");
        return $result;
    }

	public function getAllPefFEV1Results() {
        $result = mysqli_query($this->conn, "SELECT * FROM peffev1_tests");
        return $result;
    }


     /**
      * Check record is existed or not
      */
     public function isRecordExisted($patient_id) {
         $stmt = $this->conn->prepare("SELECT patient_id from fvc_tests WHERE patient_id = ?");

         $stmt->bind_param("s", $patient_id);

         $stmt->execute();

         $stmt->store_result();

         if ($stmt->num_rows > 0) {
             // user existed
             $stmt->close();
             return true;
         } else {
             // user not existed
             $stmt->close();
             return false;
         }
     }

     /**
      * Storing new spirotel test record
      * returns record details
      */

      public function storeSpirotelRecords($patient_id, $test_results) {

        $stmt = $this->conn->prepare("INSERT INTO spirotel_tests(patient_id, test_results) VALUES(?, ?)");
        $stmt->bind_param("ss", $patient_id, $test_results);
        $result = $stmt->execute();
        $stmt->close();
       // check for successful store
       if ($result) {
           $stmt = $this->conn->prepare("SELECT * FROM spirotel_tests WHERE patient_id = ?");
           $stmt->bind_param("s", $patient_id);
           $stmt->execute();
           $user = $stmt->get_result()->fetch_assoc();
           $stmt->close();

           return $user;
       } else {
           return false;
       }

       }

 }

 ?>
