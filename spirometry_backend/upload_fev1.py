import os
import argparse
import json
import psycopg2
import datetime
from dbconfig import config

def main(patient_id, file):
	decoded_file = file.read().decode("utf-8-sig").encode("utf-8")
	dbc = config('database.ini', 'postgresql')
	cnx = psycopg2.connect(**dbc)
	file_lines = decoded_file.splitlines()
	file_lines = [x.strip() for x in file_lines]
	for line in file_lines:
		columns = line.split(',')
		print columns
		fev11 = str(float(columns[0]))
		fev12 = str(float(columns[1]))
		fev13 = str(float(columns[2]))
		fev14 = str(float(columns[3]))
		# print fev14
		fev15 = str(float(columns[4]))
		# print fev15
		fev16 = str(float(columns[5]))
		test_date_raw = columns[12]
		test_date = datetime.datetime.strptime(test_date_raw, "%m/%d/%Y")
		data = (patient_id, fev11, fev12, fev13, fev14,
		        fev15, fev16, test_date.strftime('%Y-%m-%d'))
		print data
		cursor = cnx.cursor()
		query = "INSERT INTO spiro_data (patient_id, fev11, fev12, fev13, fev14, fev15, fev16, test_date) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)"
		cursor.execute(query, data)
		cursor.close()

	# Make sure data is committed to the database
	cnx.commit()
	cnx.close()

if __name__ == '__main__':
	parser = argparse.ArgumentParser(description='Upload test data to MySQL')
	parser.add_argument('patient_id', metavar='ID', type=str,
					 help='the patient id for which to generate the report')

	parser.add_argument('data_file', metavar='FILE', default='localhost', type=argparse.FileType('r'),
					 help='the file containing the test data to upload to MySQL')

	args = parser.parse_args()
	main(args.patient_id, args.data_file)
