import argparse
import time

import psycopg2
import schedule
from dbconfig import config
from reportgenerator import ReportGenerator
import reportgenerator
import datetime


def main(run_all_reports, db_config_path=None, email_config_path=None):
    dbc = config('database.ini' if db_config_path == None else db_config_path, 'postgresql')
    cnx = psycopg2.connect(**dbc)
    cursor = cnx.cursor()
    query = "SELECT nl_end_date, start_study_date, patient_id FROM patient_data"
    cursor.execute(query, ())

    for (nl_end_date, start_study_date, patient_id) in cursor:
        start_study_date = reportgenerator.convert_to_date(start_study_date)
        nl_end_date = reportgenerator.convert_to_date(nl_end_date)
        current_date = datetime.datetime.now()
        timedelta = current_date - start_study_date

        # Every two weeks during the training period, only need to send the datasheet to 
        # the CMI for this one
        if(nl_end_date is None and timedelta.day <= 30):
            if(timedelta.day % 14 == 0):
                rgObj = ReportGenerator(patient_id, viewing_month="1")
                rgObj.generateCMIDatasheet()
                rgObj.sendReports()
        # After the first four weeks of the training period, if we see 
        # a statistically-significant decrease in lung function 
        # (further clarification may be needed, but I interpreted this based on my discussions 
        # with Dr. Despotis to be a p-value < 0.1 for the FEV1Max data), 
        # we should begin sending the datasheets to the CMI weekly until the doctors 
        # there either determine to remove the patient from the study 
        # or move them to the monitoring period. If a patient is in the training period 
        # for more than four weeks but does not show a statistically-significant decrease, 
        # we can continue sending the datasheet every two weeks to the CMI 
        # until they are moved to the monitoring period.
        elif(nl_end_date is None and timedelta.day > 30):

        # check for monitoring period
        elif(nl_end_date is not None and timedelta.day >= 30):
            if(timedelta.day % 30 == 0):
                current_month = timedelta.day / 30
                rgObj = ReportGenerator(patient_id, viewing_month=str(current_month))
                rgObj.generateCMIDatasheet()
                rgObj.sendReports()
            elif(timedelta.day % 30 == 1):
                current_month = timedelta.day / 30
                rgObj = ReportGenerator(patient_id, viewing_month=str(current_month))
                rgObj.generateSiteReport()
                rgObj.sendReports()

    
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