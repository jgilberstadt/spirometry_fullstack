import argparse
import time

import psycopg2
import schedule
from dbconfig import config
from reportgenerator import ReportGenerator


def main(run_all_reports, db_config_path=None, email_config_path=None):
    dbc = config('database.ini' if db_config_path == None else db_config_path, 'postgresql')
    cnx = psycopg2.connect(**dbc)
    cursor = cnx.cursor()
    query = "SELECT nl_end_date, start_study_date, patient_id FROM patient_data"
    cursor.execute(query, ())
    

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Batch script to generate reports.')
    parser.add_argument('-a', '--all', dest='run_all_reports', action='store_true',
                        help='run all the reports for all patients')
    parser.add_argument('-s', '--schedule', dest='schedule', action='store_true',
                        help='schedule the script to be run once daily at 1 AM')
    parser.add_argument('--dbconfig', default=None, metavar='PATH', dest='dbconfig',
                        help='the path to the database connection .ini file')
    parser.add_argument('--emailconfig', default=None, metavar='PATH', dest='emailconfig',
                        help='the path to the email account .json file')
    args = parser.parse_args()
    main(args.run_all_reports, args.dbconfig, args.emailconfig)