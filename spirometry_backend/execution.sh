#! /bin/bash
# execute scripts to generate reports 
python /home/dingwen/Documents/spirometry_backend/test_service.py
# send the reports via email
/usr/local/bin/sendEmail -f qolecp@gmail.com -t qolecp@gmail.com -u "test auto sending" -m "message body" -s smtp.gmail.com:587 -o tls=yes -xu qolecp@gmail.com -xp Qol13579#
