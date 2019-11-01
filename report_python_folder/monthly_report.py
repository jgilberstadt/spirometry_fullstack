# subclass for generating the monthly report

# import libraries for PostgreSQL connection
from dbconfig import config
import psycopg2
# import libraries for loading workbook templates
from openpyxl import load_workbook
# import the basic class
from basic_report import BasicReport

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
		# query patient study number
		patient_study_number_cursor = self.cnx.cursor()
		patient_study_number_cursor.execute('SELECT patient_id FROM patient_data WHERE imei_num=%s', (self.imei_num))
		for patient_study_number in patient_study_number_cursor:
			self.ws['E2'] = patient_study_number
			print patient_study_number
			break

	# If everything is taken care by the above two methods, we can just save the xlsx in this method
	def generateReport(self):
		self.wb.save(filename = self.imei_num+"_Monthly_Report_"+str(self.current_month)+".xlsx")