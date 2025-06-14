#!/bin/bash

# Simple script to generate test logs for brute force detection

LOG_FILE="/logs/test.log"

echo "Generating JSON test authentication logs..."
echo "📁 Writing to: $LOG_FILE"

# Create the logs directory if it doesn't exist
sudo mkdir -p /logs

# Clear the log file (or create it)
sudo touch $LOG_FILE
sudo truncate -s 0 $LOG_FILE

# Function to generate ISO timestamp
get_timestamp() {
    date -u +"%Y-%m-%dT%H:%M:%SZ"
}

echo "📝 Generating 5 failed login attempts from same IP (JSON format)..."

# Failed login attempt 1
echo "{\"timestamp\": \"$(get_timestamp)\", \"level\": \"ERROR\", \"message\": \"User login failed\", \"username\": \"admin\", \"source_ip\": \"192.168.1.100\", \"action\": \"login\", \"status\": \"failure\", \"status_code\": 401}" | sudo tee -a $LOG_FILE > /dev/null
sleep 1

# Failed login attempt 2  
echo "{\"timestamp\": \"$(get_timestamp)\", \"level\": \"ERROR\", \"message\": \"User login failed\", \"username\": \"admin\", \"source_ip\": \"192.168.1.100\", \"action\": \"login\", \"status\": \"failure\", \"status_code\": 401}" | sudo tee -a $LOG_FILE > /dev/null
sleep 1

# Failed login attempt 3
echo "{\"timestamp\": \"$(get_timestamp)\", \"level\": \"ERROR\", \"message\": \"User login failed\", \"username\": \"admin\", \"source_ip\": \"192.168.1.100\", \"action\": \"login\", \"status\": \"failure\", \"status_code\": 401}" | sudo tee -a $LOG_FILE > /dev/null
sleep 1

# Failed login attempt 4
echo "{\"timestamp\": \"$(get_timestamp)\", \"level\": \"ERROR\", \"message\": \"User login failed\", \"username\": \"admin\", \"source_ip\": \"192.168.1.100\", \"action\": \"login\", \"status\": \"failure\", \"status_code\": 401}" | sudo tee -a $LOG_FILE > /dev/null
sleep 1

# Failed login attempt 5 (should trigger brute force alert)
echo "{\"timestamp\": \"$(get_timestamp)\", \"level\": \"ERROR\", \"message\": \"User login failed\", \"username\": \"admin\", \"source_ip\": \"192.168.1.100\", \"action\": \"login\", \"status\": \"failure\", \"status_code\": 401}" | sudo tee -a $LOG_FILE > /dev/null

echo "✅ Generated 5 failed login attempts in JSON format!"
echo ""
echo "📄 Log file contents:"
echo "=================="
sudo cat $LOG_FILE
echo "=================="
echo ""
echo "Java collector should now pick up these logs and send them to Kafka"
echo "Scala processor should detect the brute force pattern and generate an alert"