package br.edu.uni7.edilberto.ponto7.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import br.edu.uni7.edilberto.ponto7.R;
import br.edu.uni7.edilberto.ponto7.model.Colaborador;
import br.edu.uni7.edilberto.ponto7.model.Empresa;
import br.edu.uni7.edilberto.ponto7.model.Registro;
import br.edu.uni7.edilberto.ponto7.util.DataHora;

import static br.edu.uni7.edilberto.ponto7.util.DataHora.dataAtual;
import static br.edu.uni7.edilberto.ponto7.util.DataHora.dataExtenso;
import static br.edu.uni7.edilberto.ponto7.util.DataHora.dtos;
import static br.edu.uni7.edilberto.ponto7.util.DataHora.horaAtual;
import static br.edu.uni7.edilberto.ponto7.util.DataHora.htos;
import static br.edu.uni7.edilberto.ponto7.util.DataHora.saudacao;
import static com.google.maps.android.SphericalUtil.computeDistanceBetween;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_PERMISSIONS_CODE = 128;

    private EditText etEmpresa;
    private EditText etMatricula;
    private Button btEntrar;

    private String idempresa;
    private String matricula;
    private DatabaseReference mDatabase;
    private Colaborador colaborador;
    private Empresa empresa;
    private Registro registro;

    private TextView tvDataAtual;
    private TextView tvSaudacao;
    private TextView tvNomeColaborador;
    private TextView tvJornadaColaborador;
    private TextView tvNomeEmpresa;
    private TextView tvEnderecoEmpresa;

    private TextView tvHora1;
    private TextView tvHora2;
    private TextView tvHora3;
    private TextView tvHora4;
    private TextView tvHorasFeitas;
    private Button btRegistrar;
    private ProgressBar pbLoading;

    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        tvDataAtual = findViewById(R.id.tv_data_atual);
        tvSaudacao = findViewById(R.id.tv_saudacao);
        tvNomeColaborador = findViewById(R.id.tv_nome_colaborador);
        tvJornadaColaborador = findViewById(R.id.tv_jornada_colaborador);
        tvNomeEmpresa = findViewById(R.id.tv_nome_empresa);
        tvEnderecoEmpresa = findViewById(R.id.tv_endereco_empresa);

        tvHora1 = findViewById(R.id.tv_hora1);
        tvHora2 = findViewById(R.id.tv_hora2);
        tvHora3 = findViewById(R.id.tv_hora3);
        tvHora4 = findViewById(R.id.tv_hora4);
        tvHorasFeitas = findViewById(R.id.tv_horas_feitas);
        btRegistrar = findViewById(R.id.bt_registrar);
        btRegistrar.setOnClickListener(btRegistrarOnClickListener);
        btRegistrar.setVisibility(View.INVISIBLE);
        pbLoading = findViewById(R.id.pb_loading);

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.putExtra("empresa", "uni7");
        intent.putExtra("matricula", "900001");
        startActivityForResult(intent, 999);

        //Toolbar toolbar = findViewById(R.id.tb_main);
        //setSupportActionBar(toolbar);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        super.onActivityResult(requestCode, resultCode, resultIntent);
        if (requestCode == 999 && resultCode == Activity.RESULT_OK){
            idempresa = resultIntent.getStringExtra("empresa");
            matricula = resultIntent.getStringExtra("matricula");
            entrar(idempresa, matricula);
        }
    }

    public View.OnClickListener btRegistrarOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (localTrabalho()) {
                registrarPonto();
            }
        }
    };

    public void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void entrar(String idempresa, String matricula) {
        pbLoading.setVisibility(View.VISIBLE);
        recuperarColaborador(idempresa, matricula);
        recuperarEmpresa(idempresa);
        recuperarRegistro(matricula);
        pbLoading.setVisibility(View.GONE);
        btRegistrar.setVisibility(View.VISIBLE);
    }

    private void recuperarColaborador(String idempresa, String matricula) {
        mDatabase
                .child("colaboradores")
                .child(idempresa)
                .child(matricula)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        colaborador = dataSnapshot.getValue(Colaborador.class);

                        if (colaborador == null) {
                            Log.e("info", "Colaborador is unexpectedly null");
                            showToast("Error: Não foi possível obter os dados do Colaborador.");
                        } else {
                            Log.i("info", "colaborador: " + colaborador.getNome());
                            tvDataAtual.setText(dataExtenso(dataAtual()));
                            tvSaudacao.setText(saudacao());
                            tvNomeColaborador.setText(colaborador.getNome());
                            tvJornadaColaborador.setText("Sua jornada é de: " + colaborador.getJornada() + " horas diárias");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("info", "Erro ao recuperar dados.");
                    }
                });
    }

    private void recuperarEmpresa(String idempresa) {
        mDatabase
                .child("empresas")
                .child(idempresa)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        empresa = dataSnapshot.getValue(Empresa.class);

                        if (empresa == null) {
                            Log.e("info", "Empresa is unexpectedly null");
                            showToast("Error: Não foi possível obter os dados da Empresa.");
                        } else {
                            Log.i("info", "empresa: " + empresa.getRazao());
                            tvNomeEmpresa.setText(empresa.getFantasia() + " - " + empresa.getRazao());
                            tvEnderecoEmpresa.setText(empresa.getEndereco() + " \n" +
                                    empresa.getBairro() + " " + empresa.getMunicipio() + "/" + empresa.getUf());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("info", "Erro ao recuperar dados.");
                    }
                });
    }

    private void recuperarRegistro(String matricula) {
        Date data = dataAtual();
        recuperarRegistro(matricula, data);
    }

    private void recuperarRegistro(String matricula, Date data) {
        mDatabase
                .child("registros")
                .child(matricula)
                .child(dtos(data))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        registro = dataSnapshot.getValue(Registro.class);
                        if (registro == null) {
                            Log.e("info", "Registro is null");
                        } else {
                            if (registro.getHora1() != null) tvHora1.setText("1ª Entrada: " + registro.getHora1());
                            if (registro.getHora2() != null) tvHora2.setText("1ª Saída..: " + registro.getHora2());
                            if (registro.getHora3() != null) tvHora3.setText("2ª Entrada: " + registro.getHora3());
                            if (registro.getHora4() != null) tvHora4.setText("2ª Saída..: " + registro.getHora4());
                            tvHorasFeitas.setText("Foram feitas: " + String.valueOf(calcularHorasFeitasHoje())+" horas");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("info", "Erro ao recuperar dados.");
                    }
                });
    }

    private void recuperarRegistros() {
        mDatabase
                .child("registros")
                .child("900001")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for( DataSnapshot child : dataSnapshot.getChildren()){
                            registro = child.getValue(Registro.class);
                            if (registro == null) {
                                Log.e("info", "Registro is unexpectedly null");
                                showToast("Error: Não foi possível obter os dados do Registro.");
                            } else {
                                Log.i("info", "registro: " + registro.getHora1());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("info", "Erro ao recuperar dados.");
                    }
                });
    }

    private boolean localTrabalho() {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if( ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                AlertDialog dialog = new AlertDialog.Builder(this).create();
                dialog.setTitle("Permissão");
                dialog.setMessage("O acesso a sua localização é necessário para o registro do seu ponto. Deseja liberar agora?");
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions( MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS_CODE);
                    }
                });
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Não", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                dialog.show();
            } else {
                ActivityCompat.requestPermissions( this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS_CODE);
            }
        } else {
            long tempoAtualizacao = 10000;
            float distancia = 15;
            Location location = null;
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                showToast("GPS e Rede desabilitados");
                return false;
            } else {
                MyLocationListener myLocationListener = new MyLocationListener();
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, tempoAtualizacao, distancia, myLocationListener);
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {
                        LatLng myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        LatLng empLatLng = new LatLng(empresa.getLatitude(), empresa.getLongitude());
                        double dist = computeDistanceBetween(myLatLng, empLatLng);
                        if (dist < 20000) {
                            return true;
                        }
                    }
                }
                if (isGPSEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, tempoAtualizacao, distancia, myLocationListener);
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location != null) {
                        LatLng myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        LatLng empLatLng = new LatLng(empresa.getLatitude(), empresa.getLongitude());
                        double dist = computeDistanceBetween(myLatLng, empLatLng);
                        if (dist < 20000) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            locationManager.removeUpdates(this);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    }

    private void registrarPonto() {
        String batida = null;

        if (registro == null) {
            batida = "hora1";
        } else {
            if (registro.getHora4() == null) batida = "hora4";
            if (registro.getHora3() == null) batida = "hora3";
            if (registro.getHora2() == null) batida = "hora2";
            if (registro.getHora1() == null) batida = "hora1";
        }

        if (batida == null) {
            showToast("Todos os registros já foram realizados nesta data!");
        } else {
            mDatabase
                    .child("registros")
                    .child("900001")
                    .child(dtos(dataAtual()))
                    .child(batida)
                    .setValue(htos(horaAtual()));
            recuperarRegistro(matricula);
        }
    }

    long calcularHorasFeitasHoje(){
        return calcularMinutosFeitosHoje()/60;
    }

    long calcularMinutosFeitosHoje(){
        if(registro != null){
            if(registro.getHora1()!=null&&registro.getHora2()==null){
                Date dataHora1 = DataHora.stoh(registro.getHora1());
                //TODO teriamos que fazer um timer ou coisa parecida para isso funcionar direito
                long diff = dataAtual().getTime() - dataHora1.getTime();
                return TimeUnit.MILLISECONDS.toMinutes(diff);
            }
            if(registro.getHora1()!=null&&registro.getHora2()!=null&&registro.getHora3()==null){
                Date dataHora1 = DataHora.stoh(registro.getHora1());
                Date dataHora2 = DataHora.stoh(registro.getHora2());
                long diff = dataHora2.getTime() - dataHora1.getTime();
                return TimeUnit.MILLISECONDS.toMinutes(diff);
            }
            if(registro.getHora1()!=null&&registro.getHora2()!=null&&registro.getHora3()!=null&&registro.getHora4()==null){
                Date dataHora1 = DataHora.stoh(registro.getHora1());
                Date dataHora2 = DataHora.stoh(registro.getHora2());
                Date dataHora3 = DataHora.stoh(registro.getHora3());
                long diff1 = dataHora2.getTime() - dataHora1.getTime();
                long diff2 = dataAtual().getTime() - dataHora3.getTime();
                return TimeUnit.MILLISECONDS.toMinutes(diff1)+TimeUnit.MILLISECONDS.toMinutes(diff2);
            }
            Date dataHora1 = DataHora.stoh(registro.getHora1());
            Date dataHora2 = DataHora.stoh(registro.getHora2());
            Date dataHora3 = DataHora.stoh(registro.getHora3());
            Date dataHora4 = DataHora.stoh(registro.getHora4());
            long diff1 = dataHora2.getTime() - dataHora1.getTime();
            long diff2 = dataHora4.getTime() - dataHora3.getTime();
            return TimeUnit.MILLISECONDS.toMinutes(diff1)+TimeUnit.MILLISECONDS.toMinutes(diff2);
        }
        return 0;
    }
}
