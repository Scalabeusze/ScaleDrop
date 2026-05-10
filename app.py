import streamlit as st
import os
from modules import services, monitoring, users

# Page config always has to be first
st.set_page_config(page_title="ScaleDrop Admin", page_icon="⚙️", layout="wide")


# Session-based authentication
def check_password():
    # Initialize auth state
    if "authenticated" not in st.session_state:
        st.session_state.authenticated = False

    if not st.session_state.authenticated:
        # Centered login form
        col1, col2, col3 = st.columns([1, 1, 1])
        with col2:
            st.title("Log in")
            st.write("Please input credentials to gain access to this page.")

            ADMIN_PASSWORD = os.environ.get("ADMIN_PASSWORD", "admin123")

            with st.form("login_form"):
                username = st.text_input("User")
                password = st.text_input("Password", type="password")
                submit = st.form_submit_button("Log in", use_container_width=True)

                if submit:
                    if username == "admin" and password == ADMIN_PASSWORD:
                        st.session_state.authenticated = True
                        st.rerun()  # Reload page after successful login
                    else:
                        st.error("Incorrect login data.")

        # Stop rendering for unauthenticated users
        st.stop()


check_password()

# Navbar for logged in users
st.sidebar.title("Navigation")
page = st.sidebar.radio(
    "Select panel:", ["Services management", "Monitoring and logs", "Database"]
)

st.sidebar.divider()

if st.sidebar.button("Logout", use_container_width=True):
    st.session_state.authenticated = False
    st.rerun()

# Routing
if page == "Services management":
    services.display()
elif page == "Monitoring and logs":
    monitoring.display()
elif page == "Database":
    users.display()
