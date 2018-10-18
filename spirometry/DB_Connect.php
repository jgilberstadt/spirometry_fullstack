<?php

require_once 'Config.php';

 class DB_Connect {
 	private $conn;

 	//Connecting to database
 	public function connect() {

 		//Connecting to MySQL DB
 		$this->conn = new mysqli(DB_HOST, DB_USER, DB_PASSWORD, DB_DATABASE);

 		//return database handler
 		return $this->conn;
 	}
 }

 ?>
