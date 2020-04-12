package com.example.newproj;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.newproj.models.CurrentUser;
import com.example.newproj.models.CustomAdapter;
import com.example.newproj.models.CustomAdapterMeeting;
import com.example.newproj.models.Meeting;
import com.example.newproj.models.Parks;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class CreateMeetingActivity extends AppCompatActivity  {
    private EditText mDate,mHour,mDisc;
    private Spinner mLoc,mDogType;
    private Button mbutton;
    private DatePickerDialog picker;
    private Calendar myCalendar;
    private DatePickerDialog.OnDateSetListener date;
    private TimePickerDialog.OnTimeSetListener time;
    private FirebaseFirestore db;
    private ArrayList<Parks> parksList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_meeting);
        mLoc=findViewById(R.id.editMeetingLoc);
        mDate=findViewById(R.id.editMeetingDate);
        mHour=findViewById(R.id.editMeetingHOUR);
        mDogType=findViewById(R.id.editMeetingType);
        mDisc=findViewById(R.id.editMeetingDisc);
        mbutton=findViewById(R.id.CreateMeeting);
        myCalendar = Calendar.getInstance();
        db = FirebaseFirestore.getInstance();
        parksList = new ArrayList<Parks>();

        date= new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, month);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };

        time= new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                myCalendar.set(Calendar.HOUR,hourOfDay);
                myCalendar.set(Calendar.MINUTE,minute);
                String min=""+minute,hour=""+hourOfDay;

                if(minute<10){
                    min="0"+min;
                }
                if(hourOfDay<10){
                    hour="0"+hour;
                }
                mHour.setText( "" + hour + ":" + min);

            }
        };

        mDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(CreateMeetingActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();


            }
        });

        mHour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(CreateMeetingActivity.this,time,myCalendar.get(Calendar.HOUR_OF_DAY),myCalendar.get(Calendar.MINUTE),true).show();
            }
        });

        mbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Meeting meeting=new Meeting(((Parks)mLoc.getSelectedItem()).getName().toString(), mDate.getText().toString(), mHour.getText().toString(), mDogType.getSelectedItem().toString(), mDisc.getText().toString(), CurrentUser.currentUserEmail);
                Map<String, Object> mt = new HashMap<>();
                mt.put("Location", meeting.getLocation());
                mt.put("Date", meeting.getDate());
                mt.put("Hour", meeting.getHour());
                mt.put("DogType", meeting.getDogType());
                mt.put("Discription", meeting.getDiscription());
                mt.put("Owner", meeting.getOwner());

                db.collection("meetings").document().set(mt)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(CreateMeetingActivity.this, "הפגישה נוצרה בהצלחה", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
            }
        });

        CollectionReference user = db.collection("parks");
        user.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task< QuerySnapshot > task) {
                if (task.isSuccessful()) {
                    for(QueryDocumentSnapshot doc : task.getResult()){
                        Parks park = new Parks();
                        park.setName(doc.get("Name").toString());
                        park.setArea(doc.get("ShapeArea").toString());
                        park.setLength(doc.get("ShapeLength").toString());
                        park.setImage(doc.get("Image").toString());
                        parksList.add(park);
                    }
                    fillList();
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });






    }

    private void updateLabel() {
        String myFormat = "dd/MM/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
        mDate.setText(sdf.format(myCalendar.getTime()));
    }

    private void fillList(){
        CustomAdapterMeeting arrayAdapter = new CustomAdapterMeeting(this, parksList);
        mLoc.setAdapter(arrayAdapter);
    }
}
