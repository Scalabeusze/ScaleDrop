import streamlit as st
import pandas as pd

def display():
    st.title("ScaleDrop - Users Data (Read-Only)")
    st.write("MOCKUP of RDS data.")
    st.divider()

    st.info("psycopg2 will be used in the future. Following data is a mockup")
    
    mock_data = {
        "User ID": [1, 2, 3, 4, 5],
        "Email": ["admin@scaledrop.com", "j.doe@example.com", "anna@example.com", "test@test.com", "user5@example.com"],
        "Role": ["SUPERADMIN", "USER", "USER", "TESTER", "USER"],
        "Created At": ["2023-10-01", "2023-11-15", "2024-01-05", "2024-02-20", "2024-03-11"],
        "Status": ["Active", "Active", "Suspended", "Active", "Pending"]
    }
    
    df = pd.DataFrame(mock_data)
    
    search_term = st.text_input("Search by email:")
    if search_term:
        df = df[df['Email'].str.contains(search_term, case=False)]

    st.dataframe(df, use_container_width=True, hide_index=True)