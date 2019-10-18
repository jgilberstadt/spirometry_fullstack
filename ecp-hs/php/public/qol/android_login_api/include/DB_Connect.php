<?php
class DB_Connect {

	//Connecting to database
	public function connect() {

    $host = "host = 127.0.0.1";
    $port = "port = 5432";
    $dbname = "dbname = answers_db";
    $credentials = "user = tester password=Qol13579#";

		//Connecting to PostgreSQL DB
		$db = pg_connect("$host $port $dbname $credentials");

		/*

		if(!$db) {
			echo "Error: Unable to open database\n";
		} else {
			echo "Opened database successfully\n";
		}
		*/

		//return database handler
		return $db;
	}
}

?>
