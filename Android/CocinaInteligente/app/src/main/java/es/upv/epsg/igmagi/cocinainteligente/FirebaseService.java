package es.upv.epsg.igmagi.cocinainteligente;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import es.upv.epsg.igmagi.cocinainteligente.model.Device;
import es.upv.epsg.igmagi.cocinainteligente.model.User;
import es.upv.epsg.igmagi.cocinainteligente.model.UserViewModel;

public class FirebaseService extends Service {
    private FirebaseUser mAuth = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<Device> devices = new ArrayList<>();
    private User user;
    private boolean notification;
    private NotificationManager notificationManager;
    static final String CANAL_ID = "mi_canal";
    static final int NOTIFICACION_ID = 1;

    public FirebaseService() {
    }

    public int onStartCommand(Intent intent, int flags, int startId){
        onTaskRemoved(intent);
        db.collection("users").document(mAuth.getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                //user = documentSnapshot.toObject(User.class);
                //user.setUid(mAuth.getUid());
                user = new User(documentSnapshot.getId(), documentSnapshot.getString("name"),
                        documentSnapshot.getString("email"), documentSnapshot.getString("image"),
                        documentSnapshot.getLong("fidelity"), documentSnapshot.getDate("joinDate"),
                        (ArrayList<String>) documentSnapshot.get("recipes"),
                        (ArrayList<String>) documentSnapshot.get("favouriteReceips"),
                        (ArrayList<String>) documentSnapshot.get("devices"));
                db.collection("devices").addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()){
                            if (user.devices.contains(snapshot.getId())) {
                                devices.clear();
                                devices.add(snapshot.toObject(Device.class));
                                for(int i = 0; i < 4; i++){
                                    if(devices.get(0).getCooktop().get(0) > 60 && notification == false){
                                        notificationManager = (NotificationManager) getSystemService(NotificationManager.class);
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            NotificationChannel notificationChannel = new NotificationChannel(CANAL_ID, "Mis Notificaciones", NotificationManager.IMPORTANCE_DEFAULT);
                                            notificationChannel.setDescription("Descripcion del canal");
                                            notificationManager.createNotificationChannel(notificationChannel);
                                        }
                                        NotificationCompat.Builder notificacion = new NotificationCompat.Builder(getApplicationContext(), CANAL_ID)
                                                .setSmallIcon(R.mipmap.ic_launcher)
                                                .setContentTitle("Cocina en uso")
                                                .setContentText("El fuego está encendido")
                                                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logo1))
                                                .setWhen(System.currentTimeMillis() + 1000 * 60 * 60)
                                                .setContentInfo("más info")
                                                .setTicker("Texto en barra de estado");
                                        notificationManager.notify(NOTIFICACION_ID, notificacion.build());
                                        notification = true;
                                    }
                                }
                                notification = false;
                            }
                        }
                    }
                });
            }
        });
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceIntent = new Intent(getApplicationContext(),this.getClass());
        restartServiceIntent.setPackage(getPackageName());
        startService(restartServiceIntent);
        super.onTaskRemoved(rootIntent);
    }
}
