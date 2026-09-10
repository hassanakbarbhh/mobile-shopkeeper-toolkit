with open("app/src/main/res/layout/fragment_dues.xml", "r") as f:
    content = f.read()

new_content = """<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <com.google.android.material.card.MaterialCardView
        android:id="@+id/cardCallToday"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        android:visibility="gone"
        app:cardBackgroundColor="#FFEBEE"
        app:cardElevation="0dp"
        app:strokeWidth="1dp"
        app:strokeColor="#EF5350">
        
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:padding="16dp">
            
            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="🚨 Call Today (Critical Dues)"
                android:textStyle="bold"
                android:textColor="#C62828"
                android:textSize="14sp"/>
                
            <TextView
                android:id="@+id/tvCallTodayList"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="8dp"
                android:textSize="12sp"
                android:textColor="#B71C1C"/>
                
        </LinearLayout>
    </com.google.android.material.card.MaterialCardView>

    <FrameLayout 
        android:layout_width="match_parent" 
        android:layout_height="0dp"
        android:layout_weight="1">
        
        <androidx.recyclerview.widget.RecyclerView android:id="@+id/rvDues"
            android:layout_width="match_parent" android:layout_height="match_parent"
            android:padding="16dp" android:clipToPadding="false"/>
            
        <TextView android:id="@+id/tvNoDues" android:layout_width="wrap_content"
            android:layout_height="wrap_content" android:layout_gravity="center"
            android:text="No pending udhaar 🎉\\nAll customers are paid up."
            android:gravity="center" android:textColor="@color/text_secondary"
            android:visibility="gone"/>
    </FrameLayout>
</LinearLayout>
"""
with open("app/src/main/res/layout/fragment_dues.xml", "w") as f:
    f.write(new_content)
