
psql_host=$1
psql_port=$2
db_name=$3
psql_user=$4
psql_password=$5


if [ "$#" -ne 5 ]; then
    echo "Illegal number of parameters"
    exit 1
fi


vmstat_mb=$(vmstat --unit M)
hostname=$(hostname -f)
cpu_idle=$(echo "$vmstat_mb" | tail -1 | awk '{print $15}' | xargs)


memory_free=$(echo "$vmstat_mb" |  tail -1 | awk '{print $4}'| xargs)


cpu_kernel=$(echo "$vmstat_mb"| tail -1 | awk '{print $14}'| xargs)

disk_io=$(vmstat -d | tail -1 | awk '{print $10}')

disk_available=$(df -BM / | tail -1 | awk '{print $4}'| sed 's/[^0-9]*//g')


timestamp=$(vmstat -t | awk '{print $18, $19}' | tail -1 | xargs)


host_id="(SELECT id FROM host_info WHERE hostname='$hostname')"


insert_stmt=$(cat << EOF

INSERT INTO host_usage (
  "timestamp",
  host_id,
  memory_free,
  cpu_idle,
  cpu_kernel,
  disk_io,
  disk_available
) VALUES (
  '$timestamp',
  $host_id,
  $memory_free,
  $cpu_idle,
  $cpu_kernel,
  $disk_io,
  $disk_available
);
EOF
)

export PGPASSWORD=$psql_password


psql -h $psql_host -p $psql_port -d $db_name -U $psql_user -c "$insert_stmt"

exit $?
