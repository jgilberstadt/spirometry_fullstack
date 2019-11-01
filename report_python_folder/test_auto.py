"""
from openpyxl import load_workbook
wb = load_workbook(filename = 'Report template - R6.xlsx')
ws1 = wb['Report Datasheet (Monthly)']
print ws1['C27'].value

wb.save(filename = 'Report save.xlsx')
"""

from monthly_report import MonthlyReport

mr_obj = MonthlyReport(imei_num='176927',current_month=1)
mr_obj.generateReport()
