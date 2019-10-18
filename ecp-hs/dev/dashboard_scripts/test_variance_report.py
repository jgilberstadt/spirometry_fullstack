# test variance report generator

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

rgObj = ReportGenerator('100001', viewing_month="3", delete_files=False)
rgObj.generateVarianceReport()
#rgObj.sendReports()