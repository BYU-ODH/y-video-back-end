from datetime import datetime
from glob import glob
import json
import sys

from matplotlib import pyplot as plt
from tqdm import tqdm


START_DATE = datetime(2022, 8, 29)
END_DATE = datetime(2022, 12, 15)
DUR_HOURS = (END_DATE - START_DATE).total_seconds()/3600

timestamps = []
# Wed Feb 24 20:46:00 MST 2021
# Fri Nov  4 10:05:55 MDT 2022
format = '%a %b %d %H:%M:%S %Z %Y'
for fname in tqdm(glob(f'{sys.argv[1]}/*.log')):
    with open(fname) as f:
        for line in f:
            timestamp = json.loads(line)['timestamp']
            timestamp = datetime.strptime(timestamp, format)
            timestamps.append(timestamp)

timestamps = [t for t in timestamps if START_DATE <= t <= END_DATE]
plt.hist(timestamps, bins=int(DUR_HOURS))
plt.show()
