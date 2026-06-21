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
            password=os.environ.get("DB_PASSWORD", "postgres"),
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
        WHERE status = 'ACTIVE'
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


def format_size(size_in_bytes):
    """Change size in bytes to readable format KB, MB, GB"""
    if pd.isna(size_in_bytes) or size_in_bytes == 0:
        return "0 B"
    size_name = ("B", "KB", "MB", "GB", "TB")
    i = int(math.floor(math.log(size_in_bytes, 1024)))
    p = math.pow(1024, i)
    s = round(size_in_bytes / p, 2)
    return f"{s} {size_name[i]}"


def fetch_user_files(owner_id):
    """Fetches file info from sd_download for selected user"""
    conn = init_connection()
    if not conn:
        return pd.DataFrame()

    query = """
        SELECT 
            name, 
            size, 
            content_type, 
            last_modified
        FROM sd_download.files
        WHERE owner_id = %s AND status != 'DELETED'
        ORDER BY last_modified DESC;
    """
    try:
        with conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cur:
            cur.execute(query, (str(owner_id),))
            results = cur.fetchall()
        return pd.DataFrame(results)
    except Exception as e:
        st.error(f"Error querying files database: {e}")
        return pd.DataFrame()


def render_full_profile(user_data):
    """Renders a full-screen profile view for the selected user"""

    if st.button("Back to user list", type="primary"):
        st.session_state.selected_user_id = None
        st.rerun()

    st.divider()

    col_img, col_title = st.columns([1, 8])
    with col_img:
        avatar = user_data["avatar_url"]
        if pd.notna(avatar) and str(avatar).strip() != "":
            st.image(avatar, width=100)
        else:
            st.markdown(
                """
                <div style="width:100px; height:100px; border-radius:50%; background-color:#4F46E5; display:flex; justify-content:center; align-items:center; color:white; font-size:36px; font-weight:bold;">
                    ?
                </div>
                """,
                unsafe_allow_html=True,
            )

    with col_title:
        st.title(user_data["username"])
        st.caption(f"Internal ID: {user_data['id']}")

    st.write("")

    info_col1, info_col2, info_col3 = st.columns(3)

    with info_col1:
        st.markdown("### Basic info")
        st.write(
            f"**First Name:** {user_data['first_name'] if user_data['first_name'] else '—'}"
        )
        st.write(
            f"**Last Name:** {user_data['last_name'] if user_data['last_name'] else '—'}"
        )

    with info_col2:
        st.markdown("### Activity")
        st.write(
            "**Created:**",
            (
                user_data["created_at"].strftime("%Y-%m-%d %H:%M")
                if pd.notnull(user_data["created_at"])
                else "—"
            ),
        )
        st.write(
            "**Last login:**",
            (
                user_data["last_login_at"].strftime("%Y-%m-%d %H:%M")
                if pd.notnull(user_data["last_login_at"])
                else "Never"
            ),
        )

    with info_col3:
        files_df = fetch_user_files(user_data["id"])
        total_size_bytes = files_df["size"].sum() if not files_df.empty else 0
        total_files = len(files_df)

        st.metric(label="Total file size", value=format_size(total_size_bytes))
        st.metric(label="Number of uploaded files", value=total_files)

    st.divider()

    st.subheader("Files uploaded by user")

    if files_df.empty:
        st.info(
            "This user does not currently have any files in the system."
        )
    else:
        view_df = files_df[["name", "size", "content_type", "last_modified"]]
        styled_df = view_df.style.format({
            "size": format_size
        })

        st.dataframe(
            styled_df,
            use_container_width=True,
            hide_index=True,
            column_config={
                "name": "File name",
                "size": "Size",
                "content_type": "Type",
                "last_modified": st.column_config.DatetimeColumn(
                    "Last modification", format="YYYY-MM-DD HH:mm"
                ),
            },
        )


def display():
    if "selected_user_id" not in st.session_state:
        st.session_state.selected_user_id = None

    if st.session_state.selected_user_id is not None:
        df_all = fetch_accounts()
        if not df_all.empty:
            user_series = df_all[df_all["id"] == st.session_state.selected_user_id]
            if not user_series.empty:
                selected_data = user_series.iloc[0]
                render_full_profile(selected_data)
                return
            else:
                st.session_state.selected_user_id = (
                    None  # Fallback
                )
        else:
            st.session_state.selected_user_id = None

    st.title("ScaleDrop - User management")

    df = fetch_accounts()

    if df.empty:
        st.warning("No users in database or connection error.")
        return

    filter_col1, filter_col2, filter_col3 = st.columns([2, 2, 1])
    with filter_col1:
        search_term = st.text_input("Search (Username):")
    with filter_col2:
        pass
    with filter_col3:
        st.write("")
        st.write("")
        if st.button("Refresh data", use_container_width=True):
            fetch_accounts.clear()
            st.rerun()

    filtered_df = df.copy()
    if search_term:
        filtered_df = filtered_df[
            filtered_df["username"].str.contains(search_term, case=False, na=False)
        ]

    st.divider()

    # Pagination
    PAGE_SIZE = 20
    total_pages = math.ceil(len(filtered_df) / PAGE_SIZE) if len(filtered_df) > 0 else 1

    page_col1, page_col2 = st.columns([4, 1])
    with page_col2:
        current_page = st.number_input(
            "Page", min_value=1, max_value=total_pages, value=1, step=1
        )

    start_idx = (current_page - 1) * PAGE_SIZE
    end_idx = start_idx + PAGE_SIZE
    page_df = filtered_df.iloc[start_idx:end_idx].reset_index(drop=True)

    st.caption(
        f"Showing users {start_idx + 1} - {min(end_idx, len(filtered_df))} z {len(filtered_df)}"
    )

    view_cols = ["username", "created_at", "last_login_at", "id"]

    event = st.dataframe(
        page_df[view_cols],
        use_container_width=True,
        hide_index=True,
        on_select="rerun",
        selection_mode="single-row",
        column_config={
            "id": None,  # Hide ID column
            "username": "Username",
            "created_at": st.column_config.DatetimeColumn(
                "Created At", format="YYYY-MM-DD HH:mm"
            ),
            "last_login_at": st.column_config.DatetimeColumn(
                "Last Login At", format="YYYY-MM-DD HH:mm"
            ),
        },
    )

    selected_rows = event.selection.rows
    if selected_rows:
        row_index = selected_rows[0]
        clicked_id = page_df.iloc[row_index]["id"]
        st.session_state.selected_user_id = clicked_id
        st.rerun()
