# script to generate report
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import xlsxwriter
import datetime
import mysql.connector
import numpy as np
from sklearn import linear_model
from sklearn.metrics import mean_squared_error, r2_score
from scipy.stats import pearsonr


def main():
	cnx = mysql.connector.connect(user='root',password='mysql_db',host='localhost',database='spirometry_db')

	workbook = xlsxwriter.Workbook('test.xlsx')
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

	# generate supplemental data
	worksheet.write('G2','Supplemental Data')
	worksheet.write('I2','NL')
	worksheet.write('J2','ABNL')
	worksheet.write('G3','Questionnaire')
	worksheet.write('G4','Oximetry')
	worksheet.write('H5','O2 Sat')
	worksheet.write('H6','Duration')
	worksheet.write('H7','Heart Rate')

	patient_id = '100001'

	cursor = cnx.cursor()
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
		fev_list = [float(fev11)*1000, float(fev12)*1000, float(fev13)*1000, float(fev14)*1000, float(fev15)*1000, float(fev16)*1000]
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
		worksheet.write(row, col+6, max_fev / 1000.0)

		row += 1

	date_list = [(date-dates[0]).days for date in dates]

	# create linear regression object
	regr = linear_model.LinearRegression()
	regr.fit(np.array(date_list).reshape(-1,1), np.array(max_fev_list).reshape(-1,1))
	predicted = regr.predict(np.array(date_list).reshape(-1,1))

	slope = regr.coef_
	r = pearsonr(np.array(max_fev_list).reshape(-1,1), predicted)[0][0]
	p = pearsonr(np.array(max_fev_list).reshape(-1,1), predicted)[1][0]
	r2 = r2_score(np.array(max_fev_list).reshape(-1,1), predicted)

	print "r:", r, "r2:", r2, "p:", p

	# write linear regression statistics to spreedsheet
	worksheet.write('I24', 'Slope')
	worksheet.write('J24', 'R')
	worksheet.write('K24', 'R-square')
	worksheet.write('L24', 'Count')
	worksheet.write('M24', 'P-value')

	worksheet.write('I27', slope)
	worksheet.write('J27', r)
	worksheet.write('K27', r2)
	worksheet.write('L27', len(max_fev_list))
	worksheet.write('M27', p)

	#plt.legend(loc='best')
	#plt.xticks(np.arange(25),devices)
	fig, ax = plt.subplots(figsize=((10,6)))
	plt.plot([date_list[0],date_list[-1]],[predicted[0],predicted[-1]],"k")
	plt.plot([date_list[0],date_list[-1]],[max(max_fev_list),max(max_fev_list)],"r")
	plt.plot([date_list[0],date_list[-1]],[min(max_fev_list),min(max_fev_list)],"r")
	plt.scatter(date_list, max_fev_list, marker='s')
	plt.xlabel('Days',fontsize = 22)
	plt.ylabel('FEV1(ml)', fontsize = 22)

	"""
	plt.legend(bbox_to_anchor=(0.02,0.90,0.96,0.12), loc="lower left",
	            mode="expand", borderaxespad=0, ncol=3, fontsize = 20)
	"""
	plt.title('Monitoring Month', loc="center", fontsize=22)

	plt.show()

	fig.savefig('fev1_fig.png')

	worksheet.insert_image('B9', 'fev1_fig.png', {'x_scale':0.5, 'y_scale':0.5})

	workbook.close()

if __name__ == '__main__':
	main()
