# subclass for generating the monthly report

# import libraries for PostgreSQL connection
from dbconfig import config
import psycopg2
# import libraries for loading workbook templates
from openpyxl import load_workbook
# import the basic class
from basic_report import BasicReport

class MonthlyReport(BasicReport):
	def __init__(self):
		super().__init__(db_config_path='../credential/database.ini', 
			workbook_path='Report_Templates/Report template - R6.xlsx', report_type=0)

		# put report-specific initalization here
		# e.g. graph objects, replaced values queried from DB

	# Plot the graphs and place them in the spreedsheet
	def plotGraph(self):

	# Query the PostgreSQL for personalized values to replace the values in the report template
	def replaceValues(self):

# If everything is taken care by the above two methods, we can just save the xlsx in this method
	def generateReport(self):