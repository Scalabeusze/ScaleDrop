import streamlit as st
import time


def display():
    st.title("ScaleDrop - System Monitoring")
    st.write("THIS IS A MOCKUP - in the future here will lie CloudWatch logs.")
    st.divider()

    col1, col2 = st.columns([1, 3])

    with col1:
        selected_log_group = st.radio(
            "Select service:", ["sd-bff-service", "sd-iam-service", "sd-admin-service"]
        )
        st.button("Refresh logs", use_container_width=True)

    with col2:
        st.subheader(f"Last events: {selected_log_group}")

        # PLACEHOLDER MOCKUP
        with st.container(height=400):
            st.code(
                f"""
[INFO] {time.strftime("%Y-%m-%d %H:%M:%S")} - {selected_log_group} started successfully.
[INFO] {time.strftime("%Y-%m-%d %H:%M:%S")} - Connected to database.
[WARN] {time.strftime("%Y-%m-%d %H:%M:%S")} - High memory usage detected.
[INFO] {time.strftime("%Y-%m-%d %H:%M:%S")} - Handling incoming request to /api/v1/health...
[INFO] {time.strftime("%Y-%m-%d %H:%M:%S")} - Response 200 OK.
            """,
                language="log",
            )
