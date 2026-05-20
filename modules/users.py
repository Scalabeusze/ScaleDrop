import streamlit as st
import pandas as pd
import psycopg2
import psycopg2.extras
import os
import math

@st.cache_resource
def init_connection():
    """Creates a global connection to sd_database"""
    try:
        return psycopg2.connect(
            host=os.environ.get("DB_HOST", "localhost"),
            port=os.environ.get("DB_PORT", "5432"),
            database=os.environ.get("DB_NAME", "sd_database"),
            user=os.environ.get("DB_USER", "postgres"),
            password=os.environ.get("DB_PASSWORD", "postgres")
        )
    except Exception as e:
        st.error(f"Failed to connect to RDS: {e}")
        return None

@st.cache_data(ttl=60)
def fetch_accounts():
    """Download all accounts to memory (60 seconds cache)"""
    conn = init_connection()
    if not conn:
        return pd.DataFrame()
    
    query = """
        SELECT 
            id, 
            username, 
            first_name, 
            last_name,
            avatar_url,
            status, 
            last_login_at, 
            created_at, 
            updated_at
        FROM sd_iam.accounts
        ORDER BY created_at DESC;
    """
    try:
        with conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cur:
            cur.execute(query)
            results = cur.fetchall()
        return pd.DataFrame(results)
    except Exception as e:
        st.error(f"Error querying database: {e}")
        return pd.DataFrame()

def render_user_details(user_data):
    """Renders side panel with account details"""
    st.subheader(f"Account details")
    st.caption(f"ID: {user_data['id']}")
    
    col1, col2 = st.columns(2)
    with col1:
        st.write("**Username:**", user_data['username'])
        st.write("**First Name:**", user_data['first_name'] if user_data['first_name'] else "—")
        st.write("**Last Name:**", user_data['last_name'] if user_data['last_name'] else "—")
        
    with col2:
        status = user_data['status']
        status_color = "green" if status == "ACTIVE" else "red" if status == "LOCKED" else "orange"
        st.markdown(f"**Status:** :{status_color}[{status}]")
        
        if user_data['avatar_url']:
            st.image(user_data['avatar_url'], width=80)
        else:
            st.write("**Avatar:** None")

    st.divider()
    
    st.write("**Activity and logs**")
    st.write("**Created:**", user_data['created_at'].strftime("%Y-%m-%d %H:%M:%S") if pd.notnull(user_data['created_at']) else "—")
    st.write("**Modified:**", user_data['updated_at'].strftime("%Y-%m-%d %H:%M:%S") if pd.notnull(user_data['updated_at']) else "—")
    st.write("**Last login:**", user_data['last_login_at'].strftime("%Y-%m-%d %H:%M:%S") if pd.notnull(user_data['last_login_at']) else "Nigdy")
    
    st.divider()
    
    st.write("**MOCKUP: User files (sd-download)**")
    with st.expander("See owned files (Soon)", expanded=False):
        st.info("Here will be an integration with sd-download database, showing files owned by this user.")
        st.code("""
# Future API Call / SQL Join:
# fetch_user_files(account_id=user_data['id'])
        """, language="python")

def display():
    st.title("ScaleDrop - User management")
    
    df = fetch_accounts()

    if df.empty:
        st.warning("No users in database or connection error.")
        return

    filter_col1, filter_col2, filter_col3 = st.columns([2, 2, 1])
    with filter_col1:
        search_term = st.text_input("Search (Username):")
    with filter_col2:
        available_statuses = df["status"].unique().tolist()
        status_filter = st.multiselect("Status:", options=available_statuses, default=available_statuses)
    with filter_col3:
        st.write("")
        st.write("") 
        if st.button("Refresh data", use_container_width=True):
            fetch_accounts.clear()
            st.rerun()

    filtered_df = df.copy()
    if search_term:
        filtered_df = filtered_df[filtered_df['username'].str.contains(search_term, case=False, na=False)]
    if status_filter:
        filtered_df = filtered_df[filtered_df['status'].isin(status_filter)]

    st.divider()

    if 'selected_user_id' not in st.session_state:
        st.session_state.selected_user_id = None

    PAGE_SIZE = 15
    total_pages = math.ceil(len(filtered_df) / PAGE_SIZE) if len(filtered_df) > 0 else 1
    
    page_col1, page_col2 = st.columns([4, 1])
    with page_col2:
        current_page = st.number_input("Page", min_value=1, max_value=total_pages, value=1, step=1)
    
    start_idx = (current_page - 1) * PAGE_SIZE
    end_idx = start_idx + PAGE_SIZE
    page_df = filtered_df.iloc[start_idx:end_idx].reset_index(drop=True)

    display_cols = st.columns([1.5, 1])
    
    with display_cols[0]:
        st.caption(f"Showing users {start_idx + 1} - {min(end_idx, len(filtered_df))} z {len(filtered_df)}")
        
        view_df = page_df[['username', 'first_name', 'last_name', 'status', 'created_at']]
        
        event = st.dataframe(
            view_df,
            use_container_width=True,
            hide_index=True,
            on_select="rerun",
            selection_mode="single-row",
            column_config={
                "username": "Username",
                "first_name": "First name",
                "last_name": "Last name",
                "status": "Status",
                "created_at": st.column_config.DatetimeColumn("Created", format="YYYY-MM-DD")
            }
        )

        selected_rows = event.selection.rows
        if selected_rows:
            row_index = selected_rows[0]
            selected_user_data = page_df.iloc[row_index]
        else:
            selected_user_data = None

    if selected_user_data is not None:
        with display_cols[1]:
            render_user_details(selected_user_data)
    else:
        with display_cols[1]:
            st.info("Click a table row to show user details.")