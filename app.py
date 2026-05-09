import streamlit as st
import boto3
import os

st.set_page_config(page_title="ScaleDrop Admin", page_icon="⚙️", layout="wide")

# Simple password securing
ADMIN_PASSWORD = os.environ.get("ADMIN_PASSWORD", "admin123")
haslo = st.sidebar.text_input("Input admin password", type="password")

if haslo != ADMIN_PASSWORD:
    st.warning("Please provide correct password to unlock access.")
    st.stop()

st.title("ScaleDrop - Infrastructure management")
st.divider()

# ECS client initialization
REGION = 'eu-north-1'

@st.cache_resource
def get_ecs_client():
    return boto3.client('ecs', region_name=REGION)

try:
    ecs = get_ecs_client()
    
    # Fetch cluster list
    clusters_response = ecs.list_clusters()
    cluster_arns = clusters_response.get('clusterArns', [])
    
    if not cluster_arns:
        st.error("Could not find any ECS clusters in provided region.")
        st.stop()
        
    # Extract cluster names
    cluster_names = [arn.split('/')[-1] for arn in cluster_arns]
    
    st.subheader("Select ECS cluster")
    selected_cluster = st.selectbox("Cluster", cluster_names)
    
    st.divider()
    
    # Fetch services in selected cluster
    services_response = ecs.list_services(cluster=selected_cluster)
    service_arns = services_response.get('serviceArns', [])
    
    if not service_arns:
        st.info(f"Cluster {selected_cluster} is empty.")
    else:
        service_names = [arn.split('/')[-1] for arn in service_arns]
        
        st.subheader("Select service to manage")
        selected_service = st.selectbox("Service", service_names)
        
        # Fetch service details
        details = ecs.describe_services(cluster=selected_cluster, services=[selected_service])
        service_info = details['services'][0]
        status = service_info['status']
        desired_count = service_info['desiredCount']
        running_count = service_info['runningCount']
        
        # Show metrics
        col1, col2, col3 = st.columns(3)
        col1.metric("Status", status)
        col2.metric("Desired count", desired_count)
        col3.metric("Instances running", running_count)
        
        st.divider()
        st.subheader("Actions")
        
        col_btn1, col_btn2 = st.columns(2)
        
        with col_btn1:
            if st.button("Force New Deployment", use_container_width=True):
                with st.spinner('Sending request to AWS...'):
                    ecs.update_service(
                        cluster=selected_cluster, 
                        service=selected_service, 
                        forceNewDeployment=True
                    )
                st.success("Restart request sent! Service should redeploy soon.")
                
        with col_btn2:
            # On / Off switch
            new_count = 0 if desired_count > 0 else 1
            action = "Stop service" if desired_count > 0 else "Start service"
            
            if st.button(f"{action}", use_container_width=True):
                with st.spinner('Changing desired count...'):
                    ecs.update_service(
                        cluster=selected_cluster, 
                        service=selected_service, 
                        desiredCount=new_count
                    )
                st.success(f"Desired count changed to {new_count}. Please refresh the page in around 30 seconds.")

except Exception as e:
    st.error("AWS connection error. Please make sure that your environment variables are configured.")
    st.exception(e)