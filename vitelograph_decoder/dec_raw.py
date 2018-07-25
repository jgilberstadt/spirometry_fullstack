# decode device raw string
str_text = "GTD00001851003322873234230762750000000000000950900001807182058040961e"
did = str_text[3:13]
pef = str_text[13:16]
fev075 = str_text[16:19]
fev1 = str_text[19:22]
fev10 = str_text[22:25]
fev1fev10 = str_text[25:28]
fef2575 = str_text[28:31]
fev1pb = str_text[31:34]
pefpb = str_text[34:37]
fev1p = str_text[37:40]
pefp = str_text[40:43]
gz = str_text[43:46]
yz = str_text[46:49]
oz = str_text[49:52]
year = str_text[52:54]
month = str_text[54:56]
day = str_text[56:58]
hour = str_text[58:60]
minute = str_text[60:62]
second = str_text[62:64]
gt = str_text[64:65]
swm = str_text[65:]

print "device_id:", did 
print "pef:", pef
print "fev0.75:", fev075
print "fev1:", fev1
print "fev10:", fev10
print "fev1/fev10:", fev1fev10
print "fef2575:", fef2575
print "fev1 personal best:", fev1pb
print "pef personal best:", pefpb
print "fev1%:", fev1p
print "pef%:", pefp
print "green zone:", gz
print "yellow zone:", yz
print "orange zone:", oz
print "year:", year
print "month:", month
print "day:", day
print "hour:", hour
print "minute:", minute
print "second:", second
print "good test:", gt
print "sw number:", swm