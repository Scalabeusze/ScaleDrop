import streamlit as st
import boto3
import time
from datetime import datetime
from streamlit_autorefresh import st_autorefresh

REGION = "eu-north-1"


@st.cache_resource
def get_ecs_client():
    return boto3.client("ecs", region_name=REGION)


# Helper function for status
def get_status_ui(status):
    status_upper = status.upper()
    if status_upper == "ACTIVE":
        return f"🟢 {status}"
    elif status_upper == "DRAINING":
        return f"🟡 {status}"
    elif status_upper == "INACTIVE":
        return f"🔴 {status}"
    return f"⚪ {status}"


def display():
    # Autorefresh every 10 s, returns counter of refreshes
    count = st_autorefresh(interval=10000, limit=None, key="ecs_autorefresh")

    st.title("ScaleDrop - Infrastructure management")

    # Show last refresh time
    current_time = datetime.now().strftime("%H:%M:%S")
    st.caption(f"Data refreshes automatically (Last update: {current_time})")

    st.divider()

    try:
        ecs = get_ecs_client()

        # Fetch cluster list
        clusters_response = ecs.list_clusters()
        cluster_arns = clusters_response.get("clusterArns", [])

        if not cluster_arns:
            st.error("Could not find any ECS clusters in provided region.")
            return

        # Extract cluster names
        cluster_names = [arn.split("/")[-1] for arn in cluster_arns]

        st.subheader("Select ECS cluster")
        selected_cluster = st.selectbox("Cluster", cluster_names)

        st.divider()

        # Fetch services in selected cluster
        services_response = ecs.list_services(cluster=selected_cluster)
        service_arns = services_response.get("serviceArns", [])

        if not service_arns:
            st.info(f"Cluster {selected_cluster} is empty.")
        else:
            service_names = [arn.split("/")[-1] for arn in service_arns]

            st.subheader("Select service to manage")
            selected_service = st.selectbox("Service", service_names)

            # Fetch service details
            details = ecs.describe_services(
                cluster=selected_cluster, services=[selected_service]
            )
            service_info = details["services"][0]
            status = service_info["status"]
            desired_count = service_info["desiredCount"]
            running_count = service_info["runningCount"]

            col1, col2, col3 = st.columns(3)
            col1.metric("Status", get_status_ui(status))
            col2.metric("Desired count", desired_count)

            # Add delta if running is different from desired
            delta_instances = running_count - desired_count
            delta_color = "normal" if running_count == desired_count else "off"

            col3.metric(
                "Instances running",
                running_count,
                delta=delta_instances if delta_instances != 0 else None,
                delta_color=delta_color,
            )

            st.divider()
            st.subheader("Actions")

            col_btn1, col_btn2 = st.columns(2)

            with col_btn1:
                if st.button("Force New Deployment", use_container_width=True):
                    with st.spinner("Sending request to AWS..."):
                        ecs.update_service(
                            cluster=selected_cluster,
                            service=selected_service,
                            forceNewDeployment=True,
                        )
                    # Global toast
                    st.toast("Restart request sent! Reloading...", icon="🔄")
                    time.sleep(1.5)  # Short pause to read toast
                    st.rerun()  # Refresh AWS data

            with col_btn2:
                # Start / Stop button
                new_count = 0 if desired_count > 0 else 1
                action = "Stop service" if desired_count > 0 else "Start service"

                btn_type = "primary" if desired_count > 0 else "secondary"

                if st.button(f"{action}", type=btn_type, use_container_width=True):
                    with st.spinner("Changing desired count..."):
                        ecs.update_service(
                            cluster=selected_cluster,
                            service=selected_service,
                            desiredCount=new_count,
                        )
                    st.toast(
                        f"Desired count changed to {new_count}. Reloading...", icon="✅"
                    )
                    time.sleep(1.5)
                    st.rerun()

    except Exception as e:
        st.error(
            "AWS connection error. Please make sure that your IAM Role or environment variables are configured."
        )
        st.exception(e)
