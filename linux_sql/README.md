# Linux Cluster Monitoring Agent
## Introduction

This project is an automated tool for monitoring and recording resource usage in a Linux Cluster. It continuously gathers data on CPU, memory, and disk usage across machines, and stores the information in a specialized database. Tailored for system administrators and developers, this solution is ideal for managing and optimizing the performance of both physical and virtual Linux systems.
### Technologies
- **Bash:** Write scripts to retrieve system information from the machine
- **Docker:** Deploy and manage a PostgreSQL container with  storage.
- **Git:** Track and manage changes to the project.
- **Postgres:** Store and maintain the collected machine data.
- **Rocky 9:** The operating system environment used for development.

## Quick Start
1. Clone this repository
```
#Use cd to set the appropriate location where you want to set up the repo
git clone jarviscanada/jarvis_data_eng_HeemanshSookha.git
```
2. Start a psql instance using psql_docker.sh
```
./scripts/psql_docker.sh create db_username db_password
```
3. Create tables using ddl.sql
```
psql -h localhost -U postgres -d host_agent -f sql/ddl.sql
```

4. Insert host info to the database
```
.../linux_sql/scripts/host_info.sh <psql_host> <psql_port> <db_name> <psql_user> <psql_password>
```
5. Setup crontab to update host usage every minute
```
crontab -e

# Inside crontab and not terminal
* * * * * .../linux_sql/scripts/host_usage.sh <psql_host> <psql_port> <db_name> <psql_user> <psql_password>
```

## Implementation
### Architecture
This Project was designed for any number of hosts that have access to a shared volume space on docker where all of them shares their usage  for a distinct period of time and their info only once.
### Scripts
- `psql_docker.sh`:Controls the PostgreSQL container on a Linux host.
- `ddl.sql`:Runs to create the volume and storage tables if not already existing
- `host_info.sh`: Runs once on every host and stores their hardware info
- `host_usage.sh`: Runs every set period of time on each host and stores the usage info in the set table
- `crontab`: Runs the host_usage automatically every set period of time
### Database Modeling
The database has two tables where we keep track of each host machine and each new log is associated with such machine.
### `host_info`
- `id`,`hostname`,`cpu_number`,`cpu_architecture`,`cpu_model`,`cpu_mhz`,`l2_cache`,`timestamp`,`total_mem`. 
   Host_info uses data manipulation techniques to extract the above hardware info from `lscpu`,`meminfo` and `cpuinfo`and then stores them in the host usage table

### `host_usage`
- `timestamp`,`host_id`,`memory_free`,`cpu_idle`,`cpu_kernel`,`disk_io`,`disk_available`. Similarly to Host_info we used data manipulation techniques to extract these information from `Vmstat`,`hostname`. These info are extracted every time the script is run under crontab.

## Test
Manual testing was used to test the bash scripts DDL.
### `psql_docker.sh`
After each run, we checked to verify that it produced the appropriate results. For example: when we run the script and afterward we manually run the command prompt to see if the docker instance is running.
### `host_info.sh` and `host_usage.sh`
Same for both, we run each script and then manually verify the info from `lscpu` and `vmstat` to see that whether they match the info in the table.


## Deployment
The application is deployed using both Docker and crontab. Docker is used to run PostgreSQL in an isolated environment, while crontab is employed to execute the host_usage.sh script every minute on the host machine.

## Improvement
- Modify the Host_Info to include periodic updates to take in consideration any possible hardware upgrades and modify the input in the table.
-  Implement comprehensive logging for each script, especially for error cases. This would help in quickly identifying issues by reviewing log files.
- Develop a simple web interface or dashboard to visualize the collected data in real-time, making it easier for administrators to monitor the system?s status.