<?php

class Get_ID {
	private $conn;

	// constructor
	function __construct() {
        require_once 'include/DB_Connect.php';
	    // connecting to database
	    $db = new Db_connect();
	    $this->conn = $db->connect();
	}

    // destructor
    function __destruct() {
    }

    /**
     * Storing new user
     * returns user details
     */

    // Obtain all records of username & password
    public function query_users() {
        $result = mysqli_query($this->conn, "SELECT * FROM users");
        return $result;
    }
}

?>
