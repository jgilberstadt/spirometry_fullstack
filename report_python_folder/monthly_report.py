# subclass for generating the monthly report

# import libraries for PostgreSQL connection
from dbconfig import config
# import libraries for loading workbook templates
from openpyxl import load_workbook
# import the basic class
from basic_report import BasicReport

import psycopg2
from datetime import datetime

class MonthlyReport(BasicReport):
	def __init__(self, imei_num, current_month):
		BasicReport.__init__(self, db_config_path='credentials/database.ini', 
			workbook_path='Report_Templates/Report template - R6.xlsx', report_type=0)

		# put report-specific initalization here
		# e.g. graph objects, replaced values queried from DB
		self.imei_num = imei_num
		self.current_month = current_month
		self.ws = self.wb['Report Datasheet (Monthly)']

	# Plot the graphs and place them in the spreedsheet
	def plotGraph(self):
		print("Need implementation")

	# Query the PostgreSQL for personalized values to replace the values in the report template
	def replaceValues(self):
		cursor = self.cnx.cursor()
		# obtain current datetime
		report_date = datetime.now()
		self.ws['E3'] = report_date.strftime("%m/%d/%y")
		# query patient study number and other information that can be used later
		cursor.execute("SELECT patient_id, start_study_date, date_of_transplant, mode FROM patient_data WHERE imei_num=%s", ([self.imei_num]))
		for (patient_study_number, start_study_date, date_of_transplant, mode) in cursor:
			self.ws['E2'] = patient_study_number
			break

		# create a list of fev1max values to be used in replaceValues(self) function
		self.fev1max_list = []
		# create a list of date to be used in replaceValues(self) function
		self.test_date_list = []

		# query all fev1 values from spiro_data table
		raw_data_incremental_index = 34
		cursor.execute("SELECT fev11, fev12, fev13, fev14, fev15, fev16, test_date, is_variance, variance_test_counter FROM spiro_data WHERE imei_num=%s", ([self.imei_num]))
		for (fev11, fev12, fev13, fev14, fev15, fev16, test_date, is_variance, variance_test_counter) in cursor:
			self.ws['A'+str(raw_data_incremental_index)] = patient_study_number
			self.ws['B'+str(raw_data_incremental_index)] = fev11
			self.ws['C'+str(raw_data_incremental_index)] = fev12
			self.ws['D'+str(raw_data_incremental_index)] = fev13
			self.ws['E'+str(raw_data_incremental_index)] = fev14
			self.ws['F'+str(raw_data_incremental_index)] = fev15
			self.ws['G'+str(raw_data_incremental_index)] = fev16
			self.ws['N'+str(raw_data_incremental_index)] = test_date
			self.ws['O'+str(raw_data_incremental_index)] = start_study_date
			self.ws['Q'+str(raw_data_incremental_index)] = date_of_transplant
			self.fev1max_list.append(max(float(fev11), float(fev12), float(fev13), float(fev14), float(fev15), float(fev16)))
			self.test_date_list.append(test_date)

			# need to associate each session with mode
			if mode == 1:
			   self.ws['S'+str(raw_data_incremental_index)] = 'Pre-surv'
			elif mode == 2 or mode == 3:
			   self.ws['S'+str(raw_data_incremental_index)] = 'Month'
			#a
			raw_data_incremental_index += 1

		cursor.execute("SELECT maxrate, minrate, lowestsat FROM pulse_data WHERE imei_num=%s AND test_date BETWEEN current_date - 30 and current_date", ([self.imei_num]))
		currentminrate = 1000
		currentlowestsat = 1000
		currentmaxrate = 0
		for (maxrate, minrate, lowestsat) in cursor:
			if minrate < currentminrate and minrate > 30:
				currentminrate = minrate
			if lowestsat < currentlowestsat:
				currentlowestsat = lowestsat
			if maxrate > currentmaxrate and maxrate > 30:
				currentmaxrate = maxrate

		print(currentminrate)
		print(currentmaxrate)
		print(lowestsat)
		self.ws['I11'] = str(currentminrate)+'-'+str(currentmaxrate)
		self.ws['I8'] = lowestsat

	
			
	# If everything is taken care by the above two methods, we can just save the xlsx in this method
	def generateReport(self):
		self.wb.save(filename = self.imei_num+"_Monthly_Report_"+str(self.current_month)+".xlsx")
