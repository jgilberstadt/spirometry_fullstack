import argparse
import time

import psycopg2
#import schedule
from dbconfig import config
from reportgenerator import ReportGenerator
import reportgenerator
import datetime
import pandas as pd
import statistics


def main(run_all_reports, db_config_path=None, email_config_path=None):
    # connect database for enrollment website
    dbc_rbos = config('rbos_database.ini', 'postgresql')
    cnx_rbos = psycopg2.connect(**dbc_rbos)
    cursor_rbos = cnx_rbos.cursor()

    dbc = config('database.ini' if db_config_path == None else db_config_path, 'postgresql')
    cnx = psycopg2.connect(**dbc)
    cursor = cnx.cursor()

    query = "SELECT nl_end_date, start_study_date, imei_num, p_value, mode, monitoring_start_date FROM patient_data"
    cursor.execute(query, ())

    for (nl_end_date, start_study_date, imei_num, p_value, mode, monitoring_start_date) in cursor:
        # update Tx
        cursor_rbos.execute("SELECT datevalue FROM formattributes, basicform, event, participant WHERE formattributes.formid=basicform.id AND basicform.eventid=event.id AND basicform.title=%s AND event.participantid = participant.id AND participant.participantid=%s AND formattributes.name=%s", ('Confirmation of Eligibility Form', imei_num, 'lungTransplantationDate'))
        if(cursor_rbos.rowcount>0):
            for (curr_Tx_date) in cursor_rbos:
                Tx_date = curr_Tx_date
            update_cursor = cnx.cursor()
            update_cursor.execute("UPDATE patient_data SET date_of_transplant=%s WHERE imei_num=%s", (Tx_date, imei_num))

        start_study_date = reportgenerator.convert_to_date(start_study_date)
        current_date = datetime.date.today()
        num_sessions = cursor.rowcount

        
        """
        # To Do: add an additional column in the patient_data table indicating the end of training phase
        if(nl_end_date is None and (num_sessions == 30 or num_sessions == 37 or num_sessions == 44 or num_sessions ==51
            or num_sessions == 58 or num_sessions == 65 or num_sessions == 72 and mode==0):
        """
        if(True):
            rbObj = ReportGenerator(imei_num, viewing_month="0")
            rbObj.generateCMIDatasheet()
            # read p-value from datasheet and then store it in the DB
            filename = "CMI_Datasheet-{0}-{1}".format(imei_num, rbObj.viewing_month.get_month_name().replace(" ", "_"))
            full_filename = filename+".xlsx"
            df = pd.read_excel(full_filename, sheetname=None, header=None)
            new_p_value = float(df["Datasheet"].loc[26,"M"])
            enter_monitroing_start_date = current_date.strftime('%Y-%m-%d')
            insert_cursor = cnx.cursor()
            insert_cursor.execute("UPDATE patient_data SET p_value=%s, monitoring_start_date=%s WHERE imei_num=%s", (new_p_value, imei_num, enter_monitroing_start_date))
            
            #Code for calculating normal_range of patients
            pull_cursor = cnx.cursor()
            pull_query = "SELECT test_date, fev11, fev12, fev13, fev14, fev15, fev16 FROM spiro_data WHERE imei_num=%s"
            pull_cursor.execute(pull_query, ([imei_num]))
            #Array to hold the max fev for each of the 30 sessions
            fev_max_array = []
            #For each day, create an array for the 6 fev values, calculate the max of the six, append the max to the array
            for test_date, fev11, fev12, fev13, fev14, fev15, fev16 in pull_cursor:
                fev_array = [fev11, fev12, fev13, fev14, fev15, fev16]
                max_fev = 0
                for fev in fev_array:
                    if (fev > max_fev):
                        max_fev = fev
                fev_max_array.append(max_fev)

            #Calculate the average of the max fev's for the last month
            fev_sum = 0
            for fev in fev_max_array:
                fev_sum += fev

            average_fev = float(fev_sum / len(fev_max_array))

            #Calculate the standard deviation using methd from statistics
            standard_dev = statistics.stdev(fev_max_array)
            #Calculate normal range using standard deviation
            normal_range_lower = average_fev - float((2*standard_dev))
            normal_range_upper = average_fev + float((2*standard_dev))
            normal_range_string = "%0.2f,%0.2f" % (normal_range_lower, normal_range_upper)
            #finally, update the DB with normal range for patient

            nr_update_cursor = cnx.cursor()
            nr_update_query = "UPDATE patient_data SET normal_range=%s WHERE imei_num=%s"
            nr_update_cursor.execute(nr_update_query, (normal_range_string, imei_num))
            cnx.commit()
            pull_cursor.close()
            nr_update_cursor.close()

         # schedule for next appointment
        """
        elif(nl_end_date is None and model==0):
        """

    
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
