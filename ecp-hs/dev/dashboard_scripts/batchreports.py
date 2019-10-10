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

    query = "SELECT nl_end_date, start_study_date, imei_num, p_value, monitoring_start_date FROM patient_data"
    cursor.execute(query, ())

    for (nl_end_date, start_study_date, imei_num, p_value, monitoring_start_date) in cursor:
        # update Tx
        cursor_rbos.execute("SELECT datevalue FROM formattributes, basicform, event, participant WHERE formattributes.formid=basicform.id AND basicform.eventid=event.id AND basicform.title=%s AND event.participantid = participant.id AND participant.participantid=%s AND formattributes.name=%s", ('Confirmation of Eligibility Form', imei_num, 'lungTransplantationDate'))
        for (curr_Tx_date) in cursor_rbos:
            Tx_date = curr_Tx_date
        update_cursor = cnx.cursor()
        update_cursor.execute("UPDATE patient_data SET date_of_transplant=%s WHERE imei_num=%s", (Tx_date, imei_num))

        start_study_date = reportgenerator.convert_to_date(start_study_date)
        current_date = datetime.date.today()
        timedelta = current_date - start_study_date

        # Every two weeks during the training period, only need to send the datasheet to 
        # the CMI for this one

       # print "timedelta.days", timedelta.days
       # print "nl_end_date", nl_end_date

        if(nl_end_date is None and timedelta.days < 30):
            if(timedelta.days % 14 == 0):
                rgObj = ReportGenerator(imei_num, viewing_month="1")
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

        # To Do: add an additional column in the patient_data table indicating the end of training phase
        elif(nl_end_date is None and timedelta.days == 30):
            rbObj = ReportGenerator(imei_num, viewing_month="1")
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


        elif(nl_end_date is None and p_value is not None and timedelta.days > 30):
            if(timedelta.days % 14 == 0 and float(p_value) >= 0.01):
                current_month = timedelta.days / 30
                rgObj = ReportGenerator(imei_num, viewing_month=str(current_month))
                rgObj.generateCMIDatasheet()
                rgObj.sendReports()
            if(timedelta.days % 7 == 0 and float(p_value) < 0.01):
                current_month = timedelta.days / 30
                rgObj = ReportGenerator(imei_num, viewing_month=str(current_month))
                rgObj.generateCMIDatasheet()
                rgObj.sendReports()


        # check for monitoring period
        elif(nl_end_date is not None and timedelta.days >= 30):
            nl_end_date = reportgenerator.convert_to_date(nl_end_date)
            monitoring_start_date = reportgenerator.convert_to_date(monitoring_start_date)
            timedelta_monitoring = current_date - monitoring_start_date
            if(timedelta_monitoring.days % 2 == 0):
                current_month = timedelta_monitoring.days / 30
                rgObj = ReportGenerator(imei_num, viewing_month=str(current_month), delete_files=False)
                rgObj.generateCMIDatasheet()
                rgObj.sendReports()
            elif(timedelta_monitoring.days % 2 == 1):
                current_month = timedelta_monitoring.days / 30
                rgObj = ReportGenerator(imei_num, viewing_month=str(current_month), delete_files=False)
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
