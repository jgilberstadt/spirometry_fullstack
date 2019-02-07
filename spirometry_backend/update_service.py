# service that run nightly to updates the model

import datetime
import mysql.connector
import numpy as np
import reportgenerator as rg

cnx = mysql.connector.connect(user='root',password='mysql_db',host='localhost',database='spirometry_db')
cursor = cnx.cursor()
# select all data from
query = "SELECT patient_id, first_date FROM meta_data"
cursor.execute(query)

# check whether it is the time for generating report
for (pid, first_date) in cursor:
	fd = datetime.datetime.strptime(first_date,"%Y-%m-%d")
	now = datetime.datetime.now()
	# enter monitoring mode
	if(fd-now==56):
		rg.calc_nl(pid)
	# enter survillance mode and generate report
	elif((fd-now)%30==0 and (fd-now)>56):
		# report style 1 (CMI)
		rg.generate(pid+'_monthly_cmi',pid)
		# report style 2 (site)
		rg.site_generate(pid+'_monthly_site',pid)
