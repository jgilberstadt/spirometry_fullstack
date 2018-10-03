# script to generate report
import matplotlib.pyplot as plt
import xlsxwriter
import datetime
import mysql.connector
import numpy as np
from sklearn import linear_model
from sklearn.metrics import mean_squared_error, r2_score
import os
import argparse

email_list = {
	'100': 'qolecp@gmail.com',
	'101': 'qolecp@gmail.com',
	'102': 'qolecp@gmail.com',
	'103': 'qolecp@gmail.com',
	'104': 'qolecp@gmail.com',
	'105': 'qolecp@gmail.com',
	'106': 'qolecp@gmail.com',
	'107': 'qolecp@gmail.com',
	'108': 'qolecp@gmail.com',
	'109': 'qolecp@gmail.com',
	'110': 'qolecp@gmail.com',
	'111': 'qolecp@gmail.com'
	}

# report generated to be sent to CMI


def generate(filename, patient_id, host='localhost'):
	cnx = mysql.connector.connect(
		user='root', password='mysql_db', host=host, database='spirometry_db')

	workbook = xlsxwriter.Workbook(filename+'.xlsx')
	worksheet = workbook.add_worksheet()

	worksheet.write('B2', 'Patient Study Number')
	worksheet.write('B3', 'Date')
	worksheet.write('B4', 'Monitoring Month')
	worksheet.write('B5', 'NL Range')
	worksheet.write('B6', 'Variance')
	worksheet.write('B7', 'Lowest FEV1')

	worksheet.write('E2', 'WU1')
	now = datetime.datetime.now()
	worksheet.write('E3', now.strftime("%Y-%m-%d"))

	cursor = cnx.cursor(buffered=True)
	query = "SELECT minRate, maxRate, lowestSat, timeAbnormal, timeMinRate FROM pulse_data WHERE patient_id = %s"
	cursor.execute(query, (patient_id,))

	lowestSat = cursor[2]

	cursor = cnx.cursor(buffered=True)
	query = "SELECT survey_text FROM survey_data WHERE patient_id = %s"
	cursor.execute(query, (patient_id,))

	# generate supplemental data
	worksheet.write('G2','Supplemental Data')
	worksheet.write('I2','NL')
	worksheet.write('J2','ABNL')
	worksheet.write('G3','Questionnaire')
	worksheet.write('G4','Oximetry')
	worksheet.write('H5','O2 Sat')
	worksheet.write('H6','Duration Abnormal')
	worksheet.write('H7','Duration Lowest')
	worksheet.write('H8','Heart Rate')
	
	if lowestSat < 97:
		worksheet.write('J5', lowestSat)
		worksheet.write('J6', timeAbnormal)
		worksheet.write('J7', timeMinRate)
		worksheet.write('J8', minRate + " vs " + maxRate)
	else:
		worksheet.write('I5', '>97%')
		worksheet.write('I6', 'NA')
		worksheet.write('I7', 'NA')
		worksheet.write('I8', 'NL')

	symptomString = ""
	symptomArr = ["Cough", "RunnyNose", "Sore Throat", "Fever", "Shaking Chills", "Muscle Aches", "Recent Diarrhea", "Cough", "Cough now producing sputum/mucus", "New wheezing", "New shortness of breath", "Generalized weakness", "Lightheadedness", "Dizziness", "Shortness of breath at rest", "Shortness of breath on exertion", "Shortness of breath when lying flat", "New Shortness of breath that awakens you from sleep", "New swelling in your feet or legs"]

	for i in range (0, len(survey_text)):
		if survey_text[i] == "1":
			symptomString = symptomString + symptomArr[i] + ", "

	if symptomString != "":
		worksheet.write('I3', 'Negative')
	else:
		worksheet.write('I4', symptomString)


	cursor = cnx.cursor(buffered=True)
	query = "SELECT fev11, fev12, fev13, fev14, fev15, fev16, test_date FROM small_test WHERE patient_id = %s"
	cursor.execute(query, (patient_id,))

	worksheet.write('A30','PTN')
	worksheet.write('B30','FEV11')
	worksheet.write('C30','FEV12')
	worksheet.write('D30','FEV13')
	worksheet.write('E30','FEV14')
	worksheet.write('F30','FEV15')
	worksheet.write('G30','FEV16')
	worksheet.write('H30','FEV1MAX')
	worksheet.write('I30','MAX')
	worksheet.write('J30','%MAX')
	worksheet.write('K30','Mean')
	worksheet.write('L30','%Mean')
	worksheet.write('M30','FEV1*K')
	worksheet.write('N30','Date')
	worksheet.write('O30','SHS Date')
	worksheet.write('P30','Days')
	worksheet.write('Q30','DTx')
	worksheet.write('R30','MonthsPTx')
	worksheet.write('S30','Status')

	row = 30
	col = 1
	max_fev_list = []
	dates = []
	for (fev11, fev12, fev13, fev14, fev15, fev16, test_date) in cursor:
		fev_list = [float(fev11), float(fev12), float(fev13), float(fev14), float(fev15), float(fev16)]
		max_fev = max(fev_list)
		max_fev_list.append(max_fev)
		d = datetime.datetime.strptime(test_date,"%Y-%m-%d")
		dates.append(d)

		worksheet.write(row, col, fev11)
		worksheet.write(row, col+1, fev12)
		worksheet.write(row, col+2, fev13)
		worksheet.write(row, col+3, fev14)
		worksheet.write(row, col+4, fev15)
		worksheet.write(row, col+5, fev16)
		worksheet.write(row, col+6, max_fev)

		row += 1

	# calculate overall max, %max, mean %mean
	overall_max = max(max_fev_list)
	overall_mean = sum(max_fev_list) / len(max_fev_list)

	row = 30

	for (fev11, fev12, fev13, fev14, fev15, fev16, test_date) in cursor:
		fev_list = [float(fev11), float(fev12), float(fev13), float(fev14), float(fev15), float(fev16)]
		max_fev = max(fev_list)
		worksheet.write(row, col+7, overall_max)
		worksheet.write(row, col+8, (max_fev-overall_max)/overall_max)
		worksheet.write(row, col+9, overall_mean)
		worksheet.write(row, col+10, (max_fev-overall_mean)/overall_mean)
		worksheet.write(row, col+11, float(max_fev)*1000)
		worksheet.write(row, col+12, test_date)
		# how to get SHS date
		# worksheet.write(row, col+13, )

	date_list = [(date-dates[0]).days for date in dates]

	# create linear regression object
	regr = linear_model.LinearRegression()
	regr.fit(np.array(date_list).reshape(-1,1), np.array(max_fev_list).reshape(-1,1))
	predicted = regr.predict(np.array(date_list).reshape(-1,1))

	# plt.legend(loc='best')
	# plt.xticks(np.arange(25),devices)
	fig, ax = plt.subplots(figsize=((10,6)))
	plt.plot([date_list[0],date_list[-1]],[predicted[0],predicted[-1]],"k--")
	plt.scatter(date_list, max_fev_list, marker='s')
	plt.xlabel('Days',fontsize = 22)
	plt.ylabel('FEV1(ml)', fontsize = 22)

	plt.title('Monitoring Month', loc="center", fontsize=22)

	# plt.show()

	fig.savefig(filename+'.png')

	worksheet.insert_image('B9', filename+'.png', {'x_scale':0.5, 'y_scale':0.5})

	workbook.close()

# report generated to be sent to the site
def site_generate(filename, patient_id, host='localhost'):
	cnx = mysql.connector.connect(user='root',password='mysql_db',host=host,database='spirometry_db')

	workbook = xlsxwriter.Workbook(filename+'.xlsx')
	worksheet = workbook.add_worksheet()

	worksheet.write('B2', 'Patient Study Number')
	worksheet.write('B3', 'Date')
	worksheet.write('B4', 'Monitoring Month')
	worksheet.write('B5', 'NL Range')
	worksheet.write('B6', 'Variance')
	worksheet.write('B7', 'Lowest FEV1')

	worksheet.write('E2', 'WU1')
	now = datetime.datetime.now()
	worksheet.write('E3', now.strftime("%Y-%m-%d"))

	cursor = cnx.cursor(buffered=True)
	query = "SELECT minRate, maxRate, lowestSat, timeAbnormal, timeMinRate FROM pulse_data WHERE patient_id = %s"
	cursor.execute(query, (patient_id,))

	cursor = cnx.cursor(buffered=True)
	query = "SELECT survey_text FROM survey_data WHERE patient_id = %s"
	cursor.execute(query, (patient_id,))

	# generate supplemental data
	worksheet.write('G2','Supplemental Data')
	worksheet.write('I2','NL')
	worksheet.write('J2','ABNL')
	worksheet.write('G3','Questionnaire')
	worksheet.write('G4','Oximetry')
	worksheet.write('H5','O2 Sat')
	worksheet.write('H6','Duration Abnormal')
	worksheet.write('H7','Duration Lowest')
	worksheet.write('H8','Heart Rate')

	if lowestSat < 97:
		worksheet.write('J5', lowestSat)
		worksheet.write('J6', timeAbnormal)
		worksheet.write('J7', timeMinRate)
		worksheet.write('J8', minRate + " vs " + maxRate)
	else:
		worksheet.write('I5', '>97%')
		worksheet.write('I6', 'NA')
		worksheet.write('I7', 'NA')
		worksheet.write('I8', 'NL')

	symptomString = ""
	symptomArr = ["Cough", "RunnyNose", "Sore Throat", "Fever", "Shaking Chills", "Muscle Aches", "Recent Diarrhea", "Cough", "Cough now producing sputum/mucus", "New wheezing", "New shortness of breath", "Generalized weakness", "Lightheadedness", "Dizziness", "Shortness of breath at rest", "Shortness of breath on exertion", "Shortness of breath when lying flat", "New Shortness of breath that awakens you from sleep", "New swelling in your feet or legs"]

	for i in range (0, len(survey_text)):
		if survey_text[i] == "1":
			symptomString = symptomString + symptomArr[i] + ", "

	if symptomString != "":
		worksheet.write('I3', 'Negative')
	else:
		worksheet.write('I4', symptomString)


		cursor = cnx.cursor(buffered=True)
		query = "SELECT fev11, fev12, fev13, fev14, fev15, fev16, test_date FROM small_test WHERE patient_id = %s"
		cursor.execute(query, (patient_id,))


	row = 30
	col = 1
	max_fev_list = []
	dates = []
	for (fev11, fev12, fev13, fev14, fev15, fev16, test_date) in cursor:
		fev_list = [float(fev11)*1000, float(fev12)*1000, float(fev13)*1000, float(fev14)*1000, float(fev15)*1000, float(fev16)*1000]
		max_fev = max(fev_list)
		max_fev_list.append(max_fev)
		d = datetime.datetime.strptime(test_date,"%Y-%m-%d")
		dates.append(d)

	row += 1

	date_list = [(date-dates[0]).days for date in dates]

	# create linear regression object
	regr = linear_model.LinearRegression()
	regr.fit(np.array(date_list).reshape(-1,1), np.array(max_fev_list).reshape(-1,1))
	predicted = regr.predict(np.array(date_list).reshape(-1,1))

	# plt.legend(loc='best')
	# plt.xticks(np.arange(25),devices)
	fig, ax = plt.subplots(figsize=((10,6)))
	plt.plot([date_list[0],date_list[-1]],[predicted[0],predicted[-1]],"k--")
	plt.scatter(date_list, max_fev_list, marker='s')
	plt.xlabel('Days',fontsize = 22)
	plt.ylabel('FEV1(ml)', fontsize = 22)

	plt.title('Monitoring Month', loc="center", fontsize=22)

	# plt.show()

	fig.savefig(filename+'.png')

	worksheet.insert_image('B9', filename+'.png', {'x_scale':0.5, 'y_scale':0.5})

	workbook.close()

	# send report to sites
	send_report_site(patient_id, filename)


def calc_nl(patient_id, host='localhost'):
	cnx = mysql.connector.connect(user='root',password='mysql_db',host=host,database='spirometry_db')
	cursor = cnx.cursor(buffered=True)

	query = "SELECT fev11, fev12, fev13, fev14, fev15, fev16 FROM small_test WHERE patient_id = %s"
	cursor.execute(query, (patient_id,))

	fev_all = []

	for (fev11, fev12, fev13, fev14, fev15, fev16) in cursor:
		fev_list = [float(fev11), float(fev12), float(fev13), float(fev14), float(fev15), float(fev16)]
		max_fev = max(fev_list)
		fev_all.append(max_fev)

	low_limit = min(fev_all)
	up_limit = max(fev_all)

	nl_range = str(low_limit) + '-' + str(up_limit)

	insert_range_statement = "UPDATE meta_data SET normal_range = %s WHERE patient_id = %s VALUES (%s,%s)"
	cursor.execute(insert_range_statement,(nl_range, patient_id))

# send reports to different sites
def send_report_site(patient_id, filename):
	site_id = patient_id[0:3]
	email_addr = email_list[site_id]
	# organize command line for sending emails
	email_title = '"'+filename+'"'
	commandline = '/usr/local/bin/sendEmail -f qolecp@gmail.com -t ' + email_addr + ' -u ' + email_title + '-m "See attachment"' + ' -a ' + filename + ' -s smtp.gmail.com:587 -o tls=yes -xu qolecp@gmail.com -xp Qol13579#'
	os.system(commandline)

# send reports to cmi
def send_report_cmi(patient_id):
	pass

def nl_sendback(patient_id, host='localhost'):
	cnx = mysql.connector.connect(user='root',password='mysql_db',host=host,database='spirometry_db')
	cursor = cnx.cursor(buffered=True)

	query = "SELECT normal_range FROM meta_data WHERE patient_id = %s"
	cursor.execute(query, (patient_id,))

	# nl_range = cursor[0].split('-')
	nl_range = cursor[0]

def main(patient_id, server):
	generate('test', patient_id, server)

if __name__ == '__main__':
	parser = argparse.ArgumentParser(description='Test the report generator.')
	parser.add_argument('patient_id', metavar='ID', type=str,
						help='the patient id for which to generate the report')

	parser.add_argument('--server', dest='server', default='localhost',
						help='the address of the host server for the MySQL database')

	args = parser.parse_args()
	main(args.patient_id, args.server)
