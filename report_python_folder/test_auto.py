from openpyxl import load_workbook
wb = load_workbook(filename = 'Report template - R6.xlsx')
ws1 = wb['Report Datasheet (Monthly)']
print ws1['C27'].value

wb.save(filename = 'Report save.xlsx')