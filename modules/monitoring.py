import streamlit as st
import boto3
from datetime import datetime
import time

REGION = "eu-north-1"


@st.cache_resource
def get_logs_client():
    """Returns cached CloudWatch Logs client."""
    return boto3.client("logs", region_name=REGION)


@st.cache_data(ttl=43200)  # Refresh log group list every 12 hours
def fetch_log_groups():
    """Fetches  CloudWatch."""
    try:
        client = get_logs_client()
        response = client.describe_log_groups(logGroupNamePrefix="/ecs/")
        return [group["logGroupName"] for group in response.get("logGroups", [])]
    except Exception as e:
        st.error(f"Error fetching log groups: {str(e)}")
        return []


def fetch_cloudwatch_logs(log_group_name, limit, minutes_back):
    """Fetches and formats newest logs from selected group."""
    try:
        client = get_logs_client()
        
        start_time_ms = int((time.time() - (minutes_back * 60)) * 1000)
        
        paginator = client.get_paginator('filter_log_events')
        page_iterator = paginator.paginate(
            logGroupName=log_group_name,
            startTime=start_time_ms,
            interleaved=True
        )
        
        all_events = []
        for page in page_iterator:
            all_events.extend(page.get("events", []))
            if len(all_events) > limit * 5:
                all_events = all_events[-(limit * 5):]

        if not all_events:
            return f"No log events found in the last {minutes_back} minutes."

        latest_events = all_events[-limit:]

        formatted_logs = []
        for event in latest_events:
            dt = datetime.fromtimestamp(event["timestamp"] / 1000.0)
            time_str = dt.strftime("%Y-%m-%d %H:%M:%S")
            message = event["message"].strip()
            formatted_logs.append(f"[{time_str}] {message}")

        return "\n".join(formatted_logs)
        
    except Exception as e:
        return f"[ERROR] Failed to fetch logs: {str(e)}"


def display():
    st.title("ScaleDrop - System Monitoring")
    st.write("Real-time logs fetched directly from AWS CloudWatch.")
    st.divider()

    available_log_groups = fetch_log_groups()

    if not available_log_groups:
        st.warning("No ECS log groups found. Make sure your services are generating logs and have the prefix '/ecs/'.")
        return

    col1, col2 = st.columns([1, 3])

    with col1:
        friendly_names = {group: group.replace("/ecs/", "") for group in available_log_groups}
        
        selected_friendly_name = st.radio(
            "Select service:", 
            list(friendly_names.values())
        )
        selected_log_group = [k for k, v in friendly_names.items() if v == selected_friendly_name][0]

        st.divider()
        
        time_options = {
            "Last 15 minutes": 15,
            "Last 1 hour": 60,
            "Last 12 hours": 720,
            "Last 24 hours": 1440,
            "Last 3 days": 4320,
            "Last 7 days": 10080
        }
        selected_time_label = st.selectbox(
            "Time range:", 
            list(time_options.keys()), 
            index=1
        )
        selected_minutes = time_options[selected_time_label]

        selected_limit = st.slider(
            "Max log entries:", 
            min_value=50, 
            max_value=2000, 
            value=150, 
            step=50
        )

        st.button("Refresh logs", use_container_width=True, type="primary")

    with col2:
        st.subheader(f"Last events: {selected_friendly_name}")

        with st.spinner("Fetching logs from AWS CloudWatch..."):
            logs_content = fetch_cloudwatch_logs(
                log_group_name=selected_log_group, 
                limit=selected_limit, 
                minutes_back=selected_minutes
            )

        with st.container(height=650):
            st.code(logs_content, language="log")