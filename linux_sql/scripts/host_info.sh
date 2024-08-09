
psql_host=$1
psql_port=$2
db_name=$3
psql_user=$4
psql_password=$5

# Check # of args
if [ "$#" -ne 5 ]; then
    echo "Illegal number of parameters"
    exit 1
fi

# Save machine statistics in MB and current machine hostname to variables

hostname=$(hostname -f)
lscpu=$(lscpu)
cpuinfo=$(cat "/proc/cpuinfo")
meminfo=$(cat "/proc/meminfo")

cpu_number=$(awk '/^CPU\(s\):/ {print $2}' <<< "$lscpu" | xargs)
cpu_architecture=$(awk '/^Architecture:/ {print $2}' <<< "$lscpu" | xargs)
cpu_model=$(awk -F': ' '/^Model name:/ {print $2}' <<< "$lscpu" | xargs)
cpu_mhz=$(echo "$cpuinfo" | grep -E "^cpu\sMHz" | tail -1 | awk '{print $4}' | xargs)
l2_cache=$(awk -F': ' '/^L2 cache:/ {print $2}' <<< "$lscpu" | awk '{print $1}'| xargs)
timestamp=$(vmstat -t | awk '{print $18, $19}' | tail -1 | xargs)
total_mem=$(awk '/^MemTotal:/ {print $2}' <<< "$meminfo" | xargs)

insert_stmt=$(cat << EOF
INSERT INTO host_info (
  id,
  hostname,
  cpu_number,
  cpu_architecture,
  cpu_model,
  cpu_mhz,
  l2_cache,
  "timestamp",
  total_mem
) VALUES (
  DEFAULT,
  '$hostname',
  $cpu_number,
  '$cpu_architecture',
  '$cpu_model',
  $cpu_mhz,
  $l2_cache,
  '$timestamp',
  $total_mem
);
EOF
)

export PGPASSWORD=$psql_password

psql -h $psql_host -p $psql_port -d $db_name -U $psql_user -c "$insert_stmt"

exit $?