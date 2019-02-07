import datetime
import sys

class MonitoringMonthList:
    def __init__(self, start_study_date, nl_end_date, viewing_month):
        now = datetime.datetime.now()
        now_date = datetime.date(now.year, now.month, now.day)
        self.monitoring_months = []
        self.date_to_monitoring_month_map = {}
        self.monitoring_months.append(MonitoringMonth(-1, start_study_date, nl_end_date if nl_end_date != None else now_date, sys.maxint, 0))
        self.viewing_month_index = 0
        if nl_end_date != None:
            working_start_date = nl_end_date + datetime.timedelta(days=1)
            i = 1
            while working_start_date < now_date:
                working_end_date = working_start_date + datetime.timedelta(days=29)
                new_month = MonitoringMonth(i, working_start_date, working_end_date, sys.maxint, 0)
                self.monitoring_months.append(new_month)
                if viewing_month == str(i):
                    self.viewing_month_index = i
                working_start_date = working_end_date + datetime.timedelta(days=1)
                i += 1

            if viewing_month == None:
                viewing_month = str(i)
                self.viewing_month_index = i

    def __getitem__(self, key):
        return self.get_month(key)
    
    def __iter__(self):
        return iter(self.monitoring_months)
        
    def get_month(self, index):
        return self.monitoring_months[index]

    def get_viewing_month(self):
        return self.monitoring_months[self.viewing_month_index]

    def set_start_row(self, index, start_row):
        self.monitoring_months[index].start_row = start_row
    
    def set_end_row(self, index, end_row):
        self.monitoring_months[index].end_row = end_row
    
    def get_month_by_date(self, date):
        if not date in self.date_to_monitoring_month_map:
            for j in range(len(self.monitoring_months)):
                if self.monitoring_months[j].end_date >= date and self.monitoring_months[j].start_date <= date:
                    self.date_to_monitoring_month_map[date] = self.monitoring_months[j]
                    break

            # If we couldn't find a monitoring month to map to the date, set it to None in the map
            if not date in self.date_to_monitoring_month_map:
                self.date_to_monitoring_month_map[date] = None
        return self.date_to_monitoring_month_map[date]

    def __len__(self):
        return len(self.monitoring_months)

class MonitoringMonth:
    def __init__(self, month_number, start_date, end_date, start_row, end_row):
        self.month_number = month_number
        self.start_date = start_date
        self.end_date = end_date
        self.start_row = start_row
        self.end_row = end_row
    
    def get_month_name(self, just_number = False):
        if self.month_number > 0 and not just_number:
			return "Monitoring Month {0}".format(self.month_number)
        elif self.month_number > 0:
            return self.month_number
        else:
			return "NL Range"
    
    def __eq__(self, other):
        return self.month_number == other.month_number and \
               self.start_date == other.start_date and \
               self.end_date == other.end_date and \
               self.start_row == other.start_row and \
               self.end_row == other.end_row
    
    def __str__(self):
        return "monitoringmonths.MonitoringMonth({0},{1},{2},{3},{4})".format(
            self.month_number, 
            self.start_date, 
            self.end_date, 
            self.start_row, 
            self.end_row)